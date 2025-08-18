package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.domain.intent.enums.VersionStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentVersionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 意图版本管理命令执行器
 * 
 * @author Claude
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntentVersionManagementCmdExe {
    
    private final IntentVersionGateway intentVersionGateway;
    private final IntentGateway intentGateway;
    
    /**
     * 发布版本
     * 
     * @param versionId 版本ID
     * @return 发布结果
     */
    @Transactional
    public Response publishVersion(Long versionId) {
        try {
            log.info("开始发布意图版本: versionId={}", versionId);
            
            // 参数验证
            if (versionId == null) {
                throw new BizException("INVALID_PARAM", "版本ID不能为空");
            }
            
            // 获取版本
            IntentVersion version = intentVersionGateway.findById(versionId);
            if (version == null) {
                throw new BizException("VERSION_NOT_FOUND", "版本不存在");
            }
            
            // 检查版本状态
            if (version.getStatus() == VersionStatus.ACTIVE) {
                throw new BizException("VERSION_ALREADY_ACTIVE", "版本已经是激活状态");
            }
            
            if (version.getStatus() != VersionStatus.DRAFT) {
                throw new BizException("INVALID_VERSION_STATUS", "只有草稿状态的版本才能发布");
            }
            
            // 检查意图是否存在
            Intent intent = intentGateway.findById(version.getIntentId());
            if (intent == null) {
                throw new BizException("INTENT_NOT_FOUND", "关联的意图不存在");
            }
            
            // 下线当前激活版本
            deactivateCurrentVersion(version.getIntentId());
            
            // 激活新版本
            activateVersion(version);
            
            log.info("意图版本发布成功: versionId={}, intentId={}", versionId, version.getIntentId());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("发布意图版本业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("发布意图版本失败", e);
            throw new BizException("PUBLISH_ERROR", "发布版本失败: " + e.getMessage());
        }
    }
    
    /**
     * 下线版本
     * 
     * @param versionId 版本ID
     * @return 下线结果
     */
    @Transactional
    public Response offlineVersion(Long versionId) {
        try {
            log.info("开始下线意图版本: versionId={}", versionId);
            
            // 参数验证
            if (versionId == null) {
                throw new BizException("INVALID_PARAM", "版本ID不能为空");
            }
            
            // 获取版本
            IntentVersion version = intentVersionGateway.findById(versionId);
            if (version == null) {
                throw new BizException("VERSION_NOT_FOUND", "版本不存在");
            }
            
            // 检查版本状态
            if (version.getStatus() != VersionStatus.ACTIVE) {
                throw new BizException("VERSION_NOT_ACTIVE", "只有激活状态的版本才能下线");
            }
            
            // 下线版本
            version.setStatus(VersionStatus.DEPRECATED);
            version.setUpdatedAt(System.currentTimeMillis());
            
            intentVersionGateway.update(version);
            
            log.info("意图版本下线成功: versionId={}, intentId={}", versionId, version.getIntentId());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("下线意图版本业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("下线意图版本失败", e);
            throw new BizException("OFFLINE_ERROR", "下线版本失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除版本
     * 
     * @param versionId 版本ID
     * @return 删除结果
     */
    @Transactional
    public Response deleteVersion(Long versionId) {
        try {
            log.info("开始删除意图版本: versionId={}", versionId);
            
            // 参数验证
            if (versionId == null) {
                throw new BizException("INVALID_PARAM", "版本ID不能为空");
            }
            
            // 获取版本
            IntentVersion version = intentVersionGateway.findById(versionId);
            if (version == null) {
                throw new BizException("VERSION_NOT_FOUND", "版本不存在");
            }
            
            // 检查版本状态
            if (version.getStatus() == VersionStatus.ACTIVE) {
                throw new BizException("CANNOT_DELETE_ACTIVE", "不能删除激活状态的版本");
            }
            
            // 删除版本
            intentVersionGateway.deleteById(versionId);
            
            log.info("意图版本删除成功: versionId={}, intentId={}", versionId, version.getIntentId());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("删除意图版本业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除意图版本失败", e);
            throw new BizException("DELETE_ERROR", "删除版本失败: " + e.getMessage());
        }
    }
    
    /**
     * 复制版本
     * 
     * @param sourceVersionId 源版本ID
     * @param newVersion 新版本号
     * @param description 描述
     * @return 新版本ID
     */
    @Transactional
    public Response copyVersion(Long sourceVersionId, String newVersion, String description) {
        try {
            log.info("开始复制意图版本: sourceVersionId={}, newVersion={}", sourceVersionId, newVersion);
            
            // 参数验证
            if (sourceVersionId == null) {
                throw new BizException("INVALID_PARAM", "源版本ID不能为空");
            }
            if (newVersion == null || newVersion.trim().isEmpty()) {
                throw new BizException("INVALID_PARAM", "新版本号不能为空");
            }
            
            // 获取源版本
            IntentVersion sourceVersion = intentVersionGateway.findById(sourceVersionId);
            if (sourceVersion == null) {
                throw new BizException("VERSION_NOT_FOUND", "源版本不存在");
            }
            
            // 检查新版本号是否已存在
            List<IntentVersion> existingVersions = intentVersionGateway.findByIntentId(sourceVersion.getIntentId());
            for (IntentVersion existing : existingVersions) {
                if (newVersion.equals(existing.getVersion())) {
                    throw new BizException("VERSION_ALREADY_EXISTS", "版本号已存在: " + newVersion);
                }
            }
            
            // 创建新版本
            IntentVersion newVersionEntity = IntentVersion.builder()
                    .intentId(sourceVersion.getIntentId())
                    .version(newVersion.trim())
                    .changeNote(description != null ? description.trim() : "从版本 " + sourceVersion.getVersion() + " 复制")
                    .status(VersionStatus.DRAFT)
                    .createdBy(getCurrentUserId())
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .build();
            
            IntentVersion savedVersion = intentVersionGateway.save(newVersionEntity);
            
            log.info("意图版本复制成功: sourceVersionId={}, newVersionId={}, newVersion={}", 
                    sourceVersionId, savedVersion.getId(), newVersion);
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("复制意图版本业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("复制意图版本失败", e);
            throw new BizException("COPY_ERROR", "复制版本失败: " + e.getMessage());
        }
    }
    
    /**
     * 下线当前激活版本
     */
    private void deactivateCurrentVersion(Long intentId) {
        try {
            List<IntentVersion> activeVersions = intentVersionGateway.findByIntentIdAndStatus(intentId, VersionStatus.ACTIVE);
            
            for (IntentVersion activeVersion : activeVersions) {
                log.info("下线当前激活版本: versionId={}, intentId={}", activeVersion.getId(), intentId);
                
                activeVersion.setStatus(VersionStatus.DEPRECATED);
                activeVersion.setUpdatedAt(System.currentTimeMillis());
                
                intentVersionGateway.update(activeVersion);
            }
        } catch (Exception e) {
            log.error("下线当前激活版本失败: intentId={}", intentId, e);
            throw new RuntimeException("下线当前版本失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 激活版本
     */
    private void activateVersion(IntentVersion version) {
        try {
            log.info("激活版本: versionId={}", version.getId());
            
            version.setStatus(VersionStatus.ACTIVE);
            version.setUpdatedAt(System.currentTimeMillis());
            
            intentVersionGateway.update(version);
            
        } catch (Exception e) {
            log.error("激活版本失败: versionId={}", version.getId(), e);
            throw new RuntimeException("激活版本失败: " + e.getMessage(), e);
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