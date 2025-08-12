package com.leyue.smartcs.eval.serviceimpl;

import org.springframework.stereotype.Service;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.eval.RagEvalService;
import com.leyue.smartcs.dto.eval.*;
import com.leyue.smartcs.eval.executor.RagEvalCaseBatchDeleteCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseBatchImportCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseCreateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseDeleteCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseListQryExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseUpdateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalConfigGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalConfigUpdateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetCreateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetDeleteCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetListQryExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetUpdateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalMetricsGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalStatisticsQryExe;
import com.leyue.smartcs.eval.executor.RagasConnectionTestCmdExe;
import com.leyue.smartcs.eval.executor.RagasServiceStatusQryExe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RAG评估服务实现类
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagEvalServiceImpl implements RagEvalService {
    
    // ====== 注入现有Executor依赖 ======
    
    // 数据集管理
    private final RagEvalDatasetCreateCmdExe ragEvalDatasetCreateCmdExe;
    private final RagEvalDatasetUpdateCmdExe ragEvalDatasetUpdateCmdExe;
    private final RagEvalDatasetGetQryExe ragEvalDatasetGetQryExe;
    private final RagEvalDatasetListQryExe ragEvalDatasetListQryExe;
    private final RagEvalDatasetDeleteCmdExe ragEvalDatasetDeleteCmdExe;
    
    // 测试用例管理
    private final RagEvalCaseCreateCmdExe ragEvalCaseCreateCmdExe;
    private final RagEvalCaseBatchImportCmdExe ragEvalCaseBatchImportCmdExe;
    private final RagEvalCaseUpdateCmdExe ragEvalCaseUpdateCmdExe;
    private final RagEvalCaseGetQryExe ragEvalCaseGetQryExe;
    private final RagEvalCaseListQryExe ragEvalCaseListQryExe;
    private final RagEvalCaseDeleteCmdExe ragEvalCaseDeleteCmdExe;
    private final RagEvalCaseBatchDeleteCmdExe ragEvalCaseBatchDeleteCmdExe;
    
    // 评估相关
    private final RagEvalMetricsGetQryExe ragEvalMetricsGetQryExe;
    private final RagEvalStatisticsQryExe ragEvalStatisticsQryExe;
    
    // 配置管理
    private final RagEvalConfigGetQryExe ragEvalConfigGetQryExe;
    private final RagEvalConfigUpdateCmdExe ragEvalConfigUpdateCmdExe;
    
    // RAGAS服务
    private final RagasServiceStatusQryExe ragasServiceStatusQryExe;
    private final RagasConnectionTestCmdExe ragasConnectionTestCmdExe;
    
    // ====== 数据集管理 ======
    
    @Override
    public SingleResponse<RagEvalDatasetDTO> createDataset(RagEvalDatasetCreateCmd cmd) {
        return ragEvalDatasetCreateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<RagEvalDatasetDTO> updateDataset(RagEvalDatasetUpdateCmd cmd) {
        return ragEvalDatasetUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<RagEvalDatasetDTO> getDataset(RagEvalDatasetGetQry qry) {
        return ragEvalDatasetGetQryExe.execute(qry);
    }
    
    @Override
    public PageResponse<RagEvalDatasetDTO> listDatasets(RagEvalDatasetListQry qry) {
        return ragEvalDatasetListQryExe.execute(qry);
    }
    
    @Override
    public Response deleteDataset(RagEvalDatasetDeleteCmd cmd) {
        return ragEvalDatasetDeleteCmdExe.execute(cmd);
    }
    
    // ====== 测试用例管理 ======
    
    @Override
    public SingleResponse<RagEvalCaseDTO> createCase(RagEvalCaseCreateCmd cmd) {
        return ragEvalCaseCreateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<RagEvalCaseBatchImportResultDTO> batchImportCases(RagEvalCaseBatchImportCmd cmd) {
        return ragEvalCaseBatchImportCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<RagEvalCaseDTO> updateCase(RagEvalCaseUpdateCmd cmd) {
        return ragEvalCaseUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<RagEvalCaseDTO> getCase(RagEvalCaseGetQry qry) {
        return ragEvalCaseGetQryExe.execute(qry);
    }
    
    @Override
    public PageResponse<RagEvalCaseDTO> listCases(RagEvalCaseListQry qry) {
        return ragEvalCaseListQryExe.execute(qry);
    }
    
    @Override
    public Response deleteCase(RagEvalCaseDeleteCmd cmd) {
        return ragEvalCaseDeleteCmdExe.execute(cmd);
    }
    
    @Override
    public Response batchDeleteCases(RagEvalCaseBatchDeleteCmd cmd) {
        return ragEvalCaseBatchDeleteCmdExe.execute(cmd);
    }
    
    // ====== 评估结果查询 ======
    
    @Override
    public SingleResponse<RagEvalMetricsDTO> getRunMetrics(RagEvalMetricsGetQry qry) {
        return ragEvalMetricsGetQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalRunDTO> startEvaluation(RagEvalRunStartCmd cmd) {
        log.warn("评估运行功能已迁移到新的事件驱动架构，请使用RAGAS服务");
        return SingleResponse.<RagEvalRunDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public Response stopEvaluation(RagEvalRunStopCmd cmd) {
        log.warn("评估停止功能已迁移到新的事件驱动架构");
        return Response.buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public SingleResponse<RagEvalRunDetailDTO> getRunDetail(RagEvalRunGetQry qry) {
        log.warn("评估运行详情查询功能已迁移");
        return SingleResponse.<RagEvalRunDetailDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public PageResponse<RagEvalRunDTO> listRuns(RagEvalRunListQry qry) {
        log.warn("评估运行列表功能已迁移");
        return PageResponse.buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public SingleResponse<RagEvalRunStatusDTO> getRunStatus(RagEvalRunStatusQry qry) {
        log.warn("评估运行状态查询功能已迁移");
        return SingleResponse.<RagEvalRunStatusDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public SingleResponse<RagEvalRunDTO> rerunEvaluation(RagEvalRunRerunCmd cmd) {
        log.warn("重新评估功能已迁移");
        return SingleResponse.<RagEvalRunDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public Response deleteRun(RagEvalRunDeleteCmd cmd) {
        log.warn("删除评估运行功能已迁移");
        return Response.buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public PageResponse<RagEvalRetrievalDetailDTO> listRetrievalDetails(RagEvalRetrievalDetailListQry qry) {
        log.warn("检索详情查询功能已迁移到新的事件驱动架构");
        return PageResponse.buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public PageResponse<RagEvalGenerationDetailDTO> listGenerationDetails(RagEvalGenerationDetailListQry qry) {
        log.warn("生成详情查询功能已迁移到新的事件驱动架构");
        return PageResponse.buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public SingleResponse<RagEvalExportResultDTO> exportResults(RagEvalExportCmd cmd) {
        log.warn("导出功能已迁移到新的事件驱动架构");
        return SingleResponse.<RagEvalExportResultDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    // ====== 比较分析 ======
    
    @Override
    public SingleResponse<RagEvalCompareResultDTO> compareRuns(RagEvalCompareQry qry) {
        log.warn("比较分析功能已迁移到新的事件驱动架构");
        return SingleResponse.<RagEvalCompareResultDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public SingleResponse<RagEvalAbTestResultDTO> analyzeAbTest(RagEvalAbTestQry qry) {
        log.warn("A/B测试分析功能已迁移到新的事件驱动架构");
        return SingleResponse.<RagEvalAbTestResultDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public SingleResponse<RagEvalTrendAnalysisDTO> getTrendAnalysis(RagEvalTrendAnalysisQry qry) {
        log.warn("趋势分析功能已迁移到新的事件驱动架构");
        return SingleResponse.<RagEvalTrendAnalysisDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    // ====== 统计分析 ======
    
    @Override
    public SingleResponse<RagEvalStatisticsDTO> getStatistics(RagEvalStatisticsQry qry) {
        return ragEvalStatisticsQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalDatasetStatsDTO> getDatasetStats(RagEvalDatasetStatsQry qry) {
        log.warn("数据集统计功能已迁移到新的事件驱动架构");
        return SingleResponse.<RagEvalDatasetStatsDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    @Override
    public SingleResponse<RagEvalModelStatsDTO> getModelStats(RagEvalModelStatsQry qry) {
        log.warn("模型统计功能已迁移到新的事件驱动架构");
        return SingleResponse.<RagEvalModelStatsDTO>buildFailure("UNSUPPORTED", "功能已迁移，请使用新的评估服务");
    }
    
    // ====== 配置管理 ======
    
    @Override
    public SingleResponse<RagEvalConfigDTO> getEvalConfig(RagEvalConfigGetQry qry) {
        return ragEvalConfigGetQryExe.execute(qry);
    }
    
    @Override
    public Response updateEvalConfig(RagEvalConfigUpdateCmd cmd) {
        return ragEvalConfigUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<RagasServiceStatusDTO> getRagasServiceStatus() {
        return ragasServiceStatusQryExe.execute();
    }
    
    @Override
    public SingleResponse<RagasConnectionTestResultDTO> testRagasConnection(RagasConnectionTestCmd cmd) {
        return ragasConnectionTestCmdExe.execute(cmd);
    }
}