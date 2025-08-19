package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.knowledge.dataobject.KnowledgeBaseSettingsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库设置Mapper接口
 */
@Mapper
public interface KnowledgeBaseSettingsMapper extends BaseMapper<KnowledgeBaseSettingsDO> {
    
    /**
     * 根据知识库ID查询设置
     * @param knowledgeBaseId 知识库ID
     * @return 知识库设置
     */
    KnowledgeBaseSettingsDO selectByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);
    
    /**
     * 根据知识库ID更新设置
     * @param settings 设置对象
     * @return 影响行数
     */
    int updateByKnowledgeBaseId(KnowledgeBaseSettingsDO settings);
}