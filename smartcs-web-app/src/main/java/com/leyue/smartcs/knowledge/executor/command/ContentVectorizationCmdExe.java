package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 内容向量化执行器
 */
@Component
@Slf4j
public class ContentVectorizationCmdExe {

    @Autowired
    private ContentGateway contentGateway;

    /**
     * 执行内容向量化
     */
    public Response execute(Long contentId) {
        log.info("执行内容向量化, 内容ID: {}", contentId);
        
        try {
            
            // 查询内容
            Content content = contentGateway.findById(contentId);
            if (content == null) {
                throw new BizException("内容不存在");
            }
            
            // 状态校验
            if (!"parsed".equals(content.getStatus())) {
                log.warn("内容状态不正确, 当前状态: {}, 期望状态: parsed", content.getStatus());
                throw new BizException("只有解析完成的内容才能向量化");
            }
            
            // 检查是否有提取的文本
            if (!StringUtils.hasText(content.getTextExtracted())) {
                log.warn("内容没有提取的文本, ID: {}", contentId);
                throw new BizException("内容没有提取的文本，无法向量化");
            }
            
            // 更新内容状态为向量化中
            content.setStatus("vectorizing");
            contentGateway.save(content);
            
            // 执行向量化逻辑
            performVectorization(content);
            
            // 更新状态为向量化完成
            content.setStatus("vectorized");
            contentGateway.save(content);
            
            log.info("内容向量化完成, ID: {}", contentId);
            
            return Response.buildSuccess();
            
        } catch (Exception e) {
            log.error("内容向量化失败", e);
            
            // 更新状态为向量化失败
            try {
                Content content = contentGateway.findById(contentId);
                if (content != null) {
                    content.setStatus("vectorize_failed");
                    contentGateway.save(content);
                }
            } catch (Exception ex) {
                log.error("更新向量化失败状态出错", ex);
            }
            
            throw new BizException("内容向量化失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行实际的向量化逻辑
     */
    private void performVectorization(Content content) {
        log.info("开始向量化内容, ID: {}, 文本长度: {}", 
            content.getId(), content.getTextExtracted().length());
        
        try {
            // 1. 文本分段
            List<String> textSegments = splitTextIntoSegments(content.getTextExtracted());
            log.info("文本分段完成, 共 {} 段", textSegments.size());
            
            // 2. 生成向量
            for (int i = 0; i < textSegments.size(); i++) {
                String segment = textSegments.get(i);
                List<Double> vector = generateEmbedding(segment);
                
                // 3. 存储向量到向量数据库
                storeVector(content.getId(), i, segment, vector);
                
                log.debug("第 {} 段向量化完成, 向量维度: {}", i + 1, vector.size());
            }
            
            log.info("内容向量化处理完成, 内容ID: {}, 总段数: {}", content.getId(), textSegments.size());
            
        } catch (Exception e) {
            log.error("向量化处理失败, 内容ID: {}", content.getId(), e);
            throw new RuntimeException("向量化处理失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将文本分段
     */
    private List<String> splitTextIntoSegments(String text) {
        // TODO: 实现智能文本分段逻辑
        log.debug("开始分段文本, 原始长度: {}", text.length());
        
        // 简单实现：按段落分割，每段最大1000字符
        List<String> segments = new java.util.ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");
        
        StringBuilder currentSegment = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (currentSegment.length() + paragraph.length() > 1000) {
                if (currentSegment.length() > 0) {
                    segments.add(currentSegment.toString().trim());
                    currentSegment = new StringBuilder();
                }
            }
            currentSegment.append(paragraph).append("\n\n");
        }
        
        if (currentSegment.length() > 0) {
            segments.add(currentSegment.toString().trim());
        }
        
        return segments;
    }
    
    /**
     * 生成文本嵌入向量
     */
    private List<Double> generateEmbedding(String text) {
        // TODO: 调用AI模型生成嵌入向量
        log.debug("生成文本嵌入向量, 文本长度: {}", text.length());
        
        // 临时实现：返回模拟向量
        List<Double> vector = new java.util.ArrayList<>();
        for (int i = 0; i < 1536; i++) { // OpenAI text-embedding-ada-002 的向量维度
            vector.add(Math.random() * 2 - 1); // [-1, 1] 范围的随机数
        }
        
        return vector;
    }
    
    /**
     * 存储向量到向量数据库
     */
    private void storeVector(Long contentId, int segmentIndex, String text, List<Double> vector) {
        // TODO: 存储到向量数据库（如Pinecone、Milvus等）
        log.debug("存储向量, 内容ID: {}, 段索引: {}, 向量维度: {}", 
            contentId, segmentIndex, vector.size());
        
        // 临时实现：仅记录日志
        log.info("向量已存储 - 内容ID: {}, 段落: {}, 文本预览: {}", 
            contentId, segmentIndex, text.length() > 50 ? text.substring(0, 50) + "..." : text);
    }
} 