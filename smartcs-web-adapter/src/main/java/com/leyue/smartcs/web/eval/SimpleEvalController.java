package com.leyue.smartcs.web.eval;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.eval.SimpleEvalService;
import com.leyue.smartcs.dto.eval.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 简化评估控制器
 * 提供基础的RAG评估功能，专注于数据采集和CI/CD质量闸门
 */
@Slf4j
@RestController
@RequestMapping("/admin/eval")
@RequiredArgsConstructor
public class SimpleEvalController {
    
    private final SimpleEvalService simpleEvalService;
    
    /**
     * 获取评估服务状态
     */
    @GetMapping("/status")
    public SingleResponse<RagasServiceStatusDTO> getServiceStatus() {
        log.info("查询评估服务状态");
        return simpleEvalService.getServiceStatus();
    }
    
    /**
     * 运行基准集评估 (CI/CD质量闸门)
     */
    @PostMapping("/baseline")
    public SingleResponse<SimpleEvalResponse> runBaselineEvaluation(@Valid @RequestBody SimpleEvalRequest request) {
        log.info("运行基准集评估: itemCount={}", request.getItems() != null ? request.getItems().size() : 0);
        return simpleEvalService.runBaselineEvaluation(request);
    }
    
    /**
     * 测试评估服务连接
     */
    @PostMapping("/test-connection")
    public SingleResponse<RagasConnectionTestResultDTO> testConnection(@Valid @RequestBody RagasConnectionTestCmd cmd) {
        log.info("测试评估服务连接");
        return simpleEvalService.testConnection(cmd);
    }
}