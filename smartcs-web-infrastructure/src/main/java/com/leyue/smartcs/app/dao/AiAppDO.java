package com.leyue.smartcs.app.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * AI应用数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_ai_app", autoResultMap = true)
public class AiAppDO extends BaseDO {
    
    /**
     * 应用名称
     */
    private String name;
    
    /**
     * 应用唯一编码
     */
    private String code;
    
    /**
     * 应用描述
     */
    private String description;
    
    /**
     * 应用类型
     */
    private String type;
    
    /**
     * 应用配置信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;
    
    /**
     * 应用状态
     */
    private String status;
    
    /**
     * 应用图标
     */
    private String icon;
    
    /**
     * 应用标签
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
}