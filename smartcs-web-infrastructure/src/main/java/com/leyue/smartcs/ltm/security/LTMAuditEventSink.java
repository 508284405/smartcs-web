package com.leyue.smartcs.ltm.security;

import java.util.List;

/**
 * LTM 审计事件落地接口。
 * 可由基础设施层实现（如数据库、Kafka）以满足合规与审计要求。
 */
public interface LTMAuditEventSink {

    /**
     * 批量持久化审计事件。
     *
     * @param events 待持久化的事件集合（通常为不可变列表）
     */
    void persistBatch(List<LTMAuditLogger.AuditEvent> events);
}
