package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import com.leyue.smartcs.rag.query.pipeline.services.PrefixCompletionService;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class PrefixCompletionStage implements QueryTransformerStage {

    private final PrefixCompletionService service;
    private final DictionaryService dictionaryService;

    @Override
    public String getName() {
        return "PrefixCompletionStage";
    }

    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnablePrefixCompletion();
    }

    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) return Collections.emptyList();
        QueryContext.PrefixConfig cfg = context.getPipelineConfig().getPrefixConfig();
        int minPrefix = cfg != null ? cfg.getMinPrefixLength() : 2;
        int maxCand = cfg != null ? cfg.getMaxCandidates() : 5;
        boolean onlyShort = cfg == null || cfg.isOnlyShortQuery();
        int shortMax = cfg != null ? cfg.getShortQueryMaxLen() : 5;

        try {
            List<Query> out = new ArrayList<>(queries);
            
            // 获取字典前缀词汇集合
            Set<String> prefixWords = getDictionaryPrefixWords();
            
            for (Query q : queries) {
                String t = q.text().trim();
                if (onlyShort && t.length() > shortMax) continue;
                if (t.length() < minPrefix) continue;
                
                // 先尝试字典前缀补全
                List<String> dictCompletions = getDictionaryCompletions(t, prefixWords, maxCand);
                
                // 再使用服务补全
                List<String> serviceCompletions = service.complete(t, maxCand);
                
                // 合并结果，字典优先
                Set<String> allCompletions = new LinkedHashSet<>(dictCompletions);
                allCompletions.addAll(serviceCompletions);
                
                for (String c : allCompletions) {
                    if (!c.equals(t)) out.add(Query.from(c));
                }
            }
            // 去重
            LinkedHashSet<String> seen = new LinkedHashSet<>();
            List<Query> unique = new ArrayList<>();
            for (Query q : out) {
                String k = q.text().trim();
                if (seen.add(k)) unique.add(q);
            }
            return unique;
        } catch (Exception e) {
            log.warn("前缀补全阶段失败，跳过: {}", e.getMessage());
            throw new QueryTransformationException(getName(), "前缀补全阶段失败", e, true);
        }
    }
    
    /**
     * 获取字典前缀词汇
     */
    private Set<String> getDictionaryPrefixWords() {
        if (dictionaryService != null) {
            try {
                return dictionaryService.getPrefixSourceWords("default", "default", "default");
            } catch (Exception e) {
                log.warn("获取字典前缀词汇失败，使用服务降级: {}", e.getMessage());
            }
        }
        return Collections.emptySet();
    }
    
    /**
     * 从字典词汇中获取前缀补全
     */
    private List<String> getDictionaryCompletions(String prefix, Set<String> prefixWords, int maxCandidates) {
        List<String> completions = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();
        
        int count = 0;
        for (String word : prefixWords) {
            if (count >= maxCandidates) break;
            
            if (word.toLowerCase().startsWith(lowerPrefix) && !word.equals(prefix)) {
                completions.add(word);
                count++;
            }
        }
        
        return completions;
    }
}

