package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.api.chat.dto.SessionVO;
import com.leyue.smartcs.dto.chat.SessionDTO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatSessionConvertor {
    SessionVO toVO(SessionDTO dto);
    List<SessionVO> toVOList(List<SessionDTO> list);
} 