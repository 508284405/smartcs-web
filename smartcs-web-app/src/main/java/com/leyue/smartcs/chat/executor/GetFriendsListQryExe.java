package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.chat.Friend;
import com.leyue.smartcs.domain.chat.gateway.FriendGateway;
import com.leyue.smartcs.dto.chat.friend.FriendDTO;
import com.leyue.smartcs.dto.chat.friend.SearchFriendQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 获取好友列表查询执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetFriendsListQryExe {
    
    private final FriendGateway friendGateway;
    
    public MultiResponse<FriendDTO> execute(SearchFriendQry qry) {
        log.info("查询好友列表: userId={}, keyword={}", qry.getUserId(), qry.getKeyword());
        
        try {
            List<Friend> friends;
            
            if (qry.getKeyword() != null && !qry.getKeyword().trim().isEmpty()) {
                // 关键词搜索
                friends = friendGateway.searchFriends(qry.getUserId(), qry.getKeyword().trim());
            } else if (qry.getFriendGroup() != null && !qry.getFriendGroup().trim().isEmpty()) {
                // 分组查询
                friends = friendGateway.findFriendsByGroup(qry.getUserId(), qry.getFriendGroup());
            } else {
                // 查询所有好友
                friends = friendGateway.findFriendsByUserId(qry.getUserId());
            }
            
            // 转换为DTO
            List<FriendDTO> friendDTOs = friends.stream()
                    .map(friend -> convertToDTO(friend, qry.getUserId()))
                    .collect(Collectors.toList());
            
            log.info("查询好友列表完成: userId={}, count={}", qry.getUserId(), friendDTOs.size());
            
            return MultiResponse.of(friendDTOs);
            
        } catch (Exception e) {
            log.error("查询好友列表异常", e);
            return MultiResponse.buildFailure("FRIENDS_QUERY_ERROR", "查询好友列表失败");
        }
    }
    
    private FriendDTO convertToDTO(Friend friend, String currentUserId) {
        // 确定好友的用户ID（非当前用户的那个）
        String friendUserId = friend.getOtherUserId(currentUserId);
        
        FriendDTO dto = FriendDTO.builder()
                .id(friend.getId())
                .friendUserId(friendUserId)
                .remarkName(friend.getRemarkName())
                .friendGroup(friend.getFriendGroup())
                .status(friend.getStatus())
                .statusText(friend.getStatusText())
                .appliedAt(friend.getAppliedAt())
                .processedAt(friend.getProcessedAt())
                .createdAt(friend.getCreatedAt())
                .updatedAt(friend.getUpdatedAt())
                .displayName(friend.getDisplayName())
                .canChat(friend.canChat())
                .build();
        
        // TODO: 从用户服务获取好友的详细信息（用户名、头像等）
        // UserInfo userInfo = userService.getUserInfo(friendUserId);
        // dto.setFriendUserName(userInfo.getUserName());
        // dto.setFriendAvatar(userInfo.getAvatar());
        
        // TODO: 从在线状态服务获取好友的在线状态
        // OnlineStatus onlineStatus = userStatusService.getUserStatus(friendUserId);
        // dto.setOnlineStatus(onlineStatus.getStatus());
        // dto.setLastSeenAt(onlineStatus.getLastSeenAt());
        
        // TODO: 从消息服务获取未读消息数和最后消息
        // UnreadInfo unreadInfo = messageService.getUnreadInfo(currentUserId, friendUserId);
        // dto.setUnreadCount(unreadInfo.getUnreadCount());
        // dto.setLastMessage(unreadInfo.getLastMessage());
        // dto.setLastMessageTime(unreadInfo.getLastMessageTime());
        
        return dto;
    }
}