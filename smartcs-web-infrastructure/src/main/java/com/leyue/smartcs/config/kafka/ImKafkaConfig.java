package com.leyue.smartcs.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * IM系统Kafka配置
 * 
 * @author Claude
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class ImKafkaConfig {

    /**
     * 私聊跨节点转发主题
     */
    @Bean
    public NewTopic imDirectTopic() {
        return TopicBuilder.name("im.direct")
                .partitions(16) // 16个分区，支持更好的并发
                .replicas(3) // 3个副本保证可靠性
                .config("retention.ms", "604800000") // 7天保留期
                .config("max.message.bytes", "1048576") // 1MB最大消息大小
                .build();
    }

    /**
     * 群聊广播主题
     */
    @Bean
    public NewTopic imGroupTopic() {
        return TopicBuilder.name("im.group")
                .partitions(32) // 32个分区，支持更多群组并发
                .replicas(3) // 3个副本保证可靠性
                .config("retention.ms", "604800000") // 7天保留期
                .config("max.message.bytes", "1048576") // 1MB最大消息大小
                .build();
    }

    /**
     * 审计/统计/机器人联动事件主题
     */
    @Bean
    public NewTopic imEventTopic() {
        return TopicBuilder.name("im.event")
                .partitions(8) // 8个分区
                .replicas(3) // 3个副本保证可靠性
                .config("retention.ms", "2592000000") // 30天保留期，用于审计
                .config("max.message.bytes", "2097152") // 2MB最大消息大小，支持更丰富的事件数据
                .build();
    }

    /**
     * 系统通知主题
     */
    @Bean
    public NewTopic imNotificationTopic() {
        return TopicBuilder.name("im.notification")
                .partitions(4) // 4个分区
                .replicas(3) // 3个副本
                .config("retention.ms", "259200000") // 3天保留期
                .config("max.message.bytes", "524288") // 512KB最大消息大小
                .build();
    }
}