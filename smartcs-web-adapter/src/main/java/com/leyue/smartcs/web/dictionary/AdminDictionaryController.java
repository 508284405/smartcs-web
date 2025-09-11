package com.leyue.smartcs.web.dictionary;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.api.DictionaryAdminService;
import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.dto.dictionary.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 字典管理控制器
 * 提供字典数据的完整管理功能
 * 
 * @author Claude
 */
@Slf4j
@RestController
@RequestMapping({"/admin/dictionary", "/api/dictionaries"})
@RequiredArgsConstructor
@Tag(name = "字典管理", description = "字典数据的CRUD管理、批量操作、验证预览等功能")
public class AdminDictionaryController {

    private final DictionaryAdminService dictionaryAdminService;
    private final DictionaryService dictionaryService;

    // ==================== 基础CRUD操作 ====================

    @PostMapping("/entry")
    @Operation(summary = "创建字典条目", description = "创建新的字典数据条目")
    public SingleResponse<Long> createEntry(@Valid @RequestBody DictionaryEntryCreateCmd cmd) {
        log.info("创建字典条目: type={}, key={}", cmd.getDictionaryType(), cmd.getEntryKey());
        
        try {
            Long entryId = dictionaryAdminService.createDictionaryEntry(cmd);
            
            // 异步刷新相关缓存
            refreshCacheAsync(cmd.getDictionaryType(), cmd.getTenant(), cmd.getChannel(), cmd.getDomain());
            
            return SingleResponse.of(entryId);
        } catch (Exception e) {
            log.error("创建字典条目失败: cmd={}", cmd, e);
            return SingleResponse.buildFailure("CREATE_ENTRY_FAILED", e.getMessage());
        }
    }

    // TODO: Temporarily disabled - missing DTO methods
    /*
    @PutMapping("/entry")
    @Operation(summary = "更新字典条目", description = "更新现有字典数据条目")
    public Response updateEntry(@Valid @RequestBody DictionaryEntryUpdateCmd cmd) {
        log.info("更新字典条目: id={}, key={}", cmd.getEntryId(), cmd.getEntryKey());
        
        try {
            Boolean success = dictionaryAdminService.updateDictionaryEntry(cmd);
            if (success) {
                // 异步刷新相关缓存
                refreshCacheAsync(cmd.getDictionaryType(), cmd.getTenant(), cmd.getChannel(), cmd.getDomain());
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("UPDATE_ENTRY_FAILED", "更新字典条目失败");
            }
        } catch (Exception e) {
            log.error("更新字典条目失败: cmd={}", cmd, e);
            return Response.buildFailure("UPDATE_ENTRY_ERROR", e.getMessage());
        }
    }

    @DeleteMapping("/entry")
    @Operation(summary = "删除字典条目", description = "删除指定的字典数据条目")
    public Response deleteEntry(@Valid @RequestBody DictionaryEntryDeleteCmd cmd) {
        log.info("删除字典条目: id={}", cmd.getEntryId());
        
        try {
            Boolean success = dictionaryAdminService.deleteDictionaryEntry(cmd);
            if (success) {
                // 异步刷新相关缓存
                refreshCacheAsync(null, null, null, null);
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("DELETE_ENTRY_FAILED", "删除字典条目失败");
            }
        } catch (Exception e) {
            log.error("删除字典条目失败: cmd={}", cmd, e);
            return Response.buildFailure("DELETE_ENTRY_ERROR", e.getMessage());
        }
    }
    */

    @GetMapping
    @Operation(summary = "分页查询字典条目", description = "分页查询字典条目列表（GET方式）")
    public SingleResponse<DictionaryEntryPageResult> pageEntriesGet(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String key,
            @Parameter(description = "租户标识") @RequestParam(required = false) String tenant, 
            @Parameter(description = "渠道标识") @RequestParam(required = false) String channel,
            @Parameter(description = "领域标识") @RequestParam(required = false) String domain,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int pageSize) {
        
        log.debug("GET分页查询字典条目: key={}, tenant={}, channel={}, domain={}, page={}, size={}", 
                 key, tenant, channel, domain, pageNo, pageSize);
        
        try {
            // 构建查询参数
            DictionaryEntryPageQry qry = DictionaryEntryPageQry.builder()
                    .entryKey(key)
                    .tenant(tenant)
                    .channel(channel) 
                    .domain(domain)
                    .pageNum(pageNo)
                    .pageSize(pageSize)
                    .build();
                    
            DictionaryEntryPageResult result = dictionaryAdminService.pageDictionaryEntries(qry);
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("GET分页查询字典条目失败: key={}, tenant={}, channel={}, domain={}", 
                     key, tenant, channel, domain, e);
            return SingleResponse.buildFailure("PAGE_ENTRIES_GET_FAILED", e.getMessage());
        }
    }

