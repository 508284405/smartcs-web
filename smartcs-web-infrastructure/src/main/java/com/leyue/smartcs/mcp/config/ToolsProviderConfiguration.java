package com.leyue.smartcs.mcp.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.leyue.smartcs.mcp.OrderToolsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 工具服务提供者配置
 * 
 * <p>集中管理所有可用的LangChain4j工具服务，支持ReAct模式的工具调用。
 * 根据配置动态启用/禁用不同的工具集合。</p>
 * 
 * <h3>当前支持的工具:</h3>
 * <ul>
 *   <li>订单管理工具 - 查询、取消、确认收货、更新地址</li>
 *   <li>后续可扩展：天气查询、支付工具、数据库查询等</li>
 * </ul>
 * 
 * <h3>配置控制:</h3>
 * <ul>
 *   <li>smartcs.tools.order.enabled - 控制订单工具是否启用（默认true）</li>
 *   <li>smartcs.tools.enabled - 全局工具开关（默认true）</li>
 * </ul>
 * 
 * @author Claude
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ToolsProviderConfiguration {

    private final OrderToolsService orderToolsService;

    /**
     * 获取所有启用的工具服务
     * 
     * @return 工具服务列表
     */
    @Bean
    @ConditionalOnProperty(name = "smartcs.tools.enabled", havingValue = "true", matchIfMissing = true)
    public List<Object> enabledTools() {
        List<Object> tools = new ArrayList<>();
        
        // 添加订单管理工具
        if (isOrderToolsEnabled()) {
            tools.add(orderToolsService);
            log.info("已启用订单管理工具: OrderToolsService");
        }
        
        // 后续可在此添加更多工具
        // if (isWeatherToolsEnabled()) {
        //     tools.add(weatherToolsService);
        //     log.info("已启用天气查询工具: WeatherToolsService");
        // }
        
        log.info("工具服务配置完成: 共启用 {} 个工具集合", tools.size());
        return tools;
    }
    
    /**
     * 订单工具配置Bean - 用于单独注入订单工具
     */
    @Bean
    @ConditionalOnProperty(name = "smartcs.tools.order.enabled", havingValue = "true", matchIfMissing = true)
    public OrderToolsService orderTools() {
        log.info("配置订单工具服务: OrderToolsService");
        return orderToolsService;
    }
    
    /**
     * 检查订单工具是否启用
     */
    private boolean isOrderToolsEnabled() {
        // 这里可以添加更复杂的配置逻辑，比如读取配置文件
        // 目前简单返回true，表示默认启用
        return true;
    }
}