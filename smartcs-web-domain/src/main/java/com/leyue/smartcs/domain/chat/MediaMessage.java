package com.leyue.smartcs.domain.chat;

import lombok.Data;

/**
 * 多媒体消息实体
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Data
public class MediaMessage {
    
    /**
     * 多媒体ID
     */
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
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 是否为图片消息
     */
    public boolean isImage() {
        return mediaType != null && mediaType == 1;
    }
    
    /**
     * 是否为语音消息
     */
    public boolean isAudio() {
        return mediaType != null && mediaType == 2;
    }
    
    /**
     * 是否为视频消息
     */
    public boolean isVideo() {
        return mediaType != null && mediaType == 3;
    }
    
    /**
     * 是否为文件消息
     */
    public boolean isFile() {
        return mediaType != null && mediaType == 4;
    }
    
    /**
     * 是否为位置消息
     */
    public boolean isLocation() {
        return mediaType != null && mediaType == 5;
    }
    
    /**
     * 获取媒体类型文本
     */
    public String getMediaTypeText() {
        if (mediaType == null) {
            return "未知";
        }
        
        switch (mediaType) {
            case 1: return "图片";
            case 2: return "语音";
            case 3: return "视频";
            case 4: return "文件";
            case 5: return "位置";
            default: return "未知";
        }
    }
    
    /**
     * 获取格式化文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }
        
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));
        return String.format("%.1f %s", 
                fileSize / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * 获取格式化时长
     */
    public String getFormattedDuration() {
        if (duration == null || duration == 0) {
            return "00:00";
        }
        
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * 验证多媒体数据
     */
    public boolean isValid() {
        if (msgId == null || msgId.trim().isEmpty()) {
            return false;
        }
        
        if (mediaType == null || mediaType < 1 || mediaType > 5) {
            return false;
        }
        
        // 位置消息需要经纬度
        if (isLocation()) {
            return latitude != null && longitude != null;
        }
        
        // 其他类型需要文件URL
        return fileUrl != null && !fileUrl.trim().isEmpty();
    }
    
    /**
     * 创建图片消息
     */
    public static MediaMessage createImageMessage(String msgId, String fileUrl, 
                                                 String fileName, Long fileSize, 
                                                 Integer width, Integer height, 
                                                 String createdBy) {
        MediaMessage media = new MediaMessage();
        media.setMsgId(msgId);
        media.setMediaType(1);
        media.setFileUrl(fileUrl);
        media.setFileName(fileName);
        media.setFileSize(fileSize);
        media.setWidth(width);
        media.setHeight(height);
        media.setUploadStatus(1);
        media.setUploadProgress(100);
        media.setCreatedBy(createdBy);
        media.setCreatedAt(System.currentTimeMillis());
        media.setUpdatedAt(System.currentTimeMillis());
        return media;
    }
    
    /**
     * 创建语音消息
     */
    public static MediaMessage createAudioMessage(String msgId, String fileUrl, 
                                                 String fileName, Long fileSize, 
                                                 Integer duration, String createdBy) {
        MediaMessage media = new MediaMessage();
        media.setMsgId(msgId);
        media.setMediaType(2);
        media.setFileUrl(fileUrl);
        media.setFileName(fileName);
        media.setFileSize(fileSize);
        media.setDuration(duration);
        media.setUploadStatus(1);
        media.setUploadProgress(100);
        media.setCreatedBy(createdBy);
        media.setCreatedAt(System.currentTimeMillis());
        media.setUpdatedAt(System.currentTimeMillis());
        return media;
    }
    
    /**
     * 创建位置消息
     */
    public static MediaMessage createLocationMessage(String msgId, Double latitude, 
                                                   Double longitude, String address, 
                                                   String locationName, String createdBy) {
        MediaMessage media = new MediaMessage();
        media.setMsgId(msgId);
        media.setMediaType(5);
        media.setLatitude(latitude);
        media.setLongitude(longitude);
        media.setAddress(address);
        media.setLocationName(locationName);
        media.setUploadStatus(1);
        media.setUploadProgress(100);
        media.setCreatedBy(createdBy);
        media.setCreatedAt(System.currentTimeMillis());
        media.setUpdatedAt(System.currentTimeMillis());
        return media;
    }
}