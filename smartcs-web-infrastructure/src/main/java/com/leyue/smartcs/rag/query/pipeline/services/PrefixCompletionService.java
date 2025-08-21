package com.leyue.smartcs.rag.query.pipeline.services;

import java.util.*;

/**
 * 前缀补全服务（轻量 Trie 实现，演示用）
 */
public class PrefixCompletionService {

    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        boolean end;
        int freq; // 简单词频
    }

    private final Node root = new Node();

    public PrefixCompletionService(Collection<String> dictionary) {
        if (dictionary != null) {
            for (String word : dictionary) {
                insert(word);
            }
        }
        // 默认样例词典（可被覆盖）
        if (dictionary == null || dictionary.isEmpty()) {
            insert("射雕英雄传");
            insert("神雕侠侣");
            insert("天龙八部");
            insert("罗密欧与朱丽叶");
            insert("红楼梦");
        }
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
}

