package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.GroupDO;
import com.leyue.smartcs.domain.chat.Group;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 群组数据转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface GroupConvertor {
    
    /**
     * 领域模型转数据对象
     *
     * @param group 群组领域模型
     * @return 群组数据对象
     */
    GroupDO toDataObject(Group group);
    
    /**
     * 数据对象转领域模型
     *
     * @param groupDO 群组数据对象
     * @return 群组领域模型
     */
    Group toDomain(GroupDO groupDO);
    
    /**
     * 领域模型列表转数据对象列表
     *
     * @param groups 群组领域模型列表
     * @return 群组数据对象列表
     */
    List<GroupDO> toDataObjects(List<Group> groups);
    
    /**
     * 数据对象列表转领域模型列表
     *
     * @param groupDOs 群组数据对象列表
     * @return 群组领域模型列表
     */
    List<Group> toDomains(List<GroupDO> groupDOs);
    
    /**
     * 更新存在的群组对象
     *
     * @param source 源对象
     * @param target 目标对象
     */
    void updateDataObject(Group source, @MappingTarget GroupDO target);
}