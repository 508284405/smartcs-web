package com.leyue.smartcs.api;

import com.leyue.smartcs.dto.chat.group.GroupCreateCmd;
import com.leyue.smartcs.dto.chat.group.GroupDto;
import com.leyue.smartcs.dto.chat.group.GroupMemberDto;
import com.leyue.smartcs.dto.chat.group.GroupMessageCmd;
import com.leyue.smartcs.dto.chat.group.AddGroupMemberCmd;
import com.leyue.smartcs.dto.chat.group.RemoveGroupMemberCmd;
import com.leyue.smartcs.dto.chat.group.UpdateGroupCmd;

import java.util.List;

/**
 * 群聊服务接口
 * 
 * @author Claude
 */
public interface GroupChatService {

    /**
     * 创建群组
     * 
     * @param createCmd 创建群组命令
     * @return 群组ID
     */
    Long createGroup(GroupCreateCmd createCmd);

    /**
     * 更新群组信息
     * 
     * @param updateCmd 更新群组命令
     * @return 操作是否成功
     */
    boolean updateGroup(UpdateGroupCmd updateCmd);

    /**
     * 解散群组
     * 
     * @param groupId 群组ID
     * @param operatorId 操作者ID
     * @return 操作是否成功
     */
    boolean deleteGroup(Long groupId, Long operatorId);

    /**
     * 获取群组信息
     * 
     * @param groupId 群组ID
     * @return 群组信息
     */
    GroupDto getGroupInfo(Long groupId);

    /**
     * 添加群成员
     * 
     * @param addMemberCmd 添加成员命令
     * @return 操作是否成功
     */
    boolean addGroupMembers(AddGroupMemberCmd addMemberCmd);

    /**
     * 移除群成员
     * 
     * @param removeMemberCmd 移除成员命令
     * @return 操作是否成功
     */
    boolean removeGroupMember(RemoveGroupMemberCmd removeMemberCmd);

    /**
     * 获取群成员列表
     * 
     * @param groupId 群组ID
     * @return 群成员列表
     */
    List<GroupMemberDto> getGroupMembers(Long groupId);

    /**
     * 发送群消息
     * 
     * @param groupMessageCmd 群消息命令
     */
    void sendGroupMessage(GroupMessageCmd groupMessageCmd);

    /**
     * 获取用户创建的群组列表
     * 
     * @param userId 用户ID
     * @return 群组列表
     */
    List<GroupDto> getUserCreatedGroups(Long userId);

    /**
     * 获取用户参与的群组列表
     * 
     * @param userId 用户ID
     * @return 群组列表
     */
    List<GroupDto> getUserJoinedGroups(Long userId);

    /**
     * 检查用户是否为群成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为群成员
     */
    boolean isGroupMember(Long groupId, Long userId);
}