package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "知识库ID不能为空")
    private Long id;
    
    /**
     * 知识库名称
     */
    @Size(max = 100, message = "知识库名称长度不能超过100个字符")
    private String name;
    
    /**
     * 描述信息
     */
    @Size(max = 500, message = "描述信息长度不能超过500个字符")
    private String description;
    
    /**
     * 可见性 public/private
     */
    @Pattern(regexp = "^(public|private)$", message = "可见性只能是public或private")
    private String visibility;
} 