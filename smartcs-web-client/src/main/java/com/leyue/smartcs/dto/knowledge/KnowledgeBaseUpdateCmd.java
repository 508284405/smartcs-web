package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库更新命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseUpdateCmd extends Command {
    
    /**
     * 知识库ID
     */
    private Long id;
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 可见性 public/private
     */
    private String visibility;
} 