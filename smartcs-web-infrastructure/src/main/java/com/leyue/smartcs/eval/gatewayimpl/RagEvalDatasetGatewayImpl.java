package com.leyue.smartcs.eval.gatewayimpl;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.domain.eval.gateway.RagEvalDatasetGateway;
import com.leyue.smartcs.dto.eval.RagEvalDatasetCreateCmd;
import com.leyue.smartcs.dto.eval.RagEvalDatasetDTO;
import com.leyue.smartcs.dto.eval.RagEvalDatasetListQry;
import com.leyue.smartcs.dto.eval.RagEvalDatasetUpdateCmd;
import com.leyue.smartcs.eval.dataobject.RagEvalDatasetDO;
import com.leyue.smartcs.eval.mapper.RagEvalDatasetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RagEvalDatasetGateway 实现，将 DTO 命令与基础设施层数据访问对接。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalDatasetGatewayImpl implements RagEvalDatasetGateway {

    private final RagEvalDatasetMapper datasetMapper;
    private final IdGeneratorGateway idGeneratorGateway;

    @Override
    public RagEvalDatasetDTO createDataset(RagEvalDatasetCreateCmd cmd) {
        try {
            String datasetId = idGeneratorGateway.generateIdStr();
            long now = Instant.now().toEpochMilli();

            RagEvalDatasetDO datasetDO = new RagEvalDatasetDO();
            datasetDO.setDatasetId(datasetId);
            datasetDO.setName(cmd.getName());
            datasetDO.setDescription(cmd.getDescription());
            datasetDO.setDomain(cmd.getDomain());
            datasetDO.setTags(cmd.getTags());
            datasetDO.setCreatorId(cmd.getCreatorId());
            datasetDO.setStatus(1);
            datasetDO.setCreatedAt(now);
            datasetDO.setUpdatedAt(now);

            datasetMapper.insert(datasetDO);

            return toDTO(datasetDO);
        } catch (Exception e) {
            log.error("创建数据集失败", e);
            throw new BizException("CREATE_DATASET_FAILED", "创建数据集失败: " + e.getMessage());
        }
    }

    @Override
    public RagEvalDatasetDTO updateDataset(RagEvalDatasetUpdateCmd cmd) {
        try {
            // 先根据 datasetId 查询记录
            RagEvalDatasetDO exist = datasetMapper.selectOne(new LambdaQueryWrapper<RagEvalDatasetDO>()
                    .eq(RagEvalDatasetDO::getDatasetId, cmd.getDatasetId())
                    .eq(RagEvalDatasetDO::getIsDeleted, 0));
            if (exist == null) {
                throw new BizException("DATASET_NOT_FOUND", "数据集不存在: " + cmd.getDatasetId());
            }

            if (StringUtils.hasText(cmd.getName())) {
                exist.setName(cmd.getName());
            }
            if (StringUtils.hasText(cmd.getDescription())) {
                exist.setDescription(cmd.getDescription());
            }
            if (StringUtils.hasText(cmd.getDomain())) {
                exist.setDomain(cmd.getDomain());
            }
            if (!CollectionUtils.isEmpty(cmd.getTags())) {
                exist.setTags(cmd.getTags());
            }
            if (cmd.getStatus() != null) {
                exist.setStatus(cmd.getStatus());
            }
            exist.setUpdatedAt(Instant.now().toEpochMilli());

            datasetMapper.updateById(exist);
            return toDTO(exist);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新数据集失败", e);
            throw new BizException("UPDATE_DATASET_FAILED", "更新数据集失败: " + e.getMessage());
        }
    }

    @Override
    public RagEvalDatasetDTO getDataset(String datasetId) {
        try {
            RagEvalDatasetDO datasetDO = datasetMapper.selectOne(new LambdaQueryWrapper<RagEvalDatasetDO>()
                    .eq(RagEvalDatasetDO::getDatasetId, datasetId)
                    .eq(RagEvalDatasetDO::getIsDeleted, 0));
            return datasetDO == null ? null : toDTO(datasetDO);
        } catch (Exception e) {
            log.error("查询数据集失败", e);
            throw new BizException("GET_DATASET_FAILED", "查询数据集失败: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<RagEvalDatasetDTO> listDatasets(RagEvalDatasetListQry qry) {
        try {
            LambdaQueryWrapper<RagEvalDatasetDO> wrapper = buildQueryWrapper(qry);

            Page<RagEvalDatasetDO> page = new Page<>(
                    Objects.requireNonNullElse(qry.getPageNum(), 1),
                    Objects.requireNonNullElse(qry.getPageSize(), 10)
            );
            IPage<RagEvalDatasetDO> result = datasetMapper.selectPage(page, wrapper);

            List<RagEvalDatasetDTO> datasets = result.getRecords().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());

            return PageResponse.of(datasets, (int) result.getTotal(), (int) result.getSize(), (int) result.getCurrent());
        } catch (Exception e) {
            log.error("查询数据集列表失败", e);
            throw new BizException("LIST_DATASETS_FAILED", "查询数据集列表失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteDataset(String datasetId) {
        try {
            RagEvalDatasetDO updateDO = new RagEvalDatasetDO();
            updateDO.setIsDeleted(1);
            updateDO.setUpdatedAt(Instant.now().toEpochMilli());

            datasetMapper.update(updateDO, new LambdaQueryWrapper<RagEvalDatasetDO>()
                    .eq(RagEvalDatasetDO::getDatasetId, datasetId));
        } catch (Exception e) {
            log.error("删除数据集失败", e);
            throw new BizException("DELETE_DATASET_FAILED", "删除数据集失败: " + e.getMessage());
        }
    }

    private LambdaQueryWrapper<RagEvalDatasetDO> buildQueryWrapper(RagEvalDatasetListQry qry) {
        LambdaQueryWrapper<RagEvalDatasetDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RagEvalDatasetDO::getIsDeleted, 0);

        if (StringUtils.hasText(qry.getDatasetId())) {
            wrapper.eq(RagEvalDatasetDO::getDatasetId, qry.getDatasetId());
        }
        if (qry.getCreatorId() != null) {
            wrapper.eq(RagEvalDatasetDO::getCreatorId, qry.getCreatorId());
        }
        if (StringUtils.hasText(qry.getDomain())) {
            wrapper.eq(RagEvalDatasetDO::getDomain, qry.getDomain());
        }
        if (qry.getStatus() != null) {
            wrapper.eq(RagEvalDatasetDO::getStatus, qry.getStatus());
        }
        if (!CollectionUtils.isEmpty(qry.getTags())) {
            // 简化处理：任一标签匹配即可（JSON contains 依实现而定）
            // 这里以 like 匹配标签字符串，实际可改为 JSON 函数
            wrapper.and(w -> {
                for (int i = 0; i < qry.getTags().size(); i++) {
                    String tag = qry.getTags().get(i);
                    if (i == 0) {
                        w.like(RagEvalDatasetDO::getTags, tag);
                    } else {
                        w.or().like(RagEvalDatasetDO::getTags, tag);
                    }
                }
            });
        }
        if (StringUtils.hasText(qry.getSearchKeyword())) {
            wrapper.and(w -> w.like(RagEvalDatasetDO::getName, qry.getSearchKeyword())
                    .or().like(RagEvalDatasetDO::getDescription, qry.getSearchKeyword()));
        }
        if (qry.getCreatedFrom() != null) {
            wrapper.ge(RagEvalDatasetDO::getCreatedAt, qry.getCreatedFrom());
        }
        if (qry.getCreatedTo() != null) {
            wrapper.le(RagEvalDatasetDO::getCreatedAt, qry.getCreatedTo());
        }

        // 默认按创建时间倒序
        String sortOrder = StringUtils.hasText(qry.getSortOrder()) ? qry.getSortOrder() : "desc";
        if ("asc".equalsIgnoreCase(sortOrder)) {
            wrapper.orderByAsc(RagEvalDatasetDO::getCreatedAt);
        } else {
            wrapper.orderByDesc(RagEvalDatasetDO::getCreatedAt);
        }
        return wrapper;
    }

    private RagEvalDatasetDTO toDTO(RagEvalDatasetDO doObj) {
        RagEvalDatasetDTO dto = new RagEvalDatasetDTO();
        dto.setDatasetId(doObj.getDatasetId());
        dto.setName(doObj.getName());
        dto.setDescription(doObj.getDescription());
        dto.setDomain(doObj.getDomain());
        dto.setTags(doObj.getTags());
        dto.setStatus(doObj.getStatus());
        dto.setCreatorId(doObj.getCreatorId());
        dto.setCreateTime(toLocalDateTime(doObj.getCreatedAt()));
        dto.setUpdateTime(toLocalDateTime(doObj.getUpdatedAt()));
        // 其余字段根据需要可在后续扩展（source/size/runCount/averageMetrics等）
        dto.setCaseCount(doObj.getTotalCases());
        dto.setExtraProperties(doObj.getMetadata());
        return dto;
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}

