package com.leyue.smartcs.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.intent.*;

/**
 * 意图管理服务接口
 * 
 * @author Claude
 */
public interface IntentService {
    
    // ============ 目录管理 ============
    
    /**
     * 创建意图目录
     * @param cmd 创建命令
     * @return 创建的目录ID
     */
    SingleResponse<Long> createCatalog(IntentCatalogCreateCmd cmd);
    
    /**
     * 创建意图目录（简化方法）
     * @param name 名称
     * @param code 编码
     * @param description 描述
     * @param parentId 父目录ID
     * @param sortOrder 排序
     * @return 创建的目录
     */
    SingleResponse<IntentCatalogDTO> createCatalog(String name, String code, String description, Long parentId, Integer sortOrder);
    
    /**
     * 更新意图目录
     * @param cmd 更新命令
     * @return 更新结果
     */
    Response updateCatalog(IntentCatalogUpdateCmd cmd);
    
    /**
     * 更新意图目录（简化方法）
     * @param catalogId 目录ID
     * @param name 名称
     * @param description 描述
     * @param sortOrder 排序
     * @return 更新的目录
     */
    SingleResponse<IntentCatalogDTO> updateCatalog(Long catalogId, String name, String description, Integer sortOrder);
    
    /**
     * 根据ID获取目录详情
     * @param catalogId 目录ID
     * @return 目录详情
     */
    SingleResponse<IntentCatalogDTO> getCatalogById(Long catalogId);
    
    /**
     * 删除意图目录
     * @param catalogId 目录ID
     * @return 删除结果
     */
    Response deleteCatalog(Long catalogId);
    
    /**
     * 获取所有激活的目录
     * @return 目录列表
     */
    MultiResponse<IntentCatalogDTO> listCatalogs();
    
    /**
     * 根据父目录获取子目录
     * @param qry 查询命令
     * @return 子目录列表
     */
    MultiResponse<IntentCatalogDTO> listCatalogsByParent(IntentCatalogListQry qry);
    
    /**
     * 分页查询目录列表
     * @param parentId 父级ID
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<IntentCatalogDTO> listCatalogs(Long parentId, String keyword, Integer pageNum, Integer pageSize);
    
    // ============ 意图管理 ============
    
    /**
     * 创建意图
     * @param cmd 创建命令
     * @return 创建的意图ID
     */
    SingleResponse<Long> createIntent(IntentCreateCmd cmd);
    
    /**
     * 创建意图（简化方法）
     * @param catalogId 目录ID
     * @param name 名称
     * @param code 编码
     * @param description 描述
     * @return 创建的意图
     */
    SingleResponse<IntentDTO> createIntent(Long catalogId, String name, String code, String description);
    
    /**
     * 更新意图
     * @param cmd 更新命令
     * @return 更新结果
     */
    Response updateIntent(IntentUpdateCmd cmd);
    
    /**
     * 更新意图（简化方法）
     * @param intentId 意图ID
     * @param name 名称
     * @param description 描述
     * @return 更新的意图
     */
    SingleResponse<IntentDTO> updateIntent(Long intentId, String name, String description);
    
    /**
     * 根据ID获取意图详情
     * @param intentId 意图ID
     * @return 意图详情
     */
    SingleResponse<IntentDTO> getIntentById(Long intentId);
    
    /**
     * 删除意图
     * @param intentId 意图ID
     * @return 删除结果
     */
    Response deleteIntent(Long intentId);
    
    /**
     * 分页查询意图
     * @param qry 查询命令
     * @return 分页结果
     */
    PageResponse<IntentDTO> pageIntents(IntentPageQry qry);
    
    /**
     * 分页查询意图列表
     * @param catalogId 目录ID
     * @param status 状态
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<IntentDTO> listIntents(Long catalogId, String status, String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 更新意图标签
     * @param intentId 意图ID
     * @param labels 标签列表
     * @return 更新结果
     */
    Response updateIntentLabels(Long intentId, java.util.List<String> labels);
    
