package com.leyue.smartcs.web.admin.eval;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.eval.RagEvalService;
import com.leyue.smartcs.api.eval.SimpleEvalService;
import com.leyue.smartcs.dto.eval.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端RAG评估控制器
 * 提供RAG评估系统的管理功能，包括数据集管理、评估运行、结果分析等
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/eval")
@RequiredArgsConstructor
@Validated
public class AdminRagEvalController {
    
    private final RagEvalService ragEvalService;
    private final SimpleEvalService simpleEvalService;
    
    // ====== 数据集管理 ======
    
    /**
     * 创建评估数据集
     */
    @PostMapping("/datasets")
    public SingleResponse<RagEvalDatasetDTO> createDataset(@Valid @RequestBody RagEvalDatasetCreateCmd cmd) {
        log.info("管理端创建RAG评估数据集: {}", cmd.getName());
        return ragEvalService.createDataset(cmd);
    }
    
    /**
     * 更新评估数据集
     */
    @PutMapping("/datasets/{datasetId}")
    public SingleResponse<RagEvalDatasetDTO> updateDataset(@PathVariable String datasetId,
                                                          @Valid @RequestBody RagEvalDatasetUpdateCmd cmd) {
        log.info("管理端更新RAG评估数据集: {}", datasetId);
        // 设置数据集ID
        cmd.setDatasetId(datasetId);
        return ragEvalService.updateDataset(cmd);
    }
    
    /**
     * 查询数据集详情
     */
    @GetMapping("/datasets/{datasetId}")
    public SingleResponse<RagEvalDatasetDTO> getDataset(@PathVariable String datasetId) {
        log.info("管理端查询RAG评估数据集详情: {}", datasetId);
        RagEvalDatasetGetQry qry = new RagEvalDatasetGetQry();
        qry.setDatasetId(datasetId);
        return ragEvalService.getDataset(qry);
    }
    
    /**
     * 查询数据集列表
     */
    @GetMapping("/datasets")
    public PageResponse<RagEvalDatasetDTO> listDatasets(RagEvalDatasetListQry qry) {
        log.info("管理端查询RAG评估数据集列表，页码: {}, 页大小: {}", qry.getPageNum(), qry.getPageSize());
        return ragEvalService.listDatasets(qry);
    }
    
    /**
     * 删除评估数据集
     */
    @DeleteMapping("/datasets/{datasetId}")
    public Response deleteDataset(@PathVariable String datasetId) {
        log.info("管理端删除RAG评估数据集: {}", datasetId);
        RagEvalDatasetDeleteCmd cmd = new RagEvalDatasetDeleteCmd();
        cmd.setDatasetId(datasetId);
        return ragEvalService.deleteDataset(cmd);
    }
    
    // ====== 测试用例管理 ======
    
    /**
     * 创建测试用例
     */
    @PostMapping("/datasets/{datasetId}/cases")
    public SingleResponse<RagEvalCaseDTO> createCase(@PathVariable String datasetId,
                                                    @Valid @RequestBody RagEvalCaseCreateCmd cmd) {
        log.info("管理端创建RAG评估测试用例，数据集: {}", datasetId);
        cmd.setDatasetId(datasetId);
        return ragEvalService.createCase(cmd);
    }
    
    /**
     * 批量导入测试用例
     */
    @PostMapping("/datasets/{datasetId}/cases/batch-import")
    public SingleResponse<RagEvalCaseBatchImportResultDTO> batchImportCases(@PathVariable String datasetId,
                                                                          @Valid @RequestBody RagEvalCaseBatchImportCmd cmd) {
        log.info("管理端批量导入RAG评估测试用例，数据集: {}, 数量: {}", datasetId, cmd.getCases().size());
        cmd.setDatasetId(datasetId);
        return ragEvalService.batchImportCases(cmd);
    }
    
    /**
     * 更新测试用例
     */
    @PutMapping("/cases/{caseId}")
    public SingleResponse<RagEvalCaseDTO> updateCase(@PathVariable String caseId,
                                                    @Valid @RequestBody RagEvalCaseUpdateCmd cmd) {
        log.info("管理端更新RAG评估测试用例: {}", caseId);
        cmd.setCaseId(caseId);
        return ragEvalService.updateCase(cmd);
    }
    
    /**
     * 查询测试用例详情
     */
    @GetMapping("/cases/{caseId}")
    public SingleResponse<RagEvalCaseDTO> getCase(@PathVariable String caseId) {
        log.info("管理端查询RAG评估测试用例详情: {}", caseId);
        RagEvalCaseGetQry qry = new RagEvalCaseGetQry();
        qry.setCaseId(caseId);
        return ragEvalService.getCase(qry);
    }
    
