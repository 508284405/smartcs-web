package com.leyue.smartcs.intent.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话级意图/槽位状态存储（轻量级内存实现）
 * 用于多轮澄清最小闭环：保存待澄清的槽位与问题。
 */
@Component
public class SessionIntentStateStore {

    private final Map<String, Map<String, Object>> state = new ConcurrentHashMap<>();

    public void put(String sessionId, Map<String, Object> intentOrSlotState) {
        if (sessionId == null) return;
        state.put(sessionId, intentOrSlotState);
    }

    public Map<String, Object> get(String sessionId) {
        if (sessionId == null) return null;
        return state.get(sessionId);
    }

    public void clear(String sessionId) {
        if (sessionId == null) return;
        state.remove(sessionId);
    }
}

