package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.model.Faq;
import com.leyue.smartcs.knowledge.convertor.FaqConvertor;
import com.leyue.smartcs.knowledge.dataobject.FaqDO;
import com.leyue.smartcs.knowledge.mapper.FaqMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * FAQ网关实现类
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FaqGatewayImpl implements FaqGateway {
    
    private final FaqMapper faqMapper;
    private final FaqConvertor faqConvertor;
    
    @Override
    public Faq save(Faq faq) {
        FaqDO faqDO = faqConvertor.toDataObject(faq);
        
        if (faqDO.getId() == null) {
            // 新增
            faqMapper.insert(faqDO);
        } else {
            // 更新
            faqMapper.updateById(faqDO);
        }
        
        return faqConvertor.toDomain(faqDO);
    }
    
    @Override
    public Optional<Faq> findById(Long id) {
        FaqDO faqDO = faqMapper.selectById(id);
        return Optional.ofNullable(faqDO).map(faqConvertor::toDomain);
    }
    
    @Override
    public boolean deleteById(Long id) {
        int rows = faqMapper.deleteById(id);
        return rows > 0;
    }
    
    @Override
    public List<Faq> findByQuestionLike(String question) {
        List<FaqDO> faqDOs = faqMapper.findByQuestionLike(question);
        return faqConvertor.toDomainList(faqDOs);
    }
    
    @Override
    public List<Faq> listByPage(String keyword, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<FaqDO> faqDOs = faqMapper.listByPage(keyword, offset, pageSize);
        return faqConvertor.toDomainList(faqDOs);
    }
    
    @Override
    public long count(String keyword) {
        return faqMapper.count(keyword);
    }
    
    @Override
    public long incrementHitCount(Long id) {
        // 当前时间戳
        long now = System.currentTimeMillis();
        
        faqMapper.incrementHitCount(id);
        
        // 查询最新的命中次数
        Optional<Faq> faqOpt = findById(id);
        return faqOpt.map(Faq::getHitCount).orElse(0L);
    }
} 