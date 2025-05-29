package com.leyue.smartcs.domain.common;

import lombok.Data;

@Data
public class EmbeddingStructure {
    private Long kbId;
    private Long contentId;
    private Long chunkId;
    // private String chunkText;
    private byte[] embedding;
}
