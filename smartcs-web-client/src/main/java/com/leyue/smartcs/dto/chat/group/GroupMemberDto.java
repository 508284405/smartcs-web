package com.leyue.smartcs.dto.chat.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 群成员DTO
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberDto {
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 角色：OWNER/ADMIN/MEMBER
     */
    private String role;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 加群时间
     */
    private Long joinedAt;
}