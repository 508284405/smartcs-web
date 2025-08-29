package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.FriendDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友关系Mapper
 */
@Mapper
public interface FriendMapper extends BaseMapper<FriendDO> {
    
    /**
     * 根据用户查找好友关系
     */
    @Select("SELECT * FROM cs_friend WHERE (from_user_id = #{fromUserId} AND to_user_id = #{toUserId}) " +
            "OR (from_user_id = #{toUserId} AND to_user_id = #{fromUserId}) LIMIT 1")
    FriendDO findByUsers(@Param("fromUserId") String fromUserId, @Param("toUserId") String toUserId);
    
    /**
     * 查找用户的所有好友
     */
    @Select("SELECT * FROM cs_friend WHERE (from_user_id = #{userId} OR to_user_id = #{userId}) " +
            "AND status = 1 ORDER BY updated_at DESC")
    List<FriendDO> findFriendsByUserId(@Param("userId") String userId);
    
    /**
     * 查找用户接收到的好友申请
     */
    @Select("SELECT * FROM cs_friend WHERE to_user_id = #{userId} AND status = 0 " +
            "ORDER BY applied_at DESC")
    List<FriendDO> findApplicationsReceived(@Param("userId") String userId);
    
    /**
     * 查找用户发送的好友申请
     */
    @Select("SELECT * FROM cs_friend WHERE from_user_id = #{userId} AND status = 0 " +
            "ORDER BY applied_at DESC")
    List<FriendDO> findApplicationsSent(@Param("userId") String userId);
    
    /**
     * 根据状态查找好友关系
     */
    @Select("SELECT * FROM cs_friend WHERE (from_user_id = #{userId} OR to_user_id = #{userId}) " +
            "AND status = #{status} ORDER BY updated_at DESC")
    List<FriendDO> findByStatus(@Param("userId") String userId, @Param("status") Integer status);
    
    /**
     * 根据分组查找好友
     */
    @Select("SELECT * FROM cs_friend WHERE (from_user_id = #{userId} OR to_user_id = #{userId}) " +
            "AND status = 1 AND friend_group = #{friendGroup} ORDER BY updated_at DESC")
    List<FriendDO> findByGroup(@Param("userId") String userId, @Param("friendGroup") String friendGroup);
    
    /**
     * 搜索好友（按备注名或用户ID）
     */
    @Select("<script>" +
            "SELECT * FROM cs_friend WHERE (from_user_id = #{userId} OR to_user_id = #{userId}) " +
            "AND status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (remark_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR from_user_id LIKE CONCAT('%', #{keyword}, '%') " +
            "OR to_user_id LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY updated_at DESC" +
            "</script>")
    List<FriendDO> searchFriends(@Param("userId") String userId, @Param("keyword") String keyword);
    
    /**
     * 检查是否为好友关系
     */
    @Select("SELECT COUNT(*) FROM cs_friend WHERE ((from_user_id = #{fromUserId} AND to_user_id = #{toUserId}) " +
            "OR (from_user_id = #{toUserId} AND to_user_id = #{fromUserId})) AND status = 1")
    int checkIsFriend(@Param("fromUserId") String fromUserId, @Param("toUserId") String toUserId);
    
    /**
     * 检查是否存在申请
     */
    @Select("SELECT COUNT(*) FROM cs_friend WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} " +
            "AND status = 0")
    int checkHasApplication(@Param("fromUserId") String fromUserId, @Param("toUserId") String toUserId);
    
    /**
     * 统计好友数量
     */
    @Select("SELECT COUNT(*) FROM cs_friend WHERE (from_user_id = #{userId} OR to_user_id = #{userId}) " +
            "AND status = 1")
    long countFriends(@Param("userId") String userId);
    
    /**
     * 统计待处理申请数量
     */
    @Select("SELECT COUNT(*) FROM cs_friend WHERE to_user_id = #{userId} AND status = 0")
    long countPendingApplications(@Param("userId") String userId);
    
    /**
     * 删除过期申请（超过7天未处理）
     */
    @Select("DELETE FROM cs_friend WHERE status = 0 AND applied_at < (UNIX_TIMESTAMP() * 1000 - 7 * 24 * 60 * 60 * 1000)")
    int deleteExpiredApplications();
}