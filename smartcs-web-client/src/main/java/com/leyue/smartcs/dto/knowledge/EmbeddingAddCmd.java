package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 向量批量添加命令对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmbeddingAddCmd extends Command {
    /**
     * 文档ID
     */
    private Long docId;
    
    /**
     * 向量数据列表
     */
    private List<EmbeddingItem> items;
    
    /**
     * 模型类型
     */
    private String modelType;
    
    /**
     * 向量项内部类
     */
    @Data
    public static class EmbeddingItem {
        /**
         * 段落序号
         */
        private Integer sectionIdx;
        
        /**
         * 文本片段
         */
        private String contentSnip;
        
        /**
         * 向量数据（Base64编码）
         */
        private String vector;
    }
} 