    /**
     * 更新意图边界
     * @param intentId 意图ID
     * @param boundaries 边界列表
     * @return 更新结果
     */
    Response updateIntentBoundaries(Long intentId, java.util.List<String> boundaries);
    
    /**
     * 根据ID获取意图详情
     * @param qry 查询命令
     * @return 意图详情
     */
    SingleResponse<IntentDTO> getIntent(IntentGetQry qry);
    
    // ============ 版本管理 ============
    
    /**
     * 创建意图版本
     * @param cmd 创建命令
     * @return 创建的版本ID
     */
    SingleResponse<Long> createVersion(IntentVersionCreateCmd cmd);
    
    /**
     * 创建意图版本（简化方法）
     * @param intentId 意图ID
     * @param version 版本号
     * @param description 描述
     * @return 创建的版本
     */
    SingleResponse<IntentVersionDTO> createVersion(Long intentId, String version, String description);
    
    /**
     * 激活意图版本
     * @param cmd 激活命令
     * @return 激活结果
     */
    Response activateVersion(IntentVersionActivateCmd cmd);
    
    /**
     * 获取意图的版本列表
     * @param qry 查询命令
     * @return 版本列表
     */
    MultiResponse<IntentVersionDTO> listVersions(IntentVersionListQry qry);
    
    /**
     * 分页查询版本列表
     * @param intentId 意图ID
     * @param status 状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<IntentVersionDTO> listVersions(Long intentId, String status, Integer pageNum, Integer pageSize);
    
    /**
     * 发布版本
     * @param versionId 版本ID
     * @return 发布结果
     */
    Response publishVersion(Long versionId);
    
    /**
     * 下线版本
     * @param versionId 版本ID
     * @return 下线结果
     */
    Response offlineVersion(Long versionId);
    
    // ============ 策略管理 ============
    
    /**
     * 创建意图策略
     * @param intentId 意图ID
     * @param channel 渠道
     * @param tenant 租户
     * @param threshold 阈值
     * @param description 描述
     * @return 创建的策略
     */
    SingleResponse<IntentPolicyDTO> createPolicy(Long intentId, String channel, String tenant, Double threshold, String description);
    
    /**
     * 更新意图策略
     * @param policyId 策略ID
     * @param threshold 阈值
     * @param description 描述
     * @return 更新的策略
     */
    SingleResponse<IntentPolicyDTO> updatePolicy(Long policyId, Double threshold, String description);
    
    /**
     * 分页查询意图策略
     * @param intentId 意图ID
     * @param channel 渠道
     * @param tenant 租户
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<IntentPolicyDTO> listPolicies(Long intentId, String channel, String tenant, Integer pageNum, Integer pageSize);
    
    /**
     * 删除意图策略
     * @param policyId 策略ID
     * @return 删除结果
     */
    Response deletePolicy(Long policyId);
    
    // ============ 路由管理 ============
    
    /**
     * 创建意图路由
     * @param intentId 意图ID
     * @param channel 渠道
     * @param tenant 租户
     * @param targetService 目标服务
     * @param targetMethod 目标方法
     * @param targetParams 目标参数
     * @return 创建的路由
     */
    SingleResponse<IntentRouteDTO> createRoute(Long intentId, String channel, String tenant, String targetService, String targetMethod, String targetParams);
    
    /**
     * 更新意图路由
     * @param routeId 路由ID
     * @param targetService 目标服务
     * @param targetMethod 目标方法
     * @param targetParams 目标参数
     * @return 更新的路由
     */
    SingleResponse<IntentRouteDTO> updateRoute(Long routeId, String targetService, String targetMethod, String targetParams);
    
    /**
     * 分页查询意图路由
     * @param intentId 意图ID
     * @param channel 渠道
     * @param tenant 租户
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<IntentRouteDTO> listRoutes(Long intentId, String channel, String tenant, Integer pageNum, Integer pageSize);
    
    /**
     * 删除意图路由
     * @param routeId 路由ID
     * @return 删除结果
     */
    Response deleteRoute(Long routeId);
    
