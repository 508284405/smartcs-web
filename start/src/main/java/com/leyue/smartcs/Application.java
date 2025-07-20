package com.leyue.smartcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Starter with Spring Cloud
 *
 * @author Frank Zhang
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.leyue.smartcs", "com.alibaba.cola"})
@EnableFeignClients(basePackages = "com.leyue.smartcs.**.feign")
@EnableScheduling
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
