package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * RAG评估运行停止命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunStopCmd {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 停止原因
     */
    private String stopReason;
    
    /**
     * 是否强制停止
     */
    private Boolean forceStop;
    
    /**
     * 是否保存当前进度
     */
    private Boolean saveProgress;
    
    /**
     * 是否清理临时资源
     */
    private Boolean cleanupTempResources;
    
    /**
     * 是否发送通知
     */
    private Boolean sendNotification;
    
    /**
     * 停止后操作：NONE, RESTART, RERUN
     */
    private String postStopAction;
    
    /**
     * 是否记录停止日志
     */
    private Boolean logStopAction;
}