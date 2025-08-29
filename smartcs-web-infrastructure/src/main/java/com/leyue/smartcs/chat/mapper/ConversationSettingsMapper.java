package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.ConversationSettingsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 会话设置Mapper
 */
@Mapper
public interface ConversationSettingsMapper extends BaseMapper<ConversationSettingsDO> {
    
    /**
     * 根据用户和会话查找设置
     */
    @Select("SELECT * FROM cs_conversation_settings WHERE user_id = #{userId} AND session_id = #{sessionId} LIMIT 1")
    ConversationSettingsDO findByUserAndSession(@Param("userId") String userId, @Param("sessionId") Long sessionId);
    
    /**
     * 获取用户的所有会话设置
     */
    @Select("SELECT * FROM cs_conversation_settings WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<ConversationSettingsDO> findByUserId(@Param("userId") String userId);
    
    /**
     * 获取用户置顶的会话
     */
    @Select("SELECT * FROM cs_conversation_settings WHERE user_id = #{userId} AND is_pinned = 1 " +
            "ORDER BY pinned_at DESC")
    List<ConversationSettingsDO> findPinnedConversations(@Param("userId") String userId);
    
    /**
     * 获取用户免打扰的会话
     */
    @Select("SELECT * FROM cs_conversation_settings WHERE user_id = #{userId} AND is_muted = 1 " +
            "AND (mute_end_at IS NULL OR mute_end_at > UNIX_TIMESTAMP() * 1000) " +
            "ORDER BY muted_at DESC")
    List<ConversationSettingsDO> findMutedConversations(@Param("userId") String userId);
    
    /**
     * 获取用户归档的会话
     */
    @Select("SELECT * FROM cs_conversation_settings WHERE user_id = #{userId} AND is_archived = 1 " +
            "ORDER BY archived_at DESC")
    List<ConversationSettingsDO> findArchivedConversations(@Param("userId") String userId);
    
    /**
     * 获取用户未归档的会话设置
     */
    @Select("SELECT * FROM cs_conversation_settings WHERE user_id = #{userId} AND is_archived = 0 " +
            "ORDER BY " +
            "CASE WHEN is_pinned = 1 THEN 0 ELSE 1 END, " +
            "pinned_at DESC, " +
            "updated_at DESC")
    List<ConversationSettingsDO> findActiveConversations(@Param("userId") String userId);
    
    /**
     * 更新过期的免打扰设置
     */
    @Update("UPDATE cs_conversation_settings SET is_muted = 0, updated_at = UNIX_TIMESTAMP() * 1000 " +
            "WHERE is_muted = 1 AND mute_end_at IS NOT NULL AND mute_end_at <= UNIX_TIMESTAMP() * 1000")
    int updateExpiredMuteSettings();
    
    /**
     * 统计用户置顶会话数量
     */
    @Select("SELECT COUNT(*) FROM cs_conversation_settings WHERE user_id = #{userId} AND is_pinned = 1")
    long countPinnedConversations(@Param("userId") String userId);
    
    /**
     * 统计用户归档会话数量
     */
    @Select("SELECT COUNT(*) FROM cs_conversation_settings WHERE user_id = #{userId} AND is_archived = 1")
    long countArchivedConversations(@Param("userId") String userId);
    
    /**
     * 删除用户的会话设置
     */
    @Select("DELETE FROM cs_conversation_settings WHERE user_id = #{userId} AND session_id = #{sessionId}")
    int deleteByUserAndSession(@Param("userId") String userId, @Param("sessionId") Long sessionId);
    
    /**
     * 批量删除用户的会话设置
     */
    @Select("DELETE FROM cs_conversation_settings WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") String userId);
}