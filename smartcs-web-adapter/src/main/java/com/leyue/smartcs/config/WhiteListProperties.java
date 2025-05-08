package com.leyue.smartcs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "permission")
public class WhiteListProperties {
    
    /**
     * 完全开放的API接口白名单列表，不需要token和权限校验
     */
    private List<String> whiteList = new ArrayList<>();
}
