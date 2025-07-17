package com.leyue.smartcs.rag.processor;

import com.leyue.smartcs.domain.rag.model.*;
import com.leyue.smartcs.domain.rag.processor.IndexProcessor;
import com.leyue.smartcs.rag.vdb.KeywordStore;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParentChildIndexProcessor extends IndexProcessor {

    private final VectorStore vectorStore;
    private final KeywordStore keywordStore;

    public ParentChildIndexProcessor(VectorStore vectorStore, KeywordStore keywordStore) {
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
        SegmentationRule segRule = rule.getSegmentation();
        boolean automatic = "automatic".equals(rule.getMode());

        TokenTextSplitter parentSplitter = getSplitter(segRule, automatic, ctx.getEmbeddingModelInstance());
        parentSplitter = new TokenTextSplitter();

        TokenTextSplitter childSplitter = new TokenTextSplitter();

        List<Document> parentChunks = parentSplitter.apply(docs);
        List<Document> allChunks = new ArrayList<>();

        for (int parentIndex = 0; parentIndex < parentChunks.size(); parentIndex++) {
            Document parent = parentChunks.get(parentIndex);
            String parentId = UUID.randomUUID().toString();
            String parentHash = DigestUtils.sha256Hex(parent.getText());
            parent.getMetadata().put("doc_id", parentId);
            parent.getMetadata().put("doc_hash", parentHash);
            parent.getMetadata().put("is_parent", true);
            allChunks.add(parent);

            List<Document> childChunks = childSplitter.apply(List.of(parent));
            for (int childIndex = 0; childIndex < childChunks.size(); childIndex++) {
                Document child = childChunks.get(childIndex);
                String childId = UUID.randomUUID().toString();
                String childHash = DigestUtils.sha256Hex(child.getText());
                child.getMetadata().put("doc_id", childId);
                child.getMetadata().put("doc_hash", childHash);
                child.getMetadata().put("parent_id", parentId);
                allChunks.add(child);
            }
        }
        return allChunks;
    }

    @Override
    public void load(Dataset dataset, List<Document> docs, boolean withKeywords, ProcessorContext ctx) {
        // 同 Paragraph
        if ("high_quality".equals(dataset.getIndexingTechnique())) {
            vectorStore.add(docs);
        }
        if (withKeywords) {
            // 同上
        }
    }

    @Override
    public void clean(Dataset dataset, List<String> nodeIds, boolean withKeywords, ProcessorContext ctx) {
        // 同 Paragraph
    }

    @Override
    public List<Document> retrieve(RetrieveParams params) {
        // 同 Paragraph，但可能添加父子过滤
        // 例如使用 Filter.Expression for parent_id
        return null;
    }
} 