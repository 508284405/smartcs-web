package com.leyue.smartcs.rag.retriever;

import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService;
import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService.LTMContext;
import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService.MemoryRetrievalRequest;
import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;
import com.leyue.smartcs.domain.ltm.entity.SemanticMemory;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LTM增强的内容检索器
 * 结合知识库检索和长期记忆检索提供个性化内容
 */
@Component("ltmEnhancedContentRetriever")
@RequiredArgsConstructor
@Slf4j
public class LTMEnhancedContentRetriever implements ContentRetriever {

    private final ContentRetriever baseContentRetriever;
    private final LTMDomainService ltmDomainService;

    @Value("${smartcs.ai.ltm.retrieval.enabled:true}")
    private boolean ltmRetrievalEnabled;

    @Value("${smartcs.ai.ltm.retrieval.weight:0.3}")
    private double ltmWeight; // LTM内容在最终结果中的权重

    @Value("${smartcs.ai.ltm.retrieval.max-results:5}")
    private int maxLtmResults;

    @Value("${smartcs.ai.ltm.retrieval.threshold:0.7}")
    private double ltmThreshold;

    @Override
    public List<Content> retrieve(Query query) {
        log.debug("执行LTM增强的内容检索: query={}", query.text());

        // 基础知识库检索
        List<Content> baseResults = baseContentRetriever.retrieve(query);
        
        if (!ltmRetrievalEnabled) {
            return baseResults;
        }

        try {
            // 从查询元数据中提取用户ID
            Long userId = extractUserIdFromQuery(query);
            if (userId == null) {
                log.debug("查询中未找到用户ID，跳过LTM检索");
                return baseResults;
            }

            // 执行LTM检索
            List<Content> ltmResults = retrieveFromLTM(userId, query);

            // 合并和重排序结果
            return mergeAndRerankResults(baseResults, ltmResults);

        } catch (Exception e) {
            log.warn("LTM检索失败，返回基础结果: error={}", e.getMessage());
            return baseResults;
        }
    }

    /**
     * 从查询中提取用户ID
     */
    private Long extractUserIdFromQuery(Query query) {
        if (query.metadata() == null) {
            return null;
        }

        Object chatMemoryId = query.metadata().chatMemoryId();
        Long parsed = parseNumericIdentifier(chatMemoryId);
        if (parsed != null) {
            return parsed;
        }

        // 兼容 "userId:sessionId" 形式的 memoryId
        if (chatMemoryId instanceof String memoryIdStr && memoryIdStr.contains(":")) {
            String candidate = memoryIdStr.substring(0, memoryIdStr.indexOf(':'));
            parsed = parseNumericIdentifier(candidate);
            if (parsed != null) {
                return parsed;
            }
        }

        return null;
    }

    /**
     * 从LTM检索内容
     */
    private List<Content> retrieveFromLTM(Long userId, Query query) {
        // 构建LTM检索请求
        Map<String, Object> context = new HashMap<>();
        if (query.metadata() != null) {
            if (query.metadata().chatMemoryId() != null) {
                context.put("chat_memory_id", query.metadata().chatMemoryId().toString());
            }
            if (query.metadata().chatMessage() instanceof dev.langchain4j.data.message.UserMessage userMessage
                    && userMessage.hasSingleText()) {
                context.put("recent_user_message", userMessage.singleText());
            }
        }
        context.put("retrieval_timestamp", System.currentTimeMillis());

        MemoryRetrievalRequest request = new MemoryRetrievalRequest(
            userId,
            query.text(),
            null, // queryVector - 如果需要可以通过embedding模型生成
            context,
            maxLtmResults,
            ltmThreshold
        );

        // 检索LTM上下文
        LTMContext ltmContext = ltmDomainService.retrieveMemoryContext(request);

        // 将LTM内容转换为Content对象
        List<Content> ltmContents = new ArrayList<>();

        // 添加情景记忆内容
        if (ltmContext.getEpisodicMemories() != null) {
            ltmContext.getEpisodicMemories().forEach(episodicMemory -> {
                Content content = createContentFromEpisodicMemory(episodicMemory);
                ltmContents.add(content);
            });
        }

        // 添加语义记忆内容
        if (ltmContext.getSemanticMemories() != null) {
            ltmContext.getSemanticMemories().forEach(semanticMemory -> {
                Content content = createContentFromSemanticMemory(semanticMemory);
                ltmContents.add(content);
            });
        }

        log.debug("从LTM检索到{}条内容: userId={}", ltmContents.size(), userId);
        return ltmContents;
    }

