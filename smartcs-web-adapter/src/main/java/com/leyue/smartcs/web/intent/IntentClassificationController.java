package com.leyue.smartcs.web.intent;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.IntentClassificationService;
import com.leyue.smartcs.dto.intent.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 意图分类服务控制器
 * 提供意图分类的运行时服务，包括单个分类、批量分类、运行时配置等
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/intent")
@RequiredArgsConstructor
@Validated
public class IntentClassificationController {
    
    private final IntentClassificationService intentClassificationService;
    
    // ====== 意图分类 ======
    
    /**
     * 分类单个文本
     */
    @PostMapping("/classify")
    public SingleResponse<IntentClassifyResponseDTO> classify(@Valid @RequestBody IntentClassifyCmd cmd) {
        log.info("意图分类，渠道: {}, 租户: {}, 文本长度: {}", 
                cmd.getChannel(), cmd.getTenant(), cmd.getText() != null ? cmd.getText().length() : 0);
        return intentClassificationService.classify(cmd);
    }
    
    /**
     * 批量分类文本
     */
    @PostMapping("/classify/batch")
    public SingleResponse<IntentBatchClassifyResponseDTO> batchClassify(@Valid @RequestBody IntentBatchClassifyCmd cmd) {
        log.info("批量意图分类，渠道: {}, 租户: {}, 文本数量: {}", 
                cmd.getChannel(), cmd.getTenant(), cmd.getTexts() != null ? cmd.getTexts().length : 0);
        return intentClassificationService.batchClassify(cmd);
    }
    
    /**
     * 获取运行时配置
     */
    @GetMapping("/runtime-config")
    public SingleResponse<IntentRuntimeConfigDTO> getRuntimeConfig(IntentRuntimeConfigQry qry) {
        log.info("获取意图运行时配置，渠道: {}, 租户: {}", qry.getChannel(), qry.getTenant());
        return intentClassificationService.getRuntimeConfig(qry);
    }
    
    /**
     * 上报线上困难样本
     */
    @PostMapping("/report-hard-sample")
    public SingleResponse<Boolean> reportHardSample(@Valid @RequestBody IntentHardSampleReportCmd cmd) {
        log.info("上报线上困难样本，渠道: {}, 租户: {}, 文本长度: {}", 
                cmd.getChannel(), cmd.getTenant(), cmd.getText() != null ? cmd.getText().length() : 0);
        return intentClassificationService.reportHardSample(cmd);
    }
    
    // ====== 健康检查 ======
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public SingleResponse<String> health() {
        log.debug("意图分类服务健康检查");
        return SingleResponse.of("OK");
    }
    
    /**
     * 服务信息
     */
    @GetMapping("/info")
    public SingleResponse<IntentServiceInfoDTO> getServiceInfo() {
        log.debug("获取意图分类服务信息");
        IntentServiceInfoDTO info = new IntentServiceInfoDTO();
        info.setServiceName("Intent Classification Service");
        info.setVersion("1.0.0");
        info.setTimestamp(System.currentTimeMillis());
        return SingleResponse.of(info);
    }
}