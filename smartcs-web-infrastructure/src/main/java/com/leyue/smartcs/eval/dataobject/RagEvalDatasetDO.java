package com.leyue.smartcs.eval.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * RAG评估数据集数据对象
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_rag_eval_dataset", autoResultMap = true)
public class RagEvalDatasetDO extends BaseDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 数据集唯一标识符
     */
    private String datasetId;
    
    /**
     * 数据集名称
     */
    private String name;
    
    /**
     * 数据集描述
     */
    private String description;
    
    /**
     * 领域类型
     */
    private String domain;
    
    /**
     * 语言类型
     */
    private String language;
    
    /**
     * 总测试用例数
     */
    private Integer totalCases;
    
    /**
     * 活跃测试用例数
     */
    private Integer activeCases;
    
    /**
     * 创建者用户ID
     */
    private Long creatorId;
    
    /**
     * 创建者姓名
     */
    private String creatorName;
    
    /**
     * 标签信息
     */
    private List<String> tags;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}