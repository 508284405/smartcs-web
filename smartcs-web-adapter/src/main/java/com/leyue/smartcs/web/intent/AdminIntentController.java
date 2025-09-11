package com.leyue.smartcs.web.intent;

import com.alibaba.cola.dto.MultiResponse;
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
 * 管理端意图管理控制器
 * 提供意图管理系统的管理功能，包括目录管理、意图管理、策略配置等
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/intent")
@RequiredArgsConstructor
@Validated
public class AdminIntentController {
    
    private final IntentService intentService;
    
    // ====== 目录管理 ======
    
    /**
     * 创建意图目录
     */
    @PostMapping("/catalogs")
    public SingleResponse<IntentCatalogDTO> createCatalog(@Valid @RequestBody IntentCatalogCreateCmd cmd) {
        log.info("管理端创建意图目录: {}", cmd.getName());
        return intentService.createCatalog(cmd.getName(), cmd.getCode(), cmd.getDescription(), 
                                         cmd.getParentId(), cmd.getSortOrder());
    }
    
    /**
     * 更新意图目录
     */
    @PutMapping("/catalogs/{catalogId}")
    public SingleResponse<IntentCatalogDTO> updateCatalog(@PathVariable Long catalogId,
                                                        @Valid @RequestBody IntentCatalogUpdateCmd cmd) {
        log.info("管理端更新意图目录: {}", catalogId);
        cmd.setCatalogId(catalogId);
        return intentService.updateCatalog(cmd.getCatalogId(), cmd.getName(), cmd.getDescription(), cmd.getSortOrder());
    }
    
    /**
     * 查询目录详情
     */
    @GetMapping("/catalogs/{catalogId}")
    public SingleResponse<IntentCatalogDTO> getCatalog(@PathVariable Long catalogId) {
        log.info("管理端查询意图目录详情: {}", catalogId);
        return intentService.getCatalogById(catalogId);
    }
    
    /**
     * 查询目录列表
     */
    @GetMapping("/catalogs")
    public PageResponse<IntentCatalogDTO> listCatalogs(IntentCatalogPageQry qry) {
        log.info("管理端查询意图目录列表，父级ID: {}", qry.getParentId());
        return intentService.listCatalogs(qry.getParentId(), qry.getKeyword(), qry.getPageNum(), qry.getPageSize());
    }
    
    /**
     * 获取所有目录列表（不分页）
     */
    @GetMapping("/catalog/list")
    public MultiResponse<IntentCatalogDTO> getAllCatalogs() {
        log.info("管理端获取所有意图目录列表");
        return intentService.listCatalogs();
    }
    
    /**
     * 删除意图目录
     */
    @DeleteMapping("/catalogs/{catalogId}")
    public Response deleteCatalog(@PathVariable Long catalogId) {
        log.info("管理端删除意图目录: {}", catalogId);
        return intentService.deleteCatalog(catalogId);
    }
    
    // ====== 意图管理 ======
    
    /**
     * 创建意图
     */
    @PostMapping("/intents")
    public SingleResponse<IntentDTO> createIntent(@Valid @RequestBody IntentCreateCmd cmd) {
        log.info("管理端创建意图: {}", cmd.getName());
        return intentService.createIntent(cmd.getCatalogId(), cmd.getName(), cmd.getCode(), cmd.getDescription());
    }
    
    /**
     * 更新意图
     */
    @PutMapping("/intents/{intentId}")
    public SingleResponse<IntentDTO> updateIntent(@PathVariable Long intentId,
                                                @Valid @RequestBody IntentUpdateCmd cmd) {
        log.info("管理端更新意图: {}", intentId);
        cmd.setIntentId(intentId);
        return intentService.updateIntent(cmd.getIntentId(), cmd.getName(), cmd.getDescription());
    }
    
    /**
     * 查询意图详情
     */
    @GetMapping("/intents/{intentId}")
    public SingleResponse<IntentDTO> getIntent(@PathVariable Long intentId) {
        log.info("管理端查询意图详情: {}", intentId);
        return intentService.getIntentById(intentId);
    }
    
    /**
     * 查询意图列表
     */
    @GetMapping("/intents")
    public PageResponse<IntentDTO> listIntents(IntentPageQry qry) {
        log.info("管理端查询意图列表，目录ID: {}", qry.getCatalogId());
        return intentService.listIntents(qry.getCatalogId(), qry.getStatus(), qry.getKeyword(), 
                                       qry.getPageNum(), qry.getPageSize());
    }
    
    /**
     * 分页查询意图列表
     */
    @GetMapping("/page")
    public PageResponse<IntentDTO> pageIntents(IntentPageQry qry) {
        log.info("管理端分页查询意图列表，目录ID: {}", qry.getCatalogId());
        return intentService.listIntents(qry.getCatalogId(), qry.getStatus(), qry.getKeyword(), 
                                       qry.getPageNum(), qry.getPageSize());
    }
    
    /**
     * 删除意图
     */
    @DeleteMapping("/intents/{intentId}")
    public Response deleteIntent(@PathVariable Long intentId) {
        log.info("管理端删除意图: {}", intentId);
        return intentService.deleteIntent(intentId);
    }
    
