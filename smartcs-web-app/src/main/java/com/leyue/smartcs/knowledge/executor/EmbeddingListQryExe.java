package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.dto.knowledge.EmbeddingDTO;
import com.leyue.smartcs.dto.knowledge.EmbeddingListQry;
import com.leyue.smartcs.knowledge.dataobject.EmbeddingDO;
import com.leyue.smartcs.knowledge.mapper.EmbeddingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量数据分页查询执行器
 */
@Component
@RequiredArgsConstructor
public class EmbeddingListQryExe {

    private final EmbeddingMapper embeddingMapper;

    /**
     * 执行向量数据分页查询
     *
     * @param qry 查询条件
     * @return 分页响应
     */
    public PageResponse<EmbeddingDTO> execute(EmbeddingListQry qry) {
        // 创建分页对象
        Page<EmbeddingDO> page = new Page<>(qry.getPageIndex(), qry.getPageSize());
        
        // 执行分页查询
        IPage<EmbeddingDO> result = embeddingMapper.listByDocIdAndStrategyName(page, qry);
        
        // 转换DO到DTO
        List<EmbeddingDTO> embeddingDTOList = result.getRecords()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return PageResponse.of(
                embeddingDTOList,
                (int) result.getTotal(),
                qry.getPageSize(),
                qry.getPageIndex()
        );
    }

    /**
     * 将EmbeddingDO转换为EmbeddingDTO
     */
    private EmbeddingDTO convertToDTO(EmbeddingDO embeddingDO) {
        EmbeddingDTO dto = new EmbeddingDTO();
        dto.setId(embeddingDO.getId());
        dto.setDocId(embeddingDO.getDocId());
        dto.setSectionIdx(embeddingDO.getSectionIdx());
        dto.setContentSnip(embeddingDO.getContentSnip());
        dto.setCreatedAt(embeddingDO.getCreatedAt());
        dto.setUpdatedAt(embeddingDO.getUpdatedAt());
        return dto;
    }
} 