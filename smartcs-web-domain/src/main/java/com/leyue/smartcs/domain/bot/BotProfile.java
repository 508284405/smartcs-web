package com.leyue.smartcs.domain.bot;

import com.leyue.smartcs.domain.bot.enums.ModelTypeEnum;
import com.leyue.smartcs.domain.bot.enums.VendorTypeEnum;
import lombok.Data;



/**
 * 机器人配置领域模型
 */
@Data
public class BotProfile {
    
    /**
     * 主键，同时对应 cs_agent.agent_id
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
     * 模型厂商
     */
    private VendorTypeEnum vendor;
    
    /**
     * 模型类型
     */
    private ModelTypeEnum modelType;
    
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
    
    /**
     * 逻辑删除标识
     */
    private Integer isDeleted;
    
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
     * 验证机器人配置是否有效
     */
    public boolean isValid() {
        return botName != null && !botName.trim().isEmpty()
                && modelName != null && !modelName.trim().isEmpty()
                && promptKey != null && !promptKey.trim().isEmpty()
                && vendor != null
                && modelType != null
                && apiKey != null && !apiKey.trim().isEmpty()
                && baseUrl != null && !baseUrl.trim().isEmpty();
    }
    
    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
    
    /**
     * 标记为删除
     */
    public void markAsDeleted() {
        this.isDeleted = 1;
        this.updatedAt = System.currentTimeMillis();
    }
} 