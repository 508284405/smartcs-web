package com.leyue.smartcs.dto.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量嵌入领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingCmd {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 文本数据
     */
    private String text;
} 