package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import com.leyue.smartcs.rag.query.pipeline.services.IPrefixCompletionService;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrefixCompletionStage implements QueryTransformerStage {

    private final IPrefixCompletionService prefixCompletionService;

    @Override
    public String getName() {
        return "PrefixCompletionStage";
    }

    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnablePrefixCompletion();
    }

    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) return Collections.emptyList();
        
        QueryContext.PrefixConfig cfg = context.getPipelineConfig().getPrefixConfig();
        int minPrefix = cfg != null ? cfg.getMinPrefixLength() : 2;
        int maxCand = cfg != null ? cfg.getMaxCandidates() : 5;
        boolean onlyShort = cfg == null || cfg.isOnlyShortQuery();
        int shortMax = cfg != null ? cfg.getShortQueryMaxLen() : 5;

        try {
            List<Query> out = new ArrayList<>(queries);
            
            for (Query q : queries) {
                String t = q.text().trim();
                if (onlyShort && t.length() > shortMax) continue;
                if (t.length() < minPrefix) continue;
                
                // 创建补全上下文
                IPrefixCompletionService.CompletionContext completionContext = 
                    createCompletionContext(context);
                
                // 使用增强的前缀补全服务
                List<String> completions = prefixCompletionService.completeWithContext(
                    t, completionContext, maxCand);
                
                for (String c : completions) {
                    if (!c.equals(t)) {
                        out.add(Query.from(c));
                        
                        // 记录用户反馈（模拟正向反馈）
                        prefixCompletionService.updateWordWeight(c, 0.1);
                    }
                }
            }
            
            // 去重
            LinkedHashSet<String> seen = new LinkedHashSet<>();
            List<Query> unique = new ArrayList<>();
            for (Query q : out) {
                String k = q.text().trim();
                if (seen.add(k)) unique.add(q);
            }
            
            return unique;
            
        } catch (Exception e) {
            log.warn("前缀补全阶段失败，跳过: {}", e.getMessage());
            throw new QueryTransformationException(getName(), "前缀补全阶段失败", e, true);
        }
    }
    
    /**
     * 创建补全上下文
     */
    private IPrefixCompletionService.CompletionContext createCompletionContext(QueryContext queryContext) {
        String userId = extractUserId(queryContext);
        String sessionId = extractSessionId(queryContext);
        List<String> recentQueries = extractRecentQueries(queryContext);
        String domain = queryContext.getTenant(); // 使用租户作为业务领域
        
        return new IPrefixCompletionService.CompletionContext(userId, sessionId, recentQueries, domain);
    }
    
    private String extractUserId(QueryContext context) {
        // 从上下文中提取用户ID
        if (context.getChatHistory() != null) {
            Object userId = context.getChatHistory().get("userId");
            return userId != null ? userId.toString() : null;
        }
        return null;
    }
    
    private String extractSessionId(QueryContext context) {
        // 从上下文中提取会话ID
        if (context.getChatHistory() != null) {
            Object sessionId = context.getChatHistory().get("sessionId");
            return sessionId != null ? sessionId.toString() : null;
        }
        return null;
    }
    
    private List<String> extractRecentQueries(QueryContext context) {
        // 从上下文中提取最近的查询
        if (context.getChatHistory() != null) {
            Object recentQueries = context.getChatHistory().get("recentQueries");
            if (recentQueries instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> queries = (List<String>) recentQueries;
                return queries;
            }
        }
        return Collections.emptyList();
    }
}

