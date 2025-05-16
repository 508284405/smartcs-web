package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * FAQ创建/更新命令对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FaqAddCmd extends Command {
    /**
     * FAQ ID（更新时必填）
     */
    private Long id;
    
    /**
     * 问题文本
     */
    private String question;
    
    /**
     * 答案文本
     */
    private String answer;
    
    /**
     * 是否启用
     */
    private Boolean enabled = true;
} 