package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.knowledge.dataobject.ChunkDO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 切片Mapper接口
 */
@Mapper
public interface ChunkMapper extends BaseMapper<ChunkDO> {

    /**
     * 批量插入切片
     * @param chunks 切片列表
     */
    void insertBatch(@Param("chunks") List<ChunkDO> chunks);

    /**
     * 批量更新切片向量ID
     * @param chunks 切片列表
     */
    void updateBatchVectorId(@Param("chunks") List<ChunkDO> chunks);
} 