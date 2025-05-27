package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;



/**
 * 机器人配置数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfileDTO extends DTO {
    
    /**
     * 机器人ID
     */
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
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 创建时间 ms
     */
    private Long createdAt;
    
    /**
     * 更新时间 ms
     */
    private Long updatedAt;
    
    /**
     * 模型厂商
     */
    private String vendor;
    
    /**
     * 模型类型
     */
    private String modelType;
    
    /**
     * API密钥（敏感信息，返回时脱敏）
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