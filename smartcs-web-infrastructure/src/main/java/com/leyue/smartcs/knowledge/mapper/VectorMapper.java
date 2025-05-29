package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.knowledge.dataobject.VectorDO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 向量Mapper接口
 */
@Mapper
public interface VectorMapper extends BaseMapper<VectorDO> {

    void insertBatch(@Param("vectors") List<VectorDO> vectors);
} 