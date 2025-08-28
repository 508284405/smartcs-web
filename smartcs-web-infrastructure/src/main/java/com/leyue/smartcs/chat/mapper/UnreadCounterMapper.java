package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.UnreadCounterDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 未读计数Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface UnreadCounterMapper extends BaseMapper<UnreadCounterDO> {
    
    /**
     * 根据用户ID和会话ID查询未读计数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 未读计数记录
     */
    UnreadCounterDO findByUserAndConversation(@Param("userId") Long userId, 
                                              @Param("conversationId") String conversationId);
    
    /**
     * 增加未读计数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param increment 增量
     * @return 更新后的计数
     */
    int incrementUnreadCount(@Param("userId") Long userId, 
                           @Param("conversationId") String conversationId, 
                           @Param("increment") int increment);
    
    /**
     * 减少未读计数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param decrement 减量
     * @return 更新后的计数
     */
    int decrementUnreadCount(@Param("userId") Long userId, 
                           @Param("conversationId") String conversationId, 
                           @Param("decrement") int decrement);
    
    /**
     * 重置未读计数为0
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 影响的行数
     */
    int resetUnreadCount(@Param("userId") Long userId, 
                        @Param("conversationId") String conversationId);
    
    /**
     * 获取用户的所有会话未读计数
     *
     * @param userId 用户ID
     * @return 未读计数列表
     */
    List<UnreadCounterDO> findAllByUser(@Param("userId") Long userId);
    
    /**
     * 批量获取用户未读计数
     *
     * @param userId 用户ID
     * @param conversationIds 会话ID列表
     * @return 未读计数列表
     */
    List<UnreadCounterDO> findByUserAndConversations(@Param("userId") Long userId, 
                                                     @Param("conversationIds") List<String> conversationIds);
    
    /**
     * 删除用户的未读计数记录
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 影响的行数
     */
    int deleteByUserAndConversation(@Param("userId") Long userId, 
                                   @Param("conversationId") String conversationId);
    
    /**
     * 保存或更新未读计数（MySQL UPSERT）
     *
     * @param unreadCounter 未读计数记录
     * @return 影响的行数
     */
    int saveOrUpdate(@Param("unreadCounter") UnreadCounterDO unreadCounter);
}