package com.leyue.smartcs.rag.query.pipeline.services;

import com.leyue.smartcs.api.DictionaryService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 近义词召回服务（支持字典服务）
 * 实际可替换为向量近邻检索
 */
@Slf4j
public class SynonymRecallService {

    private final Map<String, Set<String>> synonyms = new HashMap<>();
    private final DictionaryService dictionaryService;

    public SynonymRecallService() {
        this(null);
    }

    public SynonymRecallService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
        
        // 优先使用字典服务的数据
        Map<String, Set<String>> dictionarySynonyms = getDictionarySynonyms();
        
        if (dictionarySynonyms != null && !dictionarySynonyms.isEmpty()) {
            log.debug("使用字典服务数据构建同义词映射: size={}", dictionarySynonyms.size());
            synonyms.putAll(dictionarySynonyms);
        } else {
            log.debug("使用默认样例数据构建同义词映射");
            loadDefaultSynonyms();
        }
    }
    
    /**
     * 从字典服务获取同义词数据
     */
    private Map<String, Set<String>> getDictionarySynonyms() {
        if (dictionaryService == null) {
            return null;
        }
        
        try {
            return dictionaryService.getSynonymSets("default", "default", "default");
        } catch (Exception e) {
            log.warn("从字典服务获取同义词数据失败，将使用回退数据: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 加载默认的同义词数据
     */
    private void loadDefaultSynonyms() {
        synonyms.put("国六", new HashSet<>(Arrays.asList("国VI", "国6", "China 6")));
        synonyms.put("菜鸟网络", new HashSet<>(Arrays.asList("CN", "菜鸟")));
        synonyms.put("人工智能", new HashSet<>(Arrays.asList("AI", "artificial intelligence")));
        synonyms.put("机器学习", new HashSet<>(Arrays.asList("ML", "machine learning")));
        synonyms.put("问题", new HashSet<>(Arrays.asList("疑问", "困惑", "难题", "issue", "problem")));
        synonyms.put("方法", new HashSet<>(Arrays.asList("方式", "途径", "办法", "手段", "方案", "method", "way")));
        synonyms.put("解决", new HashSet<>(Arrays.asList("处理", "解决方案", "fix", "solve", "resolve")));
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
            for (Map.Entry<String, Set<String>> e : synonyms.entrySet()) {
                if (e.getValue().contains(t)) {
                    result.add(e.getKey());
                }
            }
            if (result.size() >= topK) break;
        }
        return result.stream().limit(topK).collect(Collectors.toList());
    }
}

