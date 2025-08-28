package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.UnreadCounterDO;
import com.leyue.smartcs.domain.chat.UnreadCounter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 未读计数数据转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface UnreadCounterConvertor {
    
    /**
     * 领域模型转数据对象
     *
     * @param unreadCounter 未读计数领域模型
     * @return 未读计数数据对象
     */
    UnreadCounterDO toDataObject(UnreadCounter unreadCounter);
    
    /**
     * 数据对象转领域模型
     *
     * @param unreadCounterDO 未读计数数据对象
     * @return 未读计数领域模型
     */
    UnreadCounter toDomain(UnreadCounterDO unreadCounterDO);
    
    /**
     * 领域模型列表转数据对象列表
     *
     * @param unreadCounters 未读计数领域模型列表
     * @return 未读计数数据对象列表
     */
    List<UnreadCounterDO> toDataObjects(List<UnreadCounter> unreadCounters);
    
    /**
     * 数据对象列表转领域模型列表
     *
     * @param unreadCounterDOs 未读计数数据对象列表
     * @return 未读计数领域模型列表
     */
    List<UnreadCounter> toDomains(List<UnreadCounterDO> unreadCounterDOs);
    
    /**
     * 更新存在的未读计数对象
     *
     * @param source 源对象
     * @param target 目标对象
     */
    void updateDataObject(UnreadCounter source, @MappingTarget UnreadCounterDO target);
}