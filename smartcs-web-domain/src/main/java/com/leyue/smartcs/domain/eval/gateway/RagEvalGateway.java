package com.leyue.smartcs.domain.eval.gateway;

import com.leyue.smartcs.domain.eval.*;
import com.leyue.smartcs.domain.eval.enums.EvaluationRunStatus;
import com.leyue.smartcs.domain.eval.enums.MetricCategory;
import com.leyue.smartcs.domain.eval.enums.RunType;

import java.util.List;
import java.util.Optional;

/**
 * RAG评估网关接口
 * 定义评估系统的数据访问抽象
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalGateway {
    
    // ====== 数据集相关 ======
    
    /**
     * 创建评估数据集
     */
    RagEvalDataset createDataset(RagEvalDataset dataset);
    
    /**
     * 更新评估数据集
     */
    RagEvalDataset updateDataset(RagEvalDataset dataset);
    
    /**
     * 根据数据集ID查询数据集
     */
    Optional<RagEvalDataset> findDatasetById(String datasetId);
    
    /**
     * 根据创建者ID查询数据集列表
     */
    List<RagEvalDataset> findDatasetsByCreatorId(Long creatorId);
    
    /**
     * 查询数据集列表（支持分页和过滤）
     */
    List<RagEvalDataset> findDatasets(DatasetQueryCondition condition);
    
    /**
     * 统计数据集总数
     */
    long countDatasets(DatasetQueryCondition condition);
    
    /**
     * 删除数据集（软删除）
     */
    void deleteDataset(String datasetId);
    
    // ====== 测试用例相关 ======
    
    /**
     * 创建测试用例
     */
    RagEvalCase createCase(RagEvalCase evalCase);
    
    /**
     * 批量创建测试用例
     */
    List<RagEvalCase> createCases(List<RagEvalCase> evalCases);
    
    /**
     * 更新测试用例
     */
    RagEvalCase updateCase(RagEvalCase evalCase);
    
    /**
     * 根据用例ID查询测试用例
     */
    Optional<RagEvalCase> findCaseById(String caseId);
    
    /**
     * 根据数据集ID查询测试用例列表
     */
    List<RagEvalCase> findCasesByDatasetId(String datasetId);
    
    /**
     * 查询测试用例列表（支持分页和过滤）
     */
    List<RagEvalCase> findCases(CaseQueryCondition condition);
    
    /**
     * 统计测试用例总数
     */
    long countCases(CaseQueryCondition condition);
    
    /**
     * 删除测试用例（软删除）
     */
    void deleteCase(String caseId);
    
    /**
     * 批量删除测试用例
     */
    void deleteCases(List<String> caseIds);
    
    // ====== 评估运行相关 ======
    
    /**
     * 创建评估运行
     */
    RagEvalRun createRun(RagEvalRun evalRun);
    
    /**
     * 更新评估运行
     */
    RagEvalRun updateRun(RagEvalRun evalRun);
    
    /**
     * 根据运行ID查询评估运行
     */
    Optional<RagEvalRun> findRunById(String runId);
    
    /**
     * 查询评估运行列表（支持分页和过滤）
     */
    List<RagEvalRun> findRuns(RunQueryCondition condition);
    
    /**
     * 统计评估运行总数
     */
    long countRuns(RunQueryCondition condition);
    
    /**
     * 根据状态查询运行列表
     */
    List<RagEvalRun> findRunsByStatus(EvaluationRunStatus status);
    
    /**
     * 查询正在运行的评估
     */
    List<RagEvalRun> findRunningRuns();
    
    /**
     * 删除评估运行（软删除）
     */
    void deleteRun(String runId);
    
    // ====== 指标相关 ======
    
    /**
     * 保存评估指标
     */
    RagEvalMetric saveMetric(RagEvalMetric metric);
    
    /**
     * 批量保存评估指标
     */
    List<RagEvalMetric> saveMetrics(List<RagEvalMetric> metrics);
    
    /**
     * 根据运行ID和指标类别查询指标
     */
    Optional<RagEvalMetric> findMetricByRunIdAndCategory(String runId, MetricCategory category);
    
    /**
     * 根据运行ID查询所有指标
     */
    List<RagEvalMetric> findMetricsByRunId(String runId);
    
    /**
     * 查询指标列表（支持分页和过滤）
     */
    List<RagEvalMetric> findMetrics(MetricQueryCondition condition);
    
    /**
     * 删除指标
     */
    void deleteMetric(String runId, MetricCategory category);
    
    /**
     * 删除运行的所有指标
     */
    void deleteMetricsByRunId(String runId);
    
    // ====== 详细结果相关 ======
    
    /**
     * 保存检索详情
     */
    RagEvalRetrievalDetail saveRetrievalDetail(RagEvalRetrievalDetail detail);
    
    /**
     * 批量保存检索详情
     */
    List<RagEvalRetrievalDetail> saveRetrievalDetails(List<RagEvalRetrievalDetail> details);
    
    /**
     * 根据运行ID和用例ID查询检索详情
     */
    Optional<RagEvalRetrievalDetail> findRetrievalDetail(String runId, String caseId);
    
    /**
     * 根据运行ID查询检索详情列表
     */
    List<RagEvalRetrievalDetail> findRetrievalDetailsByRunId(String runId);
    
    /**
     * 保存生成详情
     */
    RagEvalGenerationDetail saveGenerationDetail(RagEvalGenerationDetail detail);
    
    /**
     * 批量保存生成详情
     */
    List<RagEvalGenerationDetail> saveGenerationDetails(List<RagEvalGenerationDetail> details);
    
    /**
     * 根据运行ID和用例ID查询生成详情
     */
    Optional<RagEvalGenerationDetail> findGenerationDetail(String runId, String caseId);
    
    /**
     * 根据运行ID查询生成详情列表
     */
    List<RagEvalGenerationDetail> findGenerationDetailsByRunId(String runId);
    
    /**
     * 删除运行的所有详情数据
     */
    void deleteDetailsByRunId(String runId);
    
    // ====== 统计分析相关 ======
    
    /**
     * 统计数据集的运行次数
     */
    long countRunsByDatasetId(String datasetId);
    
    /**
     * 查询数据集的最近运行记录
     */
    List<RagEvalRun> findRecentRunsByDatasetId(String datasetId, int limit);
    
    /**
     * 统计模型的运行次数
     */
    long countRunsByModelId(Long modelId);
    
    /**
     * 查询应用的最近运行记录
     */
    List<RagEvalRun> findRecentRunsByAppId(Long appId, int limit);
    
    // ====== 查询条件类 ======
    
    /**
     * 数据集查询条件
     */
    class DatasetQueryCondition {
        private Long creatorId;
        private String domain;
        private Integer status;
        private List<String> tags;
        private String searchKeyword;
        private Integer pageNum;
        private Integer pageSize;
        private String sortField;
        private String sortOrder;
        
        // getters and setters
        public Long getCreatorId() { return creatorId; }
        public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public String getSearchKeyword() { return searchKeyword; }
        public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
        public Integer getPageNum() { return pageNum; }
        public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
        public String getSortField() { return sortField; }
        public void setSortField(String sortField) { this.sortField = sortField; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    }
    
    /**
     * 测试用例查询条件
     */
    class CaseQueryCondition {
        private String datasetId;
        private String category;
        private String difficultyTag;
        private String queryType;
        private Integer status;
        private String searchKeyword;
        private Integer pageNum;
        private Integer pageSize;
        private String sortField;
        private String sortOrder;
        
        // getters and setters
        public String getDatasetId() { return datasetId; }
        public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDifficultyTag() { return difficultyTag; }
        public void setDifficultyTag(String difficultyTag) { this.difficultyTag = difficultyTag; }
        public String getQueryType() { return queryType; }
        public void setQueryType(String queryType) { this.queryType = queryType; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getSearchKeyword() { return searchKeyword; }
        public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
        public Integer getPageNum() { return pageNum; }
        public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
        public String getSortField() { return sortField; }
        public void setSortField(String sortField) { this.sortField = sortField; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    }
    
    /**
     * 运行查询条件
     */
    class RunQueryCondition {
        private String datasetId;
        private Long appId;
        private Long modelId;
        private RunType runType;
        private EvaluationRunStatus status;
        private Long initiatorId;
        private String searchKeyword;
        private Long startTimeFrom;
        private Long startTimeTo;
        private Integer pageNum;
        private Integer pageSize;
        private String sortField;
        private String sortOrder;
        
        // getters and setters
        public String getDatasetId() { return datasetId; }
        public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
        public Long getAppId() { return appId; }
        public void setAppId(Long appId) { this.appId = appId; }
        public Long getModelId() { return modelId; }
        public void setModelId(Long modelId) { this.modelId = modelId; }
        public RunType getRunType() { return runType; }
        public void setRunType(RunType runType) { this.runType = runType; }
        public EvaluationRunStatus getStatus() { return status; }
        public void setStatus(EvaluationRunStatus status) { this.status = status; }
        public Long getInitiatorId() { return initiatorId; }
        public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }
        public String getSearchKeyword() { return searchKeyword; }
        public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
        public Long getStartTimeFrom() { return startTimeFrom; }
        public void setStartTimeFrom(Long startTimeFrom) { this.startTimeFrom = startTimeFrom; }
        public Long getStartTimeTo() { return startTimeTo; }
        public void setStartTimeTo(Long startTimeTo) { this.startTimeTo = startTimeTo; }
        public Integer getPageNum() { return pageNum; }
        public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
        public String getSortField() { return sortField; }
        public void setSortField(String sortField) { this.sortField = sortField; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    }
    
    /**
     * 指标查询条件
     */
    class MetricQueryCondition {
        private String runId;
        private MetricCategory category;
        private Long createdFrom;
        private Long createdTo;
        private Integer pageNum;
        private Integer pageSize;
        private String sortField;
        private String sortOrder;
        
        // getters and setters
        public String getRunId() { return runId; }
        public void setRunId(String runId) { this.runId = runId; }
        public MetricCategory getCategory() { return category; }
        public void setCategory(MetricCategory category) { this.category = category; }
        public Long getCreatedFrom() { return createdFrom; }
        public void setCreatedFrom(Long createdFrom) { this.createdFrom = createdFrom; }
        public Long getCreatedTo() { return createdTo; }
        public void setCreatedTo(Long createdTo) { this.createdTo = createdTo; }
        public Integer getPageNum() { return pageNum; }
        public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
        public String getSortField() { return sortField; }
        public void setSortField(String sortField) { this.sortField = sortField; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    }
}