    /**
     * 查询测试用例列表
     */
    @GetMapping("/datasets/{datasetId}/cases")
    public PageResponse<RagEvalCaseDTO> listCases(@PathVariable String datasetId,
                                                 RagEvalCaseListQry qry) {
        log.info("管理端查询RAG评估测试用例列表，数据集: {}", datasetId);
        qry.setDatasetId(datasetId);
        return ragEvalService.listCases(qry);
    }
    
    /**
     * 删除测试用例
     */
    @DeleteMapping("/cases/{caseId}")
    public Response deleteCase(@PathVariable String caseId) {
        log.info("管理端删除RAG评估测试用例: {}", caseId);
        RagEvalCaseDeleteCmd cmd = new RagEvalCaseDeleteCmd();
        cmd.setCaseId(caseId);
        return ragEvalService.deleteCase(cmd);
    }
    
    /**
     * 批量删除测试用例
     */
    @PostMapping("/cases/batch-delete")
    public Response batchDeleteCases(@Valid @RequestBody RagEvalCaseBatchDeleteCmd cmd) {
        log.info("管理端批量删除RAG评估测试用例，数量: {}", cmd.getCaseIds().size());
        return ragEvalService.batchDeleteCases(cmd);
    }
    
    /**
     * 重新运行评估
     */
    @PostMapping("/runs/{runId}/rerun")
    public SingleResponse<RagEvalRunDTO> rerunEvaluation(@PathVariable String runId,
                                                        @RequestBody(required = false) RagEvalRunRerunCmd cmd) {
        log.info("管理端重新运行RAG评估: {}", runId);
        if (cmd == null) {
            cmd = new RagEvalRunRerunCmd();
        }
        cmd.setOriginalRunId(runId);
        return ragEvalService.rerunEvaluation(cmd);
    }
    
    /**
     * 删除评估运行
     */
    @DeleteMapping("/runs/{runId}")
    public Response deleteRun(@PathVariable String runId) {
        log.info("管理端删除RAG评估运行: {}", runId);
        RagEvalRunDeleteCmd cmd = new RagEvalRunDeleteCmd();
        cmd.setRunId(runId);
        return ragEvalService.deleteRun(cmd);
    }
    
    // ====== 评估结果查询 ======
    
    /**
     * 查询评估指标汇总
     */
    @GetMapping("/runs/{runId}/metrics")
    public SingleResponse<RagEvalMetricsDTO> getRunMetrics(@PathVariable String runId) {
        log.info("管理端查询RAG评估指标汇总: {}", runId);
        RagEvalMetricsGetQry qry = new RagEvalMetricsGetQry();
        qry.setRunId(runId);
        return ragEvalService.getRunMetrics(qry);
    }
    
    /**
     * 查询检索详情列表
     */
    @GetMapping("/runs/{runId}/retrieval-details")
    public PageResponse<RagEvalRetrievalDetailDTO> listRetrievalDetails(@PathVariable String runId,
                                                                      RagEvalRetrievalDetailListQry qry) {
        log.info("管理端查询RAG检索详情列表: {}", runId);
        qry.setRunId(runId);
        return ragEvalService.listRetrievalDetails(qry);
    }
    
    /**
     * 查询生成详情列表
     */
    @GetMapping("/runs/{runId}/generation-details")
    public PageResponse<RagEvalGenerationDetailDTO> listGenerationDetails(@PathVariable String runId,
                                                                        RagEvalGenerationDetailListQry qry) {
        log.info("管理端查询RAG生成详情列表: {}", runId);
        qry.setRunId(runId);
        return ragEvalService.listGenerationDetails(qry);
    }
    
    /**
     * 导出评估结果
     */
    @PostMapping("/runs/{runId}/export")
    public SingleResponse<RagEvalExportResultDTO> exportResults(@PathVariable String runId,
                                                               @Valid @RequestBody RagEvalExportCmd cmd) {
        log.info("管理端导出RAG评估结果: {}", runId);
        cmd.setRunId(runId);
        return ragEvalService.exportResults(cmd);
    }
    
    // ====== 比较分析 ======
    
    /**
     * 比较多个评估运行
     */
    @PostMapping("/compare")
    public SingleResponse<RagEvalCompareResultDTO> compareRuns(@Valid @RequestBody RagEvalCompareQry qry) {
        log.info("管理端比较RAG评估运行，数量: {}", qry.getRunIds().size());
        return ragEvalService.compareRuns(qry);
    }
    
