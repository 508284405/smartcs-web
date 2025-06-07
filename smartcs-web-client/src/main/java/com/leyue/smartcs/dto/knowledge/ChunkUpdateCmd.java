package com.leyue.smartcs.dto.knowledge;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 更新切片命令
 */
@Data
public class ChunkUpdateCmd {
    
    /**
     * 切片ID
     */
    @NotNull(message = "切片ID不能为空")
    private Long id;
    
    /**
     * 段落序号
     */
    private Integer chunkIndex;
    
    /**
     * 切片内容文本
     */
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