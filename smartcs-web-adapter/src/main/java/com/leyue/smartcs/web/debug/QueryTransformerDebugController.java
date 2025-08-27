package com.leyue.smartcs.web.debug;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.rag.factory.RagAugmentorFactory;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationTrace;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerPipeline;
import dev.langchain4j.rag.query.Query;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 调试：输出 QueryTransformer 各阶段前后变化
 */
@Slf4j
@RestController
@RequestMapping("/api/debug/query-transformer")
@RequiredArgsConstructor
public class QueryTransformerDebugController {

    private final RagAugmentorFactory ragAugmentorFactory;

    @PostMapping("/trace")
    public SingleResponse<QueryTransformationTrace> trace(@RequestBody TraceRequest req) {
        String q = req.getQuery();
        Long modelId = req.getModelId() != null ? req.getModelId() : 1L;

        log.info("QueryTransformer trace request: modelId={}, textLen={}", modelId, q == null ? 0 : q.length());

        var transformer = ragAugmentorFactory.createQueryTransformer(modelId);
        if (transformer instanceof QueryTransformerPipeline) {
            QueryTransformerPipeline pipeline = (QueryTransformerPipeline) transformer;
            QueryTransformationTrace trace = pipeline.transformWithTrace(Query.from(q));
            return SingleResponse.of(trace);
        } else {
            // 理论上不会出现；兜底使用常规 transform 并构造简易 trace
            var result = transformer.transform(Query.from(q));
            QueryTransformationTrace fallback = QueryTransformationTrace.builder()
                    .originalQuery(q)
                    .finalQueries(result.stream().map(Query::text).toList())
                    .build();
            return SingleResponse.of(fallback);
        }
    }

    @Data
    public static class TraceRequest {
        private Long modelId;
        private String query;
    }
}