    @GetMapping("/entry")
    @Operation(summary = "获取字典条目", description = "根据ID获取字典条目详情")
    public SingleResponse<DictionaryEntryDTO> getEntry(@Valid DictionaryEntryGetQry qry) {
        log.debug("获取字典条目: id={}", qry.getId());
        
        try {
            DictionaryEntryDTO entry = dictionaryAdminService.getDictionaryEntry(qry);
            return SingleResponse.of(entry);
        } catch (Exception e) {
            log.error("获取字典条目失败: qry={}", qry, e);
            return SingleResponse.buildFailure("GET_ENTRY_FAILED", e.getMessage());
        }
    }

    @PostMapping("/entry/page")
    @Operation(summary = "分页查询字典条目", description = "分页获取字典条目列表")
    public SingleResponse<DictionaryEntryPageResult> pageEntries(@Valid @RequestBody DictionaryEntryPageQry qry) {
        log.debug("分页查询字典条目: type={}, page={}, size={}", qry.getDictionaryType(), qry.getPageNum(), qry.getPageSize());
        
        try {
            DictionaryEntryPageResult result = dictionaryAdminService.pageDictionaryEntries(qry);
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("分页查询字典条目失败: qry={}", qry, e);
            return SingleResponse.buildFailure("PAGE_ENTRIES_FAILED", e.getMessage());
        }
    }

    @PostMapping("/entry/list")
    @Operation(summary = "查询字典条目列表", description = "获取字典条目列表（不分页）")
    public MultiResponse<DictionaryEntryDTO> listEntries(@Valid @RequestBody DictionaryEntryListQry qry) {
        log.debug("查询字典条目列表: type={}", qry.getDictionaryType());
        
        try {
            List<DictionaryEntryDTO> entries = dictionaryAdminService.listDictionaryEntries(qry);
            return MultiResponse.of(entries);
        } catch (Exception e) {
            log.error("查询字典条目列表失败: qry={}", qry, e);
            return MultiResponse.buildFailure("LIST_ENTRIES_FAILED", e.getMessage());
        }
    }

    // ==================== 批量操作 ====================

    @PostMapping("/entry/batch-create")
    @Operation(summary = "批量创建字典条目", description = "批量创建多个字典数据条目")
    public SingleResponse<DictionaryBatchCreateResult> batchCreateEntries(
            @Valid @RequestBody List<DictionaryEntryCreateCmd> createCmds) {
        log.info("批量创建字典条目: count={}", createCmds.size());
        
        try {
            DictionaryBatchCreateResult result = dictionaryAdminService.batchCreateDictionaryEntries(createCmds);
            
            // 异步刷新相关缓存
            refreshCacheAsync(null, null, null, null);
            
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("批量创建字典条目失败: count={}", createCmds.size(), e);
            return SingleResponse.buildFailure("BATCH_CREATE_ENTRIES_FAILED", e.getMessage());
        }
    }

    @PostMapping("/entry/batch-delete")
    @Operation(summary = "批量删除字典条目", description = "批量删除多个字典数据条目")
    public SingleResponse<DictionaryBatchDeleteResult> batchDeleteEntries(
            @Valid @RequestBody List<DictionaryEntryDeleteCmd> deleteCmds) {
        log.info("批量删除字典条目: count={}", deleteCmds.size());
        
        try {
            DictionaryBatchDeleteResult result = dictionaryAdminService.batchDeleteDictionaryEntries(deleteCmds);
            
            // 异步刷新相关缓存
            refreshCacheAsync(null, null, null, null);
            
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("批量删除字典条目失败: count={}", deleteCmds.size(), e);
            return SingleResponse.buildFailure("BATCH_DELETE_ENTRIES_FAILED", e.getMessage());
        }
    }

