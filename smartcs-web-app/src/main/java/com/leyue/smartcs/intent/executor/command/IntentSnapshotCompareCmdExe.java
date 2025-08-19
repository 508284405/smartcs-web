package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshotItem;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import com.leyue.smartcs.dto.intent.IntentDTO;
import com.leyue.smartcs.dto.intent.IntentSnapshotCompareCmd;
import com.leyue.smartcs.dto.intent.IntentSnapshotCompareResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 意图快照比较命令执行器
 * 
 * @author Claude
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntentSnapshotCompareCmdExe {
    
    private final IntentSnapshotGateway intentSnapshotGateway;
    
    /**
     * 比较两个快照的差异
     * 
     * @param cmd 比较命令
     * @return 比较结果
     */
    public SingleResponse<IntentSnapshotCompareResultDTO> execute(IntentSnapshotCompareCmd cmd) {
        try {
            log.info("开始比较意图快照: baseSnapshotId={}, targetSnapshotId={}", 
                    cmd.getBaseSnapshotId(), cmd.getTargetSnapshotId());
            
            // 参数验证
            if (cmd.getBaseSnapshotId() == null || cmd.getTargetSnapshotId() == null) {
                throw new BizException("INVALID_PARAM", "快照ID不能为空");
            }
            
            if (cmd.getBaseSnapshotId().equals(cmd.getTargetSnapshotId())) {
                throw new BizException("INVALID_PARAM", "不能比较相同的快照");
            }
            
            // 获取快照
            IntentSnapshot baseSnapshot = intentSnapshotGateway.findById(cmd.getBaseSnapshotId());
            if (baseSnapshot == null) {
                throw new BizException("SNAPSHOT_NOT_FOUND", "基准快照不存在");
            }
            
            IntentSnapshot targetSnapshot = intentSnapshotGateway.findById(cmd.getTargetSnapshotId());
            if (targetSnapshot == null) {
                throw new BizException("SNAPSHOT_NOT_FOUND", "目标快照不存在");
            }
            
            // 执行比较
            IntentSnapshotCompareResultDTO result = compareSnapshots(baseSnapshot, targetSnapshot);
            
            log.info("意图快照比较完成: addedCount={}, removedCount={}, modifiedCount={}", 
                    result.getAddedIntents().size(),
                    result.getRemovedIntents().size(),
                    result.getModifiedIntents().size());
            
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("比较意图快照业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("比较意图快照失败", e);
            throw new BizException("COMPARE_ERROR", "比较快照失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行快照比较
     */
    private IntentSnapshotCompareResultDTO compareSnapshots(IntentSnapshot baseSnapshot, IntentSnapshot targetSnapshot) {
        // 构建基准快照的意图映射
        Map<String, IntentSnapshotItem> baseIntentMap = new HashMap<>();
        if (baseSnapshot.getItems() != null) {
            for (IntentSnapshotItem item : baseSnapshot.getItems()) {
                baseIntentMap.put(item.getIntentCode(), item);
            }
        }
        
        // 构建目标快照的意图映射
        Map<String, IntentSnapshotItem> targetIntentMap = new HashMap<>();
        if (targetSnapshot.getItems() != null) {
            for (IntentSnapshotItem item : targetSnapshot.getItems()) {
                targetIntentMap.put(item.getIntentCode(), item);
            }
        }
        
        // 查找新增的意图（在目标中存在，在基准中不存在）
        List<IntentDTO> addedIntents = new ArrayList<>();
        for (String intentCode : targetIntentMap.keySet()) {
            if (!baseIntentMap.containsKey(intentCode)) {
                IntentSnapshotItem item = targetIntentMap.get(intentCode);
                addedIntents.add(convertToIntentDTO(item));
            }
        }
        
        // 查找删除的意图（在基准中存在，在目标中不存在）
        List<IntentDTO> removedIntents = new ArrayList<>();
        for (String intentCode : baseIntentMap.keySet()) {
            if (!targetIntentMap.containsKey(intentCode)) {
                IntentSnapshotItem item = baseIntentMap.get(intentCode);
                removedIntents.add(convertToIntentDTO(item));
            }
        }
        
        // 查找修改的意图（在两个快照中都存在，但内容不同）
        List<IntentDTO> modifiedIntents = new ArrayList<>();
        for (String intentCode : baseIntentMap.keySet()) {
            if (targetIntentMap.containsKey(intentCode)) {
                IntentSnapshotItem baseItem = baseIntentMap.get(intentCode);
                IntentSnapshotItem targetItem = targetIntentMap.get(intentCode);
                
                if (isIntentModified(baseItem, targetItem)) {
                    modifiedIntents.add(convertToIntentDTO(targetItem));
                }
            }
        }
        
        // 构建比较结果
        IntentSnapshotCompareResultDTO result = new IntentSnapshotCompareResultDTO();
        result.setBaseSnapshotId(baseSnapshot.getId());
        result.setTargetSnapshotId(targetSnapshot.getId());
        result.setAddedIntents(addedIntents);
        result.setRemovedIntents(removedIntents);
        result.setModifiedIntents(modifiedIntents);
        result.setCompareTime(System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 判断意图是否被修改
     */
    private boolean isIntentModified(IntentSnapshotItem baseItem, IntentSnapshotItem targetItem) {
        // 比较版本
        if (!Objects.equals(baseItem.getVersion(), targetItem.getVersion())) {
            return true;
        }
        
        // 比较意图名称
        if (!Objects.equals(baseItem.getIntentName(), targetItem.getIntentName())) {
            return true;
        }
        
        // 比较标签
        if (!Objects.equals(baseItem.getLabels(), targetItem.getLabels())) {
            return true;
        }
        
        // 比较边界
        if (!Objects.equals(baseItem.getBoundaries(), targetItem.getBoundaries())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 将快照项目转换为意图DTO
     */
    private IntentDTO convertToIntentDTO(IntentSnapshotItem item) {
        IntentDTO dto = new IntentDTO();
        dto.setId(item.getIntentId());
        dto.setCode(item.getIntentCode());
        dto.setName(item.getIntentName());
        dto.setLabels(item.getLabels());
        
        // 将List<String>转换为Map<String, Object>
        if (item.getBoundaries() != null) {
            Map<String, Object> boundariesMap = new HashMap<>();
            for (int i = 0; i < item.getBoundaries().size(); i++) {
                boundariesMap.put("boundary_" + i, item.getBoundaries().get(i));
            }
            dto.setBoundaries(boundariesMap);
        }
        
        return dto;
    }
}