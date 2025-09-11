package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.Map;

@Data
public class IntentSnapshotDTO {
    private Long id;
    private String name;
    private String code;
    private String status;
    private String scope;
    private Map<String, Object> scopeSelector;
    private String etag;
    private Long publishedAt;
    private Long createdAt;
}