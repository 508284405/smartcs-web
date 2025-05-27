package com.leyue.smartcs.domain.bot.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.enums.ModelTypeEnum;
import com.leyue.smartcs.domain.bot.enums.VendorTypeEnum;

import java.util.List;
import java.util.Optional;

/**
 * 机器人配置Gateway接口
 */
public interface BotProfileGateway {
    
    /**
     * 创建机器人配置
     * @param botProfile 机器人配置
     * @return 机器人ID
     */
    Long createBotProfile(BotProfile botProfile);
    
    /**
     * 更新机器人配置
     * @param botProfile 机器人配置
     * @return 是否更新成功
     */
    boolean updateBotProfile(BotProfile botProfile);
    
    /**
     * 根据ID查询机器人配置
     * @param botId 机器人ID
     * @return 机器人配置
     */
    Optional<BotProfile> findById(Long botId);
    
    /**
     * 根据ID删除机器人配置（逻辑删除）
     * @param botId 机器人ID
     * @return 是否删除成功
     */
    boolean deleteById(Long botId);
    
    /**
     * 分页查询机器人配置列表
     * @param pageIndex 页码
     * @param pageSize 页大小
     * @param botName 机器人名称（可选）
     * @param modelName 模型名称（可选）
     * @return 分页结果
     */
    PageResponse<BotProfile> pageQuery(int pageIndex, int pageSize, String botName, String modelName);
    
    /**
     * 查询所有有效的机器人配置
     * @return 机器人配置列表
     */
    List<BotProfile> findAllActive();
    
    /**
     * 根据机器人名称查询
     * @param botName 机器人名称
     * @return 机器人配置
     */
    Optional<BotProfile> findByBotName(String botName);
    
    /**
     * 检查机器人名称是否已存在
     * @param botName 机器人名称
     * @param excludeBotId 排除的机器人ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByBotName(String botName, Long excludeBotId);
    
    /**
     * 根据厂商和模型类型查询机器人配置
     * @param vendor 厂商类型
     * @param modelType 模型类型
     * @return 机器人配置列表
     */
    List<BotProfile> findByVendorAndModelType(VendorTypeEnum vendor, ModelTypeEnum modelType);
    
    /**
     * 启用机器人配置
     * @param botId 机器人ID
     * @return 是否成功
     */
    boolean enableById(Long botId);
    
    /**
     * 禁用机器人配置
     * @param botId 机器人ID
     * @return 是否成功
     */
    boolean disableById(Long botId);
} 