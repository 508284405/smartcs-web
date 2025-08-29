package com.leyue.smartcs.web.message;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.chat.executor.SearchMessagesQryExe;
import com.leyue.smartcs.dto.chat.MessageSearchQry;
import com.leyue.smartcs.dto.chat.MessageSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 消息搜索控制器
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/messages")
@RequiredArgsConstructor
public class MessageSearchController {
    
    private final SearchMessagesQryExe searchMessagesQryExe;
    
    /**
     * 搜索消息
     */
    @GetMapping("/search")
    public PageResponse<MessageSearchResult> searchMessages(
            @RequestParam String userId,
            @RequestParam String keyword,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Integer messageType,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(defaultValue = "time") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            log.info("收到消息搜索请求: userId={}, keyword={}, sessionId={}", userId, keyword, sessionId);
            
            // 构建查询对象
            MessageSearchQry qry = new MessageSearchQry();
            qry.setUserId(userId);
            qry.setKeyword(keyword);
            qry.setSessionId(sessionId);
            qry.setMessageType(messageType);
            qry.setStartTime(startTime);
            qry.setEndTime(endTime);
            qry.setSortBy(sortBy);
            qry.setPageIndex(page);
            qry.setPageSize(size);
            
            // 执行搜索
            PageResponse<MessageSearchResult> result = searchMessagesQryExe.execute(qry);
            
            log.info("消息搜索完成: userId={}, keyword={}, 找到{}条结果", 
                    userId, keyword, result.getTotalCount());
            
            return result;
            
        } catch (IllegalArgumentException e) {
            log.warn("消息搜索参数错误: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            log.error("消息搜索失败", e);
            throw new RuntimeException("消息搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取搜索建议
     */
    @GetMapping("/search/suggestions")
    public String[] getSearchSuggestions(@RequestParam String userId, 
                                         @RequestParam String query) {
        try {
            log.info("获取搜索建议: userId={}, query={}", userId, query);
            
            // TODO: 实现搜索建议逻辑
            // 这里可以返回用户最近的搜索历史、热门搜索词等
            String[] suggestions = {
                query + "相关建议1", 
                query + "相关建议2",
                query + "相关建议3"
            };
            
            return suggestions;
            
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            throw new RuntimeException("获取搜索建议失败");
        }
    }
}