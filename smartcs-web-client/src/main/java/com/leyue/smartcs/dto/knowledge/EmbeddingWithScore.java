package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

@Data
public class EmbeddingWithScore {
    private ChunkDTO chunk;
    private Float score;
}