    /**
     * 从情景记忆创建Content对象
     */
    private Content createContentFromEpisodicMemory(EpisodicMemory memory) {
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("source", "ltm_episodic");
        metadataMap.put("memory_id", memory.getId());
        metadataMap.put("episode_id", memory.getEpisodeId());
        metadataMap.put("importance_score", memory.getImportanceScore());
        metadataMap.put("score", memory.getImportanceScore());
        metadataMap.put("timestamp", memory.getTimestamp());
        metadataMap.put("access_count", memory.getAccessCount());
        
        if (memory.getContextMetadata() != null) {
            metadataMap.putAll(memory.getContextMetadata());
        }

        // 构建内容文本
        StringBuilder contentText = new StringBuilder();
        contentText.append("历史对话记忆：\n");
        contentText.append(memory.getContent());
        
        if (memory.getTimestamp() != null) {
            contentText.append("\n时间：").append(new java.util.Date(memory.getTimestamp()));
        }

        Metadata metadata = Metadata.from(metadataMap);
        return Content.from(TextSegment.from(contentText.toString(), metadata));
    }

    /**
     * 从语义记忆创建Content对象
     */
    private Content createContentFromSemanticMemory(SemanticMemory memory) {
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("source", "ltm_semantic");
        metadataMap.put("memory_id", memory.getId());
        metadataMap.put("concept", memory.getConcept());
        metadataMap.put("confidence", memory.getConfidence());
        metadataMap.put("score", memory.getConfidence());
        metadataMap.put("evidence_count", memory.getEvidenceCount());
        
        if (memory.getSourceEpisodes() != null) {
            metadataMap.put("source_episodes", memory.getSourceEpisodes());
        }

        // 构建内容文本
        StringBuilder contentText = new StringBuilder();
        contentText.append("用户相关知识：\n");
        contentText.append("概念：").append(memory.getConcept()).append("\n");
        contentText.append("知识：").append(memory.getKnowledge());
        
        if (memory.getConfidence() != null) {
            contentText.append("\n可信度：").append(String.format("%.2f", memory.getConfidence()));
        }

        Metadata metadata = Metadata.from(metadataMap);
        return Content.from(TextSegment.from(contentText.toString(), metadata));
    }

    /**
     * 合并和重排序结果
     */
    private List<Content> mergeAndRerankResults(List<Content> baseResults, List<Content> ltmResults) {
        List<Content> mergedResults = new ArrayList<>();

        // 计算各部分的数量分配
        int totalResults = Math.min(baseResults.size() + ltmResults.size(), 20); // 限制总结果数
        int ltmCount = (int) Math.round(totalResults * ltmWeight);
        int baseCount = totalResults - ltmCount;

        // 添加基础结果
        baseResults.stream()
            .limit(baseCount)
            .forEach(mergedResults::add);

        // 添加LTM结果
        ltmResults.stream()
            .limit(ltmCount)
            .forEach(mergedResults::add);

        // 简单的重排序：LTM结果优先级稍高
        List<Content> rerankedResults = new ArrayList<>();
        
        // 交替添加LTM和基础结果以平衡相关性
        int ltmIndex = 0, baseIndex = 0;
        while (rerankedResults.size() < mergedResults.size()) {
            // 每添加2个基础结果，添加1个LTM结果
            if (baseIndex < baseCount && (ltmIndex >= ltmCount || rerankedResults.size() % 3 != 0)) {
                rerankedResults.add(baseResults.get(baseIndex++));
            } else if (ltmIndex < ltmCount) {
                // 为LTM内容添加特殊标记
                Content ltmContent = ltmResults.get(ltmIndex++);
                Content markedLtmContent = enhanceLTMContent(ltmContent);
                rerankedResults.add(markedLtmContent);
            }
        }

        log.debug("合并检索结果: base={}, ltm={}, total={}", baseCount, ltmCount, rerankedResults.size());
        return rerankedResults;
    }

    /**
     * 增强LTM内容的可读性
     */
    private Content enhanceLTMContent(Content ltmContent) {
        // 在LTM内容前添加个性化标识
        String enhancedText = "[个性化内容] " + ltmContent.textSegment().text();

        // 保留原有元数据并添加增强标识
        Map<String, Object> enhancedMetadata = new HashMap<>(ltmContent.textSegment().metadata().toMap());
        enhancedMetadata.put("enhanced", true);
        enhancedMetadata.put("personalized", true);

        Metadata metadata = Metadata.from(enhancedMetadata);
        return Content.from(TextSegment.from(enhancedText, metadata));
    }

    private Long parseNumericIdentifier(Object candidate) {
        if (candidate == null) {
            return null;
        }
        String text = candidate.toString();
        if (text.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException ex) {
            long hashed = Math.abs(text.hashCode());
            if (hashed > 0) {
                log.debug("使用哈希值替代非数值标识: {} -> {}", text, hashed);
                return hashed;
            }
            log.debug("无法解析数字标识: {}", text);
            return null;
        }
    }
}