    // ==================== 数据验证预览 ====================

    @PostMapping("/validate")
    @Operation(summary = "验证字典数据", description = "验证字典数据的格式和完整性")
    public SingleResponse<DictionaryValidateResult> validateData(@Valid @RequestBody DictionaryValidateCmd cmd) {
        log.info("验证字典数据: type={}", cmd.getDictionaryType());
        
        try {
            DictionaryValidateResult result = dictionaryAdminService.validateDictionaryData(cmd);
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("验证字典数据失败: cmd={}", cmd, e);
            return SingleResponse.buildFailure("VALIDATE_DATA_FAILED", e.getMessage());
        }
    }

    @GetMapping("/preview")
    @Operation(summary = "预览字典效果", description = "预览字典数据在指定文本上的处理效果")
    public SingleResponse<String> previewEffect(
            @Parameter(description = "字典类型") @RequestParam String dictionaryType,
            @Parameter(description = "租户标识") @RequestParam(defaultValue = "default") String tenant,
            @Parameter(description = "渠道标识") @RequestParam(defaultValue = "default") String channel,
            @Parameter(description = "领域标识") @RequestParam(defaultValue = "default") String domain,
            @Parameter(description = "测试文本") @RequestParam String testText) {
        log.debug("预览字典效果: type={}, text={}", dictionaryType, testText);
        
        try {
            // 根据字典类型获取数据并应用处理
            String result = applyDictionaryToText(dictionaryType, tenant, channel, domain, testText);
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("预览字典效果失败: type={}, text={}", dictionaryType, testText, e);
            return SingleResponse.buildFailure("PREVIEW_EFFECT_FAILED", e.getMessage());
        }
    }

    // ==================== 导入导出 ====================
    // TODO: Temporarily disabled - missing DTO methods
    /*
    @PostMapping("/import")
    @Operation(summary = "导入字典数据", description = "从文件或数据结构导入字典数据")
    public SingleResponse<DictionaryImportResult> importData(@Valid @RequestBody DictionaryImportCmd cmd) {
        log.info("导入字典数据: type={}, source={}", cmd.getDictionaryType(), cmd.getSourceType());
        
        try {
            DictionaryImportResult result = dictionaryAdminService.importDictionaryData(cmd);
            
            // 异步刷新相关缓存
            refreshCacheAsync(cmd.getDictionaryType(), cmd.getTenant(), cmd.getChannel(), cmd.getDomain());
            
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("导入字典数据失败: cmd={}", cmd, e);
            return SingleResponse.buildFailure("IMPORT_DATA_FAILED", e.getMessage());
        }
    }

    @PostMapping("/export")
    @Operation(summary = "导出字典数据", description = "导出字典数据到文件或数据结构")
    public SingleResponse<DictionaryExportResult> exportData(@Valid @RequestBody DictionaryExportCmd cmd) {
        log.info("导出字典数据: type={}, format={}", cmd.getDictionaryType(), cmd.getExportFormat());
        
        try {
            DictionaryExportResult result = dictionaryAdminService.exportDictionaryData(cmd);
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("导出字典数据失败: cmd={}", cmd, e);
            return SingleResponse.buildFailure("EXPORT_DATA_FAILED", e.getMessage());
        }
    }
    */

