package com.leyue.smartcs.domain.chat.gateway;

import com.leyue.smartcs.domain.chat.Friend;
import java.util.List;

/**
 * 好友网关接口
 */
public interface FriendGateway {
    
    /**
     * 保存好友关系
     */
    Friend save(Friend friend);
    
    /**
     * 根据ID查找好友关系
     */
    Friend findById(Long id);
    
    /**
     * 根据用户ID查找好友关系
     */
    Friend findByUsers(String fromUserId, String toUserId);
    
    /**
     * 查找用户的所有好友
     */
    List<Friend> findFriendsByUserId(String userId);
    
    /**
     * 查找用户的好友申请列表（接收到的）
     */
    List<Friend> findFriendApplicationsReceived(String userId);
    
    /**
     * 查找用户发送的好友申请列表
     */
    List<Friend> findFriendApplicationsSent(String userId);
    
    /**
     * 根据状态查找好友关系
     */
    List<Friend> findFriendsByStatus(String userId, Integer status);
    
    /**
     * 根据分组查找好友
     */
    List<Friend> findFriendsByGroup(String userId, String friendGroup);
    
    /**
     * 搜索好友
     */
    List<Friend> searchFriends(String userId, String keyword);
    
    /**
     * 检查是否为好友关系
     */
    boolean isFriend(String fromUserId, String toUserId);
    
    /**
     * 检查是否存在好友申请
     */
    boolean hasApplication(String fromUserId, String toUserId);
    
    /**
     * 删除好友关系
     */
    boolean deleteById(Long id);
    
    /**
     * 删除用户间的好友关系
     */
    boolean deleteByUsers(String fromUserId, String toUserId);
    
    /**
     * 统计用户好友数量
     */
    long countFriends(String userId);
    
    /**
     * 统计待处理的好友申请数量
     */
    long countPendingApplications(String userId);
    
    /**
     * 批量删除过期的好友申请
     */
    int deleteExpiredApplications();
}