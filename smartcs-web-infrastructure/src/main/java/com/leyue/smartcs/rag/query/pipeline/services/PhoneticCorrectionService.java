package com.leyue.smartcs.rag.query.pipeline.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 拼音/形近字容错纠错服务（轻量版）
 * 说明：不依赖外部库，采用字典 + 简单规则的保守策略。
 */
public class PhoneticCorrectionService {

    private final Map<String, String> knownCorrections = new HashMap<>();
    private final double minConfidence;

    public PhoneticCorrectionService(double minConfidence) {
        this.minConfidence = minConfidence;
        // 已知错拼样例（演示用，可后续由词典脚本生成）
        knownCorrections.put("朱丽业", "朱丽叶");
        knownCorrections.put("罗密欧与朱丽业", "罗密欧与朱丽叶");
    }

    /**
     * 对完整查询做保守纠错：仅在命中已知纠错词典时替换。
     */
    public Result correct(String text) {
        if (text == null || text.isEmpty()) {
            return Result.builder().corrected(text).confidence(0.0).changed(false).build();
        }
        String corrected = text;
        double confidence = 0.0;
        boolean changed = false;

        for (Map.Entry<String, String> e : knownCorrections.entrySet()) {
            String from = e.getKey();
            String to = e.getValue();
            if (corrected.contains(from)) {
                corrected = corrected.replace(from, to);
                confidence = Math.max(confidence, 0.9); // 命中字典，设高置信
                changed = true;
            }
        }
        return Result.builder().corrected(corrected).confidence(confidence).changed(changed).build();
    }

    public boolean pass(double confidence) {
        return confidence >= minConfidence;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Result {
        private String corrected;
        private double confidence;
        private boolean changed;
    }
}

