package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DocDTO extends DTO {
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
     * 文件大小
     */
    private Long fileSize;
    
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