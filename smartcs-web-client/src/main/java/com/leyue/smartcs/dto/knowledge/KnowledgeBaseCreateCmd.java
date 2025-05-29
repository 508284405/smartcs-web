package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "知识库名称不能为空")
    private String name;
    
    /**
     * 知识库唯一编码
     */
    @NotBlank(message = "知识库唯一编码不能为空")
    private String code;
    
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