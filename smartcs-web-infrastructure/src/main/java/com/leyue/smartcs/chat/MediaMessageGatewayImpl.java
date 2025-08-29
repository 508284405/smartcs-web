package com.leyue.smartcs.chat;

import com.leyue.smartcs.chat.convertor.MediaMessageConvertor;
import com.leyue.smartcs.chat.dataobject.MediaMessageDO;
import com.leyue.smartcs.chat.mapper.MediaMessageMapper;
import com.leyue.smartcs.domain.chat.MediaMessage;
import com.leyue.smartcs.domain.chat.gateway.MediaMessageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 多媒体消息网关实现
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Component
@RequiredArgsConstructor
public class MediaMessageGatewayImpl implements MediaMessageGateway {
    
    private final MediaMessageMapper mediaMessageMapper;
    private final MediaMessageConvertor mediaMessageConvertor = MediaMessageConvertor.INSTANCE;
    
    @Override
    public String saveMediaMessage(MediaMessage mediaMessage) {
        // 生成媒体ID
        if (mediaMessage.getMediaId() == null || mediaMessage.getMediaId().isEmpty()) {
            mediaMessage.setMediaId(UUID.randomUUID().toString().replace("-", ""));
        }
        
        // 设置时间戳
        long now = System.currentTimeMillis();
        if (mediaMessage.getCreatedAt() == null) {
            mediaMessage.setCreatedAt(now);
        }
        mediaMessage.setUpdatedAt(now);
        
        MediaMessageDO mediaMessageDO = mediaMessageConvertor.toDataObject(mediaMessage);
        mediaMessageMapper.insert(mediaMessageDO);
        
        return mediaMessage.getMediaId();
    }
    
    @Override
    public Optional<MediaMessage> findByMsgId(String msgId) {
        MediaMessageDO mediaMessageDO = mediaMessageMapper.selectByMsgId(msgId);
        return Optional.ofNullable(mediaMessageConvertor.toDomain(mediaMessageDO));
    }
    
    @Override
    public Optional<MediaMessage> findByMediaId(String mediaId) {
        MediaMessageDO mediaMessageDO = mediaMessageMapper.selectByMediaId(mediaId);
        return Optional.ofNullable(mediaMessageConvertor.toDomain(mediaMessageDO));
    }
    
    @Override
    public List<MediaMessage> findBySessionId(Long sessionId, Integer mediaType, int limit) {
        List<MediaMessageDO> mediaMessageDOList = mediaMessageMapper.selectBySessionId(sessionId, mediaType, limit);
        return mediaMessageDOList.stream()
                .map(mediaMessageConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateUploadStatus(String mediaId, Integer status, Integer progress) {
        return mediaMessageMapper.updateUploadStatus(mediaId, status, progress) > 0;
    }
    
    @Override
    public boolean updateFileUrl(String mediaId, String fileUrl, String thumbnailUrl) {
        return mediaMessageMapper.updateFileUrl(mediaId, fileUrl, thumbnailUrl) > 0;
    }
    
    @Override
    public boolean deleteMediaMessage(String mediaId) {
        return mediaMessageMapper.deleteById(mediaId) > 0;
    }
    
    @Override
    public int deleteBySessionId(Long sessionId) {
        return mediaMessageMapper.deleteBySessionId(sessionId);
    }
    
    @Override
    public long getTotalFileSizeByUser(String userId) {
        return mediaMessageMapper.sumFileSizeByUser(userId);
    }
    
    @Override
    public long countBySessionId(Long sessionId, Integer mediaType) {
        return mediaMessageMapper.countBySessionId(sessionId, mediaType);
    }
}