package com.leyue.smartcs.chat.convertor;

import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.enums.MessageType;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.MessageVO;
import com.leyue.smartcs.dto.chat.ws.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageConvertor {
    /**
     * 将MessageDTO转换为MessageVO
     *
     * @param dto
     * @return
     */
    MessageVO toVO(MessageDTO dto);

    /**
     * 将List<MessageDTO>转换为List<MessageVO>
     *
     * @param list
     * @return
     */
    List<MessageVO> toVOList(List<MessageDTO> list);

    /**
     * 将Message转换为MessageDTO
     *
     * @param message
     * @return
     */
    MessageDTO toDTO(Message message);

    /**
     * 将List<Message>转换为List<MessageDTO>
     *
     * @param list
     * @return
     */
    List<MessageDTO> toDTOList(List<Message> list);

    /**
     * 将MessageType转换为Integer
     *
     * @param value
     * @return
     */
    default Integer map(MessageType value) {
        if (value == null)
            return null;
        return value.getCode();
    }

    /**
     * 将Integer转换为MessageType
     *
     * @param value
     * @return
     */
    default MessageType map(Integer value) {
        if (value == null)
            return null;
        return MessageType.fromCode(value);
    }

    /**
     * 将ChatMessage转换为Message
     *
     * @param chatMessage
     * @return
     */
    @Mapping(target = "timestamp", ignore = true)
    Message toMessage(ChatMessage chatMessage);
} 