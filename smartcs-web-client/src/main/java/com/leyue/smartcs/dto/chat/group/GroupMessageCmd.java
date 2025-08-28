package com.leyue.smartcs.dto.chat.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 群消息命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMessageCmd {
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：text/image/file等
     */
    private String messageType;
    
    /**
     * 消息ID（可选，用于幂等）
     */
    private String msgId;
}