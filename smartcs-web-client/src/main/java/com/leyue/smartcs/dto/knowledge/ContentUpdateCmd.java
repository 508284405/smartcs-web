package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容更新命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentUpdateCmd extends Command {
    
    /**
     * 内容ID
     */
    private Long id;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容状态 uploaded/parsed/vectorized
     */
    private String status;
    
    /**
     * 提取的文本内容
     */
    private String extractedText;
} 