package com.leyue.smartcs.web.intent;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.IntentService;
import com.leyue.smartcs.dto.intent.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端意图快照控制器
 * 提供意图快照管理功能，包括快照创建、发布、查询等
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/intent/snapshot")
@RequiredArgsConstructor
@Validated
public class AdminIntentSnapshotController {
    
    private final IntentService intentService;
    
    // ====== 快照管理 ======
    
    /**
     * 创建意图快照
     */
    @PostMapping("")
    public SingleResponse<IntentSnapshotDTO> createSnapshot(@Valid @RequestBody IntentSnapshotCreateCmd cmd) {
        log.info("管理端创建意图快照: {}", cmd.getName());
        return intentService.createSnapshot(cmd);
    }
    
    /**
     * 发布意图快照
     */
    @PostMapping("/{snapshotId}/publish")
    public Response publishSnapshot(@PathVariable Long snapshotId) {
        log.info("管理端发布意图快照: {}", snapshotId);
        IntentSnapshotPublishCmd cmd = new IntentSnapshotPublishCmd();
        cmd.setSnapshotId(snapshotId);
        return intentService.publishSnapshot(cmd);
    }
    
    /**
     * 查询快照列表
     */
    @GetMapping("")
    public PageResponse<IntentSnapshotDTO> listSnapshots(IntentSnapshotListQry qry) {
        log.info("管理端查询意图快照列表，状态: {}", qry.getStatus());
        return intentService.listSnapshots(qry);
    }
    
    /**
     * 查询快照详情
     */
    @GetMapping("/{snapshotId}")
    public SingleResponse<IntentSnapshotDTO> getSnapshot(@PathVariable Long snapshotId) {
        log.info("管理端查询意图快照详情: {}", snapshotId);
        IntentSnapshotGetQry qry = new IntentSnapshotGetQry();
        qry.setSnapshotId(snapshotId);
        return intentService.getSnapshot(qry);
    }
    
    /**
     * 删除意图快照
     */
    @DeleteMapping("/{snapshotId}")
    public Response deleteSnapshot(@PathVariable Long snapshotId) {
        log.info("管理端删除意图快照: {}", snapshotId);
        IntentSnapshotDeleteCmd cmd = new IntentSnapshotDeleteCmd();
        cmd.setSnapshotId(snapshotId);
        return intentService.deleteSnapshot(cmd);
    }
    
    /**
     * 比较快照差异
     */
    @PostMapping("/compare")
    public SingleResponse<IntentSnapshotCompareResultDTO> compareSnapshots(@Valid @RequestBody IntentSnapshotCompareCmd cmd) {
        log.info("管理端比较意图快照: {} vs {}", cmd.getBaseSnapshotId(), cmd.getTargetSnapshotId());
        return intentService.compareSnapshots(cmd);
    }
    
    /**
     * 回滚快照
     */
    @PostMapping("/{snapshotId}/rollback")
    public Response rollbackSnapshot(@PathVariable Long snapshotId) {
        log.info("管理端回滚意图快照: {}", snapshotId);
        IntentSnapshotRollbackCmd cmd = new IntentSnapshotRollbackCmd();
        cmd.setSnapshotId(snapshotId);
        return intentService.rollbackSnapshot(cmd);
    }
}