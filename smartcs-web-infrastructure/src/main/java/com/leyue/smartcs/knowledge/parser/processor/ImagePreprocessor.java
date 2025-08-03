package com.leyue.smartcs.knowledge.parser.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 图像预处理工具
 * 提供图像质量增强、格式转换、尺寸优化等功能
 * 为OCR和多模态处理做准备
 */
@Component
@Slf4j
public class ImagePreprocessor {

    @Value("${image.preprocess.enabled:true}")
    private boolean preprocessEnabled;

    @Value("${image.preprocess.enhance-contrast:true}")
    private boolean enhanceContrast;

    @Value("${image.preprocess.auto-resize:true}")
    private boolean autoResize;

    @Value("${image.preprocess.target-width:1200}")
    private int targetWidth;

    @Value("${image.preprocess.target-height:1200}")
    private int targetHeight;

    @Value("${image.preprocess.quality-threshold:0.8}")
    private double qualityThreshold;

    /**
     * 预处理图像输入流
     * 
     * @param inputStream 原始图像输入流
     * @param originalFileName 原始文件名
     * @return 预处理结果
     */
    public PreprocessResult preprocessImage(InputStream inputStream, String originalFileName) {
        if (!preprocessEnabled) {
            log.debug("图像预处理功能已禁用");
            return PreprocessResult.disabled(inputStream, originalFileName);
        }

        try {
            log.debug("开始图像预处理: fileName={}", originalFileName);

            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                log.warn("无法读取图像: fileName={}", originalFileName);
                return PreprocessResult.failed("无法读取图像文件", originalFileName);
            }

            // 评估原始图像质量
            ImageQuality originalQuality = assessImageQuality(originalImage);
            log.debug("原始图像质量评估: fileName={}, score={}", originalFileName, originalQuality.getScore());

            BufferedImage processedImage = originalImage;
            List<String> appliedOperations = new ArrayList<>();

            // 1. 尺寸优化
            if (autoResize && needsResize(originalImage)) {
                processedImage = resizeImage(processedImage);
                appliedOperations.add("resize");
                log.debug("应用尺寸调整: fileName={}", originalFileName);
            }

            // 2. 对比度增强
            if (enhanceContrast && originalQuality.needsContrastEnhancement()) {
                processedImage = enhanceContrast(processedImage);
                appliedOperations.add("contrast_enhancement");
                log.debug("应用对比度增强: fileName={}", originalFileName);
            }

            // 3. 灰度转换（如果有必要）
            if (originalQuality.needsGrayscaleConversion()) {
                processedImage = convertToGrayscale(processedImage);
                appliedOperations.add("grayscale_conversion");
                log.debug("应用灰度转换: fileName={}", originalFileName);
            }

            // 4. 噪点减少
            if (originalQuality.needsNoiseReduction()) {
                processedImage = reduceNoise(processedImage);
                appliedOperations.add("noise_reduction");
                log.debug("应用噪点减少: fileName={}", originalFileName);
            }

            // 评估处理后的图像质量
            ImageQuality processedQuality = assessImageQuality(processedImage);

            // 转换为输入流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(processedImage, "PNG", outputStream);
            InputStream processedInputStream = new ByteArrayInputStream(outputStream.toByteArray());

            log.info("图像预处理完成: fileName={}, operations={}, qualityImprovement={}", 
                    originalFileName, appliedOperations, 
                    processedQuality.getScore() - originalQuality.getScore());

            return PreprocessResult.success(processedInputStream, originalFileName, 
                    appliedOperations, originalQuality, processedQuality);

        } catch (Exception e) {
            log.error("图像预处理失败: fileName={}", originalFileName, e);
            return PreprocessResult.failed("预处理过程中发生错误: " + e.getMessage(), originalFileName);
        }
    }

    /**
     * 批量预处理图像
     */
    public List<PreprocessResult> preprocessImages(List<InputStream> inputStreams, List<String> fileNames) {
        List<PreprocessResult> results = new ArrayList<>();
        
        for (int i = 0; i < inputStreams.size() && i < fileNames.size(); i++) {
            try {
                PreprocessResult result = preprocessImage(inputStreams.get(i), fileNames.get(i));
                results.add(result);
            } catch (Exception e) {
                log.error("批量预处理第{}个图像失败: fileName={}", i, fileNames.get(i), e);
                results.add(PreprocessResult.failed("批量处理失败: " + e.getMessage(), fileNames.get(i)));
            }
        }
        
        return results;
    }

    /**
     * 判断是否需要调整尺寸
     */
    private boolean needsResize(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 如果图像过小或过大，都需要调整
        return width < 200 || height < 200 || width > targetWidth * 2 || height > targetHeight * 2;
    }

    /**
     * 调整图像尺寸
     */
    private BufferedImage resizeImage(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 计算最佳尺寸，保持宽高比
        double widthRatio = (double) targetWidth / originalWidth;
        double heightRatio = (double) targetHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }

    /**
     * 增强对比度
     */
    private BufferedImage enhanceContrast(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage enhancedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // 简单的对比度增强算法
        float contrastFactor = 1.3f;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);
                
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                // 应用对比度增强
                red = Math.min(255, Math.max(0, (int) ((red - 128) * contrastFactor + 128)));
                green = Math.min(255, Math.max(0, (int) ((green - 128) * contrastFactor + 128)));
                blue = Math.min(255, Math.max(0, (int) ((blue - 128) * contrastFactor + 128)));
                
                int newRgb = (red << 16) | (green << 8) | blue;
                enhancedImage.setRGB(x, y, newRgb);
            }
        }
        
        return enhancedImage;
    }

    /**
     * 转换为灰度图像
     */
    private BufferedImage convertToGrayscale(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        Graphics2D g2d = grayscaleImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        
        return grayscaleImage;
    }

    /**
     * 减少噪点
     */
    private BufferedImage reduceNoise(BufferedImage originalImage) {
        // 简单的3x3均值滤波器
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage denoisedImage = new BufferedImage(width, height, originalImage.getType());
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int totalRed = 0, totalGreen = 0, totalBlue = 0;
                
                // 计算3x3邻域的平均值
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int rgb = originalImage.getRGB(x + dx, y + dy);
                        totalRed += (rgb >> 16) & 0xFF;
                        totalGreen += (rgb >> 8) & 0xFF;
                        totalBlue += rgb & 0xFF;
                    }
                }
                
                int avgRed = totalRed / 9;
                int avgGreen = totalGreen / 9;
                int avgBlue = totalBlue / 9;
                
                int newRgb = (avgRed << 16) | (avgGreen << 8) | avgBlue;
                denoisedImage.setRGB(x, y, newRgb);
            }
        }
        
        return denoisedImage;
    }

    /**
     * 评估图像质量
     */
    private ImageQuality assessImageQuality(BufferedImage image) {
        ImageQuality quality = new ImageQuality();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 1. 尺寸评分
        if (width >= 300 && height >= 300) {
            quality.addScore(0.2);
        }
        
        // 2. 对比度评估
        double contrast = calculateContrast(image);
        if (contrast > 50) {
            quality.addScore(0.3);
        } else if (contrast < 20) {
            quality.setNeedsContrastEnhancement(true);
        }
        
        // 3. 亮度分布评估
        double brightness = calculateAverageBrightness(image);
        if (brightness > 30 && brightness < 220) {
            quality.addScore(0.2);
        }
        
        // 4. 噪点评估
        double noiseLevel = estimateNoiseLevel(image);
        if (noiseLevel < 0.1) {
            quality.addScore(0.3);
        } else if (noiseLevel > 0.3) {
            quality.setNeedsNoiseReduction(true);
        }
        
        return quality;
    }

    private double calculateContrast(BufferedImage image) {
        // 简化的对比度计算
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) + 
                                 0.587 * ((rgb >> 8) & 0xFF) + 
                                 0.114 * (rgb & 0xFF));
                histogram[gray]++;
            }
        }
        
        // 计算标准差作为对比度指标
        double mean = 127.5;
        double variance = 0;
        int totalPixels = width * height;
        
        for (int i = 0; i < 256; i++) {
            variance += histogram[i] * Math.pow(i - mean, 2);
        }
        
        return Math.sqrt(variance / totalPixels);
    }

    private double calculateAverageBrightness(BufferedImage image) {
        long totalBrightness = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int brightness = (int) (0.299 * ((rgb >> 16) & 0xFF) + 
                                       0.587 * ((rgb >> 8) & 0xFF) + 
                                       0.114 * (rgb & 0xFF));
                totalBrightness += brightness;
            }
        }
        
        return (double) totalBrightness / (width * height);
    }

    private double estimateNoiseLevel(BufferedImage image) {
        // 简化的噪点估算，基于边缘检测
        int width = image.getWidth();
        int height = image.getHeight();
        double totalVariation = 0;
        int count = 0;
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = getRGBGrayValue(image.getRGB(x, y));
                int right = getRGBGrayValue(image.getRGB(x + 1, y));
                int down = getRGBGrayValue(image.getRGB(x, y + 1));
                
                totalVariation += Math.abs(center - right) + Math.abs(center - down);
                count += 2;
            }
        }
        
        return totalVariation / count / 255.0; // 归一化到0-1
    }

    private int getRGBGrayValue(int rgb) {
        return (int) (0.299 * ((rgb >> 16) & 0xFF) + 
                     0.587 * ((rgb >> 8) & 0xFF) + 
                     0.114 * (rgb & 0xFF));
    }

    /**
     * 获取配置信息
     */
    public String getConfigurationInfo() {
        return String.format("图像预处理配置 - 启用:%s, 对比度增强:%s, 自动调整:%s, 目标尺寸:%dx%d",
                preprocessEnabled, enhanceContrast, autoResize, targetWidth, targetHeight);
    }

    /**
     * 图像质量评估结果
     */
    public static class ImageQuality {
        private double score = 0.0;
        private boolean needsContrastEnhancement = false;
        private boolean needsGrayscaleConversion = false;
        private boolean needsNoiseReduction = false;

        public void addScore(double points) {
            this.score += points;
        }

        public double getScore() {
            return Math.min(score, 1.0);
        }

        public boolean needsContrastEnhancement() {
            return needsContrastEnhancement;
        }

        public void setNeedsContrastEnhancement(boolean needsContrastEnhancement) {
            this.needsContrastEnhancement = needsContrastEnhancement;
        }

        public boolean needsGrayscaleConversion() {
            return needsGrayscaleConversion;
        }

        public void setNeedsGrayscaleConversion(boolean needsGrayscaleConversion) {
            this.needsGrayscaleConversion = needsGrayscaleConversion;
        }

        public boolean needsNoiseReduction() {
            return needsNoiseReduction;
        }

        public void setNeedsNoiseReduction(boolean needsNoiseReduction) {
            this.needsNoiseReduction = needsNoiseReduction;
        }
    }

    /**
     * 预处理结果
     */
    public static class PreprocessResult {
        private final boolean success;
        private final String errorMessage;
        private final InputStream processedStream;
        private final String fileName;
        private final List<String> appliedOperations;
        private final ImageQuality originalQuality;
        private final ImageQuality processedQuality;

        private PreprocessResult(boolean success, String errorMessage, InputStream processedStream,
                               String fileName, List<String> appliedOperations, 
                               ImageQuality originalQuality, ImageQuality processedQuality) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.processedStream = processedStream;
            this.fileName = fileName;
            this.appliedOperations = appliedOperations != null ? new ArrayList<>(appliedOperations) : new ArrayList<>();
            this.originalQuality = originalQuality;
            this.processedQuality = processedQuality;
        }

        public static PreprocessResult success(InputStream processedStream, String fileName,
                                             List<String> appliedOperations, ImageQuality originalQuality,
                                             ImageQuality processedQuality) {
            return new PreprocessResult(true, null, processedStream, fileName, 
                    appliedOperations, originalQuality, processedQuality);
        }

        public static PreprocessResult failed(String errorMessage, String fileName) {
            return new PreprocessResult(false, errorMessage, null, fileName, null, null, null);
        }

        public static PreprocessResult disabled(InputStream originalStream, String fileName) {
            return new PreprocessResult(true, "预处理功能已禁用", originalStream, fileName, 
                    List.of("disabled"), null, null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public InputStream getProcessedStream() { return processedStream; }
        public String getFileName() { return fileName; }
        public List<String> getAppliedOperations() { return new ArrayList<>(appliedOperations); }
        public ImageQuality getOriginalQuality() { return originalQuality; }
        public ImageQuality getProcessedQuality() { return processedQuality; }

        public boolean hasQualityImprovement() {
            return originalQuality != null && processedQuality != null &&
                   processedQuality.getScore() > originalQuality.getScore();
        }

        public double getQualityImprovement() {
            if (originalQuality == null || processedQuality == null) return 0.0;
            return processedQuality.getScore() - originalQuality.getScore();
        }
    }
}