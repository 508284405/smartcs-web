package com.leyue.smartcs.domain.intent.entity;

import com.leyue.smartcs.domain.intent.enums.RouteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 意图路由实体
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentRoute {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 版本ID
     */
    private Long versionId;
    
    /**
     * 路由类型
     */
    private RouteType routeType;
    
    /**
     * 路由配置
     */
    private Map<String, Object> routeConf;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}