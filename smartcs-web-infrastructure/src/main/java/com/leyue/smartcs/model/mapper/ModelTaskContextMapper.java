package com.leyue.smartcs.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.model.dataobject.ModelTaskContextDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型上下文Mapper接口
 */
@Mapper
public interface ModelTaskContextMapper extends BaseMapper<ModelTaskContextDO> {
    
    /**
     * 根据会话ID查询（不包含已删除的）
     * @param sessionId 会话ID
     * @return 上下文信息
     */
    ModelTaskContextDO selectBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 检查会话ID是否已存在（不包含已删除的）
     * @param sessionId 会话ID
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    int countBySessionId(@Param("sessionId") String sessionId, @Param("excludeId") Long excludeId);
    
    /**
     * 根据模型ID查询上下文列表（不包含已删除的）
     * @param modelId 模型ID
     * @return 上下文列表
     */
    List<ModelTaskContextDO> selectByModelId(@Param("modelId") Long modelId);
    
    /**
     * 查询超过指定长度的上下文
     * @param maxLength 最大长度
     * @return 上下文列表
     */
    List<ModelTaskContextDO> selectByCurrentLengthGreaterThan(@Param("maxLength") Integer maxLength);
    
    /**
     * 查询长时间未更新的上下文
     * @param updatedBefore 更新时间之前
     * @return 上下文列表
     */
    List<ModelTaskContextDO> selectByUpdatedBefore(@Param("updatedBefore") Long updatedBefore);
    
    /**
     * 更新上下文消息和长度
     * @param id 上下文ID
     * @param messages 消息内容
     * @param currentLength 当前长度
     * @return 更新条数
     */
    int updateMessagesAndLength(@Param("id") Long id, @Param("messages") String messages, @Param("currentLength") Integer currentLength);
    
    /**
     * 清空上下文消息
     * @param id 上下文ID
     * @return 更新条数
     */
    int clearMessages(@Param("id") Long id);
}