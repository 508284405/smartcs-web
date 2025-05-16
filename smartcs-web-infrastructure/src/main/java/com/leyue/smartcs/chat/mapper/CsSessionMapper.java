package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.CsSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    CsSessionDO selectBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 根据客户ID查询活跃会话
     *
     * @param customerId 客户ID
     * @return 会话对象
     */
    CsSessionDO findActiveSessionByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 查询客户最新一条处理中的会话（排队或进行中）
     *
     * @param customerId 客户ID
     * @return 会话对象
     */
    CsSessionDO findCustomerActiveSession(@Param("customerId") Long customerId);
    
    /**
     * 根据客户ID查询历史会话（按创建时间倒序）
     *
     * @param customerId 客户ID
     * @param limit 限制数量
     * @return 会话列表
     */
    List<CsSessionDO> findSessionsByCustomerId(@Param("customerId") Long customerId, @Param("limit") int limit);
    
    /**
     * 根据客服ID查询活跃会话列表
     *
     * @param agentId 客服ID
     * @return 会话列表
     */
    List<CsSessionDO> findActiveSessionsByAgentId(@Param("agentId") Long agentId);
    
    /**
     * 根据状态查询会话列表（按创建时间排序）
     *
     * @param status 会话状态
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 会话列表
     */
    List<CsSessionDO> findSessionsByStatus(@Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);
}
