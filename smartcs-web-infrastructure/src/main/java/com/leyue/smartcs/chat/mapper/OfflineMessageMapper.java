package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.OfflineMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 离线消息Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface OfflineMessageMapper extends BaseMapper<OfflineMessageDO> {
    
    /**
     * 根据接收者ID和会话ID查询离线消息
     *
     * @param receiverId 接收者ID
     * @param conversationId 会话ID
     * @param limit 限制条数
     * @return 离线消息列表
     */
    List<OfflineMessageDO> findByReceiverAndConversation(@Param("receiverId") Long receiverId, 
                                                         @Param("conversationId") String conversationId, 
                                                         @Param("limit") int limit);
    
    /**
     * 根据接收者ID获取所有未读的离线消息摘要
     *
     * @param receiverId 接收者ID
     * @return 离线消息摘要列表（按会话分组）
     */
    List<OfflineMessageDO> findUnreadSummaryByReceiver(@Param("receiverId") Long receiverId);
    
    /**
     * 根据消息ID批量删除离线消息
     *
     * @param receiverId 接收者ID
     * @param msgIds 消息ID列表
     * @return 删除的记录数
     */
    int deleteByMsgIds(@Param("receiverId") Long receiverId, @Param("msgIds") List<String> msgIds);
    
    /**
     * 根据接收者ID和会话ID清除所有离线消息
     *
     * @param receiverId 接收者ID
     * @param conversationId 会话ID
     * @return 清除的记录数
     */
    int clearByReceiverAndConversation(@Param("receiverId") Long receiverId, 
                                       @Param("conversationId") String conversationId);
    
    /**
     * 清理过期的离线消息
     *
     * @param expireTimestamp 过期时间戳
     * @return 清理的记录数
     */
    int cleanExpiredMessages(@Param("expireTimestamp") long expireTimestamp);
    
    /**
     * 批量插入离线消息
     * 
     * @param offlineMessages 离线消息列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<OfflineMessageDO> offlineMessages);
}