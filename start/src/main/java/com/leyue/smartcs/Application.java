package com.leyue.smartcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

/**
 * SmartCS Web 应用启动类
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.leyue.smartcs", "com.alibaba.cola"})
@EnableFeignClients(basePackages = "com.leyue.smartcs.**.feign")
@EnableScheduling
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.leyue.smartcs"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
