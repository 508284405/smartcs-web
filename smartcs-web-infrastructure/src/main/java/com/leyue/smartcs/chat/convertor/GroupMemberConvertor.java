package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.GroupMemberDO;
import com.leyue.smartcs.domain.chat.GroupMember;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 群成员数据转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface GroupMemberConvertor {
    
    /**
     * 领域模型转数据对象
     *
     * @param groupMember 群成员领域模型
     * @return 群成员数据对象
     */
    GroupMemberDO toDataObject(GroupMember groupMember);
    
    /**
     * 数据对象转领域模型
     *
     * @param groupMemberDO 群成员数据对象
     * @return 群成员领域模型
     */
    GroupMember toDomain(GroupMemberDO groupMemberDO);
    
    /**
     * 领域模型列表转数据对象列表
     *
     * @param groupMembers 群成员领域模型列表
     * @return 群成员数据对象列表
     */
    List<GroupMemberDO> toDataObjects(List<GroupMember> groupMembers);
    
    /**
     * 数据对象列表转领域模型列表
     *
     * @param groupMemberDOs 群成员数据对象列表
     * @return 群成员领域模型列表
     */
    List<GroupMember> toDomains(List<GroupMemberDO> groupMemberDOs);
    
    /**
     * 更新存在的群成员对象
     *
     * @param source 源对象
     * @param target 目标对象
     */
    void updateDataObject(GroupMember source, @MappingTarget GroupMemberDO target);
}