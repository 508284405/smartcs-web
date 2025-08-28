package com.leyue.smartcs.dto.chat.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建群组命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupCreateCmd {
    
    /**
     * 群组名称
     */
    private String groupName;
    
    /**
     * 群主用户ID
     */
    private Long ownerId;
    
    /**
     * 初始成员ID列表
     */
    private List<Long> memberIds;
}