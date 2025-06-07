package com.leyue.smartcs.knowledge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

/**
 * jedis配置
 */
@Configuration
public class JedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.timeout:20000}")
    private int timeout;

    @Bean
    public UnifiedJedis jedisPool() {
        HostAndPort hostAndPort = new HostAndPort(host, port);
        DefaultJedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                .database(database)
                .password(password)
                .timeoutMillis(timeout).build();
        return new UnifiedJedis(hostAndPort, jedisClientConfig);
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(host);
        jedisConnectionFactory.setPort(port);
        jedisConnectionFactory.setPassword(password);
        jedisConnectionFactory.setTimeout(timeout);
        jedisConnectionFactory.setDatabase(database);
        return jedisConnectionFactory;
    }
} 