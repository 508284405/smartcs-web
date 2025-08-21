package com.leyue.smartcs.rag.query.pipeline.services;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 近义词召回服务（轻量字典版，演示用）。
 * 实际可替换为向量近邻检索。
 */
public class SynonymRecallService {

    private final Map<String, List<String>> synonyms = new HashMap<>();

    public SynonymRecallService() {
        // 演示同义词/别名（可由 KB/实体词表生成）
        synonyms.put("国六", Arrays.asList("国VI", "国6", "China 6"));
        synonyms.put("菜鸟网络", Arrays.asList("CN", "菜鸟"));
        synonyms.put("人工智能", Arrays.asList("AI"));
        synonyms.put("机器学习", Arrays.asList("ML"));
    }

    /**
     * 对输入关键词列表，返回近义词候选（去重后）。
     */
    public List<String> recallForTerms(Collection<String> terms, int topK) {
        if (terms == null || terms.isEmpty()) return Collections.emptyList();
        Set<String> result = new LinkedHashSet<>();
        for (String t : terms) {
            // key 命中
            if (synonyms.containsKey(t)) {
                result.addAll(synonyms.get(t));
            }
            // value 命中（反向）
            for (Map.Entry<String, List<String>> e : synonyms.entrySet()) {
                if (e.getValue().contains(t)) {
                    result.add(e.getKey());
                }
            }
            if (result.size() >= topK) break;
        }
        return result.stream().limit(topK).collect(Collectors.toList());
    }
}

