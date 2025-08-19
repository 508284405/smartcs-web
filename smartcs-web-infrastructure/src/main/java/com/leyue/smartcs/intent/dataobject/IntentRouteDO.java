package com.leyue.smartcs.intent.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Map;

/**
 * 意图路由数据对象，对应t_intent_route表
 * 
 * @author Claude
 */
@Data
@TableName("t_intent_route")
public class IntentRouteDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 版本ID
     */
    @TableField("version_id")
    private Long versionId;
    
    /**
     * 路由类型
     */
    @TableField("route_type")
    private String routeType;
    
    /**
     * 路由配置
     */
    @TableField(value = "route_conf", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> routeConf;
    
    /**
     * 创建者
     */
    @TableField("created_by")
    private String createdBy;
    
    /**
     * 更新者
     */
    @TableField("updated_by")
    private String updatedBy;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}