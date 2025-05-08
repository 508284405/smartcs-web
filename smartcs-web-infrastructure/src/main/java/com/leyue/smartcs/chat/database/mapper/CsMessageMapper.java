package com.leyue.smartcs.chat.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.database.dataobject.CsMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT * FROM cs_message WHERE msg_id = #{msgId} AND is_deleted = 0")
    CsMessageDO selectByMessageId(@Param("msgId") String msgId);
    
    /**
     * 根据会话ID查询消息列表（按创建时间倒序）
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息列表
     */
    @Select("SELECT * FROM cs_message WHERE session_id = #{sessionId} AND is_deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<CsMessageDO> findMessagesBySessionId(@Param("sessionId") Long sessionId, @Param("limit") int limit);
    
    /**
     * 根据会话ID查询消息列表（分页）
     *
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param size 限制数量
     * @return 消息列表
     */
    @Select("SELECT * FROM cs_message WHERE session_id = #{sessionId} AND is_deleted = 0 ORDER BY create_time DESC LIMIT #{size} OFFSET #{offset}")
    List<CsMessageDO> selectBySessionId(@Param("sessionId") String sessionId, @Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计会话消息数量
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    @Select("SELECT COUNT(*) FROM cs_message WHERE session_id = #{sessionId} AND is_deleted = 0")
    long countBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 批量插入消息
     * 
     * @param messages 消息列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<CsMessageDO> messages);
}
