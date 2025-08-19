package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.enums.SnapshotStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import com.leyue.smartcs.dto.intent.IntentSnapshotRollbackCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 意图快照回滚命令执行器
 * 
 * @author Claude
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntentSnapshotRollbackCmdExe {
    
    private final IntentSnapshotGateway intentSnapshotGateway;
    
    /**
     * 回滚到指定快照
     * 
     * @param cmd 回滚命令
     * @return 回滚结果
     */
    @Transactional
    public Response execute(IntentSnapshotRollbackCmd cmd) {
        try {
            log.info("开始回滚意图快照: snapshotId={}, reason={}", 
                    cmd.getSnapshotId(), cmd.getRollbackReason());
            
            // 参数验证
            if (cmd.getSnapshotId() == null) {
                throw new BizException("INVALID_PARAM", "快照ID不能为空");
            }
            
            // 获取目标快照
            IntentSnapshot targetSnapshot = intentSnapshotGateway.findById(cmd.getSnapshotId());
            if (targetSnapshot == null) {
                throw new BizException("SNAPSHOT_NOT_FOUND", "目标快照不存在");
            }
            
            // 检查快照状态
            if (targetSnapshot.getStatus() == SnapshotStatus.ACTIVE) {
                throw new BizException("SNAPSHOT_ALREADY_ACTIVE", "目标快照已经是激活状态");
            }
            
            if (targetSnapshot.getStatus() != SnapshotStatus.DEPRECATED) {
                throw new BizException("INVALID_SNAPSHOT_STATUS", "只能回滚到已弃用的快照");
            }
            
            // 下线当前激活快照
            deactivateCurrentSnapshot();
            
            // 激活目标快照（回滚）
            activateSnapshot(targetSnapshot, cmd.getRollbackReason());
            
            log.info("意图快照回滚成功: snapshotId={}, etag={}", 
                    targetSnapshot.getId(), targetSnapshot.getEtag());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("回滚意图快照业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("回滚意图快照失败", e);
            throw new BizException("ROLLBACK_ERROR", "回滚快照失败: " + e.getMessage());
        }
    }
    
    /**
     * 下线当前激活快照
     */
    private void deactivateCurrentSnapshot() {
        try {
            IntentSnapshot currentActive = intentSnapshotGateway.getCurrentActiveSnapshot();
            if (currentActive != null) {
                log.info("下线当前激活快照: snapshotId={}", currentActive.getId());
                
                currentActive.setStatus(SnapshotStatus.DEPRECATED);
                currentActive.setUpdatedAt(System.currentTimeMillis());
                
                intentSnapshotGateway.update(currentActive);
            }
        } catch (Exception e) {
            log.error("下线当前激活快照失败", e);
            throw new RuntimeException("下线当前快照失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 激活快照（回滚）
     */
    private void activateSnapshot(IntentSnapshot snapshot, String rollbackReason) {
        try {
            log.info("激活快照（回滚）: snapshotId={}, reason={}", snapshot.getId(), rollbackReason);
            
            snapshot.setStatus(SnapshotStatus.ACTIVE);
            snapshot.setPublishedBy(getCurrentUserId());
            snapshot.setPublishedAt(System.currentTimeMillis());
            snapshot.setUpdatedAt(System.currentTimeMillis());
            
            // 在快照中记录回滚信息
            if (snapshot.getScopeSelector() == null) {
                snapshot.setScopeSelector(new java.util.HashMap<>());
            }
            snapshot.getScopeSelector().put("rollback_reason", rollbackReason);
            snapshot.getScopeSelector().put("rollback_time", System.currentTimeMillis());
            snapshot.getScopeSelector().put("rollback_by", getCurrentUserId());
            
            intentSnapshotGateway.update(snapshot);
            
        } catch (Exception e) {
            log.error("激活快照失败: snapshotId={}", snapshot.getId(), e);
            throw new RuntimeException("激活快照失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 集成用户上下文
        return 1L;
    }
}