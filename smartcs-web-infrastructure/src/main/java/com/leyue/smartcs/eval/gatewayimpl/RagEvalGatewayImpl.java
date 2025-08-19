package com.leyue.smartcs.eval.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.eval.*;
import com.leyue.smartcs.domain.eval.enums.EvaluationRunStatus;
import com.leyue.smartcs.domain.eval.enums.MetricCategory;
import com.leyue.smartcs.domain.eval.enums.RunType;
import com.leyue.smartcs.domain.eval.gateway.RagEvalGateway;
import com.leyue.smartcs.eval.convertor.*;
import com.leyue.smartcs.eval.dataobject.*;
import com.leyue.smartcs.eval.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RAG评估网关实现
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagEvalGatewayImpl implements RagEvalGateway {
    
    private final RagEvalDatasetMapper datasetMapper;
    private final RagEvalCaseMapper caseMapper;
    private final RagEvalRunMapper runMapper;
    private final RagEvalMetricMapper metricMapper;
    private final RagEvalRetrievalDetailMapper retrievalDetailMapper;
    private final RagEvalGenerationDetailMapper generationDetailMapper;
    
    private final RagEvalDatasetConvertor datasetConvertor;
    private final RagEvalCaseConvertor caseConvertor;
    private final RagEvalRunConvertor runConvertor;
    
    // ====== 数据集相关 ======
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalDataset createDataset(RagEvalDataset dataset) {
        log.info("创建RAG评估数据集: {}", dataset.getName());
        
        RagEvalDatasetDO datasetDO = datasetConvertor.toDataObject(dataset);
        long currentTime = Instant.now().toEpochMilli();
        datasetDO.setCreatedAt(currentTime);
        datasetDO.setUpdatedAt(currentTime);
        
        datasetMapper.insert(datasetDO);
        
        return datasetConvertor.toDomainObject(datasetDO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalDataset updateDataset(RagEvalDataset dataset) {
        log.info("更新RAG评估数据集: {}", dataset.getDatasetId());
        
        RagEvalDatasetDO datasetDO = datasetConvertor.toDataObject(dataset);
        datasetDO.setUpdatedAt(Instant.now().toEpochMilli());
        
        datasetMapper.updateById(datasetDO);
        
        return datasetConvertor.toDomainObject(datasetDO);
    }
    
    @Override
    public Optional<RagEvalDataset> findDatasetById(String datasetId) {
        LambdaQueryWrapper<RagEvalDatasetDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalDatasetDO::getDatasetId, datasetId)
                   .eq(RagEvalDatasetDO::getIsDeleted, 0);
        
        RagEvalDatasetDO datasetDO = datasetMapper.selectOne(queryWrapper);
        return Optional.ofNullable(datasetDO)
                      .map(datasetConvertor::toDomainObject);
    }
    
    @Override
    public List<RagEvalDataset> findDatasetsByCreatorId(Long creatorId) {
        List<RagEvalDatasetDO> datasetDOs = datasetMapper.selectByCreatorId(creatorId);
        return datasetDOs.stream()
                        .map(datasetConvertor::toDomainObject)
                        .collect(Collectors.toList());
    }
    
    @Override
    public List<RagEvalDataset> findDatasets(DatasetQueryCondition condition) {
        LambdaQueryWrapper<RagEvalDatasetDO> queryWrapper = buildDatasetQueryWrapper(condition);
        
        if (condition.getPageNum() != null && condition.getPageSize() != null) {
            IPage<RagEvalDatasetDO> page = new Page<>(condition.getPageNum(), condition.getPageSize());
            IPage<RagEvalDatasetDO> resultPage = datasetMapper.selectPage(page, queryWrapper);
            return resultPage.getRecords().stream()
                           .map(datasetConvertor::toDomainObject)
                           .collect(Collectors.toList());
        } else {
            List<RagEvalDatasetDO> datasetDOs = datasetMapper.selectList(queryWrapper);
            return datasetDOs.stream()
                           .map(datasetConvertor::toDomainObject)
                           .collect(Collectors.toList());
        }
    }
    
    @Override
    public long countDatasets(DatasetQueryCondition condition) {
        LambdaQueryWrapper<RagEvalDatasetDO> queryWrapper = buildDatasetQueryWrapper(condition);
        return datasetMapper.selectCount(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataset(String datasetId) {
        log.info("删除RAG评估数据集: {}", datasetId);
        
        LambdaQueryWrapper<RagEvalDatasetDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalDatasetDO::getDatasetId, datasetId);
        
        RagEvalDatasetDO updateDO = new RagEvalDatasetDO();
        updateDO.setIsDeleted(1);
        updateDO.setUpdatedAt(Instant.now().toEpochMilli());
        
        datasetMapper.update(updateDO, queryWrapper);
    }
    
    // ====== 测试用例相关 ======
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalCase createCase(RagEvalCase evalCase) {
        log.info("创建RAG评估测试用例: {}", evalCase.getCaseId());
        
        RagEvalCaseDO caseDO = caseConvertor.toDataObject(evalCase);
        long currentTime = Instant.now().toEpochMilli();
        caseDO.setCreatedAt(currentTime);
        caseDO.setUpdatedAt(currentTime);
        
        caseMapper.insert(caseDO);
        
        return caseConvertor.toDomainObject(caseDO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RagEvalCase> createCases(List<RagEvalCase> evalCases) {
        log.info("批量创建RAG评估测试用例，数量: {}", evalCases.size());
        
        long currentTime = Instant.now().toEpochMilli();
        List<RagEvalCaseDO> caseDOs = evalCases.stream()
                .map(evalCase -> {
                    RagEvalCaseDO caseDO = caseConvertor.toDataObject(evalCase);
                    caseDO.setCreatedAt(currentTime);
                    caseDO.setUpdatedAt(currentTime);
                    return caseDO;
                })
                .collect(Collectors.toList());
        
        // 批量插入
        caseDOs.forEach(caseMapper::insert);
        
        return caseDOs.stream()
                     .map(caseConvertor::toDomainObject)
                     .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalCase updateCase(RagEvalCase evalCase) {
        log.info("更新RAG评估测试用例: {}", evalCase.getCaseId());
        
        RagEvalCaseDO caseDO = caseConvertor.toDataObject(evalCase);
        caseDO.setUpdatedAt(Instant.now().toEpochMilli());
        
        caseMapper.updateById(caseDO);
        
        return caseConvertor.toDomainObject(caseDO);
    }
    
    @Override
    public Optional<RagEvalCase> findCaseById(String caseId) {
        LambdaQueryWrapper<RagEvalCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalCaseDO::getCaseId, caseId)
                   .eq(RagEvalCaseDO::getIsDeleted, 0);
        
        RagEvalCaseDO caseDO = caseMapper.selectOne(queryWrapper);
        return Optional.ofNullable(caseDO)
                      .map(caseConvertor::toDomainObject);
    }
    
    @Override
    public List<RagEvalCase> findCasesByDatasetId(String datasetId) {
        LambdaQueryWrapper<RagEvalCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalCaseDO::getDatasetId, datasetId)
                   .eq(RagEvalCaseDO::getIsDeleted, 0)
                   .orderByAsc(RagEvalCaseDO::getCreatedAt);
        
        List<RagEvalCaseDO> caseDOs = caseMapper.selectList(queryWrapper);
        return caseDOs.stream()
                     .map(caseConvertor::toDomainObject)
                     .collect(Collectors.toList());
    }
    
    @Override
    public List<RagEvalCase> findCases(CaseQueryCondition condition) {
        LambdaQueryWrapper<RagEvalCaseDO> queryWrapper = buildCaseQueryWrapper(condition);
        
        if (condition.getPageNum() != null && condition.getPageSize() != null) {
            IPage<RagEvalCaseDO> page = new Page<>(condition.getPageNum(), condition.getPageSize());
            IPage<RagEvalCaseDO> resultPage = caseMapper.selectPage(page, queryWrapper);
            return resultPage.getRecords().stream()
                           .map(caseConvertor::toDomainObject)
                           .collect(Collectors.toList());
        } else {
            List<RagEvalCaseDO> caseDOs = caseMapper.selectList(queryWrapper);
            return caseDOs.stream()
                        .map(caseConvertor::toDomainObject)
                        .collect(Collectors.toList());
        }
    }
    
    @Override
    public long countCases(CaseQueryCondition condition) {
        LambdaQueryWrapper<RagEvalCaseDO> queryWrapper = buildCaseQueryWrapper(condition);
        return caseMapper.selectCount(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCase(String caseId) {
        log.info("删除RAG评估测试用例: {}", caseId);
        
        LambdaQueryWrapper<RagEvalCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalCaseDO::getCaseId, caseId);
        
        RagEvalCaseDO updateDO = new RagEvalCaseDO();
        updateDO.setIsDeleted(1);
        updateDO.setUpdatedAt(Instant.now().toEpochMilli());
        
        caseMapper.update(updateDO, queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCases(List<String> caseIds) {
        log.info("批量删除RAG评估测试用例，数量: {}", caseIds.size());
        
        if (CollectionUtils.isEmpty(caseIds)) {
            return;
        }
        
        LambdaQueryWrapper<RagEvalCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RagEvalCaseDO::getCaseId, caseIds);
        
        RagEvalCaseDO updateDO = new RagEvalCaseDO();
        updateDO.setIsDeleted(1);
        updateDO.setUpdatedAt(Instant.now().toEpochMilli());
        
        caseMapper.update(updateDO, queryWrapper);
    }
    
    // ====== 评估运行相关 ======
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalRun createRun(RagEvalRun evalRun) {
        log.info("创建RAG评估运行: {}", evalRun.getRunId());
        
        RagEvalRunDO runDO = runConvertor.toDataObject(evalRun);
        long currentTime = Instant.now().toEpochMilli();
        runDO.setCreatedAt(currentTime);
        runDO.setUpdatedAt(currentTime);
        
        runMapper.insert(runDO);
        
        return runConvertor.toDomainObject(runDO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalRun updateRun(RagEvalRun evalRun) {
        log.info("更新RAG评估运行: {}", evalRun.getRunId());
        
        RagEvalRunDO runDO = runConvertor.toDataObject(evalRun);
        runDO.setUpdatedAt(Instant.now().toEpochMilli());
        
        runMapper.updateById(runDO);
        
        return runConvertor.toDomainObject(runDO);
    }
    
    @Override
    public Optional<RagEvalRun> findRunById(String runId) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRunDO::getRunId, runId)
                   .eq(RagEvalRunDO::getIsDeleted, 0);
        
        RagEvalRunDO runDO = runMapper.selectOne(queryWrapper);
        return Optional.ofNullable(runDO)
                      .map(runConvertor::toDomainObject);
    }
    
    @Override
    public List<RagEvalRun> findRuns(RunQueryCondition condition) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = buildRunQueryWrapper(condition);
        
        if (condition.getPageNum() != null && condition.getPageSize() != null) {
            IPage<RagEvalRunDO> page = new Page<>(condition.getPageNum(), condition.getPageSize());
            IPage<RagEvalRunDO> resultPage = runMapper.selectPage(page, queryWrapper);
            return resultPage.getRecords().stream()
                           .map(runConvertor::toDomainObject)
                           .collect(Collectors.toList());
        } else {
            List<RagEvalRunDO> runDOs = runMapper.selectList(queryWrapper);
            return runDOs.stream()
                        .map(runConvertor::toDomainObject)
                        .collect(Collectors.toList());
        }
    }
    
    @Override
    public long countRuns(RunQueryCondition condition) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = buildRunQueryWrapper(condition);
        return runMapper.selectCount(queryWrapper);
    }
    
    @Override
    public List<RagEvalRun> findRunsByStatus(EvaluationRunStatus status) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRunDO::getStatus, status.name())
                   .eq(RagEvalRunDO::getIsDeleted, 0)
                   .orderByDesc(RagEvalRunDO::getCreatedAt);
        
        List<RagEvalRunDO> runDOs = runMapper.selectList(queryWrapper);
        return runDOs.stream()
                    .map(runConvertor::toDomainObject)
                    .collect(Collectors.toList());
    }
    
    @Override
    public List<RagEvalRun> findRunningRuns() {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RagEvalRunDO::getStatus, 
                       EvaluationRunStatus.PENDING.name(), 
                       EvaluationRunStatus.RUNNING.name())
                   .eq(RagEvalRunDO::getIsDeleted, 0)
                   .orderByDesc(RagEvalRunDO::getCreatedAt);
        
        List<RagEvalRunDO> runDOs = runMapper.selectList(queryWrapper);
        return runDOs.stream()
                    .map(runConvertor::toDomainObject)
                    .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRun(String runId) {
        log.info("删除RAG评估运行: {}", runId);
        
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRunDO::getRunId, runId);
        
        RagEvalRunDO updateDO = new RagEvalRunDO();
        updateDO.setIsDeleted(1);
        updateDO.setUpdatedAt(Instant.now().toEpochMilli());
        
        runMapper.update(updateDO, queryWrapper);
    }
    
    // ====== 指标相关 ======
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalMetric saveMetric(RagEvalMetric metric) {
        log.info("保存RAG评估指标: {} - {}", metric.getRunId(), metric.getMetricCategory());
        
        // 转换为DO对象 - 需要实现相应的转换器
        RagEvalMetricDO metricDO = convertToMetricDO(metric);
        long currentTime = Instant.now().toEpochMilli();
        metricDO.setCreatedAt(currentTime);
        metricDO.setUpdatedAt(currentTime);
        
        metricMapper.insert(metricDO);
        
        return convertToMetricDomain(metricDO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RagEvalMetric> saveMetrics(List<RagEvalMetric> metrics) {
        log.info("批量保存RAG评估指标，数量: {}", metrics.size());
        
        long currentTime = Instant.now().toEpochMilli();
        List<RagEvalMetricDO> metricDOs = metrics.stream()
                .map(metric -> {
                    RagEvalMetricDO metricDO = convertToMetricDO(metric);
                    metricDO.setCreatedAt(currentTime);
                    metricDO.setUpdatedAt(currentTime);
                    return metricDO;
                })
                .collect(Collectors.toList());
        
        // 批量插入
        metricDOs.forEach(metricMapper::insert);
        
        return metricDOs.stream()
                       .map(this::convertToMetricDomain)
                       .collect(Collectors.toList());
    }
    
    @Override
    public Optional<RagEvalMetric> findMetricByRunIdAndCategory(String runId, MetricCategory category) {
        LambdaQueryWrapper<RagEvalMetricDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalMetricDO::getRunId, runId)
                   .eq(RagEvalMetricDO::getMetricCategory, category.name());
        
        RagEvalMetricDO metricDO = metricMapper.selectOne(queryWrapper);
        return Optional.ofNullable(metricDO)
                      .map(this::convertToMetricDomain);
    }
    
    @Override
    public List<RagEvalMetric> findMetricsByRunId(String runId) {
        LambdaQueryWrapper<RagEvalMetricDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalMetricDO::getRunId, runId)
                   .orderByAsc(RagEvalMetricDO::getMetricCategory);
        
        List<RagEvalMetricDO> metricDOs = metricMapper.selectList(queryWrapper);
        return metricDOs.stream()
                       .map(this::convertToMetricDomain)
                       .collect(Collectors.toList());
    }
    
    @Override
    public List<RagEvalMetric> findMetrics(MetricQueryCondition condition) {
        LambdaQueryWrapper<RagEvalMetricDO> queryWrapper = buildMetricQueryWrapper(condition);
        
        if (condition.getPageNum() != null && condition.getPageSize() != null) {
            IPage<RagEvalMetricDO> page = new Page<>(condition.getPageNum(), condition.getPageSize());
            IPage<RagEvalMetricDO> resultPage = metricMapper.selectPage(page, queryWrapper);
            return resultPage.getRecords().stream()
                           .map(this::convertToMetricDomain)
                           .collect(Collectors.toList());
        } else {
            List<RagEvalMetricDO> metricDOs = metricMapper.selectList(queryWrapper);
            return metricDOs.stream()
                          .map(this::convertToMetricDomain)
                          .collect(Collectors.toList());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMetric(String runId, MetricCategory category) {
        log.info("删除RAG评估指标: {} - {}", runId, category);
        
        LambdaQueryWrapper<RagEvalMetricDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalMetricDO::getRunId, runId)
                   .eq(RagEvalMetricDO::getMetricCategory, category.name());
        
        metricMapper.delete(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMetricsByRunId(String runId) {
        log.info("删除运行的所有RAG评估指标: {}", runId);
        
        LambdaQueryWrapper<RagEvalMetricDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalMetricDO::getRunId, runId);
        
        metricMapper.delete(queryWrapper);
    }
    
    // ====== 详细结果相关 ======
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalRetrievalDetail saveRetrievalDetail(RagEvalRetrievalDetail detail) {
        log.info("保存RAG检索评估详情: {} - {}", detail.getRunId(), detail.getCaseId());
        
        // 转换为DO对象 - 需要实现相应的转换器
        RagEvalRetrievalDetailDO detailDO = convertToRetrievalDetailDO(detail);
        long currentTime = Instant.now().toEpochMilli();
        detailDO.setCreatedAt(currentTime);
        detailDO.setUpdatedAt(currentTime);
        
        retrievalDetailMapper.insert(detailDO);
        
        return convertToRetrievalDetailDomain(detailDO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RagEvalRetrievalDetail> saveRetrievalDetails(List<RagEvalRetrievalDetail> details) {
        log.info("批量保存RAG检索评估详情，数量: {}", details.size());
        
        long currentTime = Instant.now().toEpochMilli();
        List<RagEvalRetrievalDetailDO> detailDOs = details.stream()
                .map(detail -> {
                    RagEvalRetrievalDetailDO detailDO = convertToRetrievalDetailDO(detail);
                    detailDO.setCreatedAt(currentTime);
                    detailDO.setUpdatedAt(currentTime);
                    return detailDO;
                })
                .collect(Collectors.toList());
        
        // 批量插入
        detailDOs.forEach(retrievalDetailMapper::insert);
        
        return detailDOs.stream()
                       .map(this::convertToRetrievalDetailDomain)
                       .collect(Collectors.toList());
    }
    
    @Override
    public Optional<RagEvalRetrievalDetail> findRetrievalDetail(String runId, String caseId) {
        LambdaQueryWrapper<RagEvalRetrievalDetailDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRetrievalDetailDO::getRunId, runId)
                   .eq(RagEvalRetrievalDetailDO::getCaseId, caseId);
        
        RagEvalRetrievalDetailDO detailDO = retrievalDetailMapper.selectOne(queryWrapper);
        return Optional.ofNullable(detailDO)
                      .map(this::convertToRetrievalDetailDomain);
    }
    
    @Override
    public List<RagEvalRetrievalDetail> findRetrievalDetailsByRunId(String runId) {
        LambdaQueryWrapper<RagEvalRetrievalDetailDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRetrievalDetailDO::getRunId, runId)
                   .orderByAsc(RagEvalRetrievalDetailDO::getCreatedAt);
        
        List<RagEvalRetrievalDetailDO> detailDOs = retrievalDetailMapper.selectList(queryWrapper);
        return detailDOs.stream()
                       .map(this::convertToRetrievalDetailDomain)
                       .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalGenerationDetail saveGenerationDetail(RagEvalGenerationDetail detail) {
        log.info("保存RAG生成评估详情: {} - {}", detail.getRunId(), detail.getCaseId());
        
        // 转换为DO对象 - 需要实现相应的转换器
        RagEvalGenerationDetailDO detailDO = convertToGenerationDetailDO(detail);
        long currentTime = Instant.now().toEpochMilli();
        detailDO.setCreatedAt(currentTime);
        
        generationDetailMapper.insert(detailDO);
        
        return convertToGenerationDetailDomain(detailDO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RagEvalGenerationDetail> saveGenerationDetails(List<RagEvalGenerationDetail> details) {
        log.info("批量保存RAG生成评估详情，数量: {}", details.size());
        
        long currentTime = Instant.now().toEpochMilli();
        List<RagEvalGenerationDetailDO> detailDOs = details.stream()
                .map(detail -> {
                    RagEvalGenerationDetailDO detailDO = convertToGenerationDetailDO(detail);
                    detailDO.setCreatedAt(currentTime);
                    return detailDO;
                })
                .collect(Collectors.toList());
        
        // 批量插入
        detailDOs.forEach(generationDetailMapper::insert);
        
        return detailDOs.stream()
                       .map(this::convertToGenerationDetailDomain)
                       .collect(Collectors.toList());
    }
    
    @Override
    public Optional<RagEvalGenerationDetail> findGenerationDetail(String runId, String caseId) {
        LambdaQueryWrapper<RagEvalGenerationDetailDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalGenerationDetailDO::getRunId, runId)
                   .eq(RagEvalGenerationDetailDO::getCaseId, caseId);
        
        RagEvalGenerationDetailDO detailDO = generationDetailMapper.selectOne(queryWrapper);
        return Optional.ofNullable(detailDO)
                      .map(this::convertToGenerationDetailDomain);
    }
    
    @Override
    public List<RagEvalGenerationDetail> findGenerationDetailsByRunId(String runId) {
        LambdaQueryWrapper<RagEvalGenerationDetailDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalGenerationDetailDO::getRunId, runId)
                   .orderByAsc(RagEvalGenerationDetailDO::getCreatedAt);
        
        List<RagEvalGenerationDetailDO> detailDOs = generationDetailMapper.selectList(queryWrapper);
        return detailDOs.stream()
                       .map(this::convertToGenerationDetailDomain)
                       .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDetailsByRunId(String runId) {
        log.info("删除运行的所有详情数据: {}", runId);
        
        // 删除检索详情
        LambdaQueryWrapper<RagEvalRetrievalDetailDO> retrievalQueryWrapper = new LambdaQueryWrapper<>();
        retrievalQueryWrapper.eq(RagEvalRetrievalDetailDO::getRunId, runId);
        retrievalDetailMapper.delete(retrievalQueryWrapper);
        
        // 删除生成详情
        LambdaQueryWrapper<RagEvalGenerationDetailDO> generationQueryWrapper = new LambdaQueryWrapper<>();
        generationQueryWrapper.eq(RagEvalGenerationDetailDO::getRunId, runId);
        generationDetailMapper.delete(generationQueryWrapper);
    }
    
    // ====== 统计分析相关 ======
    
    @Override
    public long countRunsByDatasetId(String datasetId) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRunDO::getDatasetId, datasetId)
                   .eq(RagEvalRunDO::getIsDeleted, 0);
        
        return runMapper.selectCount(queryWrapper);
    }
    
    @Override
    public List<RagEvalRun> findRecentRunsByDatasetId(String datasetId, int limit) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRunDO::getDatasetId, datasetId)
                   .eq(RagEvalRunDO::getIsDeleted, 0)
                   .orderByDesc(RagEvalRunDO::getCreatedAt)
                   .last("LIMIT " + limit);
        
        List<RagEvalRunDO> runDOs = runMapper.selectList(queryWrapper);
        return runDOs.stream()
                    .map(runConvertor::toDomainObject)
                    .collect(Collectors.toList());
    }
    
    @Override
    public long countRunsByModelId(Long modelId) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRunDO::getModelId, modelId)
                   .eq(RagEvalRunDO::getIsDeleted, 0);
        
        return runMapper.selectCount(queryWrapper);
    }
    
    @Override
    public List<RagEvalRun> findRecentRunsByAppId(Long appId, int limit) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRunDO::getAppId, appId)
                   .eq(RagEvalRunDO::getIsDeleted, 0)
                   .orderByDesc(RagEvalRunDO::getCreatedAt)
                   .last("LIMIT " + limit);
        
        List<RagEvalRunDO> runDOs = runMapper.selectList(queryWrapper);
        return runDOs.stream()
                    .map(runConvertor::toDomainObject)
                    .collect(Collectors.toList());
    }
    
    // ====== 私有辅助方法 ======
    
    private LambdaQueryWrapper<RagEvalDatasetDO> buildDatasetQueryWrapper(DatasetQueryCondition condition) {
        LambdaQueryWrapper<RagEvalDatasetDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalDatasetDO::getIsDeleted, 0);
        
        if (condition.getCreatorId() != null) {
            queryWrapper.eq(RagEvalDatasetDO::getCreatorId, condition.getCreatorId());
        }
        if (StringUtils.hasText(condition.getDomain())) {
            queryWrapper.eq(RagEvalDatasetDO::getDomain, condition.getDomain());
        }
        if (condition.getStatus() != null) {
            queryWrapper.eq(RagEvalDatasetDO::getStatus, condition.getStatus());
        }
        if (StringUtils.hasText(condition.getSearchKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(RagEvalDatasetDO::getName, condition.getSearchKeyword())
                    .or()
                    .like(RagEvalDatasetDO::getDescription, condition.getSearchKeyword())
            );
        }
        
        // 排序
        String sortField = StringUtils.hasText(condition.getSortField()) ? condition.getSortField() : "createdAt";
        String sortOrder = StringUtils.hasText(condition.getSortOrder()) ? condition.getSortOrder() : "desc";
        
        if ("asc".equalsIgnoreCase(sortOrder)) {
            queryWrapper.orderByAsc(RagEvalDatasetDO::getCreatedAt);
        } else {
            queryWrapper.orderByDesc(RagEvalDatasetDO::getCreatedAt);
        }
        
        return queryWrapper;
    }
    
    private LambdaQueryWrapper<RagEvalCaseDO> buildCaseQueryWrapper(CaseQueryCondition condition) {
        LambdaQueryWrapper<RagEvalCaseDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalCaseDO::getIsDeleted, 0);
        
        if (StringUtils.hasText(condition.getDatasetId())) {
            queryWrapper.eq(RagEvalCaseDO::getDatasetId, condition.getDatasetId());
        }
        if (StringUtils.hasText(condition.getCategory())) {
            queryWrapper.eq(RagEvalCaseDO::getCategory, condition.getCategory());
        }
        if (StringUtils.hasText(condition.getDifficultyTag())) {
            queryWrapper.eq(RagEvalCaseDO::getDifficultyTag, condition.getDifficultyTag());
        }
        if (StringUtils.hasText(condition.getQueryType())) {
            queryWrapper.eq(RagEvalCaseDO::getQueryType, condition.getQueryType());
        }
        if (condition.getStatus() != null) {
            queryWrapper.eq(RagEvalCaseDO::getStatus, condition.getStatus());
        }
        if (StringUtils.hasText(condition.getSearchKeyword())) {
            queryWrapper.like(RagEvalCaseDO::getQuestion, condition.getSearchKeyword());
        }
        
        // 排序
        String sortField = StringUtils.hasText(condition.getSortField()) ? condition.getSortField() : "createdAt";
        String sortOrder = StringUtils.hasText(condition.getSortOrder()) ? condition.getSortOrder() : "desc";
        
        if ("asc".equalsIgnoreCase(sortOrder)) {
            queryWrapper.orderByAsc(RagEvalCaseDO::getCreatedAt);
        } else {
            queryWrapper.orderByDesc(RagEvalCaseDO::getCreatedAt);
        }
        
        return queryWrapper;
    }
    
    private LambdaQueryWrapper<RagEvalRunDO> buildRunQueryWrapper(RunQueryCondition condition) {
        LambdaQueryWrapper<RagEvalRunDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RagEvalRunDO::getIsDeleted, 0);
        
        if (StringUtils.hasText(condition.getDatasetId())) {
            queryWrapper.eq(RagEvalRunDO::getDatasetId, condition.getDatasetId());
        }
        if (condition.getAppId() != null) {
            queryWrapper.eq(RagEvalRunDO::getAppId, condition.getAppId());
        }
        if (condition.getModelId() != null) {
            queryWrapper.eq(RagEvalRunDO::getModelId, condition.getModelId());
        }
        if (condition.getRunType() != null) {
            queryWrapper.eq(RagEvalRunDO::getRunType, condition.getRunType().name());
        }
        if (condition.getStatus() != null) {
            queryWrapper.eq(RagEvalRunDO::getStatus, condition.getStatus().name());
        }
        if (condition.getInitiatorId() != null) {
            queryWrapper.eq(RagEvalRunDO::getInitiatorId, condition.getInitiatorId());
        }
        if (StringUtils.hasText(condition.getSearchKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(RagEvalRunDO::getRunName, condition.getSearchKeyword())
                    .or()
                    .like(RagEvalRunDO::getRunDescription, condition.getSearchKeyword())
            );
        }
        if (condition.getStartTimeFrom() != null) {
            queryWrapper.ge(RagEvalRunDO::getStartTime, condition.getStartTimeFrom());
        }
        if (condition.getStartTimeTo() != null) {
            queryWrapper.le(RagEvalRunDO::getStartTime, condition.getStartTimeTo());
        }
        
        // 排序
        String sortField = StringUtils.hasText(condition.getSortField()) ? condition.getSortField() : "createdAt";
        String sortOrder = StringUtils.hasText(condition.getSortOrder()) ? condition.getSortOrder() : "desc";
        
        if ("asc".equalsIgnoreCase(sortOrder)) {
            queryWrapper.orderByAsc(RagEvalRunDO::getCreatedAt);
        } else {
            queryWrapper.orderByDesc(RagEvalRunDO::getCreatedAt);
        }
        
        return queryWrapper;
    }
    
    private LambdaQueryWrapper<RagEvalMetricDO> buildMetricQueryWrapper(MetricQueryCondition condition) {
        LambdaQueryWrapper<RagEvalMetricDO> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(condition.getRunId())) {
            queryWrapper.eq(RagEvalMetricDO::getRunId, condition.getRunId());
        }
        if (condition.getCategory() != null) {
            queryWrapper.eq(RagEvalMetricDO::getMetricCategory, condition.getCategory().name());
        }
        if (condition.getCreatedFrom() != null) {
            queryWrapper.ge(RagEvalMetricDO::getCreatedAt, condition.getCreatedFrom());
        }
        if (condition.getCreatedTo() != null) {
            queryWrapper.le(RagEvalMetricDO::getCreatedAt, condition.getCreatedTo());
        }
        
        // 排序
        String sortField = StringUtils.hasText(condition.getSortField()) ? condition.getSortField() : "createdAt";
        String sortOrder = StringUtils.hasText(condition.getSortOrder()) ? condition.getSortOrder() : "desc";
        
        if ("asc".equalsIgnoreCase(sortOrder)) {
            queryWrapper.orderByAsc(RagEvalMetricDO::getCreatedAt);
        } else {
            queryWrapper.orderByDesc(RagEvalMetricDO::getCreatedAt);
        }
        
        return queryWrapper;
    }
    
    // TODO: 实现这些转换方法，将在后续创建相应的转换器
    private RagEvalMetricDO convertToMetricDO(RagEvalMetric metric) {
        // 临时实现，后续需要创建完整的转换器
        return new RagEvalMetricDO();
    }
    
    private RagEvalMetric convertToMetricDomain(RagEvalMetricDO metricDO) {
        // 临时实现，后续需要创建完整的转换器
        return new RagEvalMetric();
    }
    
    private RagEvalRetrievalDetailDO convertToRetrievalDetailDO(RagEvalRetrievalDetail detail) {
        // 临时实现，后续需要创建完整的转换器
        return new RagEvalRetrievalDetailDO();
    }
    
    private RagEvalRetrievalDetail convertToRetrievalDetailDomain(RagEvalRetrievalDetailDO detailDO) {
        // 临时实现，后续需要创建完整的转换器
        return new RagEvalRetrievalDetail();
    }
    
    private RagEvalGenerationDetailDO convertToGenerationDetailDO(RagEvalGenerationDetail detail) {
        // 临时实现，后续需要创建完整的转换器
        return new RagEvalGenerationDetailDO();
    }
    
    private RagEvalGenerationDetail convertToGenerationDetailDomain(RagEvalGenerationDetailDO detailDO) {
        // 临时实现，后续需要创建完整的转换器
        return new RagEvalGenerationDetail();
    }
}