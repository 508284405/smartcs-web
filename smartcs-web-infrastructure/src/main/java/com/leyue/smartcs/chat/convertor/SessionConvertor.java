package com.leyue.smartcs.chat.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.dto.chat.SessionDTO;

@Mapper(componentModel = "spring")
public interface SessionConvertor {

    SessionConvertor INSTANCE = Mappers.getMapper(SessionConvertor.class);

    @Mapping(target = "sessionState", source = "sessionState")
    SessionDTO toDTO(Session session);

    @Mapping(target = "sessionState", source = "sessionState")
    Session toDomain(SessionDTO sessionDTO);
} 