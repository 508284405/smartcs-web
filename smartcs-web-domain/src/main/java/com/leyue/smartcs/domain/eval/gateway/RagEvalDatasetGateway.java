package com.leyue.smartcs.domain.eval.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.eval.RagEvalDatasetCreateCmd;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDTO;
import com.leyue.smartcs.dto.eval.RagEvalDatasetListQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetUpdateCmd;

/**
 * RAG评估数据集Gateway接口
 * 定义与Infrastructure层交互的抽象接口
 * 
 * @author Claude
 * @since 1.0.0
 */
public interface RagEvalDatasetGateway {
    
    /**
     * 创建数据集
     * 
     * @param cmd 创建命令
     * @return 创建的数据集
     */
    RagEvalDatasetDTO createDataset(RagEvalDatasetCreateCmd cmd);
    
    /**
     * 更新数据集
     * 
     * @param cmd 更新命令
     * @return 更新后的数据集
     */
    RagEvalDatasetDTO updateDataset(RagEvalDatasetUpdateCmd cmd);
    
    /**
     * 根据ID查询数据集
     * 
     * @param datasetId 数据集ID
     * @return 数据集，如果不存在返回null
     */
    RagEvalDatasetDTO getDataset(String datasetId);
    
    /**
     * 分页查询数据集列表
     * 
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResponse<RagEvalDatasetDTO> listDatasets(RagEvalDatasetListQry qry);
    
    /**
     * 删除数据集
     * 
     * @param datasetId 数据集ID
     */
    void deleteDataset(String datasetId);
}
