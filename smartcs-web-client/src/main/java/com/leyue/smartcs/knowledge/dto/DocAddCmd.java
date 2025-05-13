package com.leyue.smartcs.knowledge.dto;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档创建命令对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DocAddCmd extends Command {
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * OSS地址（已有文件时使用）
     */
    private String ossUrl;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 版本号
     */
    private Integer version = 1;
} 