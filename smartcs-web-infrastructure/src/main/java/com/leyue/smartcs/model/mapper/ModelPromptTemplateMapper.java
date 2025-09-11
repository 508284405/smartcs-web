package com.leyue.smartcs.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.model.dataobject.ModelPromptTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型Prompt模板Mapper接口
 */
@Mapper
public interface ModelPromptTemplateMapper extends BaseMapper<ModelPromptTemplateDO> {
    
    /**
     * 根据模板标识查询（不包含已删除的）
     * @param templateKey 模板标识
     * @return 模板信息
     */
    ModelPromptTemplateDO selectByTemplateKey(@Param("templateKey") String templateKey);
    
    /**
     * 检查模板标识是否已存在（不包含已删除的）
     * @param templateKey 模板标识
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    int countByTemplateKey(@Param("templateKey") String templateKey, @Param("excludeId") Long excludeId);
    
    /**
     * 根据模型类型查询活跃的模板
     * @param modelType 模型类型
     * @return 模板列表
     */
    List<ModelPromptTemplateDO> selectActiveByModelType(@Param("modelType") String modelType);
    
    /**
     * 查询系统内置模板
     * @return 系统模板列表
     */
    List<ModelPromptTemplateDO> selectSystemTemplates();
    
    /**
     * 根据状态查询模板
     * @param status 状态
     * @return 模板列表
     */
    List<ModelPromptTemplateDO> selectByStatus(@Param("status") String status);
    
    /**
     * 更新模板状态
     * @param id 模板ID
     * @param status 状态
     * @return 更新条数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}