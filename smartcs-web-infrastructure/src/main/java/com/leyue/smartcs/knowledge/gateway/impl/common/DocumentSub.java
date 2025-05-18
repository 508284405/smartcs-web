package com.leyue.smartcs.knowledge.gateway.impl.common;

import org.redisson.api.search.query.Document;

import java.util.Map;


public class DocumentSub extends Document {
    private final String id;
    private Map<String, Object> attributes;
    private byte[] payload;
    private Double score;

    public DocumentSub(String id) {
        super(id);
        this.id = id;
    }

    @Override
    public Double getScore() {
        return this.score;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setScore(String score) {
        this.score = Double.parseDouble(score);
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attributes = attrs;
    }
}
