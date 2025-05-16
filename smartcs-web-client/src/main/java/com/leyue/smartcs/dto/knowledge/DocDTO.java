package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 文档数据传输对象
 */
@Data
public class DocDTO {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * OSS存储地址
     */
    private String ossUrl;
    
    /**
     * 文档类型
     */
    private String fileType;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
} 