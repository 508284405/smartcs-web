package com.leyue.smartcs.dto.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容处理响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentProcessResponse {
    
    /**
     * 处理的内容数量
     */
    private Integer contentCount;
    
    /**
     * 生成的分块数量
     */
    private Integer chunkCount;
    
    /**
     * 生成的向量数量
     */
    private Integer vectorCount;
    
    /**
     * 创建的内容ID列表
     */
    private List<Long> contentIds;
    
    /**
     * 处理总耗时（毫秒）
     */
    private Long processingTime;
    
    /**
     * 处理状态信息
     */
    private String statusMessage;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}