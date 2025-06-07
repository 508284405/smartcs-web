package com.leyue.smartcs.dto.knowledge;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SearchResultsDTO {
    private String type;
    private Long chunkId;
    private String chunk;
    private Double score;
}
