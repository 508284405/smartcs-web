package com.leyue.smartcs.api.eval;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.eval.*;

import java.util.List;

/**
 * RAG评估服务接口
 * 提供RAG评估系统的核心功能，包括数据集管理、评估执行、结果查询等
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalService {
    
    // ====== 数据集管理 ======
    
    /**
     * 创建评估数据集
     * 
     * @param cmd 创建数据集命令
     * @return 创建的数据集信息
     */
    SingleResponse<RagEvalDatasetDTO> createDataset(RagEvalDatasetCreateCmd cmd);
    
    /**
     * 更新评估数据集
     * 
     * @param cmd 更新数据集命令
     * @return 更新的数据集信息
     */
    SingleResponse<RagEvalDatasetDTO> updateDataset(RagEvalDatasetUpdateCmd cmd);
    
    /**
     * 查询数据集详情
     * 
     * @param qry 数据集查询参数
     * @return 数据集详情
     */
    SingleResponse<RagEvalDatasetDTO> getDataset(RagEvalDatasetGetQry qry);
    
    /**
     * 查询数据集列表
     * 
     * @param qry 数据集列表查询参数
     * @return 数据集列表
     */
    PageResponse<RagEvalDatasetDTO> listDatasets(RagEvalDatasetListQry qry);
    
    /**
     * 删除评估数据集
     * 
     * @param cmd 删除数据集命令
     * @return 删除结果
     */
    Response deleteDataset(RagEvalDatasetDeleteCmd cmd);
    
    // ====== 测试用例管理 ======
    
    /**
     * 创建测试用例
     * 
     * @param cmd 创建测试用例命令
     * @return 创建的测试用例信息
     */
    SingleResponse<RagEvalCaseDTO> createCase(RagEvalCaseCreateCmd cmd);
    
    /**
     * 批量导入测试用例
     * 
     * @param cmd 批量导入测试用例命令
     * @return 导入结果
     */
    SingleResponse<RagEvalCaseBatchImportResultDTO> batchImportCases(RagEvalCaseBatchImportCmd cmd);
    
    /**
     * 更新测试用例
     * 
     * @param cmd 更新测试用例命令
     * @return 更新的测试用例信息
     */
    SingleResponse<RagEvalCaseDTO> updateCase(RagEvalCaseUpdateCmd cmd);
    
    /**
     * 查询测试用例详情
     * 
     * @param qry 测试用例查询参数
     * @return 测试用例详情
     */
    SingleResponse<RagEvalCaseDTO> getCase(RagEvalCaseGetQry qry);
    
    /**
     * 查询测试用例列表
     * 
     * @param qry 测试用例列表查询参数
     * @return 测试用例列表
     */
    PageResponse<RagEvalCaseDTO> listCases(RagEvalCaseListQry qry);
    
    /**
     * 删除测试用例
     * 
     * @param cmd 删除测试用例命令
     * @return 删除结果
     */
    Response deleteCase(RagEvalCaseDeleteCmd cmd);
    
    /**
     * 批量删除测试用例
     * 
     * @param cmd 批量删除测试用例命令
     * @return 删除结果
     */
    Response batchDeleteCases(RagEvalCaseBatchDeleteCmd cmd);
    
    // ====== 评估运行管理 ======
    
    /**
     * 启动评估运行
     * 
     * @param cmd 启动评估命令
     * @return 创建的运行信息
     */
    SingleResponse<RagEvalRunDTO> startEvaluation(RagEvalRunStartCmd cmd);
    
    /**
     * 停止评估运行
     * 
     * @param cmd 停止评估命令
     * @return 停止结果
     */
    Response stopEvaluation(RagEvalRunStopCmd cmd);
    
    /**
     * 查询评估运行详情
     * 
     * @param qry 运行查询参数
     * @return 运行详情
     */
    SingleResponse<RagEvalRunDetailDTO> getRunDetail(RagEvalRunGetQry qry);
    
    /**
     * 查询评估运行列表
     * 
     * @param qry 运行列表查询参数
     * @return 运行列表
     */
    PageResponse<RagEvalRunDTO> listRuns(RagEvalRunListQry qry);
    
    /**
     * 查询运行状态
     * 
     * @param qry 运行状态查询参数
     * @return 运行状态
     */
    SingleResponse<RagEvalRunStatusDTO> getRunStatus(RagEvalRunStatusQry qry);
    
    /**
     * 重新运行评估
     * 
     * @param cmd 重新运行命令
     * @return 新的运行信息
     */
    SingleResponse<RagEvalRunDTO> rerunEvaluation(RagEvalRunRerunCmd cmd);
    
    /**
     * 删除评估运行
     * 
     * @param cmd 删除运行命令
     * @return 删除结果
     */
    Response deleteRun(RagEvalRunDeleteCmd cmd);
    
    // ====== 评估结果查询 ======
    
    /**
     * 查询评估指标汇总
     * 
     * @param qry 指标查询参数
     * @return 指标汇总数据
     */
    SingleResponse<RagEvalMetricsDTO> getRunMetrics(RagEvalMetricsGetQry qry);
    
    /**
     * 查询检索详情列表
     * 
     * @param qry 检索详情查询参数
     * @return 检索详情列表
     */
    PageResponse<RagEvalRetrievalDetailDTO> listRetrievalDetails(RagEvalRetrievalDetailListQry qry);
    
    /**
     * 查询生成详情列表
     * 
     * @param qry 生成详情查询参数
     * @return 生成详情列表
     */
    PageResponse<RagEvalGenerationDetailDTO> listGenerationDetails(RagEvalGenerationDetailListQry qry);
    
    /**
     * 导出评估结果
     * 
     * @param cmd 导出命令
     * @return 导出结果（下载链接等）
     */
    SingleResponse<RagEvalExportResultDTO> exportResults(RagEvalExportCmd cmd);
    
    // ====== 比较分析 ======
    
    /**
     * 比较多个评估运行
     * 
     * @param qry 比较查询参数
     * @return 比较结果
     */
    SingleResponse<RagEvalCompareResultDTO> compareRuns(RagEvalCompareQry qry);
    
    /**
     * A/B测试分析
     * 
     * @param qry A/B测试查询参数
     * @return A/B测试结果
     */
    SingleResponse<RagEvalAbTestResultDTO> analyzeAbTest(RagEvalAbTestQry qry);
    
    /**
     * 查询趋势分析
     * 
     * @param qry 趋势分析查询参数
     * @return 趋势分析结果
     */
    SingleResponse<RagEvalTrendAnalysisDTO> getTrendAnalysis(RagEvalTrendAnalysisQry qry);
    
    // ====== 统计分析 ======
    
    /**
     * 查询评估统计概览
     * 
     * @param qry 统计查询参数
     * @return 统计概览数据
     */
    SingleResponse<RagEvalStatisticsDTO> getStatistics(RagEvalStatisticsQry qry);
    
    /**
     * 查询数据集使用统计
     * 
     * @param qry 数据集统计查询参数
     * @return 数据集统计数据
     */
    SingleResponse<RagEvalDatasetStatsDTO> getDatasetStats(RagEvalDatasetStatsQry qry);
    
    /**
     * 查询模型性能统计
     * 
     * @param qry 模型统计查询参数
     * @return 模型统计数据
     */
    SingleResponse<RagEvalModelStatsDTO> getModelStats(RagEvalModelStatsQry qry);
    
    // ====== 配置管理 ======
    
    /**
     * 查询评估配置
     * 
     * @param qry 配置查询参数
     * @return 评估配置
     */
    SingleResponse<RagEvalConfigDTO> getEvalConfig(RagEvalConfigGetQry qry);
    
    /**
     * 更新评估配置
     * 
     * @param cmd 配置更新命令
     * @return 更新结果
     */
    Response updateEvalConfig(RagEvalConfigUpdateCmd cmd);
    
    /**
     * 查询RAGAS服务状态
     * 
     * @return RAGAS服务状态
     */
    SingleResponse<RagasServiceStatusDTO> getRagasServiceStatus();
    
    /**
     * 测试RAGAS服务连接
     * 
     * @param cmd 连接测试命令
     * @return 测试结果
     */
    SingleResponse<RagasConnectionTestResultDTO> testRagasConnection(RagasConnectionTestCmd cmd);
}