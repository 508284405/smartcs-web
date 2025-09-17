package com.leyue.smartcs.web.ltm;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.client.ltm.dto.memory.*;
import com.leyue.smartcs.app.ltm.executor.*;
import com.leyue.smartcs.config.context.UserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

/**
 * LTM记忆管理控制器
 * 提供用户记忆查看、管理和个性化设置功能
 */
@RestController
@RequestMapping("/api/v1/ltm/memory")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "LTM记忆管理", description = "长期记忆系统的用户记忆管理接口")
public class LTMMemoryController {

    private final MemoryQueryExe memoryQueryExe;
    private final MemoryManagementCmdExe memoryManagementCmdExe;
    private final MemoryAnalyticsCmdExe memoryAnalyticsCmdExe;

    @Operation(summary = "获取用户记忆摘要", description = "获取用户的记忆统计摘要信息")
    @GetMapping("/summary")
    public SingleResponse<MemorySummaryDTO> getMemorySummary(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId) {
        
        log.debug("获取用户记忆摘要: userId={}", userId);
        ensureAuthorized(userId);

        MemorySummaryQry qry = new MemorySummaryQry();
        qry.setUserId(userId);
        
        return memoryQueryExe.getMemorySummary(qry);
    }

    @Operation(summary = "获取情景记忆列表", description = "分页获取用户的情景记忆列表")
    @GetMapping("/episodic")
    public MultiResponse<EpisodicMemoryDTO> getEpisodicMemories(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "重要性筛选") 
            @RequestParam(required = false) Double minImportance,
            @Parameter(description = "时间范围开始") 
            @RequestParam(required = false) Long startTime,
            @Parameter(description = "时间范围结束") 
            @RequestParam(required = false) Long endTime) {
        
        log.debug("获取情景记忆列表: userId={}, page={}, size={}", userId, page, size);
        ensureAuthorized(userId);
        
        EpisodicMemoryPageQry qry = new EpisodicMemoryPageQry();
        qry.setUserId(userId);
        qry.setPage(page);
        qry.setSize(size);
        qry.setMinImportance(minImportance);
        qry.setStartTime(startTime);
        qry.setEndTime(endTime);
        
        return memoryQueryExe.getEpisodicMemories(qry);
    }

    @Operation(summary = "获取语义记忆列表", description = "分页获取用户的语义记忆（知识概念）")
    @GetMapping("/semantic")
    public MultiResponse<SemanticMemoryDTO> getSemanticMemories(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "最小置信度") 
            @RequestParam(required = false) Double minConfidence,
            @Parameter(description = "概念关键词") 
            @RequestParam(required = false) String conceptKeyword) {
        
        log.debug("获取语义记忆列表: userId={}, page={}, size={}", userId, page, size);
        ensureAuthorized(userId);
        
        SemanticMemoryPageQry qry = new SemanticMemoryPageQry();
        qry.setUserId(userId);
        qry.setPage(page);
        qry.setSize(size);
        qry.setMinConfidence(minConfidence);
        qry.setConceptKeyword(conceptKeyword);
        
        return memoryQueryExe.getSemanticMemories(qry);
    }

    @Operation(summary = "获取程序性记忆列表", description = "获取用户的行为模式和偏好设置")
    @GetMapping("/procedural")
    public MultiResponse<ProceduralMemoryDTO> getProceduralMemories(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "模式类型") 
            @RequestParam(required = false) String patternType,
            @Parameter(description = "是否只显示活跃模式") 
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        log.debug("获取程序性记忆列表: userId={}, patternType={}, activeOnly={}", 
                 userId, patternType, activeOnly);
        ensureAuthorized(userId);
        
        ProceduralMemoryQry qry = new ProceduralMemoryQry();
        qry.setUserId(userId);
        qry.setPatternType(patternType);
        qry.setActiveOnly(activeOnly);
        
        return memoryQueryExe.getProceduralMemories(qry);
    }

    @Operation(summary = "搜索记忆", description = "根据关键词搜索用户的各类记忆")
    @GetMapping("/search")
    public MultiResponse<MemorySearchResultDTO> searchMemories(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "搜索关键词") 
            @RequestParam @NotNull String keyword,
            @Parameter(description = "记忆类型") 
            @RequestParam(required = false) String memoryType,
            @Parameter(description = "最大结果数") 
            @RequestParam(defaultValue = "50") int limit) {
        
        log.debug("搜索用户记忆: userId={}, keyword={}", userId, keyword);
        ensureAuthorized(userId);
        
        MemorySearchQry qry = new MemorySearchQry();
        qry.setUserId(userId);
        qry.setKeyword(keyword);
        qry.setMemoryType(memoryType);
        qry.setLimit(limit);
        
        return memoryQueryExe.searchMemories(qry);
    }

    @Operation(summary = "删除情景记忆", description = "删除指定的情景记忆")
    @DeleteMapping("/episodic/{memoryId}")
    public Response deleteEpisodicMemory(
            @Parameter(description = "记忆ID") 
            @PathVariable @NotNull @Positive Long memoryId,
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId) {
        
        log.info("删除情景记忆: memoryId={}, userId={}", memoryId, userId);
        ensureAuthorized(userId);
        
        DeleteMemoryCmd cmd = new DeleteMemoryCmd();
        cmd.setMemoryId(memoryId);
        cmd.setUserId(userId);
        cmd.setMemoryType("episodic");
        
        return memoryManagementCmdExe.deleteMemory(cmd);
    }

    @Operation(summary = "更新记忆重要性", description = "手动调整记忆的重要性评分")
    @PutMapping("/episodic/{memoryId}/importance")
    public Response updateMemoryImportance(
            @Parameter(description = "记忆ID") 
            @PathVariable @NotNull @Positive Long memoryId,
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Valid @RequestBody UpdateMemoryImportanceCmd cmd) {
        
        log.info("更新记忆重要性: memoryId={}, userId={}, importance={}", 
                 memoryId, userId, cmd.getImportanceScore());
        ensureAuthorized(userId);
        
        cmd.setMemoryId(memoryId);
        cmd.setUserId(userId);
        
        return memoryManagementCmdExe.updateMemoryImportance(cmd);
    }

    @Operation(summary = "激活/停用程序性记忆", description = "激活或停用用户的行为模式")
    @PutMapping("/procedural/{memoryId}/status")
    public Response toggleProceduralMemory(
            @Parameter(description = "记忆ID") 
            @PathVariable @NotNull @Positive Long memoryId,
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Valid @RequestBody ToggleProceduralMemoryCmd cmd) {
        
        log.info("切换程序性记忆状态: memoryId={}, userId={}, active={}", 
                memoryId, userId, cmd.getIsActive());
        ensureAuthorized(userId);
        
        cmd.setMemoryId(memoryId);
        cmd.setUserId(userId);
        
        return memoryManagementCmdExe.toggleProceduralMemory(cmd);
    }

    @Operation(summary = "获取记忆分析报告", description = "获取用户记忆的深度分析报告")
    @GetMapping("/analytics")
    public SingleResponse<MemoryAnalyticsDTO> getMemoryAnalytics(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "分析时间范围（天）") 
            @RequestParam(defaultValue = "30") int days) {
        
        log.debug("获取记忆分析报告: userId={}, days={}", userId, days);
        ensureAuthorized(userId);
        
        MemoryAnalyticsQry qry = new MemoryAnalyticsQry();
        qry.setUserId(userId);
        qry.setDays(days);
        
        return memoryAnalyticsCmdExe.generateAnalytics(qry);
    }

    @Operation(summary = "导出用户记忆", description = "导出用户的所有记忆数据")
    @PostMapping("/export")
    public SingleResponse<MemoryExportDTO> exportMemories(
            @Valid @RequestBody ExportMemoryCmd cmd) {
        
        log.info("导出用户记忆: userId={}, includeTypes={}", 
                 cmd.getUserId(), cmd.getIncludeTypes());
        ensureAuthorized(cmd.getUserId());
        
        return memoryManagementCmdExe.exportMemories(cmd);
    }

    @Operation(summary = "批量删除记忆", description = "批量删除用户指定的记忆")
    @DeleteMapping("/batch")
    public Response batchDeleteMemories(
            @Valid @RequestBody BatchDeleteMemoryCmd cmd) {
        
        log.info("批量删除记忆: userId={}, count={}", 
                 cmd.getUserId(), cmd.getMemoryIds().size());
        ensureAuthorized(cmd.getUserId());
        
        return memoryManagementCmdExe.batchDeleteMemories(cmd);
    }

    @Operation(summary = "清空过期记忆", description = "清空用户的过期和低价值记忆")
    @DeleteMapping("/cleanup")
    public Response cleanupExpiredMemories(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "保留天数") 
            @RequestParam(defaultValue = "90") int retentionDays,
            @Parameter(description = "最低重要性阈值") 
            @RequestParam(defaultValue = "0.3") double minImportanceThreshold) {
        
        log.info("清理过期记忆: userId={}, retentionDays={}, threshold={}", 
                 userId, retentionDays, minImportanceThreshold);
        ensureAuthorized(userId);
        
        CleanupMemoryCmd cmd = new CleanupMemoryCmd();
        cmd.setUserId(userId);
        cmd.setRetentionDays(retentionDays);
        cmd.setMinImportanceThreshold(minImportanceThreshold);
        
        return memoryManagementCmdExe.cleanupExpiredMemories(cmd);
    }

    private void ensureAuthorized(Long userId) {
        UserContext.UserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BizException("未登录用户无法访问长期记忆接口");
        }
        if (userId != null && !Objects.equals(currentUser.getId(), userId) && !currentUser.isAdmin()) {
            throw new BizException("无权访问目标用户的长期记忆数据");
        }
    }
}
