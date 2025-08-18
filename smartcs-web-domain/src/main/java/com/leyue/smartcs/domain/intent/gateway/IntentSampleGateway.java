package com.leyue.smartcs.domain.intent.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.intent.entity.IntentSample;
import com.leyue.smartcs.domain.intent.enums.SampleType;

import java.util.List;

/**
 * 意图样本Gateway接口
 * 
 * @author Claude
 */
public interface IntentSampleGateway {
    
    /**
     * 保存意图样本
     * @param sample 意图样本对象
     * @return 保存后的意图样本对象
     */
    IntentSample save(IntentSample sample);
    
    /**
     * 批量保存意图样本
     * @param samples 意图样本列表
     */
    void saveBatch(List<IntentSample> samples);
    
    /**
     * 更新意图样本
     * @param sample 意图样本对象
     */
    void update(IntentSample sample);
    
    /**
     * 根据ID查找意图样本
     * @param id 样本ID
     * @return 意图样本对象
     */
    IntentSample findById(Long id);
    
    /**
     * 根据版本ID查找样本列表
     * @param versionId 版本ID
     * @return 样本列表
     */
    List<IntentSample> findByVersionId(Long versionId);
    
    /**
     * 根据版本ID和样本类型查找样本列表
     * @param versionId 版本ID
     * @param type 样本类型
     * @return 样本列表
     */
    List<IntentSample> findByVersionIdAndType(Long versionId, SampleType type);
    
    /**
     * 分页查询意图样本
     * @param versionId 版本ID
     * @param type 样本类型
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<IntentSample> findByPage(Long versionId, SampleType type, String keyword, int pageNum, int pageSize);
    
    /**
     * 统计版本样本数量
     * @param versionId 版本ID
     * @return 样本数量
     */
    int countByVersionId(Long versionId);
    
    /**
     * 统计版本指定类型样本数量
     * @param versionId 版本ID
     * @param type 样本类型
     * @return 样本数量
     */
    int countByVersionIdAndType(Long versionId, SampleType type);
    
    /**
     * 删除意图样本
     * @param id 样本ID
     */
    void deleteById(Long id);
    
    /**
     * 根据版本ID删除所有样本
     * @param versionId 版本ID
     */
    void deleteByVersionId(Long versionId);
}