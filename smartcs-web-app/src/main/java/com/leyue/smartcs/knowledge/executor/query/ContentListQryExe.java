package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.dto.knowledge.ContentListQry;
import com.leyue.smartcs.knowledge.convertor.ContentConvertor;
import com.leyue.smartcs.knowledge.dataobject.ContentDO;
import com.leyue.smartcs.knowledge.mapper.ContentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 内容列表查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentListQryExe {

    private final ContentMapper contentMapper;
    private final ContentConvertor contentConvertor;

    /**
     * 执行内容列表查询
     * @param qry 查询条件
     * @return 内容列表
     */
    public PageResponse<ContentDTO> execute(ContentListQry qry) {
        log.info("执行内容列表查询: {}", qry);
        
        // 构建查询条件
        LambdaQueryWrapper<ContentDO> queryWrapper = new LambdaQueryWrapper<>();
        
        // 知识库ID过滤
        if (qry.getKnowledgeBaseId() != null) {
            queryWrapper.eq(ContentDO::getKnowledgeBaseId, qry.getKnowledgeBaseId());
        }
        
        // 标题模糊查询
        if (StringUtils.hasText(qry.getTitle())) {
            queryWrapper.like(ContentDO::getTitle, qry.getTitle());
        }
        
        // 内容类型过滤
        if (StringUtils.hasText(qry.getContentType())) {
            queryWrapper.eq(ContentDO::getContentType, qry.getContentType());
        }
        
        // 状态过滤
        if (StringUtils.hasText(qry.getStatus())) {
            queryWrapper.eq(ContentDO::getStatus, qry.getStatus());
        }
        
        // 分段模式过滤
        if (StringUtils.hasText(qry.getSegmentMode())) {
            queryWrapper.eq(ContentDO::getSegmentMode, qry.getSegmentMode());
        }
        
        // 按更新时间倒序排列
        queryWrapper.orderByDesc(ContentDO::getUpdatedAt);
        
        // 执行分页查询
        Page<ContentDO> page = new Page<>(qry.getPageIndex(), qry.getPageSize());
        IPage<ContentDO> result = contentMapper.selectPage(page, queryWrapper);
        
        // 转换为DTO
        List<ContentDTO> contentDTOList = contentConvertor.toDTO(result.getRecords());
        
        log.info("内容列表查询完成，共 {} 条记录", result.getTotal());
        return PageResponse.of(
                contentDTOList,
                (int) result.getTotal(),
                qry.getPageSize(),
                qry.getPageIndex()
        );
    }
}