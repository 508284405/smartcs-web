package com.leyue.smartcs.common.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
public class BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableLogic
    private Integer isDeleted = 0;
    
    private String createdBy;
    
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}