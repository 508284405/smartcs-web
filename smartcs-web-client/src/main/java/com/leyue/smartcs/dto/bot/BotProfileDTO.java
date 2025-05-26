package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

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
     * 该 Bot 对外允许的最大 QPS
     */
    private Integer maxQps;
    
    /**
     * LLM 采样温度
     */
    private BigDecimal temperature;
    
    /**
     * 额外配置（如系统指令、插件开关等）
     */
    private String extraConfig;
    
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
} 