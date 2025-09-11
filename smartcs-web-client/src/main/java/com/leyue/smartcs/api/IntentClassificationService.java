package com.leyue.smartcs.api;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.intent.*;

/**
 * 意图分类服务接口
 * 
 * @author Claude
 */
public interface IntentClassificationService {
    
    /**
     * 分类单个文本
     * @param cmd 分类命令
     * @return 分类结果
     */
    SingleResponse<IntentClassifyResponseDTO> classify(IntentClassifyCmd cmd);
    
    /**
     * 批量分类文本
     * @param cmd 批量分类命令
     * @return 分类结果
     */
    SingleResponse<IntentBatchClassifyResponseDTO> batchClassify(IntentBatchClassifyCmd cmd);
    
    /**
     * 获取运行时配置
     * @param qry 查询命令
     * @return 配置信息
     */
    SingleResponse<IntentRuntimeConfigDTO> getRuntimeConfig(IntentRuntimeConfigQry qry);
    
    /**
     * 上报线上困难样本
     * @param cmd 上报命令
     * @return 上报结果
     */
    SingleResponse<Boolean> reportHardSample(IntentHardSampleReportCmd cmd);
}