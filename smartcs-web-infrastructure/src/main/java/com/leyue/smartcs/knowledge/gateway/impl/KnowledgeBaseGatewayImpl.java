package com.leyue.smartcs.knowledge.gateway.impl;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseGateway;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseListQry;
import com.leyue.smartcs.knowledge.convertor.KnowledgeBaseConvertor;
import com.leyue.smartcs.knowledge.dataobject.KnowledgeBaseDO;
import com.leyue.smartcs.knowledge.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 知识库Gateway实现
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseGatewayImpl implements KnowledgeBaseGateway {
    
    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final KnowledgeBaseConvertor knowledgeBaseConvertor;
    
    @Override
    public KnowledgeBase save(KnowledgeBase knowledgeBase) {
        KnowledgeBaseDO knowledgeBaseDO = knowledgeBaseConvertor.toDO(knowledgeBase);
        knowledgeBaseMapper.insert(knowledgeBaseDO);
        return knowledgeBaseConvertor.toDomain(knowledgeBaseDO);
    }
    
    @Override
    public void update(KnowledgeBase knowledgeBase) {
        knowledgeBaseMapper.updateById(knowledgeBaseConvertor.toDO(knowledgeBase));
    }
    
    @Override
    public KnowledgeBase findById(Long id) {
        return knowledgeBaseConvertor.toDomain(knowledgeBaseMapper.selectById(id));
    }
    
    @Override
    public void deleteById(Long id) {
        knowledgeBaseMapper.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return knowledgeBaseMapper.selectCount(new LambdaQueryWrapper<KnowledgeBaseDO>()
                .eq(KnowledgeBaseDO::getName, name)) > 0;
    }

    @Override
    public PageResponse<KnowledgeBase> listByPage(KnowledgeBaseListQry qry) {
        // 构建查询条件
        LambdaQueryWrapper<KnowledgeBaseDO> queryWrapper = new LambdaQueryWrapper<>();
        
        // 名称模糊查询
        if (StringUtils.hasText(qry.getName())) {
            queryWrapper.like(KnowledgeBaseDO::getName, qry.getName());
        }
        
        // 可见性过滤
        if (StringUtils.hasText(qry.getVisibility())) {
            queryWrapper.eq(KnowledgeBaseDO::getVisibility, qry.getVisibility());
        }
        
        // 创建者过滤
        if (qry.getOwnerId() != null) {
            queryWrapper.eq(KnowledgeBaseDO::getOwnerId, qry.getOwnerId());
        }
        
        // 按更新时间倒序排列
        queryWrapper.orderByDesc(KnowledgeBaseDO::getUpdatedAt);
        
        // 执行分页查询
        Page<KnowledgeBaseDO> page = new Page<>(qry.getPageIndex(), qry.getPageSize());
        Page<KnowledgeBaseDO> result = knowledgeBaseMapper.selectPage(page, queryWrapper);
        
        // 转换为领域对象
        List<KnowledgeBase> knowledgeBases = knowledgeBaseConvertor.toDomainList(result.getRecords());
        
        return PageResponse.of(
                knowledgeBases,
                (int) result.getTotal(),
                qry.getPageSize(),
                qry.getPageIndex()
        );
    }

    @Override
    public boolean existsByCode(String code) {
        return knowledgeBaseMapper.selectCount(new LambdaQueryWrapper<KnowledgeBaseDO>()
                .eq(KnowledgeBaseDO::getCode, code)) > 0;
    }
} 