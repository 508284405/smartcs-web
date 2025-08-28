package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 群成员数据对象，对应t_im_group_member表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_im_group_member", autoResultMap = true)
public class GroupMemberDO extends BaseDO {
    
    /**
     * 群ID
     */
    private Long groupId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色：OWNER/ADMIN/MEMBER
     */
    private String role;
    
    /**
     * 加群时间
     */
    private Long joinedAt;
    
    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;
}