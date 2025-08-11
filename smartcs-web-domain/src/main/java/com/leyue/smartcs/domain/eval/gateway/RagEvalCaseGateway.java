package com.leyue.smartcs.domain.eval.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.eval.RagEvalCaseCreateCmd;
import com.leyue.smartcs.dto.eval.RagEvalCaseDTO;
import com.leyue.smartcs.dto.eval.RagEvalCaseListQry;
import com.leyue.smartcs.dto.eval.RagEvalCaseUpdateCmd;

/**
 * RAG评估测试用例Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalCaseGateway {
    
    /**
     * 创建测试用例
     * 
     * @param cmd 创建命令
     * @return 创建的测试用例
     */
    RagEvalCaseDTO createCase(RagEvalCaseCreateCmd cmd);
    
    /**
     * 更新测试用例
     * 
     * @param cmd 更新命令
     * @return 更新后的测试用例
     */
    RagEvalCaseDTO updateCase(RagEvalCaseUpdateCmd cmd);
    
    /**
     * 根据ID查询测试用例
     * 
     * @param caseId 测试用例ID
     * @return 测试用例，如果不存在返回null
     */
    RagEvalCaseDTO getCase(String caseId);
    
    /**
     * 分页查询测试用例列表
     * 
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResponse<RagEvalCaseDTO> listCases(RagEvalCaseListQry qry);
    
    /**
     * 删除测试用例
     * 
     * @param caseId 测试用例ID
     */
    void deleteCase(String caseId);
    
    /**
     * 批量删除测试用例
     * 
     * @param caseIds 测试用例ID列表
     */
    void batchDeleteCases(java.util.List<String> caseIds);
    
    /**
     * 批量导入测试用例
     * 
     * @param cmd 批量导入命令
     * @return 导入结果
     */
    com.leyue.smartcs.dto.eval.RagEvalCaseBatchImportResultDTO batchImportCases(com.leyue.smartcs.dto.eval.RagEvalCaseBatchImportCmd cmd);
}
