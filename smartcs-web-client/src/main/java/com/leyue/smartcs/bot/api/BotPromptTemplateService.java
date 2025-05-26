package com.leyue.smartcs.bot.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.bot.dto.BotPromptTemplateCreateCmd;
import com.leyue.smartcs.bot.dto.BotPromptTemplateDTO;
import com.leyue.smartcs.bot.dto.BotPromptTemplatePageQry;
import com.leyue.smartcs.bot.dto.BotPromptTemplateUpdateCmd;

/**
 * Bot Prompt模板管理服务接口
 */
public interface BotPromptTemplateService {

    /**
     * 分页查询Prompt模板
     * @param qry 查询条件
     * @return 分页结果
     */
    SingleResponse<PageResponse<BotPromptTemplateDTO>> pageQuery(BotPromptTemplatePageQry qry);

    /**
     * 新增Prompt模板
     * @param cmd 创建命令
     * @return 创建的模板ID
     */
    SingleResponse<Long> create(BotPromptTemplateCreateCmd cmd);

    /**
     * 更新Prompt模板
     * @param cmd 更新命令
     * @return 操作结果
     */
    Response update(BotPromptTemplateUpdateCmd cmd);

    /**
     * 删除Prompt模板
     * @param id 模板ID
     * @return 操作结果
     */
    Response delete(Long id);

    /**
     * 根据ID查询模板详情
     * @param id 模板ID
     * @return 模板详情
     */
    SingleResponse<BotPromptTemplateDTO> getById(Long id);
} 