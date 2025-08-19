package com.leyue.smartcs.model.service;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.dto.errorcode.ModelErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 默认模型服务 - 统一管理默认嵌入模型查找逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultModelService {

    private final ModelGateway modelGateway;

    /**
     * 获取默认的嵌入模型ID
     * 使用Spring Cache缓存结果，提高性能
     * 
     * @return 默认嵌入模型ID
     * @throws BizException 当未找到可用的嵌入模型时抛出
     */
    @Cacheable(value = "defaultEmbeddingModel", key = "'default'")
    public Long getDefaultEmbeddingModelId() {
        log.debug("开始查找默认嵌入模型");
        
        try {
            // 查找第一个可用的嵌入模型
            Long defaultModelId = modelGateway.findAll().stream()
                    .filter(model -> model.getModelType().contains(ModelType.TEXT_EMBEDDING))
                    .filter(model -> model.isActive())
                    .map(model -> model.getId())
                    .findFirst()
                    .orElseThrow(() -> new BizException(ModelErrorCode.NO_EMBEDDING_MODEL.getErrCode(), 
                            ModelErrorCode.NO_EMBEDDING_MODEL.getErrDesc()));
            
            log.info("找到默认嵌入模型，ID: {}", defaultModelId);
            return defaultModelId;
            
        } catch (BizException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("获取默认嵌入模型失败: {}", e.getMessage(), e);
            throw new BizException(ModelErrorCode.GET_DEFAULT_EMBEDDING_MODEL_FAILED.getErrCode(), 
                    ModelErrorCode.GET_DEFAULT_EMBEDDING_MODEL_FAILED.getErrDesc() + ": " + e.getMessage());
        }
    }

    /**
     * 检查是否存在可用的嵌入模型
     * 
     * @return true如果存在可用的嵌入模型，否则false
     */
    @Cacheable(value = "embeddingModelExists", key = "'exists'")
    public boolean hasAvailableEmbeddingModel() {
        log.debug("检查是否存在可用的嵌入模型");
        
        try {
            boolean exists = modelGateway.findAll().stream()
                    .anyMatch(model -> model.getModelType().contains(ModelType.TEXT_EMBEDDING) 
                            && model.isActive());
            
            log.debug("嵌入模型可用性检查结果: {}", exists);
            return exists;
            
        } catch (Exception e) {
            log.error("检查嵌入模型可用性失败: {}", e.getMessage(), e);
            return false;
        }
    }
}