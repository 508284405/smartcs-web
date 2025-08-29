package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.chat.MessageSearchQry;
import com.leyue.smartcs.dto.chat.MessageSearchResult;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 消息搜索查询执行器
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchMessagesQryExe {
    
    private final MessageGateway messageGateway;
    
    /**
     * 执行消息搜索
     *
     * @param qry 搜索查询条件
     * @return 搜索结果
     */
    public PageResponse<MessageSearchResult> execute(MessageSearchQry qry) {
        log.info("开始搜索消息，用户: {}, 关键词: {}, 会话: {}", 
                qry.getUserId(), qry.getKeyword(), qry.getSessionId());
        
        try {
            // 参数验证
            validateSearchParams(qry);
            
            // 执行搜索
            PageResponse<MessageSearchResult> results = messageGateway.searchMessages(qry);
            
            // 高亮关键词
            if (results.getData() != null && !results.getData().isEmpty()) {
                highlightKeywords(results.getData(), qry.getKeyword());
            }
            
            log.info("消息搜索完成，找到 {} 条结果", results.getTotalCount());
            return results;
            
        } catch (Exception e) {
            log.error("搜索消息失败", e);
            throw new RuntimeException("搜索消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证搜索参数
     */
    private void validateSearchParams(MessageSearchQry qry) {
        if (qry.getUserId() == null || qry.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        if (qry.getKeyword() == null || qry.getKeyword().trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        if (qry.getKeyword().trim().length() < 1) {
            throw new IllegalArgumentException("搜索关键词长度至少为1个字符");
        }
        
        if (qry.getKeyword().trim().length() > 100) {
            throw new IllegalArgumentException("搜索关键词长度不能超过100个字符");
        }
        
        // 设置默认分页参数
        if (qry.getPageIndex() <= 0) {
            qry.setPageIndex(1);
        }
        
        if (qry.getPageSize() <= 0) {
            qry.setPageSize(20);
        }
        
        if (qry.getPageSize() > 100) {
            qry.setPageSize(100); // 限制最大页面大小
        }
        
        // 验证时间范围
        if (qry.getStartTime() != null && qry.getEndTime() != null) {
            if (qry.getStartTime() >= qry.getEndTime()) {
                throw new IllegalArgumentException("开始时间必须早于结束时间");
            }
        }
    }
    
    /**
     * 高亮搜索关键词
     */
    private void highlightKeywords(List<MessageSearchResult> results, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        String cleanKeyword = keyword.trim();
        
        // 创建不区分大小写的正则表达式
        Pattern pattern = Pattern.compile("(" + Pattern.quote(cleanKeyword) + ")", 
                Pattern.CASE_INSENSITIVE);
        
        for (MessageSearchResult result : results) {
            if (result.getContent() != null) {
                // 生成高亮文本
                String highlightedContent = pattern.matcher(result.getContent())
                        .replaceAll("<mark class=\"highlight\">$1</mark>");
                result.setHighlightText(highlightedContent);
                
                // 生成摘要文本（显示关键词前后的上下文）
                result.setContent(generateSnippet(result.getContent(), cleanKeyword, 100));
            }
        }
    }
    
    /**
     * 生成包含关键词的文本片段
     */
    private String generateSnippet(String content, String keyword, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        
        int keywordIndex = content.toLowerCase().indexOf(keyword.toLowerCase());
        if (keywordIndex == -1) {
            return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
        }
        
        // 计算摘要的开始和结束位置
        int contextLength = (maxLength - keyword.length()) / 2;
        int start = Math.max(0, keywordIndex - contextLength);
        int end = Math.min(content.length(), keywordIndex + keyword.length() + contextLength);
        
        String snippet = content.substring(start, end);
        
        // 添加省略号
        if (start > 0) {
            snippet = "..." + snippet;
        }
        if (end < content.length()) {
            snippet = snippet + "...";
        }
        
        return snippet;
    }
}