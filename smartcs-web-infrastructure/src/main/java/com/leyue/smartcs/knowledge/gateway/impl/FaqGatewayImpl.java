package com.leyue.smartcs.knowledge.gateway.impl;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.model.Faq;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;
import com.leyue.smartcs.knowledge.convertor.FaqConvertor;
import com.leyue.smartcs.knowledge.dataobject.FaqDO;
import com.leyue.smartcs.dto.knowledge.FaqScoreVO;
import com.leyue.smartcs.knowledge.mapper.FaqMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

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
    public PageResponse<FaqDTO> listByPage(KnowledgeSearchQry qry) {
        Page<FaqDO> page = faqMapper.selectPage(new Page<>(qry.getPageIndex(), qry.getPageSize()), Wrappers.<FaqDO>lambdaQuery()
                .like(FaqDO::getQuestion, qry.getKeyword())
        );

        List<FaqDTO> vos = faqConvertor.toDTO(page.getRecords());
        return PageResponse.of(vos, (int) page.getSize(), (int) page.getCurrent(), (int) page.getTotal());
    }

    @Override
    public long count(String keyword) {
        return faqMapper.count(keyword);
    }

    @Override
    public long incrementHitCount(Long id) {
        // 当前时间戳
        long now = System.currentTimeMillis();

        faqMapper.incrementHitCount(id, now);

        // 查询最新的命中次数
        Optional<Faq> faqOpt = findById(id);
        return faqOpt.map(Faq::getHitCount).orElse(0L);
    }

    @Override
    public Map<Long, Float> searchByQuestionFullText(String keyword, int k) {
        log.info("全文检索FAQ: keyword={}, k={}", keyword, k);
        List<FaqScoreVO> results = faqMapper.searchByQuestionFullText(keyword, k);
        
        Map<Long, Float> resultMap = new HashMap<>();
        if (results != null) {
            for (FaqScoreVO result : results) {
                resultMap.put(result.getId(), result.getScore());
            }
        }
        return resultMap;
    }
} 