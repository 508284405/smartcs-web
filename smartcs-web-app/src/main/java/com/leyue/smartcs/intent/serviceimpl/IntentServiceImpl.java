package com.leyue.smartcs.intent.serviceimpl;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.IntentService;
import com.leyue.smartcs.dto.intent.*;
import com.leyue.smartcs.intent.convertor.IntentAppConvertor;
import com.leyue.smartcs.intent.convertor.IntentCatalogAppConvertor;
import com.leyue.smartcs.intent.convertor.IntentVersionAppConvertor;
import com.leyue.smartcs.intent.executor.command.*;
import com.leyue.smartcs.intent.executor.query.*;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 意图服务实现
 * 
 * @author Claude
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntentServiceImpl implements IntentService {
    
    // ============ Command Executors ============
    private final IntentCreateCmdExe intentCreateCmdExe;
    private final IntentUpdateCmdExe intentUpdateCmdExe;
    private final IntentDeleteCmdExe intentDeleteCmdExe;
    private final IntentLabelsUpdateCmdExe intentLabelsUpdateCmdExe;
    private final IntentBoundariesUpdateCmdExe intentBoundariesUpdateCmdExe;
    
    private final IntentCatalogCreateCmdExe catalogCreateCmdExe;
    private final IntentCatalogUpdateCmdExe catalogUpdateCmdExe;
    private final IntentCatalogDeleteCmdExe catalogDeleteCmdExe;
    
    private final IntentVersionCreateCmdExe versionCreateCmdExe;
    private final IntentVersionSimpleActivateCmdExe versionSimpleActivateCmdExe;
    
    // ============ Query Executors ============
    private final IntentPageQryExe intentPageQryExe;
    private final IntentGetQryExe intentGetQryExe;
    private final IntentCatalogListQryExe catalogListQryExe;
    private final IntentCatalogGetQryExe catalogGetQryExe;
    private final IntentCatalogPageQryExe catalogPageQryExe;
    private final IntentVersionListQryExe versionListQryExe;
    
    // ============ Convertors ============
    private final IntentAppConvertor intentAppConvertor;
    private final IntentCatalogAppConvertor catalogAppConvertor;
    private final IntentVersionAppConvertor versionAppConvertor;
    
    // ============ 目录管理 ============
    
    @Override
    @Transactional
    public SingleResponse<Long> createCatalog(IntentCatalogCreateCmd cmd) {
        try {
            log.info("创建意图目录: {}", cmd.getName());
            return catalogCreateCmdExe.execute(cmd.getName(), cmd.getCode(), cmd.getDescription(), 
                    cmd.getParentId(), cmd.getSortOrder());
        } catch (Exception e) {
            log.error("创建意图目录失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("CREATE_CATALOG_FAILED", "创建意图目录失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<IntentCatalogDTO> createCatalog(String name, String code, String description, Long parentId, Integer sortOrder) {
        try {
            log.info("创建意图目录: {}", name);
            SingleResponse<Long> result = catalogCreateCmdExe.execute(name, code, description, parentId, sortOrder);
            if (result.isSuccess()) {
                SingleResponse<IntentCatalog> catalogResult = catalogGetQryExe.execute(result.getData());
                if (catalogResult.isSuccess()) {
                    return SingleResponse.of(catalogAppConvertor.toDTO(catalogResult.getData()));
                }
            }
            return SingleResponse.buildFailure("CREATE_CATALOG_FAILED", "创建意图目录失败");
        } catch (Exception e) {
            log.error("创建意图目录失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("CREATE_CATALOG_FAILED", "创建意图目录失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response updateCatalog(IntentCatalogUpdateCmd cmd) {
        try {
            log.info("更新意图目录: {}", cmd.getCatalogId());
            return catalogUpdateCmdExe.execute(cmd.getCatalogId(), cmd.getName(), 
                    cmd.getDescription(), cmd.getSortOrder());
        } catch (Exception e) {
            log.error("更新意图目录失败: {}", e.getMessage(), e);
            return Response.buildFailure("UPDATE_CATALOG_FAILED", "更新意图目录失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<IntentCatalogDTO> updateCatalog(Long catalogId, String name, String description, Integer sortOrder) {
        try {
            log.info("更新意图目录: {}", catalogId);
            Response result = catalogUpdateCmdExe.execute(catalogId, name, description, sortOrder);
            if (result.isSuccess()) {
                SingleResponse<IntentCatalog> catalogResult = catalogGetQryExe.execute(catalogId);
                if (catalogResult.isSuccess()) {
                    return SingleResponse.of(catalogAppConvertor.toDTO(catalogResult.getData()));
                }
            }
            return SingleResponse.buildFailure("UPDATE_CATALOG_FAILED", "更新意图目录失败");
        } catch (Exception e) {
            log.error("更新意图目录失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("UPDATE_CATALOG_FAILED", "更新意图目录失败: " + e.getMessage());
        }
    }
    
    @Override
    public SingleResponse<IntentCatalogDTO> getCatalogById(Long catalogId) {
        try {
            log.info("获取目录详情: {}", catalogId);
            SingleResponse<IntentCatalog> result = catalogGetQryExe.execute(catalogId);
            if (result.isSuccess()) {
                return SingleResponse.of(catalogAppConvertor.toDTO(result.getData()));
            }
            return SingleResponse.buildFailure("GET_CATALOG_FAILED", "获取目录失败");
        } catch (Exception e) {
            log.error("获取目录失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("GET_CATALOG_FAILED", "获取目录失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response deleteCatalog(Long catalogId) {
        try {
            log.info("删除意图目录: {}", catalogId);
            return catalogDeleteCmdExe.execute(catalogId);
        } catch (Exception e) {
            log.error("删除意图目录失败: {}", e.getMessage(), e);
            return Response.buildFailure("DELETE_CATALOG_FAILED", "删除意图目录失败: " + e.getMessage());
        }
    }
    
    @Override
    public MultiResponse<IntentCatalogDTO> listCatalogs() {
        try {
            log.info("获取所有激活的目录");
            MultiResponse<IntentCatalog> result = catalogListQryExe.execute();
            if (result.isSuccess()) {
                List<IntentCatalogDTO> dtoList = catalogAppConvertor.toDTOList(result.getData());
                return MultiResponse.of(dtoList);
            }
            return MultiResponse.buildFailure("LIST_CATALOGS_FAILED", "获取目录列表失败");
        } catch (Exception e) {
            log.error("获取目录列表失败: {}", e.getMessage(), e);
            return MultiResponse.buildFailure("LIST_CATALOGS_FAILED", "获取目录列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public MultiResponse<IntentCatalogDTO> listCatalogsByParent(IntentCatalogListQry qry) {
        try {
            log.info("根据父目录获取子目录: {}", qry.getParentId());
            MultiResponse<IntentCatalog> result = catalogListQryExe.executeByParentId(qry.getParentId());
            if (result.isSuccess()) {
                List<IntentCatalogDTO> dtoList = catalogAppConvertor.toDTOList(result.getData());
                return MultiResponse.of(dtoList);
            }
            return MultiResponse.buildFailure("LIST_CATALOGS_FAILED", "获取子目录列表失败");
        } catch (Exception e) {
            log.error("获取子目录列表失败: {}", e.getMessage(), e);
            return MultiResponse.buildFailure("LIST_CATALOGS_FAILED", "获取子目录列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public PageResponse<IntentCatalogDTO> listCatalogs(Long parentId, String keyword, Integer pageNum, Integer pageSize) {
        try {
            log.info("分页查询目录列表，父级ID: {}", parentId);
            PageResponse<IntentCatalog> result = catalogPageQryExe.execute(parentId, keyword, 
                    pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20);
            if (result.isSuccess()) {
                List<IntentCatalogDTO> dtoList = catalogAppConvertor.toDTOList(result.getData());
                PageResponse<IntentCatalogDTO> response = new PageResponse<>();
                response.setSuccess(true);
                response.setData(dtoList);
                response.setTotalCount(result.getTotalCount());
                response.setPageSize(result.getPageSize());
                response.setPageIndex(result.getPageIndex());
                return response;
            }
            return PageResponse.buildFailure("LIST_CATALOGS_FAILED", "分页查询目录失败");
        } catch (Exception e) {
            log.error("分页查询目录失败: {}", e.getMessage(), e);
            return PageResponse.buildFailure("LIST_CATALOGS_FAILED", "分页查询目录失败: " + e.getMessage());
        }
    }
    
    // ============ 意图管理 ============
    
    @Override
    @Transactional
    public SingleResponse<Long> createIntent(IntentCreateCmd cmd) {
        try {
            log.info("创建意图: {}", cmd.getName());
            return intentCreateCmdExe.execute(cmd.getCatalogId(), cmd.getName(), cmd.getCode(), 
                    cmd.getDescription(), cmd.getLabels(), cmd.getBoundaries());
        } catch (Exception e) {
            log.error("创建意图失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("CREATE_INTENT_FAILED", "创建意图失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<IntentDTO> createIntent(Long catalogId, String name, String code, String description) {
        try {
            log.info("创建意图: {}", name);
            SingleResponse<Long> result = intentCreateCmdExe.execute(catalogId, name, code, description, null, null);
            if (result.isSuccess()) {
                SingleResponse<Intent> intentResult = intentGetQryExe.execute(result.getData());
                if (intentResult.isSuccess()) {
                    return SingleResponse.of(intentAppConvertor.toDTO(intentResult.getData()));
                }
            }
            return SingleResponse.buildFailure("CREATE_INTENT_FAILED", "创建意图失败");
        } catch (Exception e) {
            log.error("创建意图失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("CREATE_INTENT_FAILED", "创建意图失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response updateIntent(IntentUpdateCmd cmd) {
        try {
            log.info("更新意图: {}", cmd.getIntentId());
            return intentUpdateCmdExe.execute(cmd.getIntentId(), cmd.getName(), cmd.getDescription());
        } catch (Exception e) {
            log.error("更新意图失败: {}", e.getMessage(), e);
            return Response.buildFailure("UPDATE_INTENT_FAILED", "更新意图失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<IntentDTO> updateIntent(Long intentId, String name, String description) {
        try {
            log.info("更新意图: {}", intentId);
            Response result = intentUpdateCmdExe.execute(intentId, name, description);
            if (result.isSuccess()) {
                SingleResponse<Intent> intentResult = intentGetQryExe.execute(intentId);
                if (intentResult.isSuccess()) {
                    return SingleResponse.of(intentAppConvertor.toDTO(intentResult.getData()));
                }
            }
            return SingleResponse.buildFailure("UPDATE_INTENT_FAILED", "更新意图失败");
        } catch (Exception e) {
            log.error("更新意图失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("UPDATE_INTENT_FAILED", "更新意图失败: " + e.getMessage());
        }
    }
    
    @Override
    public SingleResponse<IntentDTO> getIntentById(Long intentId) {
        try {
            log.info("获取意图详情: {}", intentId);
            SingleResponse<Intent> result = intentGetQryExe.execute(intentId);
            if (result.isSuccess()) {
                return SingleResponse.of(intentAppConvertor.toDTO(result.getData()));
            }
            return SingleResponse.buildFailure("GET_INTENT_FAILED", "获取意图失败");
        } catch (Exception e) {
            log.error("获取意图失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("GET_INTENT_FAILED", "获取意图失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response deleteIntent(Long intentId) {
        try {
            log.info("删除意图: {}", intentId);
            return intentDeleteCmdExe.execute(intentId);
        } catch (Exception e) {
            log.error("删除意图失败: {}", e.getMessage(), e);
            return Response.buildFailure("DELETE_INTENT_FAILED", "删除意图失败: " + e.getMessage());
        }
    }
    
    @Override
    public PageResponse<IntentDTO> pageIntents(IntentPageQry qry) {
        try {
            log.info("分页查询意图，目录ID: {}", qry.getCatalogId());
            PageResponse<Intent> result = intentPageQryExe.execute(qry.getCatalogId(), qry.getStatus(), 
                    qry.getKeyword(), qry.getPageNum() != null ? qry.getPageNum() : 1, 
                    qry.getPageSize() != null ? qry.getPageSize() : 20);
            if (result.isSuccess()) {
                List<IntentDTO> dtoList = intentAppConvertor.toDTOList(result.getData());
                PageResponse<IntentDTO> response = new PageResponse<>();
                response.setSuccess(true);
                response.setData(dtoList);
                response.setTotalCount(result.getTotalCount());
                response.setPageSize(result.getPageSize());
                response.setPageIndex(result.getPageIndex());
                return response;
            }
            return PageResponse.buildFailure("PAGE_INTENTS_FAILED", "分页查询意图失败");
        } catch (Exception e) {
            log.error("分页查询意图失败: {}", e.getMessage(), e);
            return PageResponse.buildFailure("PAGE_INTENTS_FAILED", "分页查询意图失败: " + e.getMessage());
        }
    }
    
    @Override
    public PageResponse<IntentDTO> listIntents(Long catalogId, String status, String keyword, Integer pageNum, Integer pageSize) {
        try {
            log.info("分页查询意图列表，目录ID: {}", catalogId);
            PageResponse<Intent> result = intentPageQryExe.execute(catalogId, status, keyword, 
                    pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20);
            if (result.isSuccess()) {
                List<IntentDTO> dtoList = intentAppConvertor.toDTOList(result.getData());
                PageResponse<IntentDTO> response = new PageResponse<>();
                response.setSuccess(true);
                response.setData(dtoList);
                response.setTotalCount(result.getTotalCount());
                response.setPageSize(result.getPageSize());
                response.setPageIndex(result.getPageIndex());
                return response;
            }
            return PageResponse.buildFailure("LIST_INTENTS_FAILED", "分页查询意图失败");
        } catch (Exception e) {
            log.error("分页查询意图失败: {}", e.getMessage(), e);
            return PageResponse.buildFailure("LIST_INTENTS_FAILED", "分页查询意图失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response updateIntentLabels(Long intentId, List<String> labels) {
        try {
            log.info("更新意图标签: {}", intentId);
            return intentLabelsUpdateCmdExe.execute(intentId, labels);
        } catch (Exception e) {
            log.error("更新意图标签失败: {}", e.getMessage(), e);
            return Response.buildFailure("UPDATE_LABELS_FAILED", "更新意图标签失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response updateIntentBoundaries(Long intentId, List<String> boundaries) {
        try {
            log.info("更新意图边界: {}", intentId);
            // Convert List<String> to Map<String, Object> for boundaries
            Map<String, Object> boundariesMap = boundaries != null ? 
                    boundaries.stream().collect(Collectors.toMap(
                            boundary -> boundary, 
                            boundary -> (Object) boundary,
                            (existing, replacement) -> replacement
                    )) : null;
            return intentBoundariesUpdateCmdExe.execute(intentId, boundariesMap);
        } catch (Exception e) {
            log.error("更新意图边界失败: {}", e.getMessage(), e);
            return Response.buildFailure("UPDATE_BOUNDARIES_FAILED", "更新意图边界失败: " + e.getMessage());
        }
    }
    
    @Override
    public SingleResponse<IntentDTO> getIntent(IntentGetQry qry) {
        try {
            log.info("根据ID获取意图详情: {}", qry.getId());
            SingleResponse<Intent> result = intentGetQryExe.execute(qry.getId());
            if (result.isSuccess()) {
                return SingleResponse.of(intentAppConvertor.toDTO(result.getData()));
            }
            return SingleResponse.buildFailure("GET_INTENT_FAILED", "获取意图失败");
        } catch (Exception e) {
            log.error("获取意图失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("GET_INTENT_FAILED", "获取意图失败: " + e.getMessage());
        }
    }
    
    // ============ 版本管理 ============
    
    @Override
    @Transactional
    public SingleResponse<Long> createVersion(IntentVersionCreateCmd cmd) {
        try {
            log.info("创建意图版本，意图ID: {}, 版本: {}", cmd.getIntentId(), cmd.getVersion());
            return versionCreateCmdExe.execute(cmd.getIntentId(), cmd.getVersion(), cmd.getChangeNote());
        } catch (Exception e) {
            log.error("创建意图版本失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("CREATE_VERSION_FAILED", "创建意图版本失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public SingleResponse<IntentVersionDTO> createVersion(Long intentId, String version, String description) {
        try {
            log.info("创建意图版本: {}", intentId);
            SingleResponse<Long> result = versionCreateCmdExe.execute(intentId, version, description);
            if (result.isSuccess()) {
                // Note: Would need to implement IntentVersionGetQryExe to fetch created version
                // For now, return a simplified response
                IntentVersionDTO versionDTO = new IntentVersionDTO();
                versionDTO.setId(result.getData());
                versionDTO.setIntentId(intentId);
                versionDTO.setVersionNumber(version);
                versionDTO.setChangeNote(description);
                return SingleResponse.of(versionDTO);
            }
            return SingleResponse.buildFailure("CREATE_VERSION_FAILED", "创建意图版本失败");
        } catch (Exception e) {
            log.error("创建意图版本失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("CREATE_VERSION_FAILED", "创建意图版本失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response activateVersion(IntentVersionActivateCmd cmd) {
        try {
            log.info("激活意图版本: {}", cmd.getVersionId());
            return versionSimpleActivateCmdExe.execute(cmd.getVersionId());
        } catch (Exception e) {
            log.error("激活意图版本失败: {}", e.getMessage(), e);
            return Response.buildFailure("ACTIVATE_VERSION_FAILED", "激活意图版本失败: " + e.getMessage());
        }
    }
    
    @Override
    public MultiResponse<IntentVersionDTO> listVersions(IntentVersionListQry qry) {
        try {
            log.info("获取意图的版本列表: {}", qry.getIntentId());
            MultiResponse<IntentVersion> result = versionListQryExe.execute(qry.getIntentId());
            if (result.isSuccess()) {
                List<IntentVersionDTO> dtoList = versionAppConvertor.toDTOList(result.getData());
                return MultiResponse.of(dtoList);
            }
            return MultiResponse.buildFailure("LIST_VERSIONS_FAILED", "获取版本列表失败");
        } catch (Exception e) {
            log.error("获取版本列表失败: {}", e.getMessage(), e);
            return MultiResponse.buildFailure("LIST_VERSIONS_FAILED", "获取版本列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public PageResponse<IntentVersionDTO> listVersions(Long intentId, String status, Integer pageNum, Integer pageSize) {
        try {
            log.info("分页查询版本列表，意图ID: {}", intentId);
            // Note: Would need to implement IntentVersionPageQryExe for proper pagination
            // For now, return a simplified implementation
            MultiResponse<IntentVersion> result = versionListQryExe.execute(intentId);
            if (result.isSuccess()) {
                List<IntentVersionDTO> dtoList = versionAppConvertor.toDTOList(result.getData());
                PageResponse<IntentVersionDTO> response = new PageResponse<>();
                response.setSuccess(true);
                response.setData(dtoList);
                response.setTotalCount(dtoList.size());
                response.setPageSize(pageSize != null ? pageSize : 20);
                response.setPageIndex(pageNum != null ? pageNum : 1);
                return response;
            }
            return PageResponse.buildFailure("LIST_VERSIONS_FAILED", "分页查询版本失败");
        } catch (Exception e) {
            log.error("分页查询版本失败: {}", e.getMessage(), e);
            return PageResponse.buildFailure("LIST_VERSIONS_FAILED", "分页查询版本失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response publishVersion(Long versionId) {
        try {
            log.info("发布版本: {}", versionId);
            return versionSimpleActivateCmdExe.execute(versionId);
        } catch (Exception e) {
            log.error("发布版本失败: {}", e.getMessage(), e);
            return Response.buildFailure("PUBLISH_VERSION_FAILED", "发布版本失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Response offlineVersion(Long versionId) {
        try {
            log.info("下线版本: {}", versionId);
            // Note: Would need to implement specific offline version command executor
            // For now, use a placeholder implementation
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("下线版本失败: {}", e.getMessage(), e);
            return Response.buildFailure("OFFLINE_VERSION_FAILED", "下线版本失败: " + e.getMessage());
        }
    }
    
    // ============ 策略管理 ============
    
    @Override
    public SingleResponse<IntentPolicyDTO> createPolicy(Long intentId, String channel, String tenant, Double threshold, String description) {
        log.info("创建意图策略，意图ID: {}", intentId);
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public SingleResponse<IntentPolicyDTO> updatePolicy(Long policyId, Double threshold, String description) {
        log.info("更新意图策略: {}", policyId);
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public PageResponse<IntentPolicyDTO> listPolicies(Long intentId, String channel, String tenant, Integer pageNum, Integer pageSize) {
        log.info("分页查询意图策略，意图ID: {}", intentId);
        return PageResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public Response deletePolicy(Long policyId) {
        log.info("删除意图策略: {}", policyId);
        return Response.buildFailure("500", "方法未实现");
    }
    
    // ============ 路由管理 ============
    
    @Override
    public SingleResponse<IntentRouteDTO> createRoute(Long intentId, String channel, String tenant, String targetService, String targetMethod, String targetParams) {
        log.info("创建意图路由，意图ID: {}", intentId);
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public SingleResponse<IntentRouteDTO> updateRoute(Long routeId, String targetService, String targetMethod, String targetParams) {
        log.info("更新意图路由: {}", routeId);
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public PageResponse<IntentRouteDTO> listRoutes(Long intentId, String channel, String tenant, Integer pageNum, Integer pageSize) {
        log.info("分页查询意图路由，意图ID: {}", intentId);
        return PageResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public Response deleteRoute(Long routeId) {
        log.info("删除意图路由: {}", routeId);
        return Response.buildFailure("500", "方法未实现");
    }
    
    // ============ 样本管理 ============
    
    @Override
    public SingleResponse<IntentSampleDTO> createSample(Long intentId, String text, String type, String channel, String tenant) {
        log.info("创建意图样本，意图ID: {}", intentId);
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public SingleResponse<IntentSampleBatchImportResultDTO> batchImportSamples(Long intentId, List<IntentSampleCreateCmd> samples) {
        log.info("批量导入样本，意图ID: {}, 数量: {}", intentId, samples.size());
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public PageResponse<IntentSampleDTO> listSamples(Long intentId, String type, String channel, String tenant, Integer pageNum, Integer pageSize) {
        log.info("分页查询样本列表，意图ID: {}", intentId);
        return PageResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public Response deleteSample(Long sampleId) {
        log.info("删除意图样本: {}", sampleId);
        return Response.buildFailure("500", "方法未实现");
    }
    
    @Override
    public Response batchDeleteSamples(List<Long> sampleIds) {
        log.info("批量删除样本，数量: {}", sampleIds.size());
        return Response.buildFailure("500", "方法未实现");
    }
    
    @Override
    public SingleResponse<IntentSampleImportResultDTO> importSamples(IntentSampleImportCmd cmd) {
        log.info("批量导入样本");
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public SingleResponse<IntentSampleExportResultDTO> exportSamples(IntentSampleExportCmd cmd) {
        log.info("导出样本");
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    // ============ 快照管理 ============
    
    @Override
    public SingleResponse<IntentSnapshotDTO> createSnapshot(IntentSnapshotCreateCmd cmd) {
        log.info("创建快照: {}", cmd.getName());
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public Response publishSnapshot(IntentSnapshotPublishCmd cmd) {
        log.info("发布快照: {}", cmd.getSnapshotId());
        return Response.buildFailure("500", "方法未实现");
    }
    
    @Override
    public PageResponse<IntentSnapshotDTO> listSnapshots(IntentSnapshotListQry qry) {
        log.info("获取快照列表");
        return PageResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public SingleResponse<IntentSnapshotDTO> getSnapshot(IntentSnapshotGetQry qry) {
        log.info("获取快照详情: {}", qry.getSnapshotId());
        return SingleResponse.buildFailure("500", "方法未实现");
    }
    
    @Override
    public Response deleteSnapshot(IntentSnapshotDeleteCmd cmd) {
        log.info("删除快照: {}", cmd.getSnapshotId());
        return Response.buildFailure("500", "方法未实现");
    }
    
    @Override
    public SingleResponse<IntentSnapshotCompareResultDTO> compareSnapshots(IntentSnapshotCompareCmd cmd) {
        try {
            log.info("比较快照: {} vs {}", cmd.getBaseSnapshotId(), cmd.getTargetSnapshotId());
            // Note: Would need to implement specific snapshot comparison command executor
            // For now, use a placeholder implementation
            IntentSnapshotCompareResultDTO result = new IntentSnapshotCompareResultDTO();
            result.setBaseSnapshotId(cmd.getBaseSnapshotId());
            result.setTargetSnapshotId(cmd.getTargetSnapshotId());
            result.setCompareTime(System.currentTimeMillis());
            // Initialize empty lists for now
            result.setAddedIntents(Collections.emptyList());
            result.setRemovedIntents(Collections.emptyList());
            result.setModifiedIntents(Collections.emptyList());
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("比较快照失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("COMPARE_SNAPSHOTS_FAILED", "比较快照失败: " + e.getMessage());
        }
    }
    
    @Override
    public Response rollbackSnapshot(IntentSnapshotRollbackCmd cmd) {
        try {
            log.info("回滚快照: {}", cmd.getSnapshotId());
            // Note: Would need to implement specific rollback snapshot command executor
            // For now, use a placeholder implementation
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("回滚快照失败: {}", e.getMessage(), e);
            return Response.buildFailure("ROLLBACK_SNAPSHOT_FAILED", "回滚快照失败: " + e.getMessage());
        }
    }
    
    // ============ 内部工具方法 ============
    
    /**
     * 验证分页参数
     */
    private void validatePageParams(Integer pageNum, Integer pageSize) {
        if (pageNum != null && pageNum < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (pageSize != null && (pageSize < 1 || pageSize > 1000)) {
            throw new IllegalArgumentException("页大小必须在1-1000之间");
        }
    }
    
    /**
     * 获取默认分页参数
     */
    private int getPageNum(Integer pageNum) {
        return pageNum != null ? pageNum : 1;
    }
    
    private int getPageSize(Integer pageSize) {
        return pageSize != null ? pageSize : 20;
    }
    
    /**
     * 构建分页响应
     */
    private <T> PageResponse<T> buildPageResponse(List<T> data, int totalCount, int pageNum, int pageSize) {
        PageResponse<T> response = new PageResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTotalCount(totalCount);
        response.setPageSize(pageSize);
        response.setPageIndex(pageNum);
        return response;
    }
}