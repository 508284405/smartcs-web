package com.leyue.smartcs.knowledge.executor.command;

import static com.leyue.smartcs.domain.common.Constants.*;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Faq;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.knowledge.FaqAddCmd;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.knowledge.convertor.FaqConvertor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * FAQ创建/更新命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FaqAddCmdExe {
    
    private final FaqGateway faqGateway;
    private final SearchGateway searchGateway;
    private final FaqConvertor faqConvertor;
    

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
            return SingleResponse.of(faqConvertor.toDTO(savedFaq));
        }
        
        // 转换并返回结果
        FaqDTO faqDTO = faqConvertor.toDTO(savedFaq);
        return SingleResponse.of(faqDTO);
    }
}