package com.leyue.smartcs.chat.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 多媒体消息数据对象
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Data
@TableName(value = "cs_media_message", autoResultMap = true)
public class MediaMessageDO {
    
    /**
     * 自增主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 多媒体ID
     */
    @TableId
    private String mediaId;
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 多媒体类型
     * 1-图片, 2-语音, 3-视频, 4-文件, 5-位置
     */
    private Integer mediaType;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 缩略图URL
     */
    private String thumbnailUrl;
    
    /**
     * 宽度（图片/视频）
     */
    private Integer width;
    
    /**
     * 高度（图片/视频）
     */
    private Integer height;
    
    /**
     * 时长（语音/视频，单位秒）
     */
    private Integer duration;
    
    /**
     * 位置纬度
     */
    private Double latitude;
    
    /**
     * 位置经度
     */
    private Double longitude;
    
    /**
     * 位置地址
     */
    private String address;
    
    /**
     * 位置名称
     */
    private String locationName;
    
    /**
     * 上传状态
     * 0-上传中, 1-上传成功, 2-上传失败
     */
    private Integer uploadStatus;
    
    /**
     * 上传进度（0-100）
     */
    private Integer uploadProgress;
    
    /**
     * 扩展数据（JSON格式）
     */
    private String extraData;
    
    /**
     * 逻辑删除标识
     */
    @TableLogic
    private Integer isDeleted = 0;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 更新者
     */
    private String updatedBy;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}