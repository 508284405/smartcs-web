package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.MediaMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 多媒体消息Mapper接口
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Mapper
public interface MediaMessageMapper extends BaseMapper<MediaMessageDO> {
    
    /**
     * 根据消息ID查询多媒体消息
     * 
     * @param msgId 消息ID
     * @return 多媒体消息
     */
    MediaMessageDO selectByMsgId(@Param("msgId") String msgId);
    
    /**
     * 根据多媒体ID查询多媒体消息
     * 
     * @param mediaId 多媒体ID
     * @return 多媒体消息
     */
    MediaMessageDO selectByMediaId(@Param("mediaId") String mediaId);
    
    /**
     * 根据会话ID查询多媒体消息列表
     * 
     * @param sessionId 会话ID
     * @param mediaType 媒体类型（可选）
     * @param limit 限制数量
     * @return 多媒体消息列表
     */
    List<MediaMessageDO> selectBySessionId(@Param("sessionId") Long sessionId,
                                          @Param("mediaType") Integer mediaType,
                                          @Param("limit") int limit);
    
    /**
     * 更新上传状态
     * 
     * @param mediaId 多媒体ID
     * @param status 上传状态
     * @param progress 上传进度
     * @return 影响行数
     */
    int updateUploadStatus(@Param("mediaId") String mediaId,
                          @Param("status") Integer status,
                          @Param("progress") Integer progress);
    
    /**
     * 更新文件URL
     * 
     * @param mediaId 多媒体ID
     * @param fileUrl 文件URL
     * @param thumbnailUrl 缩略图URL
     * @return 影响行数
     */
    int updateFileUrl(@Param("mediaId") String mediaId,
                     @Param("fileUrl") String fileUrl,
                     @Param("thumbnailUrl") String thumbnailUrl);
    
    /**
     * 根据会话ID删除多媒体消息
     * 
     * @param sessionId 会话ID
     * @return 影响行数
     */
    int deleteBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 统计用户的多媒体文件大小
     * 
     * @param userId 用户ID
     * @return 总大小（字节）
     */
    long sumFileSizeByUser(@Param("userId") String userId);
    
    /**
     * 统计会话的多媒体数量
     * 
     * @param sessionId 会话ID
     * @param mediaType 媒体类型（可选）
     * @return 数量
     */
    long countBySessionId(@Param("sessionId") Long sessionId,
                         @Param("mediaType") Integer mediaType);
}