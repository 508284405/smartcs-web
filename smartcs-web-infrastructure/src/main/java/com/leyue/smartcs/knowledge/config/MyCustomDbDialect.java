package com.leyue.smartcs.knowledge.config;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;

public class MyCustomDbDialect implements JdbcChatMemoryRepositoryDialect {
    @Override
    public String getDeleteMessagesSql() {
        return "DELETE FROM t_cs_message WHERE session_id = ?";
    }

    @Override
    public String getInsertMessageSql() {
        return "INSERT INTO t_cs_message (session_id, msg_id, content, chat_type, timestamp) VALUES (?, UUID(), ?, ?, ?)";
    }

    /**
     * 获取会话消息列表
     * 
     * @return 消息列表
     */
    @Override
    public String getSelectMessagesSql() {
        return "SELECT content, chat_type FROM t_cs_message WHERE session_id = ? order by timestamp";
    }

    /**
     * 获取会话ID列表
     * 
     * @return 会话ID列表
     */
    @Override
    public String getSelectConversationIdsSql() {
        return "SELECT DISTINCT session_id FROM t_cs_message";
    }
}
