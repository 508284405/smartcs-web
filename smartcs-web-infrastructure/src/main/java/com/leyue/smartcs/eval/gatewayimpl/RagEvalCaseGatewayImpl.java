package com.leyue.smartcs.eval.gatewayimpl;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.domain.eval.gateway.RagEvalCaseGateway;
import com.leyue.smartcs.dto.eval.*;
import com.leyue.smartcs.eval.dataobject.RagEvalCaseDO;
import com.leyue.smartcs.eval.mapper.RagEvalCaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RAG评估测试用例Gateway实现
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalCaseGatewayImpl implements RagEvalCaseGateway {

    private final RagEvalCaseMapper caseMapper;
    private final IdGeneratorGateway idGeneratorGateway;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalCaseDTO createCase(RagEvalCaseCreateCmd cmd) {
        try {
            String caseId = idGeneratorGateway.generateIdStr();
            long now = Instant.now().toEpochMilli();

            RagEvalCaseDO caseDO = new RagEvalCaseDO();
            caseDO.setCaseId(caseId);
            caseDO.setDatasetId(cmd.getDatasetId());
            caseDO.setQuestion(cmd.getQuestion());
            caseDO.setExpectedSummary(cmd.getExpectedSummary());
            caseDO.setGroundTruthContexts(cmd.getGroundTruthContexts() != null ? 
                String.join("\n", cmd.getGroundTruthContexts()) : null);
            caseDO.setGoldEvidenceRefs(cmd.getGoldEvidenceRefs() != null ? 
                String.join("\n", cmd.getGoldEvidenceRefs()) : null);
            caseDO.setCategory(cmd.getCategory());
            caseDO.setDifficultyTag(cmd.getDifficultyTag());
            caseDO.setQueryType(cmd.getQueryType());
            caseDO.setMetadata(cmd.getExtraProperties());
            caseDO.setCreatedBy(cmd.getCreatorId());
            caseDO.setUpdatedBy(cmd.getCreatorId());
            caseDO.setStatus(1); // 默认启用状态
            caseDO.setCreatedAt(now);
            caseDO.setUpdatedAt(now);

            caseMapper.insert(caseDO);

            return toDTO(caseDO);
        } catch (Exception e) {
            log.error("创建测试用例失败", e);
            throw new BizException("CREATE_CASE_FAILED", "创建测试用例失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalCaseDTO updateCase(RagEvalCaseUpdateCmd cmd) {
        try {
            RagEvalCaseDO exist = caseMapper.selectOne(new LambdaQueryWrapper<RagEvalCaseDO>()
                    .eq(RagEvalCaseDO::getCaseId, cmd.getCaseId())
                    .eq(RagEvalCaseDO::getIsDeleted, 0));
            if (exist == null) {
                throw new BizException("CASE_NOT_FOUND", "测试用例不存在: " + cmd.getCaseId());
            }

            if (StringUtils.hasText(cmd.getQuestion())) {
                exist.setQuestion(cmd.getQuestion());
            }
            if (StringUtils.hasText(cmd.getExpectedSummary())) {
                exist.setExpectedSummary(cmd.getExpectedSummary());
            }
            if (cmd.getGroundTruthContexts() != null) {
                exist.setGroundTruthContexts(String.join("\n", cmd.getGroundTruthContexts()));
            }
            if (cmd.getGoldEvidenceRefs() != null) {
                exist.setGoldEvidenceRefs(String.join("\n", cmd.getGoldEvidenceRefs()));
            }
            if (StringUtils.hasText(cmd.getCategory())) {
                exist.setCategory(cmd.getCategory());
            }
            if (StringUtils.hasText(cmd.getDifficultyTag())) {
                exist.setDifficultyTag(cmd.getDifficultyTag());
            }
            if (StringUtils.hasText(cmd.getQueryType())) {
                exist.setQueryType(cmd.getQueryType());
            }
            if (cmd.getMetadata() != null) {
                exist.setMetadata(cmd.getMetadata());
            }
            if (cmd.getStatus() != null) {
                exist.setStatus(cmd.getStatus());
            }
            exist.setUpdatedAt(Instant.now().toEpochMilli());

            caseMapper.updateById(exist);
            return toDTO(exist);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新测试用例失败", e);
            throw new BizException("UPDATE_CASE_FAILED", "更新测试用例失败: " + e.getMessage());
        }
    }

    @Override
    public RagEvalCaseDTO getCase(String caseId) {
        try {
            RagEvalCaseDO caseDO = caseMapper.selectOne(new LambdaQueryWrapper<RagEvalCaseDO>()
                    .eq(RagEvalCaseDO::getCaseId, caseId)
                    .eq(RagEvalCaseDO::getIsDeleted, 0));
            return caseDO == null ? null : toDTO(caseDO);
        } catch (Exception e) {
            log.error("查询测试用例失败", e);
            throw new BizException("GET_CASE_FAILED", "查询测试用例失败: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<RagEvalCaseDTO> listCases(RagEvalCaseListQry qry) {
        try {
            LambdaQueryWrapper<RagEvalCaseDO> wrapper = buildQueryWrapper(qry);

            Page<RagEvalCaseDO> page = new Page<>(
                    Objects.requireNonNullElse(qry.getPageNum(), 1),
                    Objects.requireNonNullElse(qry.getPageSize(), 10)
            );
            IPage<RagEvalCaseDO> result = caseMapper.selectPage(page, wrapper);

            List<RagEvalCaseDTO> cases = result.getRecords().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());

            return PageResponse.of(cases, (int) result.getTotal(), (int) result.getSize(), (int) result.getCurrent());
        } catch (Exception e) {
            log.error("查询测试用例列表失败", e);
            throw new BizException("LIST_CASES_FAILED", "查询测试用例列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCase(String caseId) {
        try {
            RagEvalCaseDO updateDO = new RagEvalCaseDO();
            updateDO.setIsDeleted(1);
            updateDO.setUpdatedAt(Instant.now().toEpochMilli());

            caseMapper.update(updateDO, new LambdaQueryWrapper<RagEvalCaseDO>()
                    .eq(RagEvalCaseDO::getCaseId, caseId));
        } catch (Exception e) {
            log.error("删除测试用例失败", e);
            throw new BizException("DELETE_CASE_FAILED", "删除测试用例失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCases(List<String> caseIds) {
        try {
            if (CollectionUtils.isEmpty(caseIds)) {
                return;
            }

            RagEvalCaseDO updateDO = new RagEvalCaseDO();
            updateDO.setIsDeleted(1);
            updateDO.setUpdatedAt(Instant.now().toEpochMilli());

            caseMapper.update(updateDO, new LambdaQueryWrapper<RagEvalCaseDO>()
                    .in(RagEvalCaseDO::getCaseId, caseIds));
        } catch (Exception e) {
            log.error("批量删除测试用例失败", e);
            throw new BizException("BATCH_DELETE_CASES_FAILED", "批量删除测试用例失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalCaseBatchImportResultDTO batchImportCases(RagEvalCaseBatchImportCmd cmd) {
        try {
            long now = Instant.now().toEpochMilli();
            int successCount = 0;
            int failCount = 0;
            StringBuilder errors = new StringBuilder();

            for (RagEvalCaseCreateCmd caseCmd : cmd.getCases()) {
                try {
                    String caseId = idGeneratorGateway.generateIdStr();
                    
                    RagEvalCaseDO caseDO = new RagEvalCaseDO();
                    caseDO.setCaseId(caseId);
                    caseDO.setDatasetId(cmd.getDatasetId());
                    caseDO.setQuestion(caseCmd.getQuestion());
                    caseDO.setExpectedSummary(caseCmd.getExpectedSummary());
                    caseDO.setGroundTruthContexts(caseCmd.getGroundTruthContexts() != null ? 
                        String.join("\n", caseCmd.getGroundTruthContexts()) : null);
                    caseDO.setGoldEvidenceRefs(caseCmd.getGoldEvidenceRefs() != null ? 
                        String.join("\n", caseCmd.getGoldEvidenceRefs()) : null);
                    caseDO.setCategory(caseCmd.getCategory());
                    caseDO.setDifficultyTag(caseCmd.getDifficultyTag());
                    caseDO.setQueryType(caseCmd.getQueryType());
                    caseDO.setMetadata(caseCmd.getExtraProperties());
                    caseDO.setCreatedBy(caseCmd.getCreatorId());
                    caseDO.setUpdatedBy(caseCmd.getCreatorId());
                    caseDO.setStatus(1);
                    caseDO.setCreatedAt(now);
                    caseDO.setUpdatedAt(now);

                    caseMapper.insert(caseDO);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.append("问题: ").append(caseCmd.getQuestion())
                           .append(" 错误: ").append(e.getMessage()).append("; ");
                }
            }

            RagEvalCaseBatchImportResultDTO result = new RagEvalCaseBatchImportResultDTO();
            result.setTotalCount(cmd.getCases().size());
            result.setSuccessCount(successCount);
            result.setFailedCount(failCount);
            result.setErrorMessage(errors.toString());

            return result;
        } catch (Exception e) {
            log.error("批量导入测试用例失败", e);
            throw new BizException("BATCH_IMPORT_CASES_FAILED", "批量导入测试用例失败: " + e.getMessage());
        }
    }

    private LambdaQueryWrapper<RagEvalCaseDO> buildQueryWrapper(RagEvalCaseListQry qry) {
        LambdaQueryWrapper<RagEvalCaseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RagEvalCaseDO::getIsDeleted, 0);

        if (StringUtils.hasText(qry.getDatasetId())) {
            wrapper.eq(RagEvalCaseDO::getDatasetId, qry.getDatasetId());
        }
        if (StringUtils.hasText(qry.getCategory())) {
            wrapper.eq(RagEvalCaseDO::getCategory, qry.getCategory());
        }
        if (StringUtils.hasText(qry.getDifficultyTag())) {
            wrapper.eq(RagEvalCaseDO::getDifficultyTag, qry.getDifficultyTag());
        }
        if (StringUtils.hasText(qry.getQueryType())) {
            wrapper.eq(RagEvalCaseDO::getQueryType, qry.getQueryType());
        }
        if (qry.getStatus() != null) {
            wrapper.eq(RagEvalCaseDO::getStatus, qry.getStatus());
        }
        if (StringUtils.hasText(qry.getSearchKeyword())) {
            wrapper.and(w -> w.like(RagEvalCaseDO::getQuestion, qry.getSearchKeyword())
                    .or().like(RagEvalCaseDO::getExpectedSummary, qry.getSearchKeyword()));
        }

        // 默认按创建时间倒序
        String sortOrder = StringUtils.hasText(qry.getSortOrder()) ? qry.getSortOrder() : "desc";
        if ("asc".equalsIgnoreCase(sortOrder)) {
            wrapper.orderByAsc(RagEvalCaseDO::getCreatedAt);
        } else {
            wrapper.orderByDesc(RagEvalCaseDO::getCreatedAt);
        }
        
        return wrapper;
    }

    private RagEvalCaseDTO toDTO(RagEvalCaseDO doObj) {
        RagEvalCaseDTO dto = new RagEvalCaseDTO();
        dto.setCaseId(doObj.getCaseId());
        dto.setDatasetId(doObj.getDatasetId());
        dto.setQuestion(doObj.getQuestion());
        dto.setExpectedSummary(doObj.getExpectedSummary());
        dto.setGroundTruthContexts(doObj.getGroundTruthContexts() != null ? 
            List.of(doObj.getGroundTruthContexts().split("\n")) : null);
        dto.setGoldEvidenceRefs(doObj.getGoldEvidenceRefs() != null ? 
            List.of(doObj.getGoldEvidenceRefs().split("\n")) : null);
        dto.setCategory(doObj.getCategory());
        dto.setDifficultyTag(doObj.getDifficultyTag());
        dto.setQueryType(doObj.getQueryType());
        dto.setExtraProperties(doObj.getMetadata());
        dto.setCreatorId(doObj.getCreatedBy());
        dto.setStatus(doObj.getStatus());
        dto.setCreateTime(toLocalDateTime(doObj.getCreatedAt()));
        dto.setUpdateTime(toLocalDateTime(doObj.getUpdatedAt()));
        return dto;
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}