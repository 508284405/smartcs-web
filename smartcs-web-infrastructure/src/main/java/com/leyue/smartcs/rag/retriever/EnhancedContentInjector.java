package com.leyue.smartcs.rag.retriever;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容注入器
 * 负责将检索到的内容注入到聊天消息中
 */
@Component
@Slf4j
public class EnhancedContentInjector implements ContentInjector {

    private static final String DEFAULT_KNOWLEDGE_TEMPLATE = 
        "以下是相关的知识内容，请基于这些内容回答用户的问题：\n\n%s";
    
    private static final String CONTENT_SEPARATOR = "\n\n---\n\n";

    @Override
    public ChatMessage inject(List<Content> contents, ChatMessage chatMessage) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要注入，返回原始消息");
            return chatMessage;
        }

        log.debug("开始注入内容到消息: contentCount={}, messageType={}", 
                 contents.size(), chatMessage.getClass().getSimpleName());

        // 合并所有内容
        String knowledgeText = contents.stream()
            .map(content -> content.textSegment().text())
            .collect(Collectors.joining(CONTENT_SEPARATOR));

        // 格式化知识内容
        String formattedKnowledge = String.format(DEFAULT_KNOWLEDGE_TEMPLATE, knowledgeText);

        // 只处理UserMessage，LangChain4j的ContentInjector主要用于增强用户消息
        if (chatMessage instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) chatMessage;
            String enhancedText = formattedKnowledge + "\n\n用户问题：" + userMessage.singleText();
            
            log.info("内容注入完成: contentCount={}, knowledgeLength={}", 
                    contents.size(), knowledgeText.length());
            
            return UserMessage.from(enhancedText);
        }

        // 对于非UserMessage，直接返回原消息
        log.debug("消息类型 {} 不支持内容注入，返回原消息", chatMessage.getClass().getSimpleName());
        return chatMessage;
    }

    /**
     * 批量消息注入方法（向后兼容）
     * 处理消息列表的内容注入
     * 
     * @param contents 要注入的内容  
     * @param messages 原始消息列表
     * @return 注入后的消息列表
     */
    public List<ChatMessage> injectToMessages(List<Content> contents, List<ChatMessage> messages) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要注入，返回原始消息列表");
            return messages;
        }

        log.debug("开始批量注入内容到消息: contentCount={}, messageCount={}", 
                 contents.size(), messages.size());

        // 找到最后一个UserMessage并注入内容
        List<ChatMessage> injectedMessages = new java.util.ArrayList<>();
        boolean injected = false;

        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if (!injected && message instanceof UserMessage) {
                // 使用标准ContentInjector接口注入到最后一个UserMessage
                ChatMessage enhancedMessage = inject(contents, message);
                injectedMessages.add(0, enhancedMessage);
                injected = true;
            } else {
                injectedMessages.add(0, message);
            }
        }

        // 如果没有UserMessage，在开头添加一个SystemMessage包含知识内容
        if (!injected) {
            String knowledgeText = contents.stream()
                .map(content -> content.textSegment().text())
                .collect(Collectors.joining(CONTENT_SEPARATOR));
            String formattedKnowledge = String.format(DEFAULT_KNOWLEDGE_TEMPLATE, knowledgeText);
            injectedMessages.add(0, SystemMessage.from(formattedKnowledge));
        }

        log.info("批量内容注入完成: contentCount={}, messageCount={}", 
                contents.size(), injectedMessages.size());

        return injectedMessages;
    }

    /**
     * 自定义注入策略：在用户消息中注入内容
     * 
     * @param contents 要注入的内容
     * @param messages 原始消息列表
     * @param injectionTemplate 注入模板
     * @return 注入后的消息列表
     */
    public List<ChatMessage> injectIntoUserMessage(List<Content> contents, 
                                                 List<ChatMessage> messages,
                                                 String injectionTemplate) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要注入到用户消息");
            return messages;
        }

        log.debug("开始将内容注入到用户消息: contentCount={}, messageCount={}", 
                 contents.size(), messages.size());

        // 合并所有内容
        String knowledgeText = contents.stream()
            .map(content -> content.textSegment().text())
            .collect(Collectors.joining(CONTENT_SEPARATOR));

        // 使用自定义模板格式化知识内容
        String template = injectionTemplate != null ? injectionTemplate : DEFAULT_KNOWLEDGE_TEMPLATE;
        String formattedKnowledge = String.format(template, knowledgeText);

        // 创建新的消息列表并修改最后一个用户消息
        List<ChatMessage> injectedMessages = new java.util.ArrayList<>(messages);
        
        // 找到最后一个用户消息并增强
        for (int i = injectedMessages.size() - 1; i >= 0; i--) {
            if (injectedMessages.get(i) instanceof UserMessage) {
                UserMessage originalUser = (UserMessage) injectedMessages.get(i);
                String enhancedUserText = formattedKnowledge + "\n\n用户问题：" + originalUser.singleText();
                injectedMessages.set(i, UserMessage.from(enhancedUserText));
                break;
            }
        }

        log.info("用户消息内容注入完成: contentCount={}, knowledgeLength={}", 
                contents.size(), knowledgeText.length());

        return injectedMessages;
    }

    /**
     * 结构化内容注入
     * 按内容类型分类注入
     * 
     * @param contents 要注入的内容
     * @param messages 原始消息列表
     * @param query 查询对象
     * @return 注入后的消息列表
     */
    public List<ChatMessage> injectWithStructure(List<Content> contents, 
                                               List<ChatMessage> messages,
                                               Query query) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要结构化注入");
            return messages;
        }

        log.debug("开始结构化注入内容: contentCount={}, messageCount={}, query={}", 
                 contents.size(), messages.size(), query.text());

        // 按内容来源分类
        StringBuilder structuredKnowledge = new StringBuilder();
        structuredKnowledge.append("基于以下相关知识回答用户问题：\n\n");

        for (int i = 0; i < contents.size(); i++) {
            Content content = contents.get(i);
            String text = content.textSegment().text();
            
            structuredKnowledge.append("【知识片段 ").append(i + 1).append("】\n");
            structuredKnowledge.append(text);
            
            if (i < contents.size() - 1) {
                structuredKnowledge.append("\n\n");
            }
        }

        // 创建新的消息列表
        List<ChatMessage> injectedMessages = new java.util.ArrayList<>(messages);

        // 在系统消息中注入结构化知识
        boolean hasSystemMessage = messages.stream()
            .anyMatch(msg -> msg instanceof SystemMessage);

        if (hasSystemMessage) {
            for (int i = 0; i < injectedMessages.size(); i++) {
                if (injectedMessages.get(i) instanceof SystemMessage) {
                    SystemMessage originalSystem = (SystemMessage) injectedMessages.get(i);
                    String enhancedSystemText = originalSystem.text() + "\n\n" + structuredKnowledge.toString();
                    injectedMessages.set(i, SystemMessage.from(enhancedSystemText));
                    break;
                }
            }
        } else {
            injectedMessages.add(0, SystemMessage.from(structuredKnowledge.toString()));
        }

        log.info("结构化内容注入完成: contentCount={}, knowledgeLength={}", 
                contents.size(), structuredKnowledge.length());

        return injectedMessages;
    }

    /**
     * 智能内容注入
     * 根据查询和内容的相关性动态调整注入策略
     * 
     * @param contents 要注入的内容
     * @param messages 原始消息列表
     * @param query 查询对象
     * @param confidenceThreshold 置信度阈值
     * @return 注入后的消息列表
     */
    public List<ChatMessage> injectIntelligently(List<Content> contents, 
                                               List<ChatMessage> messages,
                                               Query query,
                                               double confidenceThreshold) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要智能注入");
            return messages;
        }

        log.debug("开始智能注入内容: contentCount={}, messageCount={}, threshold={}", 
                 contents.size(), messages.size(), confidenceThreshold);

        // 过滤高置信度的内容
        List<Content> highConfidenceContents = contents.stream()
            .filter(content -> {
                // 这里可以实现更复杂的置信度计算逻辑
                // 暂时返回所有内容
                return true;
            })
            .collect(Collectors.toList());

        if (highConfidenceContents.isEmpty()) {
            log.info("没有高置信度的内容，跳过注入");
            return messages;
        }

        // 根据内容数量选择注入策略
        if (highConfidenceContents.size() <= 3) {
            // 内容较少，使用结构化注入
            return injectWithStructure(highConfidenceContents, messages, query);
        } else {
            // 内容较多，使用标准注入
            return injectToMessages(highConfidenceContents, messages);
        }
    }
}