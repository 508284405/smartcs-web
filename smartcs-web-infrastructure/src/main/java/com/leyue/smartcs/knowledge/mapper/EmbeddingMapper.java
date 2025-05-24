package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leyue.smartcs.dto.knowledge.EmbeddingListQry;
import com.leyue.smartcs.knowledge.dataobject.EmbeddingDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 向量 Mapper接口
 */
@Mapper
public interface EmbeddingMapper extends BaseMapper<EmbeddingDO> {
    
    /**
     * 根据文档ID查询所有向量
     * @param docId 文档ID
     * @return 向量列表
     */
    List<EmbeddingDO> findByDocId(@Param("docId") Long docId);
    
    /**
     * 根据文档ID和段落序号查询向量
     * @param docId 文档ID
     * @param sectionIdx 段落序号
     * @return 向量
     */
    EmbeddingDO findByDocIdAndSectionIdx(@Param("docId") Long docId, @Param("sectionIdx") Integer sectionIdx);
    
    /**
     * 根据文档ID删除所有向量
     * @param docId 文档ID
     * @return 删除行数
     */
    int deleteByDocId(@Param("docId") Long docId);
    
    /**
     * 分页查询向量数据
     * @param page 分页对象
     * @param qry 查询条件
     * @return 分页结果
     */
    IPage<EmbeddingDO> listByDocIdAndStrategyName(IPage<EmbeddingDO> page, @Param("qry") EmbeddingListQry qry);
} 