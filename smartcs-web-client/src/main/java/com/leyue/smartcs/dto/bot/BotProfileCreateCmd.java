package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;



/**
 * 创建机器人配置命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfileCreateCmd extends Command {
    
    /**
     * 机器人名称
     */
    @NotEmpty(message = "机器人名称不能为空")
    private String botName;
    
    /**
     * 使用的 LLM / 模型标识，如 gpt-4o、bge-large
     */
    @NotEmpty(message = "LLM/模型标识不能为空")
    private String modelName;
    
    /**
     * 默认 Prompt 模板 key，关联 bot_prompt_template
     */
    @NotEmpty(message = "默认 Prompt 模板 key 不能为空")
    private String promptKey;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 模型厂商
     */
    @NotEmpty(message = "模型厂商不能为空")
    private String vendor;
    
    /**
     * 模型类型
     */
    @NotEmpty(message = "模型类型不能为空")
    private String modelType;
    
    /**
     * API密钥
     */
    @NotEmpty(message = "API密钥不能为空")
    private String apiKey;
    
    /**
     * API基础URL
     */
    @NotEmpty(message = "API基础URL不能为空")
    private String baseUrl;
    
    /**
     * 模型具体配置（JSON格式）
     */
    private String options;
    
    /**
     * 是否启用，默认为true
     */
    private Boolean enabled = true;
} 