    /**
     * A/B测试分析
     */
    @PostMapping("/ab-test/analyze")
    public SingleResponse<RagEvalAbTestResultDTO> analyzeAbTest(@Valid @RequestBody RagEvalAbTestQry qry) {
        log.info("管理端分析A/B测试: {} vs {}", qry.getBaselineRunId(), qry.getExperimentRunId());
        return ragEvalService.analyzeAbTest(qry);
    }
    
    /**
     * 查询趋势分析
     */
    @GetMapping("/trends")
    public SingleResponse<RagEvalTrendAnalysisDTO> getTrendAnalysis(RagEvalTrendAnalysisQry qry) {
        log.info("管理端查询RAG评估趋势分析");
        return ragEvalService.getTrendAnalysis(qry);
    }
    
    // ====== 统计分析 ======
    
    /**
     * 查询评估统计概览
     */
    @GetMapping("/statistics")
    public SingleResponse<RagEvalStatisticsDTO> getStatistics(RagEvalStatisticsQry qry) {
        log.info("管理端查询RAG评估统计概览");
        return ragEvalService.getStatistics(qry);
    }
    
    /**
     * 查询数据集使用统计
     */
    @GetMapping("/datasets/{datasetId}/stats")
    public SingleResponse<RagEvalDatasetStatsDTO> getDatasetStats(@PathVariable String datasetId,
                                                                 RagEvalDatasetStatsQry qry) {
        log.info("管理端查询数据集使用统计: {}", datasetId);
        qry.setDatasetId(datasetId);
        return ragEvalService.getDatasetStats(qry);
    }
    
    /**
     * 查询模型性能统计
     */
    @GetMapping("/models/{modelId}/stats")
    public SingleResponse<RagEvalModelStatsDTO> getModelStats(@PathVariable Long modelId,
                                                             RagEvalModelStatsQry qry) {
        log.info("管理端查询模型性能统计: {}", modelId);
        qry.setModelId(modelId);
        return ragEvalService.getModelStats(qry);
    }
    
    // ====== 配置管理 ======
    
    /**
     * 查询评估配置
     */
    @GetMapping("/config")
    public SingleResponse<RagEvalConfigDTO> getEvalConfig() {
        log.info("管理端查询RAG评估配置");
        RagEvalConfigGetQry qry = new RagEvalConfigGetQry();
        return ragEvalService.getEvalConfig(qry);
    }
    
    /**
     * 更新评估配置
     */
    @PutMapping("/config")
    public Response updateEvalConfig(@Valid @RequestBody RagEvalConfigUpdateCmd cmd) {
        log.info("管理端更新RAG评估配置");
        return ragEvalService.updateEvalConfig(cmd);
    }
    
    /**
     * 查询RAGAS服务状态
     */
    @GetMapping("/ragas/status")
    public SingleResponse<RagasServiceStatusDTO> getRagasServiceStatus() {
        log.info("管理端查询RAGAS服务状态");
        
        try {
            // 使用新的简化评估服务获取健康状态
            SingleResponse<RagasServiceStatusDTO> response = simpleEvalService.getServiceStatus();
            if (response.isSuccess()) {
                return response;
            }
            
            log.warn("简化评估服务获取状态失败，回退到原有实现");
            return ragEvalService.getRagasServiceStatus();
            
        } catch (Exception e) {
            log.warn("使用简化评估服务获取服务状态失败，回退到原有实现: {}", e.getMessage());
            return ragEvalService.getRagasServiceStatus();
        }
    }
    
    /**
     * 测试RAGAS服务连接
     */
    @PostMapping("/ragas/test-connection")
    public SingleResponse<RagasConnectionTestResultDTO> testRagasConnection(@Valid @RequestBody RagasConnectionTestCmd cmd) {
        log.info("管理端测试RAGAS服务连接");
        
        try {
            // 使用新的简化评估服务进行连接测试
            SingleResponse<RagasConnectionTestResultDTO> response = simpleEvalService.testConnection(cmd);
            if (response.isSuccess()) {
                return response;
            }
            
            log.warn("简化评估服务连接测试失败，回退到原有实现");
            return ragEvalService.testRagasConnection(cmd);
            
        } catch (Exception e) {
            log.warn("使用简化评估服务测试连接失败，回退到原有实现: {}", e.getMessage());
            return ragEvalService.testRagasConnection(cmd);
        }
    }
    
    /**
     * 运行基准集评估（新增）
     * 用于CI/CD质量闸门
     */
    @PostMapping("/baseline/run")
    public SingleResponse<SimpleEvalResponse> runBaselineEvaluation(@Valid @RequestBody SimpleEvalRequest request) {
        log.info("管理端运行基准集评估: itemCount={}", request.getItems() != null ? request.getItems().size() : 0);
        
        return simpleEvalService.runBaselineEvaluation(request);
    }
}