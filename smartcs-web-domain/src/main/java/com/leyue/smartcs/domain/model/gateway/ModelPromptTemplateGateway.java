package com.leyue.smartcs.domain.model.gateway;

import com.leyue.smartcs.domain.model.ModelPromptTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 模型Prompt模板网关接口
 */
public interface ModelPromptTemplateGateway {
    
    /**
     * 保存模板
     * 
     * @param template 模板对象
     * @return 保存后的模板
     */
    ModelPromptTemplate save(ModelPromptTemplate template);
    
    /**
     * 根据ID查找模板
     * 
     * @param id 模板ID
     * @return 模板对象（可选）
     */
    Optional<ModelPromptTemplate> findById(Long id);
    
    /**
     * 根据模板键查找模板
     * 
     * @param templateKey 模板键
     * @return 模板对象（可选）
     */
    Optional<ModelPromptTemplate> findByTemplateKey(String templateKey);
    
    /**
     * 检查模板键是否存在
     * 
     * @param templateKey 模板键
     * @return 是否存在
     */
    boolean existsByTemplateKey(String templateKey);
    
    /**
     * 根据模型类型查找模板列表
     * 
     * @param modelType 模型类型
     * @return 模板列表
     */
    List<ModelPromptTemplate> findByModelType(String modelType);
    
    /**
     * 获取系统内置模板列表
     * 
     * @return 系统模板列表
     */
    List<ModelPromptTemplate> findSystemTemplates();
    
    /**
     * 获取用户自定义模板列表
     * 
     * @param createdBy 创建人
     * @return 用户模板列表
     */
    List<ModelPromptTemplate> findUserTemplates(String createdBy);
    
    /**
     * 获取活跃模板列表
     * 
     * @return 活跃模板列表
     */
    List<ModelPromptTemplate> findActiveTemplates();
    
    /**
     * 分页查询模板
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 模板列表
     */
    List<ModelPromptTemplate> findWithPagination(int offset, int limit);
    
    /**
     * 统计模板总数
     * 
     * @return 模板总数
     */
    long countTemplates();
    
    /**
     * 更新模板
     * 
     * @param template 模板对象
     * @return 是否更新成功
     */
    boolean update(ModelPromptTemplate template);
    
    /**
     * 激活模板
     * 
     * @param id 模板ID
     * @return 是否激活成功
     */
    boolean activate(Long id);
    
    /**
     * 停用模板
     * 
     * @param id 模板ID
     * @return 是否停用成功
     */
    boolean deactivate(Long id);
    
    /**
     * 删除模板
     * 
     * @param id 模板ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
    
    /**
     * 根据模板键删除模板
     * 
     * @param templateKey 模板键
     * @return 是否删除成功
     */
    boolean deleteByTemplateKey(String templateKey);
    
    /**
     * 批量删除模板
     * 
     * @param ids 模板ID列表
     * @return 删除数量
     */
    int batchDelete(List<Long> ids);
    
    /**
     * 搜索模板
     * 
     * @param keyword 关键词
     * @param modelType 模型类型（可选）
     * @param isSystem 是否系统模板（可选）
     * @return 模板列表
     */
    List<ModelPromptTemplate> searchTemplates(String keyword, String modelType, Boolean isSystem);
}