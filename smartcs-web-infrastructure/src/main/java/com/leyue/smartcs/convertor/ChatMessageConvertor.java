package com.leyue.smartcs.convertor;

import com.leyue.smartcs.api.chat.dto.MessageVO;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.api.chat.dto.SendMessageRequest;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageConvertor {
    MessageVO toVO(MessageDTO dto);
    List<MessageVO> toVOList(List<MessageDTO> list);
    void copyToCmd(SendMessageRequest request, @MappingTarget SendMessageCmd cmd);
} 