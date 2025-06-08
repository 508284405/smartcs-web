package com.leyue.smartcs.dto.knowledge;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建切片命令
 */
@Data
public class ChunkCreateCmd {
    
    /**
     * 内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long contentId;
    
    /**
     * 切片内容文本
     */
    @NotBlank(message = "切片内容不能为空")
    private String content;
    
    /**
     * 切片token数
     */
    private Integer tokenSize;
    
    /**
     * 附加元信息，如页码、起止时间、原始位置等
     */
    private String metadata;
} 