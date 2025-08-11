package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RAG评估导出结果DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalExportResultDTO {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 导出任务ID
     */
    private String exportTaskId;
    
    /**
     * 下载链接
     */
    private String downloadUrl;
    
    /**
     * 文件大小
     */
    private String fileSize;
    
    /**
     * 文件格式
     */
    private String fileFormat;
    
    /**
     * 导出状态：PROCESSING, COMPLETED, FAILED
     */
    private String status;
    
    /**
     * 导出完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * 过期时间
     */
    private Long expireTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 导出配置
     */
    private String exportConfig;
}
