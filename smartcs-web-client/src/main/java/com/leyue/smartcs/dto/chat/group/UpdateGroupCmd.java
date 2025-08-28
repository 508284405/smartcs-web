package com.leyue.smartcs.dto.chat.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新群组命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupCmd {
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 操作者ID
     */
    private Long operatorId;
    
    /**
     * 新的群组名称
     */
    private String groupName;
}