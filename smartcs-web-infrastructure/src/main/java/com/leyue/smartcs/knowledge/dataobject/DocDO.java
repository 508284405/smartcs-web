package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档数据对象，对应cs_doc表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cs_doc")
public class DocDO extends BaseDO {
    
    /**
     * 文档标题
     */
    @TableField("title")
    private String title;
    
    /**
     * OSS存储地址
     */
    @TableField("oss_url")
    private String ossUrl;
    
    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;
    
    /**
     * 版本号
     */
    @TableField("version_no")
    private Integer version;
} 