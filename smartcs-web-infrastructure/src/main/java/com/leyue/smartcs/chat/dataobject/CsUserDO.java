package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户数据对象，对应cs_user表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cs_user")
public class CsUserDO extends BaseDO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 手机号掩码
     */
    private String phoneMask;
    
    /**
     * 用户类型 0=消费者 1=客服
     */
    private Integer userType;
    
    /**
     * 状态 1=正常 0=禁用
     */
    private Integer status;
}
