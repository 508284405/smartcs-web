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

    void insertBatch(@Param("chunks") List<ChunkDO> chunks);
} 