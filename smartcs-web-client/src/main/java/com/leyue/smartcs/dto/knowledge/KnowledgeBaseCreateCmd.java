package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库创建命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseCreateCmd extends Command {
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 知识库唯一编码
     */
    private String code;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 可见性 public/private
     */
    private String visibility;
} 