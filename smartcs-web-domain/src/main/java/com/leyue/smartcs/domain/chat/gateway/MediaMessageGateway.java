package com.leyue.smartcs.domain.chat.gateway;

import com.leyue.smartcs.domain.chat.MediaMessage;

import java.util.List;
import java.util.Optional;

/**
 * 多媒体消息网关接口
 * 
 * @author Claude
 * @since 2024-08-29
 */
public interface MediaMessageGateway {
    
    /**
     * 保存多媒体消息
     * 
     * @param mediaMessage 多媒体消息
     * @return 多媒体ID
     */
    String saveMediaMessage(MediaMessage mediaMessage);
    
    /**
     * 根据消息ID查找多媒体消息
     * 
     * @param msgId 消息ID
     * @return 多媒体消息
     */
    Optional<MediaMessage> findByMsgId(String msgId);
    
    /**
     * 根据多媒体ID查找多媒体消息
     * 
     * @param mediaId 多媒体ID
     * @return 多媒体消息
     */
    Optional<MediaMessage> findByMediaId(String mediaId);
    
    /**
     * 根据会话ID查找所有多媒体消息
     * 
     * @param sessionId 会话ID
     * @param mediaType 媒体类型（可选）
     * @param limit 限制数量
     * @return 多媒体消息列表
     */
    List<MediaMessage> findBySessionId(Long sessionId, Integer mediaType, int limit);
    
    /**
     * 更新上传状态
     * 
     * @param mediaId 多媒体ID
     * @param status 上传状态
     * @param progress 上传进度
     * @return 是否成功
     */
    boolean updateUploadStatus(String mediaId, Integer status, Integer progress);
    
    /**
     * 更新文件URL
     * 
     * @param mediaId 多媒体ID
     * @param fileUrl 文件URL
     * @param thumbnailUrl 缩略图URL
     * @return 是否成功
     */
    boolean updateFileUrl(String mediaId, String fileUrl, String thumbnailUrl);
    
    /**
     * 删除多媒体消息
     * 
     * @param mediaId 多媒体ID
     * @return 是否成功
     */
    boolean deleteMediaMessage(String mediaId);
    
    /**
     * 批量删除会话的多媒体消息
     * 
     * @param sessionId 会话ID
     * @return 删除数量
     */
    int deleteBySessionId(Long sessionId);
    
    /**
     * 统计用户的多媒体文件大小
     * 
     * @param userId 用户ID
     * @return 总大小（字节）
     */
    long getTotalFileSizeByUser(String userId);
    
    /**
     * 统计会话的多媒体数量
     * 
     * @param sessionId 会话ID
     * @param mediaType 媒体类型（可选）
     * @return 数量
     */
    long countBySessionId(Long sessionId, Integer mediaType);
}