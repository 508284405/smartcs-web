package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import com.leyue.smartcs.rag.query.pipeline.services.SynonymRecallService;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SynonymRecallStage implements QueryTransformerStage {

    private final SynonymRecallService service;
    private final DictionaryService dictionaryService;

    @Override
    public String getName() {
        return "SynonymRecallStage";
    }

    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnableSynonymRecall();
    }

    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) return Collections.emptyList();
        QueryContext.SynonymConfig cfg = context.getPipelineConfig().getSynonymConfig();
        int topK = cfg != null ? cfg.getTopK() : 5;
        try {
            List<Query> out = new ArrayList<>(queries);
            
            // 优先尝试使用字典服务
            Map<String, Set<String>> synonymSets = getDictionarySynonyms();
            
            for (Query q : queries) {
                Set<String> terms = extractTerms(q.text());
                
                // 先尝试字典同义词
                Set<String> dictSynonyms = getDictionarySynonymsForTerms(terms, synonymSets);
                
                // 如果字典没有足够的同义词，再使用服务
                List<String> serviceSyns = service.recallForTerms(terms, topK);
                
                // 合并同义词
                Set<String> allSynonyms = new LinkedHashSet<>(dictSynonyms);
                allSynonyms.addAll(serviceSyns);
                
                for (String s : allSynonyms) {
                    // 简单策略：将同义词附加生成一条变体
                    String nv = q.text() + " " + s;
                    out.add(Query.from(nv.trim()));
                }
            }
            // 去重
            LinkedHashSet<String> seen = new LinkedHashSet<>();
            List<Query> unique = new ArrayList<>();
            for (Query q : out) {
                String k = q.text().trim().toLowerCase();
                if (seen.add(k)) unique.add(q);
            }
            return unique;
        } catch (Exception e) {
            log.warn("近义词召回阶段失败，跳过: {}", e.getMessage());
            throw new QueryTransformationException(getName(), "近义词召回失败", e, true);
        }
    }

    /**
     * 获取字典同义词数据
     */
    private Map<String, Set<String>> getDictionarySynonyms() {
        if (dictionaryService != null) {
            try {
                return dictionaryService.getSemanticSynonyms("default", "default", "default");
            } catch (Exception e) {
                log.warn("获取字典同义词失败，使用服务降级: {}", e.getMessage());
            }
        }
        return Collections.emptyMap();
    }
    
    /**
     * 为词汇获取字典同义词
     */
    private Set<String> getDictionarySynonymsForTerms(Set<String> terms, Map<String, Set<String>> synonymSets) {
        Set<String> result = new HashSet<>();
        for (String term : terms) {
            Set<String> synonyms = synonymSets.get(term.toLowerCase());
            if (synonyms != null) {
                result.addAll(synonyms);
            }
        }
        return result;
    }

    private Set<String> extractTerms(String text) {
        if (text == null || text.isEmpty()) return Collections.emptySet();
        // 简易分词：按空白/标点切分，保留中文连续片段与英文/数字 token
        String[] parts = text.split("[\\s\\p{Punct}]+");
        return Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> s.length() > 1)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

