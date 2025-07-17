package com.leyue.smartcs.rag.processor;

import com.leyue.smartcs.domain.rag.model.*;
import com.leyue.smartcs.domain.rag.processor.IndexProcessor;
import com.leyue.smartcs.rag.vdb.KeywordStore;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.ai.document.Document;

import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParagraphIndexProcessor extends IndexProcessor {
    
    private final VectorStore vectorStore;
    private final KeywordStore keywordStore;
    
    public ParagraphIndexProcessor(VectorStore vectorStore, KeywordStore keywordStore) {
        this.vectorStore = vectorStore;
        this.keywordStore = keywordStore;
    }
    
    @Override
    public List<Document> extract(ExtractSetting setting, ProcessorContext ctx) {
        try {
            Resource resource = new UrlResource(setting.getFileUrl());
            DocumentReader reader = new TikaDocumentReader(resource);
            return reader.read();
        } catch (Exception e) {
            throw new RuntimeException("提取文档失败", e);
        }
    }
    
    @Override
    public List<Document> transform(List<Document> docs, ProcessorContext ctx) {
        Rule rule = ctx.getProcessRule();
        boolean automatic = "automatic".equals(rule.getMode());
        TokenTextSplitter splitter = getSplitter(rule.getSegmentation(), automatic, ctx.getEmbeddingModelInstance());
        
        List<Document> splitDocs = splitter.apply(docs);
        List<Document> result = new ArrayList<>();
        
        for (Document doc : splitDocs) {
            String content = doc.getText().trim();
            if (!content.isEmpty()) {
                String docId = UUID.randomUUID().toString();
                String docHash = DigestUtils.sha256Hex(content);
                doc.getMetadata().put("doc_id", docId);
                doc.getMetadata().put("doc_hash", docHash);
                result.add(doc);
            }
        }
        return result;
    }
    
    @Override
    public void load(Dataset dataset, List<Document> docs, boolean withKeywords, ProcessorContext ctx) {
        if ("high_quality".equals(dataset.getIndexingTechnique())) {
            vectorStore.add(docs);
        }
        if (withKeywords) {
            List<String> keywordsList = ctx.getKeywordsList();
            if (keywordsList != null && !keywordsList.isEmpty()) {
                keywordStore.addTexts(docs, keywordsList);
            } else {
                keywordStore.addTexts(docs);
            }
        }
    }
    
    @Override
    public void clean(Dataset dataset, List<String> nodeIds, boolean withKeywords, ProcessorContext ctx) {
        if ("high_quality".equals(dataset.getIndexingTechnique())) {
            if (nodeIds != null && !nodeIds.isEmpty()) {
                vectorStore.delete(nodeIds);
            }
        }
        if (withKeywords) {
            if (nodeIds != null && !nodeIds.isEmpty()) {
                keywordStore.deleteByIds(nodeIds);
            } else {
                keywordStore.delete();
            }
        }
    }
    
    @Override
    public List<Document> retrieve(RetrieveParams params) {
        SearchRequest request = SearchRequest.builder().query(params.getQuery())
                .topK(params.getTopK())
                .similarityThreshold(params.getScoreThreshold()).build();
        
        List<Document> results = vectorStore.similaritySearch(request);
        List<Document> filtered = new ArrayList<>();
        for (Document doc : results) {
            float score = (float) doc.getMetadata().getOrDefault("score", 0.0f);
            if (score > params.getScoreThreshold()) {
                filtered.add(doc);
            }
        }
        // TODO: 处理 rerankingModel
        return filtered;
    }
} 