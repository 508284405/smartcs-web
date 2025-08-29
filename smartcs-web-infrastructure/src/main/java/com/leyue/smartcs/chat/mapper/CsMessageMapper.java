package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.CsMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface CsMessageMapper extends BaseMapper<CsMessageDO> {
    
    /**
     * 根据消息ID查询消息
     *
     * @param msgId 消息ID
     * @return 消息对象
     */
    CsMessageDO selectByMessageId(@Param("msgId") String msgId);
    
    /**
     * 根据会话ID查询消息列表（按创建时间倒序）
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<CsMessageDO> findMessagesBySessionId(@Param("sessionId") Long sessionId, @Param("limit") int limit);
    
    /**
     * 根据会话ID和消息ID查询该消息之前的消息列表
     *
     * @param sessionId 会话ID
     * @param beforeMessageId 消息ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<CsMessageDO> findMessagesBySessionIdBeforeMessageId(@Param("sessionId") Long sessionId, @Param("beforeMessageId") String beforeMessageId, @Param("limit") int limit);
    
    /**
     * 根据会话ID查询消息列表（分页）
     *
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param size 限制数量
     * @return 消息列表
     */
    List<CsMessageDO> selectBySessionId(@Param("sessionId") String sessionId, @Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计会话消息数量
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    long countBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 批量插入消息
     * 
     * @param messages 消息列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<CsMessageDO> messages);
    
    /**
     * 更新消息撤回状态
     * 
     * @param msgId 消息ID
     * @param recalledBy 撤回操作者ID
     * @param recallReason 撤回原因
     * @param recalledAt 撤回时间
     * @return 影响行数
     */
    int updateRecallStatus(@Param("msgId") String msgId, 
                          @Param("recalledBy") String recalledBy, 
                          @Param("recallReason") String recallReason, 
                          @Param("recalledAt") Long recalledAt);
    
    /**
     * 根据消息ID更新消息
     * 
     * @param message 消息对象
     * @return 影响行数
     */
    int updateByMessageId(CsMessageDO message);
    
    /**
     * 搜索消息
     * 
     * @param keyword 搜索关键词
     * @param userId 用户ID
     * @param sessionId 会话ID（可选）
     * @param messageType 消息类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param sortBy 排序方式
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 搜索结果列表
     */
    List<CsMessageDO> searchMessages(@Param("keyword") String keyword,
                                     @Param("userId") String userId,
                                     @Param("sessionId") Long sessionId,
                                     @Param("messageType") Integer messageType,
                                     @Param("startTime") Long startTime,
                                     @Param("endTime") Long endTime,
                                     @Param("sortBy") String sortBy,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);
    
    /**
     * 统计搜索消息数量
     * 
     * @param keyword 搜索关键词
     * @param userId 用户ID
     * @param sessionId 会话ID（可选）
     * @param messageType 消息类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 消息数量
     */
    long countSearchMessages(@Param("keyword") String keyword,
                            @Param("userId") String userId,
                            @Param("sessionId") Long sessionId,
                            @Param("messageType") Integer messageType,
                            @Param("startTime") Long startTime,
                            @Param("endTime") Long endTime);
}
