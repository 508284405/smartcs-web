package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.api.DictionaryService;
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
    private final DictionaryService dictionaryService;

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
            
            // 优先尝试使用字典服务
            Map<String, String> corrections = getDictionaryCorrections();
            
            for (Query q : queries) {
                out.add(q); // 保留原始
                
                // 优先使用字典纠错
                String corrected = applyDictionaryCorrections(q.text(), corrections);
                if (corrected != null && !corrected.equals(q.text())) {
                    out.add(Query.from(corrected.trim()));
                } else {
                    // 降级使用原有服务
                    PhoneticCorrectionService.Result r = service.correct(q.text());
                    if (r.isChanged() && service.pass(r.getConfidence())) {
                        String serviceCorrected = r.getCorrected();
                        if (serviceCorrected != null && !serviceCorrected.trim().isEmpty()) {
                            out.add(Query.from(serviceCorrected.trim()));
                        }
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
    
    /**
     * 获取字典纠错数据
     */
    private Map<String, String> getDictionaryCorrections() {
        if (dictionaryService != null) {
            try {
                return dictionaryService.getPhoneticCorrections("default", "default", "default");
            } catch (Exception e) {
                log.warn("获取字典拼音纠错数据失败: {}", e.getMessage());
            }
        }
        return Collections.emptyMap();
    }
    
    /**
     * 应用字典纠错
     */
    private String applyDictionaryCorrections(String text, Map<String, String> corrections) {
        if (text == null || text.isEmpty() || corrections.isEmpty()) {
            return text;
        }
        
        String result = text;
        for (Map.Entry<String, String> entry : corrections.entrySet()) {
            if (result.contains(entry.getKey())) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }
        
        return result.equals(text) ? null : result;
    }
}

