package com.leyue.smartcs.web.group;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.GroupChatService;
import com.leyue.smartcs.dto.chat.group.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群聊管理控制器
 * 
 * @author Claude
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/im/groups")
public class GroupChatController {
    
    private final GroupChatService groupChatService;

    /**
     * 创建群组
     *
     * @param createCmd 创建群组命令
     * @return 群组ID
     */
    @PostMapping
    public SingleResponse<Long> createGroup(@RequestBody GroupCreateCmd createCmd) {
        log.info("创建群组: {}", createCmd);
        
        try {
            Long groupId = groupChatService.createGroup(createCmd);
            if (groupId != null) {
                return SingleResponse.of(groupId);
            } else {
                return SingleResponse.buildFailure("GROUP_001", "创建群组失败");
            }
        } catch (Exception e) {
            log.error("创建群组失败: {}", createCmd, e);
            return SingleResponse.buildFailure("GROUP_001", "创建群组失败");
        }
    }

    /**
     * 更新群组信息
     *
     * @param groupId 群组ID
     * @param updateCmd 更新群组命令
     * @return 操作结果
     */
    @PutMapping("/{groupId}")
    public Response updateGroup(@PathVariable Long groupId, @RequestBody UpdateGroupCmd updateCmd) {
        updateCmd.setGroupId(groupId);
        log.info("更新群组信息: {}", updateCmd);
        
        try {
            boolean success = groupChatService.updateGroup(updateCmd);
            if (success) {
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("GROUP_002", "更新群组信息失败");
            }
        } catch (Exception e) {
            log.error("更新群组信息失败: {}", updateCmd, e);
            return Response.buildFailure("GROUP_002", "更新群组信息失败");
        }
    }

    /**
     * 解散群组
     *
     * @param groupId 群组ID
     * @param operatorId 操作者ID
     * @return 操作结果
     */
    @DeleteMapping("/{groupId}")
    public Response deleteGroup(@PathVariable Long groupId, @RequestParam Long operatorId) {
        log.info("解散群组: groupId={}, operatorId={}", groupId, operatorId);
        
        try {
            boolean success = groupChatService.deleteGroup(groupId, operatorId);
            if (success) {
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("GROUP_003", "解散群组失败");
            }
        } catch (Exception e) {
            log.error("解散群组失败: groupId={}, operatorId={}", groupId, operatorId, e);
            return Response.buildFailure("GROUP_003", "解散群组失败");
        }
    }

    /**
     * 获取群组信息
     *
     * @param groupId 群组ID
     * @return 群组信息
     */
    @GetMapping("/{groupId}")
    public SingleResponse<GroupDto> getGroupInfo(@PathVariable Long groupId) {
        log.debug("获取群组信息: groupId={}", groupId);
        
        try {
            GroupDto groupInfo = groupChatService.getGroupInfo(groupId);
            if (groupInfo != null) {
                return SingleResponse.of(groupInfo);
            } else {
                return SingleResponse.buildFailure("GROUP_004", "群组不存在");
            }
        } catch (Exception e) {
            log.error("获取群组信息失败: groupId={}", groupId, e);
            return SingleResponse.buildFailure("GROUP_004", "获取群组信息失败");
        }
    }

    /**
     * 添加群成员
     *
     * @param groupId 群组ID
     * @param addMemberCmd 添加成员命令
     * @return 操作结果
     */
    @PostMapping("/{groupId}/members")
    public Response addGroupMembers(@PathVariable Long groupId, @RequestBody AddGroupMemberCmd addMemberCmd) {
        addMemberCmd.setGroupId(groupId);
        log.info("添加群成员: {}", addMemberCmd);
        
        try {
            boolean success = groupChatService.addGroupMembers(addMemberCmd);
            if (success) {
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("GROUP_005", "添加群成员失败");
            }
        } catch (Exception e) {
            log.error("添加群成员失败: {}", addMemberCmd, e);
            return Response.buildFailure("GROUP_005", "添加群成员失败");
        }
    }

