package com.leyue.smartcs.web.app;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.AiAppService;
import com.leyue.smartcs.dto.app.AiAppDTO;
import com.leyue.smartcs.dto.app.AiAppCreateCmd;
import com.leyue.smartcs.dto.app.AiAppUpdateCmd;
import com.leyue.smartcs.dto.app.AiAppStatusUpdateCmd;
import com.leyue.smartcs.dto.app.AiAppListQry;
import com.leyue.smartcs.dto.app.AiAppPromptOptimizeCmd;
import com.leyue.smartcs.dto.app.AiAppPromptOptimizeResponse;
import com.leyue.smartcs.dto.app.AiAppFunctionConfigCmd;
import com.leyue.smartcs.dto.app.AiAppFunctionConfigResponse;
import com.leyue.smartcs.dto.app.AiAppChatCmd;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI应用管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/app")
@RequiredArgsConstructor
public class AdminAiAppController {
    
    private final AiAppService aiAppService;
    
    /**
     * 创建AI应用
     */
    @PostMapping
    public SingleResponse<AiAppDTO> createApp(@RequestBody @Valid AiAppCreateCmd cmd) {
        log.info("创建AI应用请求: {}", cmd.getName());
        return aiAppService.createApp(cmd);
    }
    
    /**
     * 更新AI应用
     */
    @PutMapping
    public Response updateApp(@RequestBody @Valid AiAppUpdateCmd cmd) {
        log.info("更新AI应用请求: {}", cmd.getId());
        return aiAppService.updateApp(cmd);
    }
    
    /**
     * 更新AI应用状态
     */
    @PutMapping("/status")
    public Response updateAppStatus(@RequestBody @Valid AiAppStatusUpdateCmd cmd) {
        log.info("更新AI应用状态请求: {}, 状态: {}", cmd.getId(), cmd.getStatus());
        return aiAppService.updateAppStatus(cmd);
    }
    
    /**
     * 查询AI应用详情
     */
    @GetMapping("/{id}")
    public SingleResponse<AiAppDTO> getApp(@PathVariable Long id) {
        log.info("查询AI应用详情请求: {}", id);
        return aiAppService.getApp(id);
    }
    
    /**
     * 删除AI应用
     */
    @DeleteMapping("/{id}")
    public Response deleteApp(@PathVariable Long id) {
        log.info("删除AI应用请求: {}", id);
        return aiAppService.deleteApp(id);
    }
    
    /**
     * 分页查询AI应用列表
     */
    @GetMapping
    public PageResponse<AiAppDTO> listApps(@Valid AiAppListQry qry) {
        log.info("分页查询AI应用列表请求: 页码={}, 每页={}", qry.getPageIndex(), qry.getPageSize());
        return aiAppService.listApps(qry);
    }
    
    /**
     * 优化Prompt
     */
    @PostMapping("/optimize-prompt")
    public SingleResponse<AiAppPromptOptimizeResponse> optimizePrompt(@RequestBody @Valid AiAppPromptOptimizeCmd cmd) {
        log.info("优化Prompt请求: appId={}, originalPrompt length={}", cmd.getAppId(), 
                cmd.getOriginalPrompt() != null ? cmd.getOriginalPrompt().length() : 0);
        return aiAppService.optimizePrompt(cmd);
    }
    
    /**
     * 更新功能配置
     */
    @PutMapping("/{id}/function-config")
    public Response updateFunctionConfig(@PathVariable Long id, @RequestBody @Valid AiAppFunctionConfigCmd cmd) {
        log.info("更新AI应用功能配置请求: appId={}", id);
        cmd.setAppId(id);
        return aiAppService.updateFunctionConfig(cmd);
    }
    
    /**
     * 获取功能配置
     */
    @GetMapping("/{id}/function-config")
    public SingleResponse<AiAppFunctionConfigResponse> getFunctionConfig(@PathVariable Long id) {
        log.info("获取AI应用功能配置请求: appId={}", id);
        return aiAppService.getFunctionConfig(id);
    }
    
    /**
     * AI应用聊天（SSE流式响应）
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatWithApp(@RequestBody @Valid AiAppChatCmd cmd) {
        log.info("AI应用聊天请求: appId={}, message length={}, sessionId={}", 
                cmd.getAppId(), 
                cmd.getMessage() != null ? cmd.getMessage().length() : 0,
                cmd.getSessionId());
        return aiAppService.chatWithAppSSE(cmd);
    }
}