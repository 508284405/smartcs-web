package com.leyue.smartcs.domain.eval;

import com.leyue.smartcs.domain.eval.enums.EvaluationRunStatus;
import com.leyue.smartcs.domain.eval.enums.RunType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估运行记录领域模型
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRun {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 运行唯一标识符
     */
    private String runId;
    
    /**
     * 使用的数据集ID
     */
    private String datasetId;
    
    /**
     * 关联的AI应用ID
     */
    private Long appId;
    
    /**
     * 使用的模型提供商ID
     */
    private Long providerId;
    
    /**
     * 使用的模型ID
     */
    private Long modelId;
    
    /**
     * 运行名称
     */
    private String runName;
    
    /**
     * 运行描述
     */
    private String runDescription;
    
    /**
     * 运行类型
     */
    private RunType runType;
    
    /**
     * 评估模式：offline, online
     */
    private String evaluationMode;
    
    /**
     * RAG配置快照（检索、重排序、生成参数）
     */
    private RagConfigSnapshot ragConfigSnapshot;
    
    /**
     * 模型配置快照
     */
    private ModelConfigSnapshot modelConfigSnapshot;
    
    /**
     * 选择的评估指标列表
     */
    private List<String> selectedMetrics;
    
    /**
     * 总测试用例数
     */
    private Integer totalCases;
    
    /**
     * 已完成用例数
     */
    private Integer completedCases;
    
    /**
     * 失败用例数
     */
    private Integer failedCases;
    
    /**
     * 状态
     */
    private EvaluationRunStatus status;
    
    /**
     * 开始时间（毫秒时间戳）
     */
    private Long startTime;
    
    /**
     * 结束时间（毫秒时间戳）
     */
    private Long endTime;
    
    /**
     * 运行时长（毫秒）
     */
    private Long durationMs;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 进度信息
     */
    private ProgressInfo progressInfo;
    
    /**
     * A/B测试时的基准运行ID
     */
    private String comparisonBaselineRunId;
    
    /**
     * 发起人用户ID
     */
    private Long initiatorId;
    
    /**
     * 发起人姓名
     */
    private String initiatorName;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 是否删除：0-否，1-是
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private Long createdBy;
    
    /**
     * 更新人
     */
    private Long updatedBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 检查运行是否完成
     */
    public boolean isCompleted() {
        return status == EvaluationRunStatus.COMPLETED;
    }
    
    /**
     * 检查运行是否失败
     */
    public boolean isFailed() {
        return status == EvaluationRunStatus.FAILED;
    }
    
    /**
     * 检查运行是否正在执行
     */
    public boolean isRunning() {
        return status == EvaluationRunStatus.RUNNING;
    }
    
    /**
     * 检查是否为A/B测试
     */
    public boolean isAbTest() {
        return runType == RunType.AB_TEST;
    }
    
    /**
     * 开始运行
     */
    public void start() {
        this.status = EvaluationRunStatus.RUNNING;
        this.startTime = System.currentTimeMillis();
        this.updatedAt = this.startTime;
    }
    
    /**
     * 完成运行
     */
    public void complete() {
        this.status = EvaluationRunStatus.COMPLETED;
        this.endTime = System.currentTimeMillis();
        if (this.startTime != null) {
            this.durationMs = this.endTime - this.startTime;
        }
        this.updatedAt = this.endTime;
    }
    
    /**
     * 运行失败
     */
    public void fail(String errorMessage) {
        this.status = EvaluationRunStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = System.currentTimeMillis();
        if (this.startTime != null) {
            this.durationMs = this.endTime - this.startTime;
        }
        this.updatedAt = this.endTime;
    }
    
    /**
     * 更新进度
     */
    public void updateProgress(int completed, int failed) {
        this.completedCases = completed;
        this.failedCases = failed;
        this.updatedAt = System.currentTimeMillis();
        
        if (this.progressInfo == null) {
            this.progressInfo = new ProgressInfo();
        }
        this.progressInfo.updateProgress(this.totalCases, completed, failed);
    }
    
    /**
     * RAG配置快照
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RagConfigSnapshot {
        /**
         * 检索配置
         */
        private Map<String, Object> retrievalConfig;
        
        /**
         * 重排序配置
         */
        private Map<String, Object> rerankConfig;
        
        /**
         * 生成配置
         */
        private Map<String, Object> generationConfig;
        
        /**
         * 知识库配置
         */
        private Map<String, Object> knowledgeBaseConfig;
    }
    
    /**
     * 模型配置快照
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModelConfigSnapshot {
        /**
         * 模型名称
         */
        private String modelName;
        
        /**
         * 模型参数
         */
        private Map<String, Object> modelParams;
        
        /**
         * 提示模板
         */
        private String promptTemplate;
        
        /**
         * 嵌入模型配置
         */
        private Map<String, Object> embeddingConfig;
    }
    
    /**
     * 进度信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProgressInfo {
        /**
         * 总数
         */
        private Integer total;
        
        /**
         * 已完成数
         */
        private Integer completed;
        
        /**
         * 失败数
         */
        private Integer failed;
        
        /**
         * 进度百分比
         */
        private Double percentage;
        
        /**
         * 当前处理的用例ID
         */
        private String currentCaseId;
        
        /**
         * 预估剩余时间（毫秒）
         */
        private Long estimatedRemainingTime;
        
        /**
         * 更新进度
         */
        public void updateProgress(Integer total, Integer completed, Integer failed) {
            this.total = total;
            this.completed = completed;
            this.failed = failed;
            if (total != null && total > 0) {
                this.percentage = (double) (completed + failed) / total * 100;
            }
        }
    }
}