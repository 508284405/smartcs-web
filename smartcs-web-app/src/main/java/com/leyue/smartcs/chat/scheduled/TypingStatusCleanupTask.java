package com.leyue.smartcs.chat.scheduled;

import com.leyue.smartcs.chat.service.TypingStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 输入状态清理定时任务
 * 清理过期的输入状态，防止Redis中积累过多无效数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TypingStatusCleanupTask {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 每分钟执行一次清理任务
     * 清理Redis中以chat:typing:session:开头的过期键
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void cleanupExpiredTypingStatus() {
        try {
            String pattern = "chat:typing:session:*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                long deletedCount = 0;
                for (String key : keys) {
                    Long ttl = redisTemplate.getExpire(key);
                    // 如果键已过期(ttl = -2)或没有设置过期时间(ttl = -1)，则删除
                    if (ttl != null && ttl <= 0) {
                        Boolean deleted = redisTemplate.delete(key);
                        if (Boolean.TRUE.equals(deleted)) {
                            deletedCount++;
                        }
                    }
                }
                
                if (deletedCount > 0) {
                    log.debug("清理过期输入状态键: {} 个", deletedCount);
                }
            }
            
        } catch (Exception e) {
            log.error("清理输入状态失败: {}", e.getMessage(), e);
        }
    }
}