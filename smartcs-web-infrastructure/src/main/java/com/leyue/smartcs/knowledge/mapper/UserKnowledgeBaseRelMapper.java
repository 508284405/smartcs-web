package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.knowledge.dataobject.UserKnowledgeBaseRelDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户知识库权限关系Mapper接口
 */
@Mapper
public interface UserKnowledgeBaseRelMapper extends BaseMapper<UserKnowledgeBaseRelDO> {
} 