package com.leyue.smartcs.web.app;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.leyue.smartcs.api.AiAppService;
import com.leyue.smartcs.dto.app.AiAppChatCmd;
import com.leyue.smartcs.dto.app.AiAppDTO;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 公开AI应用控制器
 * 提供无需认证的应用预览和运行功能
 */
@Slf4j
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class PublicAiAppController {
    
    private final AiAppService aiAppService;
    
    /**
     * 获取公开应用信息
     * 仅返回已发布状态的应用信息
     */
    @GetMapping("/{id}")
    public SingleResponse<AiAppDTO> getPublicApp(@PathVariable Long id) {
        log.info("获取公开应用信息请求: {}", id);
        
        // 获取应用信息
        SingleResponse<AiAppDTO> response = aiAppService.getApp(id);
        if (!response.isSuccess() || response.getData() == null) {
            return response;
        }
        
        AiAppDTO app = response.getData();
        
        // 检查应用状态，只有PUBLISHED状态才允许公开访问
        if (!"PUBLISHED".equals(app.getStatus())) {
            throw new BizException("应用尚未发布，无法公开访问");
        }
        
        return response;
    }
    
    /**
     * 公开应用聊天接口（SSE流式响应）
     * 仅允许与已发布应用进行对话
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SentinelResource(value = "public-app:chat",
            blockHandler = "chatWithPublicAppBlockHandler",
            fallback = "chatWithPublicAppFallback")
    public SseEmitter chatWithPublicApp(@RequestBody @Valid AiAppChatCmd cmd) {
        log.info("公开应用聊天请求: appId={}, message length={}, sessionId={}", 
                cmd.getAppId(), 
                cmd.getMessage() != null ? cmd.getMessage().length() : 0,
                cmd.getSessionId());
        
        // 验证应用状态
        SingleResponse<AiAppDTO> appResponse = aiAppService.getApp(cmd.getAppId());
        if (!appResponse.isSuccess() || appResponse.getData() == null) {
            throw new BizException("应用不存在");
        }
        
        AiAppDTO app = appResponse.getData();
        if (!"PUBLISHED".equals(app.getStatus())) {
            throw new BizException("应用尚未发布，无法使用聊天功能");
        }
        
        // 调用现有的聊天服务
        return aiAppService.chatWithAppSSE(cmd);
    }

    public SseEmitter chatWithPublicAppFallback(AiAppChatCmd cmd, Throwable throwable) {
        log.warn("公开应用聊天降级: appId={}, error={}",
                cmd != null ? cmd.getAppId() : null, throwable.getMessage());
        long timeout = cmd != null && cmd.getTimeout() != null ? cmd.getTimeout() : 30000L;
        SseEmitter emitter = new SseEmitter(timeout);
        try {
            emitter.send(SseEmitter.event().name("error").data("服务繁忙，请稍后再试"));
        } catch (IOException e) {
            log.error("发送降级消息失败", e);
        } finally {
            emitter.complete();
        }
        return emitter;
    }

    public SseEmitter chatWithPublicAppBlockHandler(AiAppChatCmd cmd, BlockException ex) {
        log.warn("公开应用聊天触发限流: appId={}, rule={}",
                cmd != null ? cmd.getAppId() : null, ex.getRule());
        return chatWithPublicAppFallback(cmd, ex);
    }
}
