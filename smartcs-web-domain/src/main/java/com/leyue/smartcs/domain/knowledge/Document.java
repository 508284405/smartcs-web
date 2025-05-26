package com.leyue.smartcs.domain.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
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
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件大小（字节）
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
    
    /**
     * 检查文档是否可处理
     * @return 是否可处理
     */
    public boolean canProcess() {
        return this.ossUrl != null && !this.ossUrl.isEmpty();
    }
    
    /**
     * 版本递增
     * @return 新版本号
     */
    public Integer incrementVersion() {
        this.version = (this.version == null ? 0 : this.version) + 1;
        return this.version;
    }
} 