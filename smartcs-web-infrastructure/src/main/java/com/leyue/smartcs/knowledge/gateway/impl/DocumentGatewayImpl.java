package com.leyue.smartcs.knowledge.gateway.impl;

import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.Document;
import com.leyue.smartcs.knowledge.convertor.DocConvertor;
import com.leyue.smartcs.knowledge.dataobject.DocDO;
import com.leyue.smartcs.knowledge.mapper.DocMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 文档网关实现类
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentGatewayImpl implements DocumentGateway {
    
    private final DocMapper docMapper;
    private final DocConvertor docConvertor;
    
    @Override
    public Document save(Document document) {
        DocDO docDO = docConvertor.toDataObject(document);
        
        if (docDO.getId() == null) {
            // 新增
            docMapper.insert(docDO);
        } else {
            // 更新
            docMapper.updateById(docDO);
        }
        
        return docConvertor.toDomain(docDO);
    }
    
    @Override
    public Optional<Document> findById(Long id) {
        DocDO docDO = docMapper.selectById(id);
        return Optional.ofNullable(docDO).map(docConvertor::toDomain);
    }
    
    @Override
    public boolean deleteById(Long id) {
        int rows = docMapper.deleteById(id);
        return rows > 0;
    }
    
    @Override
    public List<Document> listByPage(String keyword, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<DocDO> docDOs = docMapper.listByPage(keyword, offset, pageSize);
        return docConvertor.toDomainList(docDOs);
    }
    
    @Override
    public long count(String keyword) {
        return docMapper.count(keyword);
    }
} 