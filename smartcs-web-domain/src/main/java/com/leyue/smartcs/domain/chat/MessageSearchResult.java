package com.leyue.smartcs.domain.chat;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 消息搜索结果领域对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageSearchResult {
    
    /**
     * 消息对象
     */
    private Message message;
    
    /**
     * 匹配得分（用于相关性排序）
     */
    private Double score;
    
    /**
     * 上下文消息ID（用于跳转时定位）
     */
    private String contextMsgId;
}