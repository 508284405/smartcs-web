package com.leyue.smartcs.dto.chat.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移除群成员命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemoveGroupMemberCmd {
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 操作者ID
     */
    private Long operatorId;
    
    /**
     * 要移除的成员ID
     */
    private Long memberId;
}