    /**
     * 移除群成员
     *
     * @param groupId 群组ID
     * @param memberId 成员ID
     * @param operatorId 操作者ID
     * @return 操作结果
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public Response removeGroupMember(@PathVariable Long groupId, 
                                    @PathVariable Long memberId,
                                    @RequestParam Long operatorId) {
        
        RemoveGroupMemberCmd removeCmd = RemoveGroupMemberCmd.builder()
                .groupId(groupId)
                .memberId(memberId)
                .operatorId(operatorId)
                .build();
        
        log.info("移除群成员: {}", removeCmd);
        
        try {
            boolean success = groupChatService.removeGroupMember(removeCmd);
            if (success) {
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("GROUP_006", "移除群成员失败");
            }
        } catch (Exception e) {
            log.error("移除群成员失败: {}", removeCmd, e);
            return Response.buildFailure("GROUP_006", "移除群成员失败");
        }
    }

    /**
     * 获取群成员列表
     *
     * @param groupId 群组ID
     * @return 群成员列表
     */
    @GetMapping("/{groupId}/members")
    public MultiResponse<GroupMemberDto> getGroupMembers(@PathVariable Long groupId) {
        log.debug("获取群成员列表: groupId={}", groupId);
        
        try {
            List<GroupMemberDto> members = groupChatService.getGroupMembers(groupId);
            return MultiResponse.of(members);
        } catch (Exception e) {
            log.error("获取群成员列表失败: groupId={}", groupId, e);
            return MultiResponse.buildFailure("GROUP_007", "获取群成员列表失败");
        }
    }

    /**
     * 发送群消息
     *
     * @param groupId 群组ID
     * @param messageCmd 群消息命令
     * @return 操作结果
     */
    @PostMapping("/{groupId}/messages")
    public Response sendGroupMessage(@PathVariable Long groupId, @RequestBody GroupMessageCmd messageCmd) {
        messageCmd.setGroupId(groupId);
        log.info("发送群消息: {}", messageCmd);
        
        try {
            groupChatService.sendGroupMessage(messageCmd);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("发送群消息失败: {}", messageCmd, e);
            return Response.buildFailure("GROUP_008", "发送群消息失败");
        }
    }

    /**
     * 获取用户创建的群组列表
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    @GetMapping("/created")
    public MultiResponse<GroupDto> getUserCreatedGroups(@RequestParam Long userId) {
        log.debug("获取用户创建的群组: userId={}", userId);
        
        try {
            List<GroupDto> groups = groupChatService.getUserCreatedGroups(userId);
            return MultiResponse.of(groups);
        } catch (Exception e) {
            log.error("获取用户创建的群组失败: userId={}", userId, e);
            return MultiResponse.buildFailure("GROUP_009", "获取用户创建的群组失败");
        }
    }

    /**
     * 获取用户参与的群组列表
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    @GetMapping("/joined")
    public MultiResponse<GroupDto> getUserJoinedGroups(@RequestParam Long userId) {
        log.debug("获取用户参与的群组: userId={}", userId);
        
        try {
            List<GroupDto> groups = groupChatService.getUserJoinedGroups(userId);
            return MultiResponse.of(groups);
        } catch (Exception e) {
            log.error("获取用户参与的群组失败: userId={}", userId, e);
            return MultiResponse.buildFailure("GROUP_010", "获取用户参与的群组失败");
        }
    }

    /**
     * 检查用户是否为群成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为群成员
     */
    @GetMapping("/{groupId}/members/{userId}/exists")
    public SingleResponse<Boolean> isGroupMember(@PathVariable Long groupId, @PathVariable Long userId) {
        log.debug("检查群成员: groupId={}, userId={}", groupId, userId);
        
        try {
            boolean isMember = groupChatService.isGroupMember(groupId, userId);
            return SingleResponse.of(isMember);
        } catch (Exception e) {
            log.error("检查群成员失败: groupId={}, userId={}", groupId, userId, e);
            return SingleResponse.buildFailure("GROUP_011", "检查群成员失败");
        }
    }
}