package com.leyue.smartcs.chat;

import com.leyue.smartcs.chat.convertor.GroupConvertor;
import com.leyue.smartcs.chat.convertor.GroupMemberConvertor;
import com.leyue.smartcs.chat.dataobject.GroupDO;
import com.leyue.smartcs.chat.dataobject.GroupMemberDO;
import com.leyue.smartcs.chat.mapper.GroupMapper;
import com.leyue.smartcs.chat.mapper.GroupMemberMapper;
import com.leyue.smartcs.domain.chat.Group;
import com.leyue.smartcs.domain.chat.GroupMember;
import com.leyue.smartcs.domain.chat.gateway.GroupGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 群组网关接口实现
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupGatewayImpl implements GroupGateway {
    
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupConvertor groupConvertor;
    private final GroupMemberConvertor groupMemberConvertor;
    
    @Override
    @Transactional
    public Long createGroup(Group group) {
        log.info("创建群组: {}", group);
        
        GroupDO groupDO = groupConvertor.toDataObject(group);
        groupMapper.insert(groupDO);
        return groupDO.getId();
    }
    
    @Override
    public Group findByGroupId(Long groupId) {
        log.debug("查询群组信息: groupId={}", groupId);
        
        GroupDO groupDO = groupMapper.findByGroupId(groupId);
        return groupDO != null ? groupConvertor.toDomain(groupDO) : null;
    }
    
    @Override
    public boolean updateGroup(Group group) {
        log.info("更新群组信息: {}", group);
        
        try {
            GroupDO groupDO = groupConvertor.toDataObject(group);
            return groupMapper.updateById(groupDO) > 0;
        } catch (Exception e) {
            log.error("更新群组信息失败: {}", group, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteGroup(Long groupId) {
        log.info("删除群组: groupId={}", groupId);
        
        try {
            // 软删除群组
            int deletedRows = groupMapper.deleteByGroupId(groupId);
            
            // 软删除所有群成员
            if (deletedRows > 0) {
                List<GroupMemberDO> members = groupMemberMapper.findByGroupId(groupId);
                for (GroupMemberDO member : members) {
                    groupMemberMapper.removeMember(groupId, member.getUserId());
                }
            }
            
            return deletedRows > 0;
        } catch (Exception e) {
            log.error("删除群组失败: groupId={}", groupId, e);
            return false;
        }
    }
    
    @Override
    public List<Group> findByOwnerId(Long ownerId) {
        log.debug("查询用户创建的群组: ownerId={}", ownerId);
        
        List<GroupDO> groupDOs = groupMapper.findByOwnerId(ownerId);
        return groupConvertor.toDomains(groupDOs);
    }
    
    @Override
    public boolean addGroupMember(GroupMember groupMember) {
        log.info("添加群成员: {}", groupMember);
        
        try {
            GroupMemberDO groupMemberDO = groupMemberConvertor.toDataObject(groupMember);
            return groupMemberMapper.insert(groupMemberDO) > 0;
        } catch (Exception e) {
            log.error("添加群成员失败: {}", groupMember, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean batchAddGroupMembers(List<GroupMember> groupMembers) {
        log.info("批量添加群成员: {} 个", groupMembers.size());
        
        if (groupMembers == null || groupMembers.isEmpty()) {
            return true;
        }
        
        try {
            List<GroupMemberDO> groupMemberDOs = groupMemberConvertor.toDataObjects(groupMembers);
            return groupMemberMapper.batchInsert(groupMemberDOs) > 0;
        } catch (Exception e) {
            log.error("批量添加群成员失败: {}", groupMembers, e);
            return false;
        }
    }
    
    @Override
    public boolean removeGroupMember(Long groupId, Long userId) {
        log.info("移除群成员: groupId={}, userId={}", groupId, userId);
        
        return groupMemberMapper.removeMember(groupId, userId) > 0;
    }
    
    @Override
    public boolean updateMemberRole(Long groupId, Long userId, String role) {
        log.info("更新群成员角色: groupId={}, userId={}, role={}", groupId, userId, role);
        
        return groupMemberMapper.updateMemberRole(groupId, userId, role) > 0;
    }
    
    @Override
    public List<GroupMember> findGroupMembers(Long groupId) {
        log.debug("查询群成员列表: groupId={}", groupId);
        
        List<GroupMemberDO> groupMemberDOs = groupMemberMapper.findByGroupId(groupId);
        return groupMemberConvertor.toDomains(groupMemberDOs);
    }
    
    @Override
    public List<Long> findGroupMemberIds(Long groupId) {
        log.debug("查询群成员ID列表: groupId={}", groupId);
        
        return groupMemberMapper.findMemberIdsByGroupId(groupId);
    }
    
    @Override
    public boolean isMember(Long groupId, Long userId) {
        log.debug("检查是否为群成员: groupId={}, userId={}", groupId, userId);
        
        GroupMemberDO memberDO = groupMemberMapper.findByGroupIdAndUserId(groupId, userId);
        return memberDO != null && (memberDO.getIsDeleted() == null || memberDO.getIsDeleted() == 0);
    }
    
    @Override
    public List<Group> findUserGroups(Long userId) {
        log.debug("查询用户参与的群组: userId={}", userId);
        
        List<GroupDO> groupDOs = groupMapper.findUserGroups(userId);
        return groupConvertor.toDomains(groupDOs);
    }
    
    @Override
    public int countGroupMembers(Long groupId) {
        log.debug("统计群成员数量: groupId={}", groupId);
        
        return groupMemberMapper.countByGroupId(groupId);
    }
}