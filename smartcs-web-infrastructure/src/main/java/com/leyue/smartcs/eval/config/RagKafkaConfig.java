package com.leyue.smartcs.eval.config;

import com.leyue.smartcs.dto.eval.event.EvalConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * RAG评估系统Kafka配置
 * 负责创建评估事件的Topic和Producer配置
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "eval.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class RagKafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    
    @Value("${eval.kafka.topics.rag-events:rag.events}")
    private String ragEventsTopic;
    
    @Value("${eval.kafka.topics.rag-eval-in:rag.eval.in}")
    private String ragEvalInTopic;
    
    @Value("${eval.kafka.topics.partitions:3}")
    private int topicPartitions;
    
    @Value("${eval.kafka.topics.replicas:1}")
    private short topicReplicas;
    
    /**
     * Kafka Admin配置
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
    
    /**
     * 创建RAG事件主题
     */
    @Bean
    public NewTopic ragEventsTopic() {
        return new NewTopic(ragEventsTopic, topicPartitions, topicReplicas);
    }
    
    /**
     * 创建RAG事件死信队列主题
     */
    @Bean
    public NewTopic ragEventsDlqTopic() {
        return new NewTopic(ragEventsTopic + ".dlq", topicPartitions, topicReplicas);
    }
    
    /**
     * 创建RAG评估输入主题
     */
    @Bean
    public NewTopic ragEvalInTopic() {
        return new NewTopic(ragEvalInTopic, topicPartitions, topicReplicas);
    }
    
    /**
     * 创建RAG评估输入死信队列主题
     */
    @Bean
    public NewTopic ragEvalInDlqTopic() {
        return new NewTopic(ragEvalInTopic + ".dlq", topicPartitions, topicReplicas);
    }
    
    /**
     * RAG评估事件Producer Factory
     */
    @Bean("ragEventProducerFactory")
    public ProducerFactory<String, String> ragEventProducerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // 高性能配置
        configs.put(ProducerConfig.ACKS_CONFIG, "1"); // 仅leader确认，平衡性能和可靠性
        configs.put(ProducerConfig.LINGER_MS_CONFIG, 5); // 5ms批处理延迟
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024); // 32KB批处理大小
        configs.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 64 * 1024 * 1024); // 64MB缓冲区
        configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Snappy压缩
        
        // 重试和超时配置
        configs.put(ProducerConfig.RETRIES_CONFIG, 3);
        configs.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configs.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        // 幂等性保证
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new DefaultKafkaProducerFactory<>(configs);
    }
    
    /**
     * RAG评估事件KafkaTemplate
     */
    @Bean("ragEventKafkaTemplate")
    public KafkaTemplate<String, String> ragEventKafkaTemplate() {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(ragEventProducerFactory());
        template.setDefaultTopic(ragEventsTopic);
        
        // KafkaTemplate已配置完成
        
        log.info("RAG评估事件KafkaTemplate已配置, 默认主题: {}", ragEventsTopic);
        return template;
    }
    
    /**
     * 获取RAG事件主题名称
     */
    public String getRagEventsTopic() {
        return ragEventsTopic;
    }
    
    /**
     * 获取RAG事件死信队列主题名称
     */
    public String getRagEventsDlqTopic() {
        return ragEventsTopic + ".dlq";
    }
    
    /**
     * 获取RAG评估输入主题名称
     */
    public String getRagEvalInTopic() {
        return ragEvalInTopic;
    }
    
    /**
     * 获取RAG评估输入死信队列主题名称
     */
    public String getRagEvalInDlqTopic() {
        return ragEvalInTopic + ".dlq";
    }
}