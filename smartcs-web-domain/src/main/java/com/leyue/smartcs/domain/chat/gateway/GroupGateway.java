package com.leyue.smartcs.domain.chat.gateway;

import com.leyue.smartcs.domain.chat.Group;
import com.leyue.smartcs.domain.chat.GroupMember;

import java.util.List;

/**
 * 群组网关接口
 * 
 * @author Claude
 */
public interface GroupGateway {

    /**
     * 创建群组
     * 
     * @param group 群组信息
     * @return 群组ID
     */
    Long createGroup(Group group);

    /**
     * 根据群ID获取群组信息
     * 
     * @param groupId 群ID
     * @return 群组信息
     */
    Group findByGroupId(Long groupId);

    /**
     * 更新群组信息
     * 
     * @param group 群组信息
     * @return 操作是否成功
     */
    boolean updateGroup(Group group);

    /**
     * 删除群组
     * 
     * @param groupId 群ID
     * @return 操作是否成功
     */
    boolean deleteGroup(Long groupId);

    /**
     * 获取用户创建的群组列表
     * 
     * @param ownerId 群主ID
     * @return 群组列表
     */
    List<Group> findByOwnerId(Long ownerId);

    /**
     * 添加群成员
     * 
     * @param groupMember 群成员信息
     * @return 操作是否成功
     */
    boolean addGroupMember(GroupMember groupMember);

    /**
     * 批量添加群成员
     * 
     * @param groupMembers 群成员列表
     * @return 操作是否成功
     */
    boolean batchAddGroupMembers(List<GroupMember> groupMembers);

    /**
     * 移除群成员
     * 
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 操作是否成功
     */
    boolean removeGroupMember(Long groupId, Long userId);

    /**
     * 更新群成员角色
     * 
     * @param groupId 群ID
     * @param userId 用户ID
     * @param role 新角色
     * @return 操作是否成功
     */
    boolean updateMemberRole(Long groupId, Long userId, String role);

    /**
     * 获取群成员列表
     * 
     * @param groupId 群ID
     * @return 群成员列表
     */
    List<GroupMember> findGroupMembers(Long groupId);

    /**
     * 获取群成员ID列表
     * 
     * @param groupId 群ID
     * @return 成员ID列表
     */
    List<Long> findGroupMemberIds(Long groupId);

    /**
     * 检查用户是否为群成员
     * 
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 是否为群成员
     */
    boolean isMember(Long groupId, Long userId);

    /**
     * 获取用户参与的群组列表
     * 
     * @param userId 用户ID
     * @return 群组列表
     */
    List<Group> findUserGroups(Long userId);

    /**
     * 获取群成员数量
     * 
     * @param groupId 群ID
     * @return 成员数量
     */
    int countGroupMembers(Long groupId);
}