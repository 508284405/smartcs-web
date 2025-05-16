package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.model.Faq;
import com.leyue.smartcs.dto.knowledge.FaqAddCmd;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * FAQ创建/更新命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FaqAddCmdExe {
    
    private final FaqGateway faqGateway;
    
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
        
        // 保存并转换结果
        Faq savedFaq = faqGateway.save(faq);
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
} 