    // ==================== 版本管理 ====================
    // TODO: Temporarily disabled - missing DTO methods  
    /*

    @PostMapping("/publish")
    @Operation(summary = "发布字典数据", description = "将字典数据发布为生效状态")
    public Response publishData(@Valid @RequestBody DictionaryPublishCmd cmd) {
        log.info("发布字典数据: type={}, version={}", cmd.getDictionaryType(), cmd.getTargetVersion());
        
        try {
            Boolean success = dictionaryAdminService.publishDictionaryData(cmd);
            if (success) {
                // 异步刷新相关缓存
                refreshCacheAsync(cmd.getDictionaryType(), cmd.getTenant(), cmd.getChannel(), cmd.getDomain());
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("PUBLISH_DATA_FAILED", "发布字典数据失败");
            }
        } catch (Exception e) {
            log.error("发布字典数据失败: cmd={}", cmd, e);
            return Response.buildFailure("PUBLISH_DATA_ERROR", e.getMessage());
        }
    }

    @PostMapping("/rollback")
    @Operation(summary = "回滚字典数据", description = "回滚字典数据到指定版本")
    public Response rollbackData(@Valid @RequestBody DictionaryRollbackCmd cmd) {
        log.info("回滚字典数据: type={}, version={}", cmd.getDictionaryType(), cmd.getTargetVersion());
        
        try {
            Boolean success = dictionaryAdminService.rollbackDictionaryData(cmd);
            if (success) {
                // 异步刷新相关缓存
                refreshCacheAsync(cmd.getDictionaryType(), cmd.getTenant(), cmd.getChannel(), cmd.getDomain());
                return Response.buildSuccess();
            } else {
                return Response.buildFailure("ROLLBACK_DATA_FAILED", "回滚字典数据失败");
            }
        } catch (Exception e) {
            log.error("回滚字典数据失败: cmd={}", cmd, e);
            return Response.buildFailure("ROLLBACK_DATA_ERROR", e.getMessage());
        }
    }

    @PostMapping("/history")
    @Operation(summary = "获取字典历史", description = "获取字典数据的变更历史记录")
    public MultiResponse<DictionaryHistoryDTO> getHistory(@Valid @RequestBody DictionaryHistoryQry qry) {
        log.debug("获取字典历史: type={}", qry.getDictionaryType());
        
        try {
            List<DictionaryHistoryDTO> history = dictionaryAdminService.getDictionaryHistory(qry);
            return MultiResponse.of(history);
        } catch (Exception e) {
            log.error("获取字典历史失败: qry={}", qry, e);
            return MultiResponse.buildFailure("GET_HISTORY_FAILED", e.getMessage());
        }
    }

    // ==================== 缓存管理 ====================

    @PostMapping("/cache/refresh")
    @Operation(summary = "刷新字典缓存", description = "手动刷新字典数据缓存")
    public Response refreshCache(
            @Parameter(description = "字典类型") @RequestParam(required = false) String dictionaryType,
            @Parameter(description = "租户标识") @RequestParam(required = false) String tenant,
            @Parameter(description = "渠道标识") @RequestParam(required = false) String channel,
            @Parameter(description = "领域标识") @RequestParam(required = false) String domain) {
        log.info("刷新字典缓存: type={}, tenant={}, channel={}, domain={}", dictionaryType, tenant, channel, domain);
        
        try {
            dictionaryService.refreshCache(dictionaryType, tenant, channel, domain);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("刷新字典缓存失败: type={}, tenant={}, channel={}, domain={}", 
                     dictionaryType, tenant, channel, domain, e);
            return Response.buildFailure("REFRESH_CACHE_FAILED", e.getMessage());
        }
    }

    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计", description = "获取字典缓存的统计信息")
    public SingleResponse<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = dictionaryService.getCacheStats();
            return SingleResponse.of(stats);
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return SingleResponse.buildFailure("GET_CACHE_STATS_FAILED", e.getMessage());
        }
    }
    */

    // ==================== 统计分析 ====================
    // TODO: Temporarily disabled - missing DTO methods
    /*

    @PostMapping("/stats")
    @Operation(summary = "获取字典统计", description = "获取字典数据的统计分析信息")
    public SingleResponse<DictionaryStatsResult> getStatistics(@Valid @RequestBody DictionaryStatsQry qry) {
        log.debug("获取字典统计: type={}", qry.getDictionaryType());
        
        try {
            DictionaryStatsResult stats = dictionaryAdminService.getDictionaryStats(qry);
            return SingleResponse.of(stats);
        } catch (Exception e) {
            log.error("获取字典统计失败: qry={}", qry, e);
            return SingleResponse.buildFailure("GET_STATS_FAILED", e.getMessage());
        }
    }

    @GetMapping("/types")
    @Operation(summary = "获取字典类型", description = "获取系统支持的所有字典类型列表")
    public MultiResponse<DictionaryTypeDTO> getDictionaryTypes() {
        try {
            List<DictionaryTypeDTO> types = dictionaryAdminService.getDictionaryTypes();
            return MultiResponse.of(types);
        } catch (Exception e) {
            log.error("获取字典类型失败", e);
            return MultiResponse.buildFailure("GET_DICT_TYPES_FAILED", e.getMessage());
        }
    }

    @PostMapping("/configs")
    @Operation(summary = "获取字典配置", description = "获取字典配置信息列表")
    public MultiResponse<DictionaryConfigDTO> getConfigs(@Valid @RequestBody DictionaryConfigQry qry) {
        log.debug("获取字典配置: tenant={}, channel={}, domain={}", qry.getTenant(), qry.getChannel(), qry.getDomain());
        
        try {
            List<DictionaryConfigDTO> configs = dictionaryAdminService.getDictionaryConfigs(qry);
            return MultiResponse.of(configs);
        } catch (Exception e) {
            log.error("获取字典配置失败: qry={}", qry, e);
            return MultiResponse.buildFailure("GET_CONFIGS_FAILED", e.getMessage());
        }
    }
    */

