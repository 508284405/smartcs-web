package com.leyue.smartcs.knowledge.parser.processor;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Tesseract OCR处理器
 * 集成Tesseract OCR引擎进行图像文字识别
 * 支持中英文混合识别和置信度评估
 */
@Component
@Slf4j
public class TesseractOcrProcessor {

    @Value("${pdf.ocr.enabled:true}")
    private boolean ocrEnabled;

    @Value("${pdf.ocr.language:chi_sim+eng}")
    private String ocrLanguage;

    @Value("${pdf.ocr.confidence-threshold:0.7}")
    private double confidenceThreshold;

    @Value("${pdf.ocr.min-image-size:100}")
    private int minImageSize;

    // Tesseract实例缓存，避免重复创建
    private final Map<String, ITesseract> tesseractCache = new ConcurrentHashMap<>();

    /**
     * 对图像进行OCR文字识别
     * 
     * @param image 待识别的图像
     * @return OCR识别结果
     */
    public OcrResult performOcr(BufferedImage image) {
        if (!ocrEnabled) {
            log.debug("OCR功能已禁用");
            return OcrResult.disabled();
        }

        if (image == null) {
            log.warn("输入图像为null，跳过OCR处理");
            return OcrResult.failed("输入图像为null");
        }

        // 检查图像尺寸
        if (image.getWidth() < minImageSize || image.getHeight() < minImageSize) {
            log.debug("图像尺寸过小，跳过OCR: {}x{}", image.getWidth(), image.getHeight());
            return OcrResult.skipped("图像尺寸过小");
        }

        try {
            log.debug("开始OCR识别: 语言={}, 图像尺寸={}x{}", ocrLanguage, image.getWidth(), image.getHeight());
            
            ITesseract tesseract = getTesseractInstance(ocrLanguage);
            String text = tesseract.doOCR(image);
            
            // 评估OCR结果质量
            OcrQuality quality = evaluateOcrQuality(text, image);
            
            if (quality.getConfidence() < confidenceThreshold) {
                log.debug("OCR置信度过低: confidence={}, threshold={}", 
                        quality.getConfidence(), confidenceThreshold);
                return OcrResult.lowConfidence(text, quality);
            }
            
            log.info("OCR识别成功: textLength={}, confidence={}", text.length(), quality.getConfidence());
            return OcrResult.success(text, quality);
            
        } catch (TesseractException e) {
            log.error("OCR识别失败", e);
            return OcrResult.failed("OCR识别失败: " + e.getMessage());
        }
    }

    /**
     * 批量OCR处理
     * 
     * @param images 图像列表
     * @return OCR结果列表
     */
    public java.util.List<OcrResult> performBatchOcr(java.util.List<BufferedImage> images) {
        if (!ocrEnabled || images == null || images.isEmpty()) {
            return java.util.List.of();
        }

        log.info("开始批量OCR处理: 图像数量={}", images.size());
        
        java.util.List<OcrResult> results = new java.util.ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            try {
                OcrResult result = performOcr(images.get(i));
                results.add(result);
                log.debug("完成第{}张图像OCR处理: status={}", i + 1, result.getStatus());
            } catch (Exception e) {
                log.error("第{}张图像OCR处理失败", i + 1, e);
                results.add(OcrResult.failed("处理失败: " + e.getMessage()));
            }
        }
        
        log.info("批量OCR处理完成: 总数={}, 成功={}", 
                results.size(), results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum());
        
