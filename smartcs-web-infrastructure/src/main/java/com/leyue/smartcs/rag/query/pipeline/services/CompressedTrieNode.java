package com.leyue.smartcs.rag.query.pipeline.services;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;

/**
 * 压缩Trie节点实现
 * 支持路径压缩、并发访问、权重统计和时效性管理
 */
public class CompressedTrieNode {
    
    // 子节点映射 - 支持并发访问
    private final Map<String, CompressedTrieNode> children = new ConcurrentHashMap<>();
    
    // 节点信息
    private final AtomicReference<NodeInfo> nodeInfo = new AtomicReference<>();
    
    // 压缩路径 - 当只有一个子节点时可以压缩路径
    private volatile String compressedPath;
    
    /**
     * 节点信息封装类
     */
    public static class NodeInfo {
        private final boolean isEndOfWord;
        private final double weight;
        private final AtomicLong accessCount;
        private final long lastAccessed;
        private final long createdTime;
        
        public NodeInfo(boolean isEndOfWord, double weight) {
            this.isEndOfWord = isEndOfWord;
            this.weight = weight;
            this.accessCount = new AtomicLong(0);
            this.lastAccessed = System.currentTimeMillis();
            this.createdTime = System.currentTimeMillis();
        }
        
        private NodeInfo(boolean isEndOfWord, double weight, long accessCount, long lastAccessed, long createdTime) {
            this.isEndOfWord = isEndOfWord;
            this.weight = weight;
            this.accessCount = new AtomicLong(accessCount);
            this.lastAccessed = lastAccessed;
            this.createdTime = createdTime;
        }
        
        public NodeInfo withWeight(double newWeight) {
            return new NodeInfo(isEndOfWord, newWeight, accessCount.get(), System.currentTimeMillis(), createdTime);
        }
        
        public NodeInfo withEndOfWord(boolean isEnd) {
            return new NodeInfo(isEnd, weight, accessCount.get(), System.currentTimeMillis(), createdTime);
        }
        
        public boolean isEndOfWord() { return isEndOfWord; }
        public double getWeight() { return weight; }
        public long getAccessCount() { return accessCount.get(); }
        public long getLastAccessed() { return lastAccessed; }
        public long getCreatedTime() { return createdTime; }
        
        public void incrementAccess() {
            accessCount.incrementAndGet();
        }
        
        /**
         * 计算综合得分（权重 + 访问频率 + 时效性）
         */
        public double calculateScore() {
            long now = System.currentTimeMillis();
            long ageInHours = (now - lastAccessed) / (1000 * 60 * 60);
            
            // 时效性衰减因子
            double timeFactor = Math.exp(-ageInHours / 24.0); // 24小时衰减
            
            // 访问频率因子
            double frequencyFactor = Math.log1p(accessCount.get()) / 10.0;
            
            // 综合得分 = 基础权重 * 时效性 + 频率得分
            return weight * timeFactor + frequencyFactor;
        }
    }
    
    /**
     * 构造函数
     */
    public CompressedTrieNode() {
        this.nodeInfo.set(new NodeInfo(false, 0.0));
    }
    
    /**
     * 插入词条
     */
    public void insert(String word, double weight) {
        if (word == null || word.isEmpty()) return;
        
        insertRecursive(word, 0, weight);
    }
    
    private void insertRecursive(String word, int index, double weight) {
        if (index >= word.length()) {
            NodeInfo current = nodeInfo.get();
            nodeInfo.set(current.withEndOfWord(true).withWeight(Math.max(current.getWeight(), weight)));
            return;
        }
        
        // 尝试路径压缩
        if (compressedPath != null) {
            int commonPrefixLen = getCommonPrefixLength(compressedPath, word.substring(index));
            
            if (commonPrefixLen == compressedPath.length()) {
                // 完全匹配压缩路径，继续递归
                insertRecursive(word, index + commonPrefixLen, weight);
                return;
            } else if (commonPrefixLen > 0) {
                // 部分匹配，需要分裂节点
                splitCompressedPath(commonPrefixLen);
            } else {
                // 无匹配，转换为普通节点
                expandCompressedNode();
            }
        }
        
        // 普通插入逻辑
        String key = String.valueOf(word.charAt(index));
        CompressedTrieNode child = children.computeIfAbsent(key, k -> new CompressedTrieNode());
        child.insertRecursive(word, index + 1, weight);
        
        // 尝试压缩当前节点
        tryCompress();
    }
    
    /**
     * 获取子节点映射（用于遍历）
     */
    public Map<String, CompressedTrieNode> getChildren() {
        return children;
    }
    
    /**
     * 获取压缩路径
     */
    public String getCompressedPath() {
        return compressedPath;
    }
    
    /**
     * 搜索前缀补全
     */
    public void findCompletions(String prefix, String currentPath, 
                              java.util.List<CompletionResult> results, int limit) {
        if (results.size() >= limit) return;
        
        NodeInfo info = nodeInfo.get();
        if (info.isEndOfWord()) {
            info.incrementAccess();
            results.add(new CompletionResult(currentPath, info.calculateScore()));
        }
        
        // 遍历子节点
        for (Map.Entry<String, CompressedTrieNode> entry : children.entrySet()) {
            if (results.size() >= limit) break;
            
            String childPath = currentPath + entry.getKey();
            CompressedTrieNode child = entry.getValue();
            
            if (child.compressedPath != null) {
                childPath += child.compressedPath;
            }
            
            child.findCompletions(prefix, childPath, results, limit);
        }
    }
    
