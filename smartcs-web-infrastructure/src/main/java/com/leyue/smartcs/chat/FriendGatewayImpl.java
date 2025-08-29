package com.leyue.smartcs.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.chat.convertor.FriendConvertor;
import com.leyue.smartcs.chat.dataobject.FriendDO;
import com.leyue.smartcs.chat.mapper.FriendMapper;
import com.leyue.smartcs.domain.chat.Friend;
import com.leyue.smartcs.domain.chat.gateway.FriendGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友网关实现
 */
@Component
@RequiredArgsConstructor
public class FriendGatewayImpl implements FriendGateway {
    
    private final FriendMapper friendMapper;
    private final FriendConvertor friendConvertor = FriendConvertor.INSTANCE;
    
    @Override
    public Friend save(Friend friend) {
        FriendDO friendDO = friendConvertor.toDO(friend);
        
        if (friendDO.getId() == null) {
            friendMapper.insert(friendDO);
        } else {
            friendMapper.updateById(friendDO);
        }
        
        return friendConvertor.toDomain(friendDO);
    }
    
    @Override
    public Friend findById(Long id) {
        FriendDO friendDO = friendMapper.selectById(id);
        return friendDO != null ? friendConvertor.toDomain(friendDO) : null;
    }
    
    @Override
    public Friend findByUsers(String fromUserId, String toUserId) {
        FriendDO friendDO = friendMapper.findByUsers(fromUserId, toUserId);
        return friendDO != null ? friendConvertor.toDomain(friendDO) : null;
    }
    
    @Override
    public List<Friend> findFriendsByUserId(String userId) {
        List<FriendDO> friendDOList = friendMapper.findFriendsByUserId(userId);
        return friendConvertor.toDomainList(friendDOList);
    }
    
    @Override
    public List<Friend> findFriendApplicationsReceived(String userId) {
        List<FriendDO> friendDOList = friendMapper.findApplicationsReceived(userId);
        return friendConvertor.toDomainList(friendDOList);
    }
    
    @Override
    public List<Friend> findFriendApplicationsSent(String userId) {
        List<FriendDO> friendDOList = friendMapper.findApplicationsSent(userId);
        return friendConvertor.toDomainList(friendDOList);
    }
    
    @Override
    public List<Friend> findFriendsByStatus(String userId, Integer status) {
        List<FriendDO> friendDOList = friendMapper.findByStatus(userId, status);
        return friendConvertor.toDomainList(friendDOList);
    }
    
    @Override
    public List<Friend> findFriendsByGroup(String userId, String friendGroup) {
        List<FriendDO> friendDOList = friendMapper.findByGroup(userId, friendGroup);
        return friendConvertor.toDomainList(friendDOList);
    }
    
    @Override
    public List<Friend> searchFriends(String userId, String keyword) {
        List<FriendDO> friendDOList = friendMapper.searchFriends(userId, keyword);
        return friendConvertor.toDomainList(friendDOList);
    }
    
    @Override
    public boolean isFriend(String fromUserId, String toUserId) {
        int count = friendMapper.checkIsFriend(fromUserId, toUserId);
        return count > 0;
    }
    
    @Override
    public boolean hasApplication(String fromUserId, String toUserId) {
        int count = friendMapper.checkHasApplication(fromUserId, toUserId);
        return count > 0;
    }
    
    @Override
    public boolean deleteById(Long id) {
        int affected = friendMapper.deleteById(id);
        return affected > 0;
    }
    
    @Override
    public boolean deleteByUsers(String fromUserId, String toUserId) {
        LambdaQueryWrapper<FriendDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(FriendDO::getFromUserId, fromUserId).eq(FriendDO::getToUserId, toUserId))
               .or(w -> w.eq(FriendDO::getFromUserId, toUserId).eq(FriendDO::getToUserId, fromUserId));
        
        int affected = friendMapper.delete(wrapper);
        return affected > 0;
    }
    
    @Override
    public long countFriends(String userId) {
        return friendMapper.countFriends(userId);
    }
    
    @Override
    public long countPendingApplications(String userId) {
        return friendMapper.countPendingApplications(userId);
    }
    
    @Override
    public int deleteExpiredApplications() {
        return friendMapper.deleteExpiredApplications();
    }
}