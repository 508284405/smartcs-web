package com.leyue.smartcs.app.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具管理器
 * 统一管理AI可用的工具集合
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ToolManager {

    private final KnowledgeSearchTool knowledgeSearchTool;
    
    // 缓存工具实例
    private final Map<String, Object> toolCache = new ConcurrentHashMap<>();

    /**
     * 获取所有可用工具
     * 
     * @return 工具列表
     */
    public List<Object> getAllTools() {
        return List.of(
            knowledgeSearchTool
            // 这里可以添加更多工具
        );
    }

    /**
     * 获取知识库相关工具
     * 
     * @return 知识库工具列表
     */
    public List<Object> getKnowledgeTools() {
        return List.of(knowledgeSearchTool);
    }

    /**
     * 根据应用类型获取推荐工具
     * 
     * @param appType 应用类型
     * @return 推荐工具列表
     */
    public List<Object> getRecommendedTools(String appType) {
        return switch (appType) {
            case "knowledge_qa" -> getKnowledgeTools();
            case "general_chat" -> List.of(); // 一般聊天不需要特殊工具
            case "data_analysis" -> List.of(); // 未来可以添加数据分析工具
            default -> getAllTools(); // 默认返回所有工具
        };
    }

    /**
     * 根据知识库ID获取相关工具
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 相关工具列表
     */
    public List<Object> getToolsForKnowledgeBase(Long knowledgeBaseId) {
        log.debug("获取知识库相关工具: knowledgeBaseId={}", knowledgeBaseId);
        
        // 如果指定了知识库，返回知识库相关工具
        if (knowledgeBaseId != null) {
            return getKnowledgeTools();
        }
        
        return List.of();
    }

    /**
     * 检查工具是否可用
     * 
     * @param toolName 工具名称
     * @return 是否可用
     */
    public boolean isToolAvailable(String toolName) {
        try {
            return switch (toolName) {
                case "knowledge_search" -> knowledgeSearchTool != null;
                default -> false;
            };
        } catch (Exception e) {
            log.error("检查工具可用性失败: toolName={}, error={}", toolName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取工具描述
     * 
     * @return 工具描述映射
     */
    public Map<String, String> getToolDescriptions() {
        Map<String, String> descriptions = new ConcurrentHashMap<>();
        descriptions.put("knowledge_search", "搜索知识库中的相关内容");
        descriptions.put("knowledge_base_search", "在指定知识库中搜索内容");
        descriptions.put("knowledge_summary", "获取知识库摘要信息");
        descriptions.put("knowledge_detailed", "获取详细的知识库搜索结果");
        
        return descriptions;
    }

    /**
     * 预热工具
     * 初始化所有工具以提高首次调用性能
     */
    public void warmupTools() {
        log.info("开始预热工具");
        
        try {
            // 检查所有工具的可用性
            boolean knowledgeToolAvailable = isToolAvailable("knowledge_search");
            log.info("工具可用性检查完成: knowledge_search={}", knowledgeToolAvailable);
            
            log.info("工具预热完成");
        } catch (Exception e) {
            log.error("工具预热失败", e);
        }
    }

    /**
     * 获取工具统计信息
     * 
     * @return 工具统计
     */
    public Map<String, Object> getToolStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("total_tools", getAllTools().size());
        stats.put("knowledge_tools", getKnowledgeTools().size());
        stats.put("cache_size", toolCache.size());
        
        return stats;
    }
}