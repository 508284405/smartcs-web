package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.FriendDO;
import com.leyue.smartcs.domain.chat.Friend;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 好友转换器
 */
@Mapper
public interface FriendConvertor {
    
    FriendConvertor INSTANCE = Mappers.getMapper(FriendConvertor.class);
    
    /**
     * DO转领域对象
     */
    Friend toDomain(FriendDO friendDO);
    
    /**
     * 领域对象转DO
     */
    FriendDO toDO(Friend friend);
    
    /**
     * DO列表转领域对象列表
     */
    List<Friend> toDomainList(List<FriendDO> friendDOList);
    
    /**
     * 领域对象列表转DO列表
     */
    List<FriendDO> toDOList(List<Friend> friendList);
}