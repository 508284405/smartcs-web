package com.leyue.smartcs.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.app.dataobject.AppTestMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI应用测试消息Mapper
 */
@Mapper
public interface AppTestMessageMapper extends BaseMapper<AppTestMessageDO> {
    
    /**
     * 根据消息ID查询消息
     * @param messageId 消息ID
     * @return 消息信息
     */
    AppTestMessageDO selectByMessageId(@Param("messageId") String messageId);
    
    /**
     * 根据会话ID查询消息列表（按时间排序）
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 消息列表
     */
    List<AppTestMessageDO> findMessagesBySessionId(@Param("sessionId") String sessionId,
                                                  @Param("limit") Integer limit,
                                                  @Param("offset") Integer offset);
    
    /**
     * 根据会话ID统计消息数量
     * @param sessionId 会话ID
     * @return 消息数量
     */
    Integer countMessagesBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据会话ID获取最新的消息
     * @param sessionId 会话ID
     * @param messageType 消息类型（可选）
     * @return 最新消息
     */
    AppTestMessageDO findLatestMessageBySessionId(@Param("sessionId") String sessionId,
                                                 @Param("messageType") String messageType);
    
    /**
     * 批量插入消息
     * @param messages 消息列表
     * @return 插入行数
     */
    int batchInsert(@Param("messages") List<AppTestMessageDO> messages);
}