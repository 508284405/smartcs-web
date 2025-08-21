package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import com.leyue.smartcs.rag.query.pipeline.services.PhoneticCorrectionService;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class PhoneticCorrectionStage implements QueryTransformerStage {

    private final PhoneticCorrectionService service;

    @Override
    public String getName() {
        return "PhoneticCorrectionStage";
    }

    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnablePhoneticCorrection();
    }

    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) return Collections.emptyList();
        try {
            List<Query> out = new ArrayList<>();
            for (Query q : queries) {
                out.add(q); // 保留原始
                PhoneticCorrectionService.Result r = service.correct(q.text());
                if (r.isChanged() && service.pass(r.getConfidence())) {
                    String corrected = r.getCorrected();
                    if (corrected != null && !corrected.trim().isEmpty()) {
                        out.add(Query.from(corrected.trim()));
                    }
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
            log.warn("拼音改写阶段失败，跳过: {}", e.getMessage());
            throw new QueryTransformationException(getName(), "拼音改写失败", e, true);
        }
    }
}

