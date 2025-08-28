package com.leyue.smartcs.chat.serviceimpl;

import com.leyue.smartcs.api.GroupChatService;
import com.leyue.smartcs.api.OfflineMessageService;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.domain.chat.Group;
import com.leyue.smartcs.domain.chat.GroupMember;
import com.leyue.smartcs.domain.chat.gateway.GroupGateway;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.dto.chat.group.*;
import com.leyue.smartcs.dto.chat.ws.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 群聊服务实现
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatServiceImpl implements GroupChatService {

    private final GroupGateway groupGateway;
    private final IdGeneratorGateway idGeneratorGateway;
    private final WebSocketSessionManager sessionManager;
    private final OfflineMessageService offlineMessageService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageDistributionService messageDistributionService;

    @Override
    @Transactional
    public Long createGroup(GroupCreateCmd createCmd) {
        log.info("创建群组: {}", createCmd);
        
        // 生成群组ID
        Long groupId = idGeneratorGateway.generateId();
        
        // 创建群组
        Group group = Group.create(groupId, createCmd.getGroupName(), createCmd.getOwnerId());
        Long id = groupGateway.createGroup(group);
        
        if (id != null) {
            // 添加群主为成员
            GroupMember owner = GroupMember.createOwner(groupId, createCmd.getOwnerId());
            groupGateway.addGroupMember(owner);
            
            // 添加初始成员
            if (createCmd.getMemberIds() != null && !createCmd.getMemberIds().isEmpty()) {
                List<GroupMember> members = createCmd.getMemberIds().stream()
                        .filter(memberId -> !memberId.equals(createCmd.getOwnerId())) // 排除群主
                        .map(memberId -> GroupMember.createMember(groupId, memberId))
                        .collect(Collectors.toList());
                
                if (!members.isEmpty()) {
                    groupGateway.batchAddGroupMembers(members);
                }
            }
            
            return groupId;
        }
        
        return null;
    }

    @Override
    public boolean updateGroup(UpdateGroupCmd updateCmd) {
        log.info("更新群组信息: {}", updateCmd);
        
        // 检查权限
        if (!hasAdminPermission(updateCmd.getGroupId(), updateCmd.getOperatorId())) {
            log.warn("用户无权限更新群组: groupId={}, operatorId={}", 
                    updateCmd.getGroupId(), updateCmd.getOperatorId());
            return false;
        }
        
        Group group = groupGateway.findByGroupId(updateCmd.getGroupId());
        if (group == null) {
            log.warn("群组不存在: groupId={}", updateCmd.getGroupId());
            return false;
        }
        
        group.updateName(updateCmd.getGroupName());
        return groupGateway.updateGroup(group);
    }

    @Override
    @Transactional
    public boolean deleteGroup(Long groupId, Long operatorId) {
        log.info("解散群组: groupId={}, operatorId={}", groupId, operatorId);
        
        Group group = groupGateway.findByGroupId(groupId);
        if (group == null) {
            log.warn("群组不存在: groupId={}", groupId);
            return false;
        }
        
        // 只有群主可以解散群组
        if (!group.isOwner(operatorId)) {
            log.warn("用户无权限解散群组: groupId={}, operatorId={}", groupId, operatorId);
            return false;
        }
        
        return groupGateway.deleteGroup(groupId);
    }

    @Override
    public GroupDto getGroupInfo(Long groupId) {
        log.debug("获取群组信息: groupId={}", groupId);
        
        Group group = groupGateway.findByGroupId(groupId);
        if (group == null) {
            return null;
        }
        
        int memberCount = groupGateway.countGroupMembers(groupId);
        
        return GroupDto.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .ownerId(group.getOwnerId())
                .memberCount(memberCount)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public boolean addGroupMembers(AddGroupMemberCmd addMemberCmd) {
        log.info("添加群成员: {}", addMemberCmd);
        
        // 检查权限
        if (!hasAdminPermission(addMemberCmd.getGroupId(), addMemberCmd.getOperatorId())) {
            log.warn("用户无权限添加群成员: groupId={}, operatorId={}", 
                    addMemberCmd.getGroupId(), addMemberCmd.getOperatorId());
            return false;
        }
        
        // 过滤已存在的成员
        List<Long> newMemberIds = addMemberCmd.getMemberIds().stream()
                .filter(memberId -> !groupGateway.isMember(addMemberCmd.getGroupId(), memberId))
                .collect(Collectors.toList());
        
        if (newMemberIds.isEmpty()) {
            log.info("所有成员都已在群中: groupId={}", addMemberCmd.getGroupId());
            return true;
        }
        
        List<GroupMember> newMembers = newMemberIds.stream()
                .map(memberId -> GroupMember.createMember(addMemberCmd.getGroupId(), memberId))
                .collect(Collectors.toList());
        
        return groupGateway.batchAddGroupMembers(newMembers);
    }

    @Override
    public boolean removeGroupMember(RemoveGroupMemberCmd removeMemberCmd) {
        log.info("移除群成员: {}", removeMemberCmd);
        
        Group group = groupGateway.findByGroupId(removeMemberCmd.getGroupId());
        if (group == null) {
            log.warn("群组不存在: groupId={}", removeMemberCmd.getGroupId());
            return false;
        }
        
        // 不能移除群主
        if (group.isOwner(removeMemberCmd.getMemberId())) {
            log.warn("不能移除群主: groupId={}, memberId={}", 
                    removeMemberCmd.getGroupId(), removeMemberCmd.getMemberId());
            return false;
        }
        
        // 检查权限：群主和管理员可以移除普通成员，成员可以退群
        if (!hasRemovePermission(removeMemberCmd.getGroupId(), 
                removeMemberCmd.getOperatorId(), removeMemberCmd.getMemberId())) {
            log.warn("用户无权限移除群成员: groupId={}, operatorId={}, memberId={}", 
                    removeMemberCmd.getGroupId(), removeMemberCmd.getOperatorId(), removeMemberCmd.getMemberId());
            return false;
        }
        
        return groupGateway.removeGroupMember(removeMemberCmd.getGroupId(), removeMemberCmd.getMemberId());
    }

    @Override
    public List<GroupMemberDto> getGroupMembers(Long groupId) {
        log.debug("获取群成员列表: groupId={}", groupId);
        
        List<GroupMember> members = groupGateway.findGroupMembers(groupId);
        
        return members.stream()
                .map(member -> GroupMemberDto.builder()
                        .groupId(member.getGroupId())
                        .userId(member.getUserId())
                        .role(member.getRole())
                        .roleName(member.getRoleEnum().getDesc())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void sendGroupMessage(GroupMessageCmd groupMessageCmd) {
        log.info("发送群消息: {}", groupMessageCmd);
        
        // 检查用户是否为群成员
        if (!groupGateway.isMember(groupMessageCmd.getGroupId(), groupMessageCmd.getSenderId())) {
            log.warn("用户不是群成员，无法发送消息: groupId={}, senderId={}", 
                    groupMessageCmd.getGroupId(), groupMessageCmd.getSenderId());
            return;
        }
        
        // 获取群成员ID列表
        List<Long> memberIds = groupGateway.findGroupMemberIds(groupMessageCmd.getGroupId());
        
        // 生成消息ID
        String msgId = groupMessageCmd.getMsgId();
        if (msgId == null || msgId.isEmpty()) {
            msgId = idGeneratorGateway.generateIdStr();
        }
        
        // 构建群消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMsgId(msgId);
        chatMessage.setContent(groupMessageCmd.getContent());
        chatMessage.setChatType("GROUP");
        chatMessage.setFromUserId(String.valueOf(groupMessageCmd.getSenderId()));
        chatMessage.setGroupId(groupMessageCmd.getGroupId());
        
        // 通过跨节点分发服务处理群消息投递
        messageDistributionService.publishGroupMessage(chatMessage, memberIds);
        
        // 发布群聊事件用于审计和统计
        messageDistributionService.publishEvent(java.util.Map.of(
                "type", "GROUP_MESSAGE",
                "groupId", groupMessageCmd.getGroupId(),
                "msgId", msgId,
                "senderId", groupMessageCmd.getSenderId(),
                "memberCount", memberIds.size(),
                "timestamp", System.currentTimeMillis()
        ));
    }

    @Override
    public List<GroupDto> getUserCreatedGroups(Long userId) {
        log.debug("获取用户创建的群组: userId={}", userId);
        
        List<Group> groups = groupGateway.findByOwnerId(userId);
        
        return groups.stream()
                .map(group -> GroupDto.builder()
                        .groupId(group.getGroupId())
                        .groupName(group.getGroupName())
                        .ownerId(group.getOwnerId())
                        .memberCount(groupGateway.countGroupMembers(group.getGroupId()))
                        .createdAt(group.getCreatedAt())
                        .updatedAt(group.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupDto> getUserJoinedGroups(Long userId) {
        log.debug("获取用户参与的群组: userId={}", userId);
        
        List<Group> groups = groupGateway.findUserGroups(userId);
        
        return groups.stream()
                .map(group -> GroupDto.builder()
                        .groupId(group.getGroupId())
                        .groupName(group.getGroupName())
                        .ownerId(group.getOwnerId())
                        .memberCount(groupGateway.countGroupMembers(group.getGroupId()))
                        .createdAt(group.getCreatedAt())
                        .updatedAt(group.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isGroupMember(Long groupId, Long userId) {
        return groupGateway.isMember(groupId, userId);
    }
    
    /**
     * 检查用户是否有管理员权限（群主或管理员）
     */
    private boolean hasAdminPermission(Long groupId, Long userId) {
        List<GroupMember> members = groupGateway.findGroupMembers(groupId);
        return members.stream()
                .filter(member -> member.getUserId().equals(userId))
                .anyMatch(GroupMember::isAdminOrOwner);
    }
    
    /**
     * 检查移除权限
     */
    private boolean hasRemovePermission(Long groupId, Long operatorId, Long targetMemberId) {
        // 成员可以退群（移除自己）
        if (operatorId.equals(targetMemberId)) {
            return true;
        }
        
        // 管理员可以移除普通成员
        return hasAdminPermission(groupId, operatorId);
    }
    
    /**
     * 生成消息摘要
     */
    private String generateMessageBrief(String content) {
        if (content == null || content.isEmpty()) {
            return "[空消息]";
        }
        
        int maxLength = 50;
        if (content.length() <= maxLength) {
            return content;
        }
        
        return content.substring(0, maxLength) + "...";
    }
}