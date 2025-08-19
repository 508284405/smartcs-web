package com.leyue.smartcs.domain.intent.gateway;

import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.domain.intent.enums.VersionStatus;

import java.util.List;

/**
 * 意图版本Gateway接口
 * 
 * @author Claude
 */
public interface IntentVersionGateway {
    
    /**
     * 保存意图版本
     * @param version 意图版本对象
     * @return 保存后的意图版本对象
     */
    IntentVersion save(IntentVersion version);
    
    /**
     * 更新意图版本
     * @param version 意图版本对象
     */
    void update(IntentVersion version);
    
    /**
     * 根据ID查找意图版本
     * @param id 版本ID
     * @return 意图版本对象
     */
    IntentVersion findById(Long id);
    
    /**
     * 根据意图ID查找所有版本
     * @param intentId 意图ID
     * @return 版本列表
     */
    List<IntentVersion> findByIntentId(Long intentId);
    
    /**
     * 根据意图ID和版本号查找版本
     * @param intentId 意图ID
     * @param versionNumber 版本号
     * @return 意图版本对象
     */
    IntentVersion findByIntentIdAndVersionNumber(Long intentId, String versionNumber);
    
    /**
     * 根据意图ID查找当前激活版本
     * @param intentId 意图ID
     * @return 激活版本对象
     */
    IntentVersion findActiveVersionByIntentId(Long intentId);
    
    /**
     * 根据状态查找版本列表
     * @param status 版本状态
     * @return 版本列表
     */
    List<IntentVersion> findByStatus(VersionStatus status);
    
    /**
     * 根据意图ID和状态查找版本列表
     * @param intentId 意图ID
     * @param status 版本状态
     * @return 版本列表
     */
    List<IntentVersion> findByIntentIdAndStatus(Long intentId, VersionStatus status);
    
    /**
     * 获取意图的下一个版本号
     * @param intentId 意图ID
     * @return 下一个版本号
     */
    String getNextVersionNumber(Long intentId);
    
    /**
     * 删除意图版本
     * @param id 版本ID
     */
    void deleteById(Long id);
}