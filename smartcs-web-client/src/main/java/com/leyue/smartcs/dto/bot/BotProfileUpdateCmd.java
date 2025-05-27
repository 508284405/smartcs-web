package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;



/**
 * 更新机器人配置命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfileUpdateCmd extends Command {
    
    /**
     * 机器人ID
     */
    @NotNull(message = "botId cannot be null")
    private Long botId;
    
    /**
     * 机器人名称
     */
    private String botName;
    
    /**
     * 使用的 LLM / 模型标识，如 gpt-4o、bge-large
     */
    private String modelName;
    
    /**
     * 默认 Prompt 模板 key，关联 bot_prompt_template
     */
    private String promptKey;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 模型厂商
     */
    private String vendor;
    
    /**
     * 模型类型
     */
    private String modelType;
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * API基础URL
     */
    private String baseUrl;
    
    /**
     * 模型具体配置（JSON格式）
     */
    private String options;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
} 