package com.leyue.smartcs.rag.query.pipeline;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryTransformer 测试/调试用：记录各阶段的查询前后变化
 */
@Data
@Builder
public class QueryTransformationTrace {

    /** 原始用户查询 */
    private String originalQuery;

    /** 最终输出的查询集合（文本） */
    private List<String> finalQueries;

    /** 各阶段处理的前后变化 */
    @Builder.Default
    private List<StageTrace> stages = new ArrayList<>();

    @Data
    @Builder
    public static class StageTrace {
        /** 阶段名称 */
        private String stage;
        /** 输入查询文本列表（去重前的快照） */
        private List<String> before;
        /** 输出查询文本列表（去重前的快照） */
        private List<String> after;
        /** 新增的查询（after - before） */
        private List<String> added;
        /** 被移除的查询（before - after） */
        private List<String> removed;
        /** 保持不变的查询（交集） */
        private List<String> unchanged;
        /** 阶段耗时（毫秒） */
        private long elapsedMs;
        /** 附注（如：skipped/failure原因） */
        private String note;
    }
}

