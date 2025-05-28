package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容创建命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentCreateCmd extends Command {
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容类型 document/audio/video
     */
    private String contentType;
    
    /**
     * OSS存储地址
     */
    private String ossUrl;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
} 