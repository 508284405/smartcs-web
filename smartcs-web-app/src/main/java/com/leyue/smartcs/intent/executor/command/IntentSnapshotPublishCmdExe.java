package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshotItem;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.domain.intent.enums.IntentStatus;
import com.leyue.smartcs.domain.intent.enums.SnapshotStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentVersionGateway;
import com.leyue.smartcs.dto.intent.IntentSnapshotPublishCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图快照发布命令执行器
 * 
 * @author Claude
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntentSnapshotPublishCmdExe {
    
    private final IntentSnapshotGateway intentSnapshotGateway;
    private final IntentGateway intentGateway;
    private final IntentVersionGateway intentVersionGateway;
    private final ObjectMapper objectMapper;
    
    /**
     * 发布意图快照
     * 
     * @param cmd 发布命令
     * @return 发布结果
     */
    @Transactional
    public Response execute(IntentSnapshotPublishCmd cmd) {
        try {
            log.info("开始发布意图快照: snapshotId={}", cmd.getSnapshotId());
            
            // 参数验证
            if (cmd.getSnapshotId() == null) {
                throw new BizException("INVALID_PARAM", "快照ID不能为空");
            }
            
            // 获取快照
            IntentSnapshot snapshot = intentSnapshotGateway.findById(cmd.getSnapshotId());
            if (snapshot == null) {
                throw new BizException("SNAPSHOT_NOT_FOUND", "快照不存在");
            }
            
            // 检查快照状态
            if (snapshot.getStatus() == SnapshotStatus.ACTIVE) {
                throw new BizException("SNAPSHOT_ALREADY_ACTIVE", "快照已经是激活状态");
            }
            
            if (snapshot.getStatus() != SnapshotStatus.DRAFT) {
                throw new BizException("INVALID_SNAPSHOT_STATUS", "只有草稿状态的快照才能发布");
            }
            
            // 构建快照内容
            buildSnapshotContent(snapshot);
            
            // 下线当前激活快照
            deactivateCurrentSnapshot();
            
            // 激活新快照
            activateSnapshot(snapshot);
            
            log.info("意图快照发布成功: snapshotId={}, etag={}", snapshot.getId(), snapshot.getEtag());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("发布意图快照业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("发布意图快照失败", e);
            throw new BizException("PUBLISH_ERROR", "发布快照失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建快照内容
     */
    private void buildSnapshotContent(IntentSnapshot snapshot) {
        try {
            log.debug("构建快照内容: snapshotId={}", snapshot.getId());
            
            // 获取所有激活的意图
            List<Intent> activeIntents = intentGateway.findByStatus(IntentStatus.ACTIVE);
            
            // 构建快照项目
            List<IntentSnapshotItem> items = new ArrayList<>();
            
            for (Intent intent : activeIntents) {
                // 获取意图的激活版本
                IntentVersion activeVersion = intentVersionGateway.findActiveVersionByIntentId(intent.getId());
                if (activeVersion == null) {
                    log.warn("意图没有激活版本，跳过: intentId={}", intent.getId());
                    continue;
                }
                
                // 创建快照项目
                List<String> boundariesList = new ArrayList<>();
                if (intent.getBoundaries() != null) {
                    // 将Map<String, Object>转换为List<String>
                    boundariesList.addAll(intent.getBoundaries().values().stream()
                            .map(Object::toString)
                            .collect(java.util.stream.Collectors.toList()));
                }
                
                IntentSnapshotItem item = IntentSnapshotItem.builder()
                        .snapshotId(snapshot.getId())
                        .intentId(intent.getId())
                        .intentCode(intent.getCode())
                        .intentName(intent.getName())
                        .versionId(activeVersion.getId())
                        .version(activeVersion.getVersion())
                        .labels(intent.getLabels())
                        .boundaries(boundariesList)
                        .build();
                
                items.add(item);
            }
            
            // 设置快照项目
            snapshot.setItems(items);
            
            // 生成ETag
            String etag = generateETag(snapshot);
            snapshot.setEtag(etag);
            
            log.info("快照内容构建完成: intentCount={}, etag={}", items.size(), etag);
            
        } catch (Exception e) {
            log.error("构建快照内容失败: snapshotId={}", snapshot.getId(), e);
            throw new RuntimeException("构建快照内容失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 下线当前激活快照
     */
    private void deactivateCurrentSnapshot() {
        try {
            IntentSnapshot currentActive = intentSnapshotGateway.getCurrentActiveSnapshot();
            if (currentActive != null) {
                log.info("下线当前激活快照: snapshotId={}", currentActive.getId());
                
                currentActive.setStatus(SnapshotStatus.DEPRECATED);
                currentActive.setUpdatedAt(System.currentTimeMillis());
                
                intentSnapshotGateway.update(currentActive);
            }
        } catch (Exception e) {
            log.error("下线当前激活快照失败", e);
            throw new RuntimeException("下线当前快照失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 激活快照
     */
    private void activateSnapshot(IntentSnapshot snapshot) {
        try {
            log.info("激活快照: snapshotId={}", snapshot.getId());
            
            snapshot.setStatus(SnapshotStatus.ACTIVE);
            snapshot.setPublishedBy(getCurrentUserId());
            snapshot.setPublishedAt(System.currentTimeMillis());
            snapshot.setUpdatedAt(System.currentTimeMillis());
            
            intentSnapshotGateway.update(snapshot);
            
        } catch (Exception e) {
            log.error("激活快照失败: snapshotId={}", snapshot.getId(), e);
            throw new RuntimeException("激活快照失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成ETag
     */
    private String generateETag(IntentSnapshot snapshot) {
        try {
            // 构建用于计算ETag的数据
            Map<String, Object> etagData = new HashMap<>();
            etagData.put("snapshotId", snapshot.getId());
            etagData.put("name", snapshot.getName());
            etagData.put("scope", snapshot.getScope());
            etagData.put("scopeSelector", snapshot.getScopeSelector());
            
            // 添加意图信息
            List<Map<String, Object>> intentData = new ArrayList<>();
            if (snapshot.getItems() != null) {
                for (IntentSnapshotItem item : snapshot.getItems()) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("intentId", item.getIntentId());
                    itemData.put("intentCode", item.getIntentCode());
                    itemData.put("version", item.getVersion());
                    itemData.put("labels", item.getLabels());
                    itemData.put("boundaries", item.getBoundaries());
                    intentData.add(itemData);
                }
            }
            etagData.put("intents", intentData);
            
            // 序列化并生成哈希
            String json = objectMapper.writeValueAsString(etagData);
            return "W/\"" + Integer.toHexString(json.hashCode()) + "\"";
            
        } catch (Exception e) {
            log.error("生成ETag失败", e);
            return "W/\"" + System.currentTimeMillis() + "\"";
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 集成用户上下文
        return 1L;
    }
}