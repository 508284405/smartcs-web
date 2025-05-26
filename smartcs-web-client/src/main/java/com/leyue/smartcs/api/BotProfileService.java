package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.bot.BotProfileCreateCmd;
import com.leyue.smartcs.dto.bot.BotProfileUpdateCmd;
import com.leyue.smartcs.dto.bot.BotProfileDeleteCmd;
import com.leyue.smartcs.dto.bot.BotProfilePageQry;
import com.leyue.smartcs.dto.bot.BotProfileGetQry;
import com.leyue.smartcs.dto.bot.BotProfileDTO;

/**
 * 机器人配置管理服务接口
 */
public interface BotProfileService {
    
    /**
     * 创建机器人配置
     * @param cmd 创建命令
     * @return 机器人配置DTO
     */
    SingleResponse<BotProfileDTO> createBotProfile(BotProfileCreateCmd cmd);
    
    /**
     * 更新机器人配置
     * @param cmd 更新命令
     * @return 机器人配置DTO
     */
    SingleResponse<BotProfileDTO> updateBotProfile(BotProfileUpdateCmd cmd);
    
    /**
     * 删除机器人配置
     * @param cmd 删除命令
     * @return 删除结果
     */
    SingleResponse<Boolean> deleteBotProfile(BotProfileDeleteCmd cmd);
    
    /**
     * 获取机器人配置详情
     * @param qry 查询参数
     * @return 机器人配置DTO
     */
    SingleResponse<BotProfileDTO> getBotProfile(BotProfileGetQry qry);
    
    /**
     * 分页查询机器人配置列表
     * @param qry 查询参数
     * @return 分页结果
     */
    PageResponse<BotProfileDTO> pageBotProfiles(BotProfilePageQry qry);
} 