    /**
     * 更新意图标签
     */
    @PutMapping("/intents/{intentId}/labels")
    public Response updateIntentLabels(@PathVariable Long intentId,
                                     @Valid @RequestBody IntentLabelsUpdateCmd cmd) {
        log.info("管理端更新意图标签: {}", intentId);
        cmd.setIntentId(intentId);
        return intentService.updateIntentLabels(cmd.getIntentId(), cmd.getLabels());
    }
    
    /**
     * 更新意图边界
     */
    @PutMapping("/intents/{intentId}/boundaries")
    public Response updateIntentBoundaries(@PathVariable Long intentId,
                                         @Valid @RequestBody IntentBoundariesUpdateCmd cmd) {
        log.info("管理端更新意图边界: {}", intentId);
        cmd.setIntentId(intentId);
        return intentService.updateIntentBoundaries(cmd.getIntentId(), cmd.getBoundaries());
    }
    
    // ====== 版本管理 ======
    
    /**
     * 创建意图版本
     */
    @PostMapping("/intents/{intentId}/versions")
    public SingleResponse<IntentVersionDTO> createVersion(@PathVariable Long intentId,
                                                        @Valid @RequestBody IntentVersionCreateCmd cmd) {
        log.info("管理端创建意图版本，意图ID: {}, 版本: {}", intentId, cmd.getVersion());
        cmd.setIntentId(intentId);
        return intentService.createVersion(cmd.getIntentId(), cmd.getVersion(), cmd.getDescription());
    }
    
    /**
     * 查询版本列表
     */
    @GetMapping("/intents/{intentId}/versions")
    public PageResponse<IntentVersionDTO> listVersions(@PathVariable Long intentId,
                                                     IntentVersionPageQry qry) {
        log.info("管理端查询意图版本列表，意图ID: {}", intentId);
        qry.setIntentId(intentId);
        return intentService.listVersions(qry.getIntentId(), qry.getStatus(), qry.getPageNum(), qry.getPageSize());
    }
    
    /**
     * 发布版本
     */
    @PostMapping("/versions/{versionId}/publish")
    public Response publishVersion(@PathVariable Long versionId) {
        log.info("管理端发布意图版本: {}", versionId);
        return intentService.publishVersion(versionId);
    }
    
    /**
     * 下线版本
     */
    @PostMapping("/versions/{versionId}/offline")
    public Response offlineVersion(@PathVariable Long versionId) {
        log.info("管理端下线意图版本: {}", versionId);
        return intentService.offlineVersion(versionId);
    }
    
    // ====== 策略管理 ======
    
    /**
     * 创建意图策略
     */
    @PostMapping("/intents/{intentId}/policies")
    public SingleResponse<IntentPolicyDTO> createPolicy(@PathVariable Long intentId,
                                                      @Valid @RequestBody IntentPolicyCreateCmd cmd) {
        log.info("管理端创建意图策略，意图ID: {}", intentId);
        cmd.setIntentId(intentId);
        return intentService.createPolicy(cmd.getIntentId(), cmd.getChannel(), cmd.getTenant(), 
                                        cmd.getThreshold(), cmd.getDescription());
    }
    
    /**
     * 更新意图策略
     */
    @PutMapping("/policies/{policyId}")
    public SingleResponse<IntentPolicyDTO> updatePolicy(@PathVariable Long policyId,
                                                      @Valid @RequestBody IntentPolicyUpdateCmd cmd) {
        log.info("管理端更新意图策略: {}", policyId);
        cmd.setPolicyId(policyId);
        return intentService.updatePolicy(cmd.getPolicyId(), cmd.getThreshold(), cmd.getDescription());
    }
    
    /**
     * 查询策略列表
     */
    @GetMapping("/intents/{intentId}/policies")
    public PageResponse<IntentPolicyDTO> listPolicies(@PathVariable Long intentId,
                                                    IntentPolicyPageQry qry) {
        log.info("管理端查询意图策略列表，意图ID: {}", intentId);
        qry.setIntentId(intentId);
        return intentService.listPolicies(qry.getIntentId(), qry.getChannel(), qry.getTenant(), 
                                        qry.getPageNum(), qry.getPageSize());
    }
    
    /**
     * 删除意图策略
     */
    @DeleteMapping("/policies/{policyId}")
    public Response deletePolicy(@PathVariable Long policyId) {
        log.info("管理端删除意图策略: {}", policyId);
        return intentService.deletePolicy(policyId);
    }
    
    // ====== 路由管理 ======
    
    /**
     * 创建意图路由
     */
    @PostMapping("/intents/{intentId}/routes")
    public SingleResponse<IntentRouteDTO> createRoute(@PathVariable Long intentId,
                                                    @Valid @RequestBody IntentRouteCreateCmd cmd) {
        log.info("管理端创建意图路由，意图ID: {}", intentId);
        cmd.setIntentId(intentId);
        return intentService.createRoute(cmd.getIntentId(), cmd.getChannel(), cmd.getTenant(), 
                                       cmd.getTargetService(), cmd.getTargetMethod(), cmd.getTargetParams());
    }
    