    /**
     * 更新词条权重
     */
    public boolean updateWeight(String word, double weightDelta) {
        return updateWeightRecursive(word, 0, weightDelta);
    }
    
    private boolean updateWeightRecursive(String word, int index, double weightDelta) {
        if (index >= word.length()) {
            NodeInfo current = nodeInfo.get();
            if (current.isEndOfWord()) {
                double newWeight = Math.max(0, current.getWeight() + weightDelta);
                nodeInfo.set(current.withWeight(newWeight));
                return true;
            }
            return false;
        }
        
        if (compressedPath != null) {
            String remaining = word.substring(index);
            if (remaining.startsWith(compressedPath)) {
                return updateWeightRecursive(word, index + compressedPath.length(), weightDelta);
            }
            return false;
        }
        
        String key = String.valueOf(word.charAt(index));
        CompressedTrieNode child = children.get(key);
        return child != null && child.updateWeightRecursive(word, index + 1, weightDelta);
    }
    
    /**
     * 计算公共前缀长度
     */
    private int getCommonPrefixLength(String str1, String str2) {
        int minLen = Math.min(str1.length(), str2.length());
        int i = 0;
        while (i < minLen && str1.charAt(i) == str2.charAt(i)) {
            i++;
        }
        return i;
    }
    
    /**
     * 分裂压缩路径
     */
    private void splitCompressedPath(int splitPoint) {
        if (compressedPath == null || splitPoint <= 0) return;
        
        String keepPath = compressedPath.substring(0, splitPoint);
        String splitPath = compressedPath.substring(splitPoint);
        
        // 创建新的子节点承接分裂的部分
        CompressedTrieNode newChild = new CompressedTrieNode();
        newChild.compressedPath = splitPath;
        newChild.nodeInfo.set(this.nodeInfo.get());
        newChild.children.putAll(this.children);
        
        // 更新当前节点
        this.compressedPath = keepPath;
        this.children.clear();
        this.children.put(String.valueOf(splitPath.charAt(0)), newChild);
        this.nodeInfo.set(new NodeInfo(false, 0.0));
    }
    
    /**
     * 展开压缩节点
     */
    private void expandCompressedNode() {
        if (compressedPath == null) return;
        
        CompressedTrieNode current = this;
        for (int i = 0; i < compressedPath.length(); i++) {
            String key = String.valueOf(compressedPath.charAt(i));
            CompressedTrieNode newNode = new CompressedTrieNode();
            current.children.put(key, newNode);
            current = newNode;
        }
        
        current.nodeInfo.set(this.nodeInfo.get());
        current.children.putAll(this.children);
        
        this.compressedPath = null;
        this.children.clear();
        this.nodeInfo.set(new NodeInfo(false, 0.0));
    }
    
    /**
     * 尝试压缩当前节点
     */
    private void tryCompress() {
        if (children.size() == 1 && !nodeInfo.get().isEndOfWord()) {
            Map.Entry<String, CompressedTrieNode> entry = children.entrySet().iterator().next();
            String childKey = entry.getKey();
            CompressedTrieNode child = entry.getValue();
            
            if (child.compressedPath != null) {
                this.compressedPath = childKey + child.compressedPath;
            } else {
                this.compressedPath = childKey;
            }
            
            this.nodeInfo.set(child.nodeInfo.get());
            this.children.clear();
            this.children.putAll(child.children);
        }
    }
    
    /**
     * 获取节点统计信息
     */
    public int getNodeCount() {
        int count = 1;
        for (CompressedTrieNode child : children.values()) {
            count += child.getNodeCount();
        }
        return count;
    }
    
    public int getWordCount() {
        int count = nodeInfo.get().isEndOfWord() ? 1 : 0;
        for (CompressedTrieNode child : children.values()) {
            count += child.getWordCount();
        }
        return count;
    }
    
    /**
     * 清理过期节点
     */
    public void cleanup(long maxAgeMs) {
        long now = System.currentTimeMillis();
        NodeInfo info = nodeInfo.get();
        
        if (info.isEndOfWord() && (now - info.getLastAccessed()) > maxAgeMs && info.getAccessCount() == 0) {
            nodeInfo.set(info.withEndOfWord(false).withWeight(0.0));
        }
        
        children.entrySet().removeIf(entry -> {
            entry.getValue().cleanup(maxAgeMs);
            return entry.getValue().isEmpty();
        });
    }
    
    private boolean isEmpty() {
        return !nodeInfo.get().isEndOfWord() && children.isEmpty();
    }
    
    /**
     * 补全结果封装类
     */
    public static class CompletionResult {
        private final String text;
        private final double score;
        
        public CompletionResult(String text, double score) {
            this.text = text;
            this.score = score;
        }
        
        public String getText() { return text; }
        public double getScore() { return score; }
    }
}