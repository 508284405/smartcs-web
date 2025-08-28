package com.leyue.smartcs.dto.chat.offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 离线消息确认命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfflineMessageAckCmd {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 已确认的消息ID列表
     */
    private List<String> msgIds;
}