    /**
     * 更新意图路由
     */
    @PutMapping("/routes/{routeId}")
    public SingleResponse<IntentRouteDTO> updateRoute(@PathVariable Long routeId,
                                                    @Valid @RequestBody IntentRouteUpdateCmd cmd) {
        log.info("管理端更新意图路由: {}", routeId);
        cmd.setRouteId(routeId);
        return intentService.updateRoute(cmd.getRouteId(), cmd.getTargetService(), cmd.getTargetMethod(), cmd.getTargetParams());
    }
    
    /**
     * 查询路由列表
     */
    @GetMapping("/intents/{intentId}/routes")
    public PageResponse<IntentRouteDTO> listRoutes(@PathVariable Long intentId,
                                                 IntentRoutePageQry qry) {
        log.info("管理端查询意图路由列表，意图ID: {}", intentId);
        qry.setIntentId(intentId);
        return intentService.listRoutes(qry.getIntentId(), qry.getChannel(), qry.getTenant(), 
                                      qry.getPageNum(), qry.getPageSize());
    }
    
    /**
     * 删除意图路由
     */
    @DeleteMapping("/routes/{routeId}")
    public Response deleteRoute(@PathVariable Long routeId) {
        log.info("管理端删除意图路由: {}", routeId);
        return intentService.deleteRoute(routeId);
    }
    
    // ====== 样本管理 ======
    
    /**
     * 创建意图样本
     */
    @PostMapping("/intents/{intentId}/samples")
    public SingleResponse<IntentSampleDTO> createSample(@PathVariable Long intentId,
                                                      @Valid @RequestBody IntentSampleCreateCmd cmd) {
        log.info("管理端创建意图样本，意图ID: {}", intentId);
        cmd.setIntentId(intentId);
        return intentService.createSample(cmd.getIntentId(), cmd.getText(), cmd.getType(), 
                                        cmd.getChannel(), cmd.getTenant());
    }
    
    /**
     * 批量导入样本
     */
    @PostMapping("/intents/{intentId}/samples/batch-import")
    public SingleResponse<IntentSampleBatchImportResultDTO> batchImportSamples(@PathVariable Long intentId,
                                                                             @Valid @RequestBody IntentSampleBatchImportCmd cmd) {
        log.info("管理端批量导入意图样本，意图ID: {}, 数量: {}", intentId, cmd.getSamples().size());
        cmd.setIntentId(intentId);
        return intentService.batchImportSamples(cmd.getIntentId(), cmd.getSamples());
    }
    
    /**
     * 查询样本列表
     */
    @GetMapping("/intents/{intentId}/samples")
    public PageResponse<IntentSampleDTO> listSamples(@PathVariable Long intentId,
                                                   IntentSamplePageQry qry) {
        log.info("管理端查询意图样本列表，意图ID: {}", intentId);
        qry.setIntentId(intentId);
        return intentService.listSamples(qry.getIntentId(), qry.getType(), qry.getChannel(), 
                                       qry.getTenant(), qry.getPageNum(), qry.getPageSize());
    }
    
    /**
     * 删除意图样本
     */
    @DeleteMapping("/samples/{sampleId}")
    public Response deleteSample(@PathVariable Long sampleId) {
        log.info("管理端删除意图样本: {}", sampleId);
        return intentService.deleteSample(sampleId);
    }
    
    /**
     * 批量删除样本
     */
    @PostMapping("/samples/batch-delete")
    public Response batchDeleteSamples(@Valid @RequestBody IntentSampleBatchDeleteCmd cmd) {
        log.info("管理端批量删除意图样本，数量: {}", cmd.getSampleIds().size());
        return intentService.batchDeleteSamples(cmd.getSampleIds());
    }
    
    // ====== 槽位模板管理 ======
    
    /**
     * 获取意图槽位模板
     */
    @GetMapping("/intents/{intentId}/slot-template")
    public SingleResponse<SlotTemplateDTO> getSlotTemplate(@PathVariable Long intentId) {
        log.info("管理端获取意图槽位模板: {}", intentId);
        return intentService.getSlotTemplate(intentId);
    }
    
    /**
     * 更新意图槽位模板
     */
    @PutMapping("/intents/{intentId}/slot-template")
    public Response updateSlotTemplate(@PathVariable Long intentId,
                                     @Valid @RequestBody SlotTemplateDTO slotTemplate) {
        log.info("管理端更新意图槽位模板: {}", intentId);
        return intentService.updateSlotTemplate(intentId, slotTemplate);
    }
    
    /**
     * 测试槽位填充
     */
    @PostMapping("/intents/{intentId}/slot-template/test")
    public SingleResponse<SlotFillingTestResultDTO> testSlotFilling(@PathVariable Long intentId,
                                                                  @Valid @RequestBody SlotFillingTestCmd cmd) {
        log.info("管理端测试槽位填充: intentId={}, query={}", intentId, cmd.getQuery());
        return intentService.testSlotFilling(intentId, cmd);
    }
}