package com.leyue.smartcs.knowledge.parser.merger;

import com.leyue.smartcs.knowledge.parser.extractor.ApacheTikaTextExtractor;
import com.leyue.smartcs.knowledge.parser.extractor.TikaTableExtractor;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.leyue.smartcs.service.TracingSupport;

/**
 * 多模态内容合并器
 * 整合文本、表格、OCR等多种解析结果
 * 基于LangChain4j框架的Document统一格式
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MultiModalContentMerger {

    private final ApacheTikaTextExtractor tikaTextExtractor;
    private final TikaTableExtractor tikaTableExtractor;

    @Value("${pdf.merger.parallel-processing:true}")
    private boolean parallelProcessing;

    @Value("${pdf.merger.content-priority:text,ocr,table}")
    private String contentPriority;

    @Value("${pdf.merger.quality-threshold:0.5}")
    private double qualityThreshold;

    @Value("${pdf.merger.max-content-length:50000}")
    private int maxContentLength;

    // 并行处理线程池
    private final Executor executor = Executors.newCachedThreadPool();

    /**
     * 合并多模态内容
     * 
     * @param inputStream 文档输入流
     * @param sourceFileName 源文件名
     * @return 合并后的Document列表
     */
    public List<Document> mergeMultiModalContent(InputStream inputStream, String sourceFileName) {
        log.info("开始多模态内容合并: fileName={}, parallel={}", sourceFileName, parallelProcessing);

        try {
            if (parallelProcessing) {
                return mergeParallel(inputStream, sourceFileName);
            } else {
                return mergeSequential(inputStream, sourceFileName);
            }
        } catch (Exception e) {
            log.error("多模态内容合并失败: fileName={}", sourceFileName, e);
            return createFallbackDocuments(sourceFileName, e.getMessage());
        }
    }

    /**
     * 并行合并内容
     */
    private List<Document> mergeParallel(InputStream inputStream, String sourceFileName) {
        try {
            // 克隆输入流用于并行处理
            byte[] data = inputStream.readAllBytes();

            // 并行提取不同类型的内容
            CompletableFuture<Document> textFuture = TracingSupport.supplyAsync(() -> {
                try {
                    return tikaTextExtractor.extractText(
                            new java.io.ByteArrayInputStream(data), sourceFileName);
                } catch (Exception e) {
                    log.warn("并行文本提取失败: fileName={}", sourceFileName, e);
                    return null;
                }
            }, executor);

            CompletableFuture<List<TikaTableExtractor.TableData>> tableFuture = TracingSupport.supplyAsync(() -> {
                try {
                    return tikaTableExtractor.extractTables(
                            new java.io.ByteArrayInputStream(data), sourceFileName);
                } catch (Exception e) {
                    log.warn("并行表格提取失败: fileName={}", sourceFileName, e);
                    return new ArrayList<>();
                }
            }, executor);

            CompletableFuture<List<Document>> ocrFuture = TracingSupport.supplyAsync(() -> {
                try {
                    // OCR处理暂时返回空列表，后续可以扩展
                    return new ArrayList<Document>();
                } catch (Exception e) {
                    log.warn("并行OCR处理失败: fileName={}", sourceFileName, e);
                    return new ArrayList<Document>();
                }
            }, executor);

            // 等待所有任务完成
            Document textDoc = textFuture.get();
            List<TikaTableExtractor.TableData> tables = tableFuture.get();
            List<Document> ocrDocs = ocrFuture.get();

            return mergeResults(textDoc, tables, ocrDocs, sourceFileName);

        } catch (Exception e) {
            log.error("并行合并处理失败: fileName={}", sourceFileName, e);
            throw new RuntimeException("并行合并失败", e);
        }
    }

    /**
     * 顺序合并内容
     */
    private List<Document> mergeSequential(InputStream inputStream, String sourceFileName) {
        try {
            byte[] data = inputStream.readAllBytes();

            // 顺序提取内容
            Document textDoc = tikaTextExtractor.extractText(
                    new java.io.ByteArrayInputStream(data), sourceFileName);

            List<TikaTableExtractor.TableData> tables = tikaTableExtractor.extractTables(
                    new java.io.ByteArrayInputStream(data), sourceFileName);

            // OCR处理暂时返回空列表，后续可以扩展
            List<Document> ocrDocs = new ArrayList<>();

            return mergeResults(textDoc, tables, ocrDocs, sourceFileName);

        } catch (Exception e) {
            log.error("顺序合并处理失败: fileName={}", sourceFileName, e);
            throw new RuntimeException("顺序合并失败", e);
        }
    }

    /**
     * 合并提取结果
     */
    private List<Document> mergeResults(Document textDoc, List<TikaTableExtractor.TableData> tables,
                                       List<Document> ocrDocs, String sourceFileName) {
        List<Document> mergedDocuments = new ArrayList<>();

        try {
            // 创建内容质量评估器
            ContentQualityAssessor qualityAssessor = new ContentQualityAssessor();

            // 根据配置的优先级处理内容
            String[] priorities = contentPriority.split(",");
            
            for (String priority : priorities) {
                switch (priority.trim().toLowerCase()) {
                    case "text":
                        if (textDoc != null && qualityAssessor.assessQuality(textDoc) >= qualityThreshold) {
                            mergedDocuments.add(enhanceDocument(textDoc, "text", sourceFileName));
                            log.debug("添加文本内容: length={}", textDoc.text().length());
                        }
                        break;

                    case "table":
                        for (TikaTableExtractor.TableData table : tables) {
                            if (table != null && !table.isEmpty()) {
                                Document tableDoc = convertTableToDocument(table, sourceFileName);
                                if (qualityAssessor.assessQuality(tableDoc) >= qualityThreshold) {
                                    mergedDocuments.add(enhanceDocument(tableDoc, "table", sourceFileName));
                                    log.debug("添加表格内容: table={}, rows={}", 
                                            table.getTableIndex(), table.getRowCount());
                                }
                            }
                        }
                        break;

                    case "ocr":
                        for (Document ocrDoc : ocrDocs) {
                            if (ocrDoc != null && qualityAssessor.assessQuality(ocrDoc) >= qualityThreshold) {
                                mergedDocuments.add(enhanceDocument(ocrDoc, "ocr", sourceFileName));
                                log.debug("添加OCR内容: length={}", ocrDoc.text().length());
                            }
                        }
                        break;

                    default:
                        log.warn("未知的内容优先级: {}", priority);
                        break;
                }
            }

            // 内容长度检查和截断
            mergedDocuments = limitContentLength(mergedDocuments);

            log.info("内容合并完成: fileName={}, documentCount={}", sourceFileName, mergedDocuments.size());
            return mergedDocuments;

        } catch (Exception e) {
            log.error("结果合并失败: fileName={}", sourceFileName, e);
            return createFallbackDocuments(sourceFileName, e.getMessage());
        }
    }

    /**
     * 将表格数据转换为Document
     */
    private Document convertTableToDocument(TikaTableExtractor.TableData table, String sourceFileName) {
        String tableContent = table.toJson(); // 使用JSON格式存储表格
        
        Metadata metadata = Metadata.from("contentType", "table")
                .put("sourceFileName", sourceFileName)
                .put("tableIndex", String.valueOf(table.getTableIndex()))
                .put("rowCount", String.valueOf(table.getRowCount()))
                .put("columnCount", String.valueOf(table.getColumnCount()))
                .put("hasHeaders", String.valueOf(table.hasHeaders()));

        return Document.from(tableContent, metadata);
    }

    /**
     * 增强Document的元数据
     */
    private Document enhanceDocument(Document doc, String contentType, String sourceFileName) {
        Metadata enhancedMetadata = Metadata.from("contentType", contentType)
                .put("sourceFileName", sourceFileName)
                .put("contentLength", String.valueOf(doc.text().length()))
                .put("processingTimestamp", String.valueOf(System.currentTimeMillis()))
                .put("merger", "multimodal");

        // 保留原有元数据
        if (doc.metadata() != null) {
            for (Map.Entry<String, Object> entry : doc.metadata().toMap().entrySet()) {
                enhancedMetadata = enhancedMetadata.put(entry.getKey(), entry.getValue().toString());
            }
        }

        return Document.from(doc.text(), enhancedMetadata);
    }

    /**
     * 限制内容长度
     */
    private List<Document> limitContentLength(List<Document> documents) {
        List<Document> limitedDocs = new ArrayList<>();
        
        for (Document doc : documents) {
            if (doc.text().length() > maxContentLength) {
                String truncatedText = doc.text().substring(0, maxContentLength) + "... [内容已截断]";
                Metadata metadata = doc.metadata() != null ? doc.metadata() : Metadata.from("", "");
                metadata = metadata.put("truncated", "true")
                               .put("originalLength", String.valueOf(doc.text().length()));
                limitedDocs.add(Document.from(truncatedText, metadata));
                log.warn("内容已截断: originalLength={}, maxLength={}", doc.text().length(), maxContentLength);
            } else {
                limitedDocs.add(doc);
            }
        }
        
        return limitedDocs;
    }

    /**
     * 创建降级Document
     */
    private List<Document> createFallbackDocuments(String sourceFileName, String errorMessage) {
        log.warn("创建降级Document: fileName={}, error={}", sourceFileName, errorMessage);
        
        Metadata fallbackMetadata = Metadata.from("contentType", "fallback")
                .put("sourceFileName", sourceFileName)
                .put("errorMessage", errorMessage)
                .put("processingStatus", "failed");

        Document fallbackDoc = Document.from(
                "文档解析失败，请检查文档格式和内容。错误信息: " + errorMessage, 
                fallbackMetadata);

        return List.of(fallbackDoc);
    }

    /**
     * 获取配置信息
     */
    public String getConfigurationInfo() {
        return String.format("多模态合并配置 - 并行处理:%s, 内容优先级:%s, 质量阈值:%.2f, 最大长度:%d",
                parallelProcessing, contentPriority, qualityThreshold, maxContentLength);
    }

    /**
     * 内容质量评估器
     */
    private static class ContentQualityAssessor {
        
        /**
         * 评估内容质量 (0.0 - 1.0)
         */
        public double assessQuality(Document document) {
            if (document == null || document.text() == null || document.text().trim().isEmpty()) {
                return 0.0;
            }

            String text = document.text().trim();
            double score = 0.0;

            // 基础分数：有内容
            score += 0.3;

            // 长度评分
            if (text.length() >= 20) {
                score += 0.2;
            }
            if (text.length() >= 100) {
                score += 0.1;
            }

            // 内容多样性评分
            if (containsAlphanumeric(text)) {
                score += 0.2;
            }

            // OCR置信度评分（如果有）
            if (document.metadata() != null && document.metadata().toMap().containsKey("ocrConfidence")) {
                try {
                    double confidence = Double.parseDouble(
                            document.metadata().toMap().get("ocrConfidence").toString());
                    score += confidence * 0.2;
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }

            return Math.min(score, 1.0);
        }

        private boolean containsAlphanumeric(String text) {
            return text.matches(".*[a-zA-Z0-9\u4e00-\u9fff].*");
        }
    }
}
