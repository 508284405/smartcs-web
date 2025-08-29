package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.MessageReactionDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息表情反应Mapper
 */
@Mapper
public interface MessageReactionMapper extends BaseMapper<MessageReactionDO> {
    
    /**
     * 查询消息的所有表情反应
     */
    @Select("SELECT * FROM cs_message_reaction WHERE msg_id = #{msgId} ORDER BY created_at ASC")
    List<MessageReactionDO> selectByMsgId(@Param("msgId") String msgId);
    
    /**
     * 查询用户对特定消息的表情反应
     */
    @Select("SELECT * FROM cs_message_reaction WHERE msg_id = #{msgId} AND user_id = #{userId}")
    List<MessageReactionDO> selectByMsgIdAndUserId(@Param("msgId") String msgId, @Param("userId") String userId);
    
    /**
     * 查询特定表情反应
     */
    @Select("SELECT * FROM cs_message_reaction WHERE msg_id = #{msgId} AND user_id = #{userId} AND reaction_emoji = #{emoji}")
    MessageReactionDO selectByMsgIdAndUserIdAndEmoji(@Param("msgId") String msgId, 
                                                    @Param("userId") String userId, 
                                                    @Param("emoji") String emoji);
    
    /**
     * 删除特定表情反应
     */
    @Delete("DELETE FROM cs_message_reaction WHERE msg_id = #{msgId} AND user_id = #{userId} AND reaction_emoji = #{emoji}")
    int deleteByMsgIdAndUserIdAndEmoji(@Param("msgId") String msgId, 
                                      @Param("userId") String userId, 
                                      @Param("emoji") String emoji);
    
    /**
     * 统计消息的表情反应总数
     */
    @Select("SELECT COUNT(*) FROM cs_message_reaction WHERE msg_id = #{msgId}")
    int countByMsgId(@Param("msgId") String msgId);
    
    /**
     * 统计特定表情的反应数量
     */
    @Select("SELECT COUNT(*) FROM cs_message_reaction WHERE msg_id = #{msgId} AND reaction_emoji = #{emoji}")
    int countByMsgIdAndEmoji(@Param("msgId") String msgId, @Param("emoji") String emoji);
    
    /**
     * 删除消息的所有表情反应
     */
    @Delete("DELETE FROM cs_message_reaction WHERE msg_id = #{msgId}")
    int deleteByMsgId(@Param("msgId") String msgId);
}