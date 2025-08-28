package com.leyue.smartcs.dto.chat.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 群组DTO
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 群组名称
     */
    private String groupName;
    
    /**
     * 群主用户ID
     */
    private Long ownerId;
    
    /**
     * 群主用户名
     */
    private String ownerName;
    
    /**
     * 成员数量
     */
    private Integer memberCount;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}