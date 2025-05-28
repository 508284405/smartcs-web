package com.leyue.smartcs.startup;

import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动时初始化机器人模型Bean
 * 从机器人配置表中获取启用的BotProfile配置，自动注册机器人模型
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BotModelInitializer implements CommandLineRunner {

    private final BotProfileGateway botProfileGateway;
    private final ModelBeanManagerService modelBeanManagerService;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化机器人模型Bean...");
        
        try {
            // 获取所有启用的机器人配置
            List<BotProfile> activeBotProfiles = botProfileGateway.findAllActive();
            
            if (activeBotProfiles.isEmpty()) {
                log.info("未找到启用的机器人配置，跳过模型Bean初始化");
                return;
            }
            
            log.info("找到 {} 个启用的机器人配置，开始注册模型Bean", activeBotProfiles.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            // 为每个启用的机器人配置创建模型Bean
            for (BotProfile botProfile : activeBotProfiles) {
                try {
                    String beanName = modelBeanManagerService.createModelBean(botProfile);
                    log.info("成功注册机器人模型Bean - botId: {}, botName: {}, beanName: {}", 
                            botProfile.getBotId(), botProfile.getBotName(), beanName);
                    successCount++;
                } catch (Exception e) {
                    log.error("注册机器人模型Bean失败 - botId: {}, botName: {}, error: {}", 
                            botProfile.getBotId(), botProfile.getBotName(), e.getMessage(), e);
                    failureCount++;
                }
            }
            
            log.info("机器人模型Bean初始化完成 - 成功: {}, 失败: {}, 总计: {}", 
                    successCount, failureCount, activeBotProfiles.size());
                    
        } catch (Exception e) {
            log.error("初始化机器人模型Bean过程中发生异常: {}", e.getMessage(), e);
            throw e;
        }
    }
} 