    // ==================== 缓存管理 ====================

    @PostMapping("/refresh")
    @Operation(summary = "刷新字典缓存", description = "手动刷新字典数据缓存")
    public Response refreshCache(
            @Parameter(description = "字典类型") @RequestParam(required = false) String dictionaryType,
            @Parameter(description = "租户标识") @RequestParam(required = false) String tenant,
            @Parameter(description = "渠道标识") @RequestParam(required = false) String channel,
            @Parameter(description = "领域标识") @RequestParam(required = false) String domain) {
        log.info("刷新字典缓存: type={}, tenant={}, channel={}, domain={}", dictionaryType, tenant, channel, domain);
        
        try {
            dictionaryService.refreshCache(dictionaryType, tenant, channel, domain);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("刷新字典缓存失败: type={}, tenant={}, channel={}, domain={}", 
                     dictionaryType, tenant, channel, domain, e);
            return Response.buildFailure("REFRESH_CACHE_FAILED", e.getMessage());
        }
    }

    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计", description = "获取字典缓存的统计信息")
    public SingleResponse<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = dictionaryService.getCacheStats();
            return SingleResponse.of(stats);
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return SingleResponse.buildFailure("GET_CACHE_STATS_FAILED", e.getMessage());
        }
    }

    @GetMapping("/types")
    @Operation(summary = "获取字典类型", description = "获取系统支持的所有字典类型列表")
    public MultiResponse<DictionaryTypeDTO> getDictionaryTypes() {
        try {
            List<DictionaryTypeDTO> types = dictionaryAdminService.getDictionaryTypes();
            return MultiResponse.of(types);
        } catch (Exception e) {
            log.error("获取字典类型失败", e);
            return MultiResponse.buildFailure("GET_DICT_TYPES_FAILED", e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 异步刷新缓存
     */
    private void refreshCacheAsync(String dictionaryType, String tenant, String channel, String domain) {
        // 这里可以用异步方式刷新缓存，避免影响响应时间
        try {
            dictionaryService.refreshCache(dictionaryType, tenant, channel, domain);
        } catch (Exception e) {
            log.warn("异步刷新字典缓存失败: type={}, tenant={}, channel={}, domain={}", 
                     dictionaryType, tenant, channel, domain, e);
        }
    }

    /**
     * 将字典应用到文本上进行预览
     */
    private String applyDictionaryToText(String dictionaryType, String tenant, String channel, String domain, String testText) {
        try {
            // 根据字典类型获取对应的数据
            switch (dictionaryType) {
                case "normalization_rules":
                    Map<String, String> rules = dictionaryService.getNormalizationRules(tenant, channel, domain);
                    return applyMapRules(testText, rules);
                case "phonetic_corrections":
                    Map<String, String> corrections = dictionaryService.getPhoneticCorrections(tenant, channel, domain);
                    return applyMapRules(testText, corrections);
                case "stop_words":
                    java.util.Set<String> stopWords = dictionaryService.getStopWords(tenant, channel, domain);
                    return removeStopWords(testText, stopWords);
                default:
                    return testText + " [未支持的字典类型预览: " + dictionaryType + "]";
            }
        } catch (Exception e) {
            return testText + " [预览失败: " + e.getMessage() + "]";
        }
    }

    /**
     * 应用映射规则
     */
    private String applyMapRules(String text, Map<String, String> rules) {
        String result = text;
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 移除停用词
     */
    private String removeStopWords(String text, java.util.Set<String> stopWords) {
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(word);
            }
        }
        return result.toString();
    }
}