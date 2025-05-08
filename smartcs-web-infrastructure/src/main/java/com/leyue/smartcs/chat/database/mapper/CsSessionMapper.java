package com.leyue.smartcs.chat.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.database.dataobject.CsSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会话Mapper接口
 */
@Mapper
public interface CsSessionMapper extends BaseMapper<CsSessionDO> {
    
    /**
     * 根据会话ID查询会话
     *
     * @param sessionId 会话ID
     * @return 会话对象
     */
    @Select("SELECT * FROM cs_session WHERE session_id = #{sessionId} AND is_deleted = 0")
    CsSessionDO selectBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据客户ID查询活跃会话
     *
     * @param customerId 客户ID
     * @return 会话对象
     */
    @Select("SELECT * FROM cs_session WHERE customer_id = #{customerId} AND session_state = 1 AND is_deleted = 0 LIMIT 1")
    CsSessionDO findActiveSessionByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 根据客户ID查询历史会话（按创建时间倒序）
     *
     * @param customerId 客户ID
     * @param limit 限制数量
     * @return 会话列表
     */
    @Select("SELECT * FROM cs_session WHERE customer_id = #{customerId} AND is_deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<CsSessionDO> findSessionsByCustomerId(@Param("customerId") Long customerId, @Param("limit") int limit);
    
    /**
     * 根据客服ID查询活跃会话列表
     *
     * @param agentId 客服ID
     * @return 会话列表
     */
    @Select("SELECT * FROM cs_session WHERE agent_id = #{agentId} AND session_state = 1 AND is_deleted = 0")
    List<CsSessionDO> findActiveSessionsByAgentId(@Param("agentId") Long agentId);
    
    /**
     * 根据状态查询会话列表（按创建时间排序）
     *
     * @param status 会话状态
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 会话列表
     */
    @Select("SELECT * FROM cs_session WHERE status = #{status} AND is_deleted = 0 ORDER BY create_time ASC LIMIT #{limit} OFFSET #{offset}")
    List<CsSessionDO> findSessionsByStatus(@Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);
}
