package com.leyue.smartcs.rag.query.pipeline.stages;

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
            for (Query q : queries) {
                String t = q.text().trim();
                if (onlyShort && t.length() > shortMax) continue;
                if (t.length() < minPrefix) continue;
                List<String> completions = service.complete(t, maxCand);
                for (String c : completions) {
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
            throw new QueryTransformationException(getName(), "前缀补全天段失败", e, true);
        }
    }
}