    // ============ 样本管理 ============
    
    /**
     * 创建意图样本
     * @param intentId 意图ID
     * @param text 文本内容
     * @param type 样本类型
     * @param channel 渠道
     * @param tenant 租户
     * @return 创建的样本
     */
    SingleResponse<IntentSampleDTO> createSample(Long intentId, String text, String type, String channel, String tenant);
    
    /**
     * 批量导入样本
     * @param intentId 意图ID
     * @param samples 样本列表
     * @return 导入结果
     */
    SingleResponse<IntentSampleBatchImportResultDTO> batchImportSamples(Long intentId, java.util.List<IntentSampleCreateCmd> samples);
    
    /**
     * 分页查询样本列表
     * @param intentId 意图ID
     * @param type 类型
     * @param channel 渠道
     * @param tenant 租户
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<IntentSampleDTO> listSamples(Long intentId, String type, String channel, String tenant, Integer pageNum, Integer pageSize);
    
    /**
     * 删除意图样本
     * @param sampleId 样本ID
     * @return 删除结果
     */
    Response deleteSample(Long sampleId);
    
    /**
     * 批量删除样本
     * @param sampleIds 样本ID列表
     * @return 删除结果
     */
    Response batchDeleteSamples(java.util.List<Long> sampleIds);
    
    /**
     * 批量导入样本
     * @param cmd 导入命令
     * @return 导入结果
     */
    SingleResponse<IntentSampleImportResultDTO> importSamples(IntentSampleImportCmd cmd);
    
    /**
     * 导出样本
     * @param cmd 导出命令
     * @return 导出结果
     */
    SingleResponse<IntentSampleExportResultDTO> exportSamples(IntentSampleExportCmd cmd);
    
    // ============ 快照管理 ============
    
    /**
     * 创建快照
     * @param cmd 创建命令
     * @return 创建的快照详情
     */
    SingleResponse<IntentSnapshotDTO> createSnapshot(IntentSnapshotCreateCmd cmd);
    
    /**
     * 发布快照
     * @param cmd 发布命令
     * @return 发布结果
     */
    Response publishSnapshot(IntentSnapshotPublishCmd cmd);
    
    /**
     * 获取快照列表
     * @param qry 查询命令
     * @return 快照列表
     */
    PageResponse<IntentSnapshotDTO> listSnapshots(IntentSnapshotListQry qry);
    
    /**
     * 获取快照详情
     * @param qry 查询命令
     * @return 快照详情
     */
    SingleResponse<IntentSnapshotDTO> getSnapshot(IntentSnapshotGetQry qry);
    
    /**
     * 删除快照
     * @param cmd 删除命令
     * @return 删除结果
     */
    Response deleteSnapshot(IntentSnapshotDeleteCmd cmd);
    
    /**
     * 比较快照
     * @param cmd 比较命令
     * @return 比较结果
     */
    SingleResponse<IntentSnapshotCompareResultDTO> compareSnapshots(IntentSnapshotCompareCmd cmd);
    
    /**
     * 回滚快照
     * @param cmd 回滚命令
     * @return 回滚结果
     */
    Response rollbackSnapshot(IntentSnapshotRollbackCmd cmd);
    
    // ============ 槽位模板管理 ============
    
    /**
     * 获取意图槽位模板
     * @param intentId 意图ID
     * @return 槽位模板详情，如果不存在或未启用则返回空模板
     */
    SingleResponse<SlotTemplateDTO> getSlotTemplate(Long intentId);
    
    /**
     * 更新意图槽位模板
     * @param intentId 意图ID
     * @param slotTemplate 槽位模板数据
     * @return 更新结果
     */
    Response updateSlotTemplate(Long intentId, SlotTemplateDTO slotTemplate);
    
    /**
     * 测试槽位填充
     * @param intentId 意图ID
     * @param cmd 测试命令
     * @return 测试结果
     */
    SingleResponse<SlotFillingTestResultDTO> testSlotFilling(Long intentId, SlotFillingTestCmd cmd);
}