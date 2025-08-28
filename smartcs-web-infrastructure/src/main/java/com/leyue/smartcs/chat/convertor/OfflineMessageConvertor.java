package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.OfflineMessageDO;
import com.leyue.smartcs.domain.chat.OfflineMessage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 离线消息数据转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface OfflineMessageConvertor {
    
    /**
     * 领域模型转数据对象
     *
     * @param offlineMessage 离线消息领域模型
     * @return 离线消息数据对象
     */
    OfflineMessageDO toDataObject(OfflineMessage offlineMessage);
    
    /**
     * 数据对象转领域模型
     *
     * @param offlineMessageDO 离线消息数据对象
     * @return 离线消息领域模型
     */
    OfflineMessage toDomain(OfflineMessageDO offlineMessageDO);
    
    /**
     * 领域模型列表转数据对象列表
     *
     * @param offlineMessages 离线消息领域模型列表
     * @return 离线消息数据对象列表
     */
    List<OfflineMessageDO> toDataObjects(List<OfflineMessage> offlineMessages);
    
    /**
     * 数据对象列表转领域模型列表
     *
     * @param offlineMessageDOs 离线消息数据对象列表
     * @return 离线消息领域模型列表
     */
    List<OfflineMessage> toDomains(List<OfflineMessageDO> offlineMessageDOs);
    
    /**
     * 更新存在的离线消息对象
     *
     * @param source 源对象
     * @param target 目标对象
     */
    void updateDataObject(OfflineMessage source, @MappingTarget OfflineMessageDO target);
}