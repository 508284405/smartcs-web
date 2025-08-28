package com.leyue.smartcs.dto.chat.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 添加群成员命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddGroupMemberCmd {
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 操作者ID
     */
    private Long operatorId;
    
    /**
     * 要添加的成员ID列表
     */
    private List<Long> memberIds;
}