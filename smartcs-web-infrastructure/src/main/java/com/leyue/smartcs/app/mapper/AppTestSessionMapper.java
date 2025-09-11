package com.leyue.smartcs.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.app.dataobject.AppTestSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI应用测试会话Mapper
 */
@Mapper
public interface AppTestSessionMapper extends BaseMapper<AppTestSessionDO> {
    
    /**
     * 根据会话ID查询会话
     * @param sessionId 会话ID
     * @return 会话信息
     */
    AppTestSessionDO selectBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据应用ID查询活跃会话列表
     * @param appId 应用ID
     * @param limit 限制数量
     * @return 会话列表
     */
    List<AppTestSessionDO> findActiveSessionsByAppId(@Param("appId") Long appId, @Param("limit") Integer limit);
    
    /**
     * 根据用户ID查询会话列表
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 会话列表
     */
    List<AppTestSessionDO> findSessionsByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 更新会话消息统计
     * @param sessionId 会话ID
     * @param messageCount 消息数量
     * @param lastMessageTime 最后消息时间
     * @param totalTokens 总Token数
     * @param totalCost 总费用
     * @return 更新行数
     */
    int updateSessionStats(@Param("sessionId") String sessionId,
                          @Param("messageCount") Integer messageCount,
                          @Param("lastMessageTime") Long lastMessageTime,
                          @Param("totalTokens") Integer totalTokens,
                          @Param("totalCost") java.math.BigDecimal totalCost);
}