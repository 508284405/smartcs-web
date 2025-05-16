package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.enums.SessionState;
import com.leyue.smartcs.dto.chat.SessionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SessionConvertor {

    SessionConvertor INSTANCE = Mappers.getMapper(SessionConvertor.class);

    @Mapping(target = "sessionState", source = "sessionState")
    SessionDTO toDTO(Session session);

    @Mapping(target = "sessionState", source = "sessionState")
    Session toDomain(SessionDTO sessionDTO);
} 