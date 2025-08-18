package com.leyue.smartcs.intent.service;

import com.leyue.smartcs.domain.intent.domainservice.ClassificationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 意图感知的RAG服务
 * 整合意图分类和RAG检索，提供智能化的知识检索和响应生成
 * 
 * @author Claude
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntentAwareRagService {
    
    private final ClassificationDomainService classificationDomainService;
    
    /**
     * 执行意图感知的聊天预处理
     * 在RAG检索之前进行意图分类，优化检索策略
     * 
     * @param message 用户消息
     * @param channel 渠道
     * @param tenant 租户
     * @param sessionId 会话ID
     * @return 包含意图信息和优化参数的上下文
     */
    public Map<String, Object> preprocessChatWithIntent(String message, String channel, String tenant, String sessionId) {
        Map<String, Object> context = new HashMap<>();
        
        try {
            log.debug("开始意图感知预处理: sessionId={}, message_length={}", sessionId, message.length());
            
            // 1. 执行意图分类
            Map<String, Object> intentResult = classificationDomainService.classifyUserInput(message, channel, tenant);
            
            String intentCode = (String) intentResult.get("intent_code");
            String catalogCode = (String) intentResult.get("catalog_code");
            Double confidenceScore = (Double) intentResult.get("confidence_score");
            
            // 2. 基于意图优化RAG参数
            Map<String, Object> ragOptimization = optimizeRagBasedOnIntent(intentCode, catalogCode, confidenceScore);
            
            // 3. 构建增强的上下文
            context.put("intent_classification", intentResult);
            context.put("rag_optimization", ragOptimization);
            context.put("session_id", sessionId);
            context.put("original_message", message);
            context.put("enhanced_query", buildEnhancedQuery(message, intentCode, catalogCode));
            context.put("knowledge_filters", buildKnowledgeFilters(intentCode, catalogCode));
            
            log.info("意图感知预处理完成: sessionId={}, intent={}, catalog={}, confidence={}", 
                    sessionId, intentCode, catalogCode, confidenceScore);
            
            return context;
            
        } catch (Exception e) {
            log.error("意图感知预处理失败，使用默认参数: sessionId={}", sessionId, e);
            
            // 返回默认上下文
            context.put("intent_classification", createDefaultIntentResult());
            context.put("rag_optimization", createDefaultRagOptimization());
            context.put("session_id", sessionId);
            context.put("original_message", message);
            context.put("enhanced_query", message);
            context.put("knowledge_filters", new HashMap<>());
            
            return context;
        }
    }
    
    /**
     * 基于意图优化RAG参数
     */
    private Map<String, Object> optimizeRagBasedOnIntent(String intentCode, String catalogCode, Double confidenceScore) {
        Map<String, Object> optimization = new HashMap<>();
        
        try {
            // 基于意图类型调整检索参数
            if ("greeting".equals(intentCode) || "goodbye".equals(intentCode)) {
                // 问候类意图：减少检索，使用通用回复
                optimization.put("knowledge_search_enabled", false);
                optimization.put("web_search_enabled", false);
                optimization.put("system_prompt_template", "greeting_template");
                
            } else if ("question".equals(intentCode) || "inquiry".equals(intentCode)) {
                // 询问类意图：增强知识检索
                optimization.put("knowledge_search_enabled", true);
                optimization.put("knowledge_top_k", 8);
                optimization.put("knowledge_score_threshold", 0.6);
                optimization.put("web_search_enabled", true);
                optimization.put("web_search_max_results", 5);
                optimization.put("system_prompt_template", "knowledge_qa_template");
                
            } else if ("complaint".equals(intentCode)) {
                // 投诉类意图：优先检索FAQ和解决方案
                optimization.put("knowledge_search_enabled", true);
                optimization.put("knowledge_filter_tags", "FAQ,solution,troubleshoot");
                optimization.put("knowledge_top_k", 5);
                optimization.put("web_search_enabled", false);
                optimization.put("system_prompt_template", "complaint_handling_template");
                
            } else if ("technical_support".equals(intentCode)) {
                // 技术支持：详细检索技术文档
                optimization.put("knowledge_search_enabled", true);
                optimization.put("knowledge_filter_tags", "technical,documentation,guide");
                optimization.put("knowledge_top_k", 10);
                optimization.put("knowledge_score_threshold", 0.5);
                optimization.put("web_search_enabled", true);
                optimization.put("system_prompt_template", "technical_support_template");
                
            } else {
                // 默认意图：平衡的检索策略
                optimization.put("knowledge_search_enabled", true);
                optimization.put("knowledge_top_k", 5);
                optimization.put("knowledge_score_threshold", 0.7);
                optimization.put("web_search_enabled", true);
                optimization.put("web_search_max_results", 3);
                optimization.put("system_prompt_template", "default_template");
            }
            
            // 基于置信度调整策略
            if (confidenceScore != null && confidenceScore < 0.6) {
                // 低置信度：增加检索范围，降低阈值
                optimization.put("knowledge_score_threshold", 0.5);
                optimization.put("knowledge_top_k", 
                        Math.min(10, (Integer) optimization.getOrDefault("knowledge_top_k", 5) + 2));
                optimization.put("fallback_to_general_search", true);
            }
            
            // 基于目录优化检索范围
            if (catalogCode != null && !catalogCode.equals("UNKNOWN")) {
                optimization.put("knowledge_catalog_filter", catalogCode);
                optimization.put("preferred_knowledge_scope", catalogCode);
            }
            
            log.debug("RAG参数优化完成: intent={}, catalog={}, confidence={}, optimization={}", 
                    intentCode, catalogCode, confidenceScore, optimization);
            
            return optimization;
            
        } catch (Exception e) {
            log.warn("RAG参数优化失败，使用默认参数: intent={}", intentCode, e);
            return createDefaultRagOptimization();
        }
    }
    
    /**
     * 构建增强查询
     */
    private String buildEnhancedQuery(String originalMessage, String intentCode, String catalogCode) {
        try {
            StringBuilder enhancedQuery = new StringBuilder(originalMessage);
            
            // 基于意图添加查询增强
            if ("question".equals(intentCode)) {
                enhancedQuery.append(" [查询类问题]");
            } else if ("complaint".equals(intentCode)) {
                enhancedQuery.append(" [投诉问题]");
            } else if ("technical_support".equals(intentCode)) {
                enhancedQuery.append(" [技术支持]");
            }
            
            // 基于目录添加领域标记
            if (catalogCode != null && !catalogCode.equals("UNKNOWN")) {
                enhancedQuery.append(" [").append(catalogCode).append("领域]");
            }
            
            return enhancedQuery.toString();
            
        } catch (Exception e) {
            log.debug("构建增强查询失败，使用原始查询: {}", e.getMessage());
            return originalMessage;
        }
    }
    
    /**
     * 构建知识过滤器
     */
    private Map<String, Object> buildKnowledgeFilters(String intentCode, String catalogCode) {
        Map<String, Object> filters = new HashMap<>();
        
        try {
            // 基于意图设置标签过滤
            if ("complaint".equals(intentCode)) {
                filters.put("required_tags", new String[]{"FAQ", "solution", "complaint_handling"});
            } else if ("technical_support".equals(intentCode)) {
                filters.put("required_tags", new String[]{"technical", "documentation", "troubleshoot"});
            } else if ("question".equals(intentCode)) {
                filters.put("preferred_tags", new String[]{"FAQ", "guide", "explanation"});
            }
            
            // 基于目录设置分类过滤
            if (catalogCode != null && !catalogCode.equals("UNKNOWN")) {
                filters.put("catalog_filter", catalogCode);
            }
            
            return filters;
            
        } catch (Exception e) {
            log.debug("构建知识过滤器失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 创建默认意图结果
     */
    private Map<String, Object> createDefaultIntentResult() {
        Map<String, Object> defaultResult = new HashMap<>();
        defaultResult.put("intent_code", "UNKNOWN");
        defaultResult.put("intent_name", "未知意图");
        defaultResult.put("catalog_code", "UNKNOWN");
        defaultResult.put("catalog_name", "未知分类");
        defaultResult.put("confidence_score", 0.0);
        defaultResult.put("reason_code", "CLASSIFICATION_FAILED");
        defaultResult.put("reasoning", "意图分类失败，使用默认处理");
        return defaultResult;
    }
    
    /**
     * 创建默认RAG优化参数
     */
    private Map<String, Object> createDefaultRagOptimization() {
        Map<String, Object> defaultOptimization = new HashMap<>();
        defaultOptimization.put("knowledge_search_enabled", true);
        defaultOptimization.put("knowledge_top_k", 5);
        defaultOptimization.put("knowledge_score_threshold", 0.7);
        defaultOptimization.put("web_search_enabled", true);
        defaultOptimization.put("web_search_max_results", 3);
        defaultOptimization.put("system_prompt_template", "default_template");
        return defaultOptimization;
    }
    
    /**
     * 获取意图相关的系统提示模板
     */
    public String getSystemPromptForIntent(String intentCode, String catalogCode) {
        try {
            if ("greeting".equals(intentCode)) {
                return "你是一个友好的AI助手。请对用户的问候做出自然、友好的回应。";
                
            } else if ("complaint".equals(intentCode)) {
                return "你是一个专业的客服助手。用户遇到了问题或投诉，请以耐心、理解和解决问题的态度回应。" +
                       "首先表示理解用户的困扰，然后提供具体的解决方案或指导。";
                       
            } else if ("technical_support".equals(intentCode)) {
                return "你是一个技术支持专家。请提供准确、详细的技术信息和解决方案。" +
                       "使用专业但易懂的语言，必要时提供步骤指导。";
                       
            } else if ("question".equals(intentCode)) {
                return "你是一个知识渊博的AI助手。请基于可用的知识库信息提供准确、有用的答案。" +
                       "如果信息不够完整，请说明并提供相关的补充建议。";
                       
            } else {
                return "你是一个有帮助的AI助手。请根据用户的问题提供准确、有用的回答。";
            }
            
        } catch (Exception e) {
            log.debug("获取系统提示失败，使用默认提示: {}", e.getMessage());
            return "你是一个有帮助的AI助手。请根据用户的问题提供准确、有用的回答。";
        }
    }
}