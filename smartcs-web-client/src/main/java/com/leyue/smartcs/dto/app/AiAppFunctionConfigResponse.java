package com.leyue.smartcs.dto.app;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI应用功能配置响应
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAppFunctionConfigResponse {
    
    /**
     * 应用ID
     */
    private Long appId;
    
    /**
     * 功能配置
     */
    private Map<String, Object> functionConfig;
    
    /**
     * 对话开场白开关
     */
    private Boolean conversationOpenerEnabled;
    
    /**
     * 下一步问题建议开关
     */
    private Boolean nextQuestionSuggestionEnabled;
    
    /**
     * 文字转语音开关
     */
    private Boolean textToSpeechEnabled;
    
    /**
     * 语音转文字开关
     */
    private Boolean speechToTextEnabled;
    
    /**
     * 引用和归属开关
     */
    private Boolean citationEnabled;
    
    /**
     * 内容审查开关
     */
    private Boolean contentModerationEnabled;
    
    /**
     * 标杆回复开关
     */
    private Boolean standardReplyEnabled;
}