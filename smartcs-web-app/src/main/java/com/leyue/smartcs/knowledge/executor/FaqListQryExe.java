package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.model.Faq;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FAQ列表查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FaqListQryExe {
    
    private final FaqGateway faqGateway;
    
    /**
     * 执行FAQ列表查询
     * @param qry 查询条件
     * @return FAQ列表（分页）
     */
    public PageResponse<FaqDTO> execute(KnowledgeSearchQry qry) {
        log.info("执行FAQ列表查询: {}", qry);
        
        // 获取分页参数
        Integer pageNum = qry.getPageIndex();
        Integer pageSize = qry.getPageSize();
        String keyword = qry.getKeyword();
        
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 执行查询
        List<Faq> faqs = faqGateway.listByPage(keyword, pageNum, pageSize);
        long total = faqGateway.count(keyword);
        
        // 转换结果
        List<FaqDTO> faqDTOs = convertToDTOs(faqs);
        
        log.info("FAQ列表查询完成，共 {} 条记录", total);
        return PageResponse.of(faqDTOs, pageSize, pageNum, (int)total);
    }
    
    /**
     * 批量转换为DTO
     * @param faqs FAQ实体列表
     * @return FAQ DTO列表
     */
    private List<FaqDTO> convertToDTOs(List<Faq> faqs) {
        if (faqs == null || faqs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<FaqDTO> dtos = new ArrayList<>(faqs.size());
        for (Faq faq : faqs) {
            FaqDTO dto = new FaqDTO();
            dto.setId(faq.getId());
            dto.setQuestion(faq.getQuestion());
            dto.setAnswer(faq.getAnswer());
            dto.setHitCount(faq.getHitCount());
            dto.setVersion(faq.getVersion());
            dto.setCreatedAt(faq.getCreatedAt());
            dto.setUpdatedAt(faq.getUpdatedAt());
            dtos.add(dto);
        }
        
        return dtos;
    }
} 