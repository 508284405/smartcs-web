package com.leyue.smartcs.eval.serviceimpl;

import org.springframework.stereotype.Service;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.eval.RagEvalService;
import com.leyue.smartcs.dto.eval.RagEvalAbTestQry;
import com.leyue.smartcs.dto.eval.RagEvalAbTestResultDTO;
import com.leyue.smartcs.dto.eval.RagEvalCaseBatchDeleteCmd;
import com.leyue.smartcs.dto.eval.RagEvalCaseBatchImportCmd;
import com.leyue.smartcs.dto.eval.RagEvalCaseBatchImportResultDTO;
import com.leyue.smartcs.dto.eval.RagEvalCaseCreateCmd;
import com.leyue.smartcs.dto.eval.RagEvalCaseDTO;
import com.leyue.smartcs.dto.eval.RagEvalCaseDeleteCmd;
import com.leyue.smartcs.dto.eval.RagEvalCaseGetQry;
import com.leyue.smartcs.dto.eval.RagEvalCaseListQry;
import com.leyue.smartcs.dto.eval.RagEvalCaseUpdateCmd;
import com.leyue.smartcs.dto.eval.RagEvalCompareQry;
import com.leyue.smartcs.dto.eval.RagEvalCompareResultDTO;
import com.leyue.smartcs.dto.eval.RagEvalConfigDTO;
import com.leyue.smartcs.dto.eval.RagEvalConfigGetQry;
import com.leyue.smartcs.dto.eval.RagEvalConfigUpdateCmd;
import com.leyue.smartcs.dto.eval.RagEvalDatasetCreateCmd;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDTO;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDeleteCmd;
import com.leyue.smartcs.dto.eval.RagEvalDatasetGetQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetListQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetStatsDTO;
import com.leyue.smartcs.dto.eval.RagEvalDatasetStatsQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetUpdateCmd;
import com.leyue.smartcs.dto.eval.RagEvalExportCmd;
import com.leyue.smartcs.dto.eval.RagEvalExportResultDTO;
import com.leyue.smartcs.dto.eval.RagEvalGenerationDetailDTO;
import com.leyue.smartcs.dto.eval.RagEvalGenerationDetailListQry;
import com.leyue.smartcs.dto.eval.RagEvalMetricsDTO;
import com.leyue.smartcs.dto.eval.RagEvalMetricsGetQry;
import com.leyue.smartcs.dto.eval.RagEvalModelStatsDTO;
import com.leyue.smartcs.dto.eval.RagEvalModelStatsQry;
import com.leyue.smartcs.dto.eval.RagEvalRetrievalDetailDTO;
import com.leyue.smartcs.dto.eval.RagEvalRetrievalDetailListQry;
import com.leyue.smartcs.dto.eval.RagEvalRunDTO;
import com.leyue.smartcs.dto.eval.RagEvalRunDeleteCmd;
import com.leyue.smartcs.dto.eval.RagEvalRunDetailDTO;
import com.leyue.smartcs.dto.eval.RagEvalRunGetQry;
import com.leyue.smartcs.dto.eval.RagEvalRunListQry;
import com.leyue.smartcs.dto.eval.RagEvalRunRerunCmd;
import com.leyue.smartcs.dto.eval.RagEvalRunStartCmd;
import com.leyue.smartcs.dto.eval.RagEvalRunStatusDTO;
import com.leyue.smartcs.dto.eval.RagEvalRunStatusQry;
import com.leyue.smartcs.dto.eval.RagEvalRunStopCmd;
import com.leyue.smartcs.dto.eval.RagEvalStatisticsDTO;
import com.leyue.smartcs.dto.eval.RagEvalStatisticsQry;
import com.leyue.smartcs.dto.eval.RagEvalTrendAnalysisDTO;
import com.leyue.smartcs.dto.eval.RagEvalTrendAnalysisQry;
import com.leyue.smartcs.dto.eval.RagasConnectionTestCmd;
import com.leyue.smartcs.dto.eval.RagasConnectionTestResultDTO;
import com.leyue.smartcs.dto.eval.RagasServiceStatusDTO;
import com.leyue.smartcs.eval.executor.RagEvalAbTestQryExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseBatchDeleteCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseBatchImportCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseCreateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseDeleteCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseListQryExe;
import com.leyue.smartcs.eval.executor.RagEvalCaseUpdateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalCompareQryExe;
import com.leyue.smartcs.eval.executor.RagEvalConfigGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalConfigUpdateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetCreateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetDeleteCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetListQryExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetStatsQryExe;
import com.leyue.smartcs.eval.executor.RagEvalDatasetUpdateCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalExportCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalGenerationDetailListQryExe;
import com.leyue.smartcs.eval.executor.RagEvalMetricsGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalModelStatsQryExe;
import com.leyue.smartcs.eval.executor.RagEvalRetrievalDetailListQryExe;
import com.leyue.smartcs.eval.executor.RagEvalRunDeleteCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalRunGetQryExe;
import com.leyue.smartcs.eval.executor.RagEvalRunListQryExe;
import com.leyue.smartcs.eval.executor.RagEvalRunRerunCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalRunStartCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalRunStatusQryExe;
import com.leyue.smartcs.eval.executor.RagEvalRunStopCmdExe;
import com.leyue.smartcs.eval.executor.RagEvalStatisticsQryExe;
import com.leyue.smartcs.eval.executor.RagEvalTrendAnalysisQryExe;
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
    
    // ====== 注入Executor依赖 ======
    
    // 数据集管理Executor
    private final RagEvalDatasetCreateCmdExe ragEvalDatasetCreateCmdExe;
    private final RagEvalDatasetUpdateCmdExe ragEvalDatasetUpdateCmdExe;
    private final RagEvalDatasetGetQryExe ragEvalDatasetGetQryExe;
    private final RagEvalDatasetListQryExe ragEvalDatasetListQryExe;
    private final RagEvalDatasetDeleteCmdExe ragEvalDatasetDeleteCmdExe;
    
    // 测试用例管理Executor
    private final RagEvalCaseCreateCmdExe ragEvalCaseCreateCmdExe;
    private final RagEvalCaseBatchImportCmdExe ragEvalCaseBatchImportCmdExe;
    private final RagEvalCaseUpdateCmdExe ragEvalCaseUpdateCmdExe;
    private final RagEvalCaseGetQryExe ragEvalCaseGetQryExe;
    private final RagEvalCaseListQryExe ragEvalCaseListQryExe;
    private final RagEvalCaseDeleteCmdExe ragEvalCaseDeleteCmdExe;
    private final RagEvalCaseBatchDeleteCmdExe ragEvalCaseBatchDeleteCmdExe;
    
    // 评估运行管理Executor
    private final RagEvalRunStartCmdExe ragEvalRunStartCmdExe;
    private final RagEvalRunStopCmdExe ragEvalRunStopCmdExe;
    private final RagEvalRunGetQryExe ragEvalRunGetQryExe;
    private final RagEvalRunListQryExe ragEvalRunListQryExe;
    private final RagEvalRunStatusQryExe ragEvalRunStatusQryExe;
    private final RagEvalRunRerunCmdExe ragEvalRunRerunCmdExe;
    private final RagEvalRunDeleteCmdExe ragEvalRunDeleteCmdExe;
    
    // 评估结果查询Executor
    private final RagEvalMetricsGetQryExe ragEvalMetricsGetQryExe;
    private final RagEvalRetrievalDetailListQryExe ragEvalRetrievalDetailListQryExe;
    private final RagEvalGenerationDetailListQryExe ragEvalGenerationDetailListQryExe;
    private final RagEvalExportCmdExe ragEvalExportCmdExe;
    
    // 比较分析Executor
    private final RagEvalCompareQryExe ragEvalCompareQryExe;
    private final RagEvalAbTestQryExe ragEvalAbTestQryExe;
    private final RagEvalTrendAnalysisQryExe ragEvalTrendAnalysisQryExe;
    
    // 统计分析Executor
    private final RagEvalStatisticsQryExe ragEvalStatisticsQryExe;
    private final RagEvalDatasetStatsQryExe ragEvalDatasetStatsQryExe;
    private final RagEvalModelStatsQryExe ragEvalModelStatsQryExe;
    
    // 配置管理Executor
    private final RagEvalConfigGetQryExe ragEvalConfigGetQryExe;
    private final RagEvalConfigUpdateCmdExe ragEvalConfigUpdateCmdExe;
    
    // RAGAS服务Executor
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
    
    // ====== 评估运行管理 ======
    
    @Override
    public SingleResponse<RagEvalRunDTO> startEvaluation(RagEvalRunStartCmd cmd) {
        return ragEvalRunStartCmdExe.execute(cmd);
    }
    
    @Override
    public Response stopEvaluation(RagEvalRunStopCmd cmd) {
        return ragEvalRunStopCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<RagEvalRunDetailDTO> getRunDetail(RagEvalRunGetQry qry) {
        return ragEvalRunGetQryExe.execute(qry);
    }
    
    @Override
    public PageResponse<RagEvalRunDTO> listRuns(RagEvalRunListQry qry) {
        return ragEvalRunListQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalRunStatusDTO> getRunStatus(RagEvalRunStatusQry qry) {
        return ragEvalRunStatusQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalRunDTO> rerunEvaluation(RagEvalRunRerunCmd cmd) {
        return ragEvalRunRerunCmdExe.execute(cmd);
    }
    
    @Override
    public Response deleteRun(RagEvalRunDeleteCmd cmd) {
        return ragEvalRunDeleteCmdExe.execute(cmd);
    }
    
    // ====== 评估结果查询 ======
    
    @Override
    public SingleResponse<RagEvalMetricsDTO> getRunMetrics(RagEvalMetricsGetQry qry) {
        return ragEvalMetricsGetQryExe.execute(qry);
    }
    
    @Override
    public PageResponse<RagEvalRetrievalDetailDTO> listRetrievalDetails(RagEvalRetrievalDetailListQry qry) {
        return ragEvalRetrievalDetailListQryExe.execute(qry);
    }
    
    @Override
    public PageResponse<RagEvalGenerationDetailDTO> listGenerationDetails(RagEvalGenerationDetailListQry qry) {
        return ragEvalGenerationDetailListQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalExportResultDTO> exportResults(RagEvalExportCmd cmd) {
        return ragEvalExportCmdExe.execute(cmd);
    }
    
    // ====== 比较分析 ======
    
    @Override
    public SingleResponse<RagEvalCompareResultDTO> compareRuns(RagEvalCompareQry qry) {
        return ragEvalCompareQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalAbTestResultDTO> analyzeAbTest(RagEvalAbTestQry qry) {
        return ragEvalAbTestQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalTrendAnalysisDTO> getTrendAnalysis(RagEvalTrendAnalysisQry qry) {
        return ragEvalTrendAnalysisQryExe.execute(qry);
    }
    
    // ====== 统计分析 ======
    
    @Override
    public SingleResponse<RagEvalStatisticsDTO> getStatistics(RagEvalStatisticsQry qry) {
        return ragEvalStatisticsQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalDatasetStatsDTO> getDatasetStats(RagEvalDatasetStatsQry qry) {
        return ragEvalDatasetStatsQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<RagEvalModelStatsDTO> getModelStats(RagEvalModelStatsQry qry) {
        return ragEvalModelStatsQryExe.execute(qry);
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