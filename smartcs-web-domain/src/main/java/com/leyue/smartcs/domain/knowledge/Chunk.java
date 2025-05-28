package com.leyue.smartcs.domain.knowledge;

import com.leyue.smartcs.dto.knowledge.enums.StrategyNameEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容切片领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chunk {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 段落序号
     */
    private Integer chunkIndex;
    
    /**
     * 该段文本内容
     */
    private String text;
    
    /**
     * 切片token数
     */
    private Integer tokenSize;
    
    /**
     * 向量数据库中的ID（如Milvus主键）
     */
    private String vectorId;

    /**
     * 向量数据
     */
    private float[] vector;
    
    /**
     * 附加元信息，如页码、起止时间、原始位置等
     */
    private String metadata;

    /**
     * 解析策略名称，用于指定文档解析方式
     */
    private StrategyNameEnum strategyName;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 获取文本摘要，用于预览
     * @param maxLength 最大长度
     * @return 文本摘要
     */
    public String getTextSummary(int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
    
    /**
     * 检查文本是否有效
     * @return 是否有效
     */
    public boolean isValidText() {
        return this.text != null && !this.text.trim().isEmpty();
    }
    
    /**
     * 检查是否已生成向量
     * @return 是否已向量化
     */
    public boolean hasVector() {
        return this.vectorId != null && !this.vectorId.trim().isEmpty();
    }
    
    /**
     * 估算token数量（简单估算，1个中文字符约等于1.5个token）
     * @return 估算的token数
     */
    public Integer estimateTokenCount() {
        if (text == null) {
            return 0;
        }
        // 简单估算：英文单词数 + 中文字符数 * 1.5
        int chineseChars = 0;
        int englishWords = 0;
        String[] words = text.split("\\s+");
        englishWords = words.length;
        
        for (char c : text.toCharArray()) {
            if (c >= 0x4e00 && c <= 0x9fff) {
                chineseChars++;
            }
        }
        
        return (int) Math.ceil(englishWords + chineseChars * 1.5);
    }
} 