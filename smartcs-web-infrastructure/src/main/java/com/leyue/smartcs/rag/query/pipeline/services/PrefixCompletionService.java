package com.leyue.smartcs.rag.query.pipeline.services;

import com.leyue.smartcs.api.DictionaryService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 前缀补全服务（轻量 Trie 实现，支持字典服务）
 */
@Slf4j
public class PrefixCompletionService implements IPrefixCompletionService {

    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        boolean end;
        int freq; // 简单词频
    }

    private final Node root = new Node();
    private final DictionaryService dictionaryService;

    public PrefixCompletionService(Collection<String> dictionary) {
        this(dictionary, null);
    }

    public PrefixCompletionService(Collection<String> dictionary, DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
        
        // 优先使用字典服务的数据
        Set<String> prefixWords = getDictionaryPrefixWords();
        
        if (prefixWords != null && !prefixWords.isEmpty()) {
            log.debug("使用字典服务数据构建前缀词汇树: size={}", prefixWords.size());
            for (String word : prefixWords) {
                insert(word);
            }
        } else if (dictionary != null && !dictionary.isEmpty()) {
            log.debug("使用传入字典数据构建前缀词汇树: size={}", dictionary.size());
            for (String word : dictionary) {
                insert(word);
            }
        } else {
            log.debug("使用默认样例词典构建前缀词汇树");
            insertDefaultWords();
        }
    }
    
    /**
     * 从字典服务获取前缀词汇数据
     */
    private Set<String> getDictionaryPrefixWords() {
        if (dictionaryService == null) {
            return null;
        }
        
        try {
            return dictionaryService.getPrefixWords("default", "default", "default");
        } catch (Exception e) {
            log.warn("从字典服务获取前缀词汇失败，将使用回退数据: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 插入默认的样例词汇
     */
    private void insertDefaultWords() {
        insert("射雕英雄传");
        insert("神雕侠侣");
        insert("天龙八部");
        insert("罗密欧与朱丽叶");
        insert("红楼梦");
        insert("如何");
        insert("怎么");
        insert("什么");
        insert("为什么");
        insert("哪里");
        insert("how");
        insert("what");
        insert("where");
        insert("when");
        insert("why");
    }

    private void insert(String word) {
        Node cur = root;
        for (char c : word.toCharArray()) {
            cur = cur.children.computeIfAbsent(c, k -> new Node());
        }
        cur.end = true;
        cur.freq++;
    }

    public List<String> complete(String prefix, int limit) {
        List<String> results = new ArrayList<>();
        if (prefix == null || prefix.isEmpty()) return results;

        Node cur = root;
        for (char c : prefix.toCharArray()) {
            cur = cur.children.get(c);
            if (cur == null) return results;
        }
        dfs(prefix, cur, results, limit);
        // 简单按长度/频率排序
        results.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        if (results.size() > limit) return results.subList(0, limit);
        return results;
    }

    private void dfs(String path, Node node, List<String> out, int limit) {
        if (out.size() >= limit) return;
        if (node.end) out.add(path);
        for (Map.Entry<Character, Node> e : node.children.entrySet()) {
            if (out.size() >= limit) break;
            dfs(path + e.getKey(), e.getValue(), out, limit);
        }
    }

    @Override
    public List<String> completeWithContext(String prefix, IPrefixCompletionService.CompletionContext context, int limit) {
        // For now, just use the basic completion - could be enhanced with context later
        return complete(prefix, limit);
    }

    @Override
    public void addWords(Collection<String> words) {
        if (words != null) {
            for (String word : words) {
                insert(word);
            }
        }
    }

    @Override
    public void addWord(String word, double weight) {
        // Use the existing insert method, weight is ignored for now
        insert(word);
    }

    @Override
    public void updateWordWeight(String word, double feedback) {
        // Implementation could be enhanced to track and update weights
        log.debug("Word weight update requested for '{}' with feedback: {}", word, feedback);
    }

    @Override
    public void warmupCache(Collection<String> topPrefixes) {
        // Implementation could be enhanced to pre-compute completions for top prefixes
        log.debug("Cache warmup requested for {} prefixes", topPrefixes != null ? topPrefixes.size() : 0);
    }

    @Override
    public IPrefixCompletionService.DictionaryStats getStats() {
        // Count nodes recursively
        int[] nodeCount = {0};
        countNodes(root, nodeCount);
        
        return new IPrefixCompletionService.DictionaryStats(
            nodeCount[0], // approximated total words as node count
            nodeCount[0], // total nodes
            0L, // memory usage (not tracked for now)
            100L // cache hit rate (mock value)
        );
    }
    
    private void countNodes(Node node, int[] count) {
        count[0]++;
        for (Node child : node.children.values()) {
            countNodes(child, count);
        }
    }

    @Override
    public void cleanup() {
        // Could implement cleanup of low-frequency nodes
        log.debug("Cleanup requested for prefix completion service");
    }
}

