package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.intent.domainservice.ClassificationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 意图分类命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentClassifyCmdExe {
    
    private final ClassificationDomainService classificationDomainService;
    
    /**
     * 执行意图分类
     */
    public SingleResponse<Map<String, Object>> execute(String text, String channel, String tenant) {
        
        Map<String, Object> result = classificationDomainService.classifyUserInput(text, channel, tenant);
        
        log.info("意图分类完成，输入: {}, 结果: {}", text, result.get("intent_code"));
        
        return SingleResponse.of(result);
    }
    
    /**
     * 批量执行意图分类
     */
    public SingleResponse<Map<String, Map<String, Object>>> executeBatch(String[] texts, String channel, String tenant) {
        
        Map<String, Map<String, Object>> results = classificationDomainService.batchClassify(texts, channel, tenant);
        
        log.info("批量意图分类完成，处理数量: {}", texts.length);
        
        return SingleResponse.of(results);
    }
}