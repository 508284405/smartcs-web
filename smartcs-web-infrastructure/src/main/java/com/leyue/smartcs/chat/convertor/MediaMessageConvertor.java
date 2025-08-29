package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.chat.dataobject.MediaMessageDO;
import com.leyue.smartcs.domain.chat.MediaMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 多媒体消息转换器
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Mapper
public interface MediaMessageConvertor {
    
    MediaMessageConvertor INSTANCE = Mappers.getMapper(MediaMessageConvertor.class);
    
    /**
     * DO转换为领域对象
     * 
     * @param dataObject 数据对象
     * @return 领域对象
     */
    MediaMessage toDomain(MediaMessageDO dataObject);
    
    /**
     * 领域对象转换为DO
     * 
     * @param domain 领域对象
     * @return 数据对象
     */
    MediaMessageDO toDataObject(MediaMessage domain);
}