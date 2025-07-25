package com.leyue.smartcs.knowledge.parser.model;

import com.leyue.smartcs.dto.knowledge.ModelRequest;

import dev.langchain4j.model.chat.ChatModel;
import lombok.Data;

@Data
public class ParserExtendParam {
    private ChatModel chatModel;
    private ModelRequest modelRequest;
}
