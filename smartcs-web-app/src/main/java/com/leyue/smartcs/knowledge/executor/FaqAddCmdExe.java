package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.domain.knowledge.Faq;
import com.leyue.smartcs.dto.knowledge.FaqAddCmd;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.leyue.smartcs.domain.common.Constants.FAQ_INDEX_REDISEARCH;


/**
 * FAQ创建/更新命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FaqAddCmdExe {
    
    private final FaqGateway faqGateway;
    private final SearchGateway searchGateway;
    

    @PostConstruct
    public void init() {
        log.info("初始化FAQ搜索功能");
    }
    
    /**
     * 执行FAQ创建/更新命令
     * @param cmd FAQ创建/更新命令
     * @return 创建/更新后的FAQ
     */
    public SingleResponse<FaqDTO> execute(FaqAddCmd cmd) {
        log.info("执行FAQ创建/更新命令: {}", cmd);
        
        // 参数校验
        if (cmd.getQuestion() == null || cmd.getQuestion().trim().isEmpty()) {
            throw new BizException("FAQ问题不能为空");
        }
        
        if (cmd.getAnswer() == null || cmd.getAnswer().trim().isEmpty()) {
            throw new BizException("FAQ答案不能为空");
        }
        
        // 创建或加载实体
        Faq faq;
        if (cmd.getId() != null) {
            // 更新现有FAQ
            Optional<Faq> existingFaqOpt = faqGateway.findById(cmd.getId());
            if (existingFaqOpt.isEmpty()) {
                throw new BizException("FAQ不存在，ID: " + cmd.getId());
            }
            faq = existingFaqOpt.get();
            faq.setQuestion(cmd.getQuestion());
            faq.setAnswer(cmd.getAnswer());
            faq.setEnabled(cmd.getEnabled());
            log.info("更新FAQ, ID: {}", faq.getId());
        } else {
            // 创建新FAQ
            faq = Faq.builder()
                    .question(cmd.getQuestion())
                    .answer(cmd.getAnswer())
                    .hitCount(0L)
                    .version(1)
                    .enabled(cmd.getEnabled())
                    .build();
            log.info("创建新FAQ");
        }
        
        // 保存实体
        Faq savedFaq = faqGateway.save(faq);
        
        // 同步到搜索索引
        try {
            boolean indexed = searchGateway.indexDocument(FAQ_INDEX_REDISEARCH, savedFaq.getId(), savedFaq);
            if (!indexed) {
                log.warn("FAQ搜索索引同步失败: {}", savedFaq.getId());
                throw new BizException("FAQ搜索索引同步失败");
            }
        } catch (Exception e) {
            log.error("FAQ搜索索引同步异常: {}", savedFaq.getId(), e);
            return SingleResponse.of(convertToDTO(savedFaq));
        }
        
        // 转换并返回结果
        FaqDTO faqDTO = convertToDTO(savedFaq);
        return SingleResponse.of(faqDTO);
    }
    
    /**
     * 转换为DTO
     * @param faq FAQ实体
     * @return FAQ DTO
     */
    private FaqDTO convertToDTO(Faq faq) {
        if (faq == null) {
            return null;
        }
        FaqDTO dto = new FaqDTO();
        dto.setId(faq.getId());
        dto.setQuestion(faq.getQuestion());
        dto.setAnswer(faq.getAnswer());
        dto.setHitCount(faq.getHitCount());
        dto.setVersion(faq.getVersion());
        dto.setCreatedAt(faq.getCreatedAt());
        dto.setUpdatedAt(faq.getUpdatedAt());
        return dto;
    }

    /**
     * 转换为搜索文档
     * @param faq FAQ实体
     * @return 搜索文档
     */
    private Map<Object, Object> convertToSearchDocument(Faq faq) {
        Map<Object, Object> document = new HashMap<>();
        document.put("question", faq.getQuestion());
        document.put("answer", faq.getAnswer());
        document.put("enabled", faq.getEnabled());
        document.put("hitCount", faq.getHitCount());
        document.put("version", faq.getVersion());
        document.put("createdAt", faq.getCreatedAt());
        document.put("updatedAt", faq.getUpdatedAt());
        return document;
    }
} 