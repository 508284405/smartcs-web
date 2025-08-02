package com.leyue.smartcs.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 增强的Redis配置
 * 支持集群和单机模式，提供高可用性
 */
@Configuration
@Slf4j
public class EnhancedRedisConfig {

    @Value("${spring.redis.cluster.nodes:}")
    private String clusterNodes;
    
    @Value("${spring.redis.cluster.max-redirects:3}")
    private int maxRedirects;
    
    @Value("${spring.redis.host:localhost}")
    private String standaloneHost;
    
    @Value("${spring.redis.port:6379}")
    private int standalonePort;
    
    @Value("${spring.redis.password:}")
    private String password;
    
    @Value("${spring.redis.database:0}")
    private int database;
    
    @Value("${spring.redis.timeout:5000}")
    private int timeout;
    
    @Value("${spring.redis.lettuce.pool.max-active:8}")
    private int maxActive;
    
    @Value("${spring.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;
    
    @Value("${spring.redis.lettuce.pool.min-idle:0}")
    private int minIdle;

    /**
     * Redis集群连接工厂
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.redis.cluster.nodes")
    public RedisConnectionFactory redisClusterConnectionFactory() {
        log.info("配置Redis集群连接: nodes={}", clusterNodes);
        
        List<String> nodes = Arrays.asList(clusterNodes.split(","));
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(nodes);
        clusterConfig.setMaxRedirects(maxRedirects);
        
        if (!password.isEmpty()) {
            clusterConfig.setPassword(password);
        }
        
        // 配置集群拓扑刷新
        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(Duration.ofMinutes(30))
                .enableAllAdaptiveRefreshTriggers()
                .build();
        
        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .disconnectedBehavior(ClusterClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .build();
        
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .clientOptions(clusterClientOptions)
                .build();
        
        return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }

    /**
     * Redis单机连接工厂（作为后备）
     */
    @Bean
    @ConditionalOnProperty(name = "spring.redis.cluster.nodes", havingValue = "", matchIfMissing = true)
    public RedisConnectionFactory redisStandaloneConnectionFactory() {
        log.info("配置Redis单机连接: {}:{}", standaloneHost, standalonePort);
        
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(standaloneHost);
        standaloneConfig.setPort(standalonePort);
        standaloneConfig.setDatabase(database);
        
        if (!password.isEmpty()) {
            standaloneConfig.setPassword(password);
        }
        
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .build();
        
        return new LettuceConnectionFactory(standaloneConfig, clientConfig);
    }

    /**
     * Redis模板配置
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("配置RedisTemplate");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();
        
        return template;
    }

    /**
     * Redis健康检查器
     */
    @Bean
    public RedisHealthChecker redisHealthChecker(RedisTemplate<String, Object> redisTemplate) {
        return new RedisHealthChecker(redisTemplate);
    }

    /**
     * Redis健康检查实现
     */
    public static class RedisHealthChecker {
        private final RedisTemplate<String, Object> redisTemplate;
        
        public RedisHealthChecker(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }
        
        /**
         * 检查Redis连接健康状态
         */
        public boolean isHealthy() {
            try {
                redisTemplate.hasKey("health:check");
                return true;
            } catch (Exception e) {
                log.error("Redis健康检查失败", e);
                return false;
            }
        }
        
        /**
         * 执行连接测试
         */
        public boolean testConnection() {
            try {
                String testKey = "test:connection:" + System.currentTimeMillis();
                String testValue = "ping";
                
                redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(10));
                String result = (String) redisTemplate.opsForValue().get(testKey);
                redisTemplate.delete(testKey);
                
                return testValue.equals(result);
            } catch (Exception e) {
                log.error("Redis连接测试失败", e);
                return false;
            }
        }
        
        /**
         * 获取连接信息
         */
        public String getConnectionInfo() {
            try {
                return redisTemplate.getConnectionFactory().getConnection().toString();
            } catch (Exception e) {
                return "无法获取连接信息: " + e.getMessage();
            }
        }
    }
}