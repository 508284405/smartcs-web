package com.leyue.smartcs.web.bot;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.bot.api.BotPromptTemplateService;
import com.leyue.smartcs.bot.dto.BotPromptTemplateCreateCmd;
import com.leyue.smartcs.bot.dto.BotPromptTemplateDTO;
import com.leyue.smartcs.bot.dto.BotPromptTemplatePageQry;
import com.leyue.smartcs.bot.dto.BotPromptTemplateUpdateCmd;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * Bot Prompt模板管理Controller
 */
@RestController
@RequestMapping("/api/admin/bot/prompt-template")
public class AdminBotPromptTemplateController {
    
    @Resource
    private BotPromptTemplateService botPromptTemplateService;
    
    /**
     * 分页查询Prompt模板
     */
    @GetMapping("/page")
    public SingleResponse<PageResponse<BotPromptTemplateDTO>> pageQuery(BotPromptTemplatePageQry qry) {
        return botPromptTemplateService.pageQuery(qry);
    }
    
    /**
     * 查询Prompt模板列表
     */
    @GetMapping("/list")
    public MultiResponse<BotPromptTemplateDTO> listTemplates(
            @RequestParam(required = false) String templateKey,
            @RequestParam(required = false) String context) {
        return botPromptTemplateService.listTemplates(templateKey, context);
    }
    
    /**
     * 新增Prompt模板
     */
    @PostMapping
    public SingleResponse<Long> create(@Valid @RequestBody BotPromptTemplateCreateCmd cmd) {
        return botPromptTemplateService.create(cmd);
    }
    
    /**
     * 更新Prompt模板
     */
    @PostMapping("/update")
    public Response update(@Valid @RequestBody BotPromptTemplateUpdateCmd cmd) {
        return botPromptTemplateService.update(cmd);
    }
    
    /**
     * 删除Prompt模板
     */
    @DeleteMapping("/{id}")
    public Response delete(@PathVariable Long id) {
        return botPromptTemplateService.delete(id);
    }
    
    /**
     * 根据ID查询模板详情
     */
    @GetMapping("/{id}")
    public SingleResponse<BotPromptTemplateDTO> getById(@PathVariable Long id) {
        return botPromptTemplateService.getById(id);
    }
} 