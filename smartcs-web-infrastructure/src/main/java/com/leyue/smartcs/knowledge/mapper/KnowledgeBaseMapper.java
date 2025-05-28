package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.knowledge.dataobject.KnowledgeBaseDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库Mapper接口
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseDO> {
} 