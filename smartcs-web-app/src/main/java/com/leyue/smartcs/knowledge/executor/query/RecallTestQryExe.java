package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.RecallTestQry;
import com.leyue.smartcs.dto.knowledge.RecallTestResultDTO;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecallTestQryExe {

  private final EmbeddingStore<TextSegment> embeddingStore;
  private final DynamicModelManager dynamicModelManager;
  private final ChunkGateway chunkGateway;

  public MultiResponse<RecallTestResultDTO> execute(RecallTestQry qry) {
    String method = StringUtils.hasText(qry.getRetrievalMethod()) ? qry.getRetrievalMethod() : "vector";
    int topK = qry.getTopK() != null ? qry.getTopK() : 10;
    float threshold = qry.getScoreThreshold() != null ? qry.getScoreThreshold() : 0f;

    switch (method) {
      case "full_text":
        return MultiResponse.of(fullTextSearch(qry, topK));
      case "hybrid":
        List<RecallTestResultDTO> v = vectorSearch(qry, topK, threshold);
        List<RecallTestResultDTO> f = fullTextSearch(qry, topK);
        // 简单融合：按分数降序合并去重
        Map<Long, RecallTestResultDTO> merged = new java.util.LinkedHashMap<>();
        v.forEach(r -> merged.putIfAbsent(r.getChunkId(), r));
        f.forEach(r -> merged.merge(r.getChunkId(), r, (a, b) -> a.getScore() >= b.getScore() ? a : b));
        List<RecallTestResultDTO> hybrid = new ArrayList<>(merged.values());
        hybrid.sort(Comparator.comparing(RecallTestResultDTO::getScore).reversed());
        if (hybrid.size() > topK) {
          hybrid = hybrid.subList(0, topK);
        }
        return MultiResponse.of(hybrid);
      default:
        return MultiResponse.of(vectorSearch(qry, topK, threshold));
    }
  }

  private List<RecallTestResultDTO> vectorSearch(RecallTestQry qry, int topK, float threshold) {
    try {
      // 选择嵌入模型（如果前端后续提供模型ID可扩展）
      EmbeddingModel embeddingModel = dynamicModelManager.getEmbeddingModel(null);
      dev.langchain4j.data.embedding.Embedding q = embeddingModel.embed(qry.getQuery()).content();

      EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
          .queryEmbedding(q)
          .maxResults(topK)
          .minScore(Double.valueOf(threshold))
          .build();

      List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
      if (CollectionUtils.isEmpty(matches)) {
        return java.util.Collections.emptyList();
      }

      List<RecallTestResultDTO> results = new ArrayList<>();
      for (EmbeddingMatch<TextSegment> match : matches) {
        TextSegment segment = match.embedded();
        RecallTestResultDTO dto = new RecallTestResultDTO();
        dto.setScore(match.score().floatValue());
        dto.setContent(segment.text());
        if (segment.metadata() != null) {
          Map<String, Object> md = segment.metadata().toMap();
          dto.setMetadata(md);
          // 尝试从元数据提取 chunkId/contentId/docTitle/chunkIndex
          Object chunkId = md.getOrDefault("chunkId", md.get("id"));
          if (chunkId != null) {
            try {
              dto.setChunkId(Long.valueOf(chunkId.toString()));
            } catch (NumberFormatException ignore) {
            }
          }
          Object contentId = md.get("contentId");
          if (contentId != null) {
            try {
              dto.setContentId(Long.valueOf(contentId.toString()));
            } catch (NumberFormatException ignore) {
            }
          }
          Object title = md.get("title");
          if (title != null) {
            dto.setDocTitle(title.toString());
          }
          Object idx = md.get("chunkIndex");
          if (idx != null) {
            try {
              dto.setChunkIndex(Integer.parseInt(idx.toString()));
            } catch (NumberFormatException ignore) {
            }
          }
        }

        // 若缺失 docTitle 或 chunkIndex，可尝试通过 chunkId 回表补全
        if (dto.getChunkId() != null && (dto.getDocTitle() == null || dto.getChunkIndex() == null)) {
          Chunk chunk = chunkGateway.findById(dto.getChunkId());
          if (chunk != null) {
            dto.setContentId(chunk.getContentId());
            dto.setChunkIndex(parseChunkIndex(chunk.getChunkIndex()));
            if (dto.getDocTitle() == null && StringUtils.hasText(chunk.getMetadata())) {
              try {
                Map<String, Object> m = JSON.parseObject(chunk.getMetadata());
                Object t = m.get("title");
                if (t != null) {
                  dto.setDocTitle(t.toString());
                }
              } catch (Exception ignored) {
              }
            }
          }
        }

        results.add(dto);
      }

      return results;
    } catch (Exception e) {
      log.error("向量召回测试失败", e);
      return java.util.Collections.emptyList();
    }
  }

  private List<RecallTestResultDTO> fullTextSearch(RecallTestQry qry, int topK) {
    // 占位实现：暂未提供全文检索，返回空列表，前端可正常处理
    return java.util.Collections.emptyList();
  }

  private Integer parseChunkIndex(String chunkIndex) {
    if (!StringUtils.hasText(chunkIndex)) {
      return null;
    }
    try {
      return Integer.parseInt(chunkIndex);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}