        return results;
    }

    /**
     * 获取Tesseract实例（支持缓存）
     */
    private ITesseract getTesseractInstance(String language) {
        return tesseractCache.computeIfAbsent(language, lang -> {
            log.info("创建Tesseract实例: language={}", lang);
            ITesseract tesseract = new Tesseract();
            tesseract.setLanguage(lang);
            
            // 设置OCR引擎模式（默认使用LSTM OCR引擎模式）
            tesseract.setOcrEngineMode(1);
            
            // 设置页面分割模式（自动检测）
            tesseract.setPageSegMode(1);
            
            return tesseract;
        });
    }

    /**
     * 评估OCR结果质量
     */
    private OcrQuality evaluateOcrQuality(String text, BufferedImage image) {
        if (text == null || text.trim().isEmpty()) {
            return new OcrQuality(0.0, "无文本输出");
        }

        double confidence = 1.0;
        StringBuilder issues = new StringBuilder();

        // 基于文本长度评估
        if (text.length() < 10) {
            confidence -= 0.2;
            issues.append("文本过短;");
        }

        // 基于特殊字符比例评估
        long specialChars = text.chars()
                .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
                .count();
        double specialRatio = (double) specialChars / text.length();
        if (specialRatio > 0.3) {
            confidence -= 0.3;
            issues.append("特殊字符过多;");
        }

        // 基于重复字符评估
        if (hasExcessiveRepetition(text)) {
            confidence -= 0.2;
            issues.append("重复字符过多;");
        }

        // 基于图像质量评估
        double imageQuality = evaluateImageQuality(image);
        confidence = confidence * imageQuality;

        return new OcrQuality(Math.max(0.0, confidence), issues.toString());
    }

    /**
     * 检查是否有过度重复的字符
     */
    private boolean hasExcessiveRepetition(String text) {
        if (text.length() < 20) return false;
        
        // 检查连续重复字符
        int maxRepeat = 0;
        int currentRepeat = 1;
        char prevChar = text.charAt(0);
        
        for (int i = 1; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            if (currentChar == prevChar) {
                currentRepeat++;
            } else {
                maxRepeat = Math.max(maxRepeat, currentRepeat);
                currentRepeat = 1;
                prevChar = currentChar;
            }
        }
        maxRepeat = Math.max(maxRepeat, currentRepeat);
        
        return maxRepeat > 5; // 连续5个相同字符视为异常
    }

    /**
     * 评估图像质量
     */
    private double evaluateImageQuality(BufferedImage image) {
        if (image == null) return 0.0;
        
        double quality = 1.0;
        
        // 基于图像尺寸评估
        int pixels = image.getWidth() * image.getHeight();
        if (pixels < 10000) { // 100x100
            quality -= 0.3;
        } else if (pixels < 40000) { // 200x200
            quality -= 0.1;
        }
        
        // 可以添加更多图像质量评估指标
        // 如：对比度、清晰度、噪声等
        
        return Math.max(0.1, quality);
    }

    /**
     * 检查OCR配置是否有效
     */
    public boolean isConfigurationValid() {
        try {
            if (!ocrEnabled) {
                return true; // OCR禁用时配置始终有效
            }
            
            // 尝试创建Tesseract实例
            getTesseractInstance(ocrLanguage);
            log.info("OCR配置验证通过: language={}, enabled={}", ocrLanguage, ocrEnabled);
            return true;
            
        } catch (Exception e) {
            log.error("OCR配置验证失败", e);
            return false;
        }
    }

    /**
     * 获取OCR配置信息
     */
    public String getConfigurationInfo() {
        return String.format("OCR配置 - 启用:%s, 语言:%s, 置信度阈值:%.2f, 最小图像尺寸:%d", 
                ocrEnabled, ocrLanguage, confidenceThreshold, minImageSize);
    }

    /**
     * OCR结果类
     */
    public static class OcrResult {
        private final String status;
        private final String text;
        private final OcrQuality quality;
        private final String errorMessage;

        private OcrResult(String status, String text, OcrQuality quality, String errorMessage) {
            this.status = status;
            this.text = text;
            this.quality = quality;
            this.errorMessage = errorMessage;
        }

        public static OcrResult success(String text, OcrQuality quality) {
            return new OcrResult("success", text, quality, null);
        }

        public static OcrResult failed(String errorMessage) {
            return new OcrResult("failed", null, null, errorMessage);
        }

        public static OcrResult disabled() {
            return new OcrResult("disabled", null, null, "OCR功能已禁用");
        }

        public static OcrResult skipped(String reason) {
            return new OcrResult("skipped", null, null, reason);
        }

        public static OcrResult lowConfidence(String text, OcrQuality quality) {
            return new OcrResult("low_confidence", text, quality, "置信度过低");
        }

        // Getters
        public String getStatus() { return status; }
        public String getText() { return text; }
        public OcrQuality getQuality() { return quality; }
        public String getErrorMessage() { return errorMessage; }
        
        public boolean isSuccess() { return "success".equals(status); }
        public boolean isFailed() { return "failed".equals(status); }
        public boolean isDisabled() { return "disabled".equals(status); }
        public boolean isSkipped() { return "skipped".equals(status); }
        public boolean isLowConfidence() { return "low_confidence".equals(status); }
    }

    /**
     * OCR质量评估类
     */
    public static class OcrQuality {
        private final Double confidence;
        private final String issues;

        public OcrQuality(Double confidence, String issues) {
            this.confidence = confidence;
            this.issues = issues;
        }

        public Double getConfidence() { return confidence; }
        public String getIssues() { return issues; }
        
        public boolean isHighQuality() { return confidence >= 0.8; }
        public boolean isAcceptable() { return confidence >= 0.5; }
    }
}