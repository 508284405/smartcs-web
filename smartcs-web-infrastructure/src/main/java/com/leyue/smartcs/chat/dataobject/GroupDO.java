package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 群组数据对象，对应t_im_group表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_im_group", autoResultMap = true)
public class GroupDO extends BaseDO {
    
    /**
     * 群ID
     */
    private Long groupId;
    
    /**
     * 群名称
     */
    private String groupName;
    
    /**
     * 群主用户ID
     */
    private Long ownerId;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;
}