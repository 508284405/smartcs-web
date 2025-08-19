package com.leyue.smartcs.domain.intent.domainservice;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.domain.intent.enums.VersionStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentVersionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 版本领域服务
 * 
 * @author Claude
 */
@Service
@RequiredArgsConstructor
public class VersionDomainService {
    
    private final IntentVersionGateway versionGateway;
    
    /**
     * 创建新版本
     * @param intentId 意图ID
     * @param versionName 版本名称
     * @param changeNote 变更说明
     * @param createdBy 创建者ID
     * @return 创建的版本对象
     */
    public IntentVersion createVersion(Long intentId, String versionName, String changeNote, Long createdBy) {
        if (intentId == null) {
            throw new BizException("意图ID不能为空");
        }
        
        // 生成版本号
        String versionNumber = versionGateway.getNextVersionNumber(intentId);
        
        long currentTime = System.currentTimeMillis();
        IntentVersion version = IntentVersion.builder()
                .intentId(intentId)
                .versionNumber(versionNumber)
                .versionName(StringUtils.hasText(versionName) ? versionName : "版本" + versionNumber)
                .status(VersionStatus.DRAFT)
                .changeNote(changeNote)
                .sampleCount(0)
                .createdBy(createdBy)
                .isDeleted(false)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
        
        return versionGateway.save(version);
    }
    
    /**
     * 激活版本
     * @param versionId 版本ID
     * @param approvedBy 审批者ID
     */
    public void activateVersion(Long versionId, Long approvedBy) {
        IntentVersion version = versionGateway.findById(versionId);
        if (version == null) {
            throw new BizException("版本不存在");
        }
        
        if (version.getStatus() == VersionStatus.ACTIVE) {
            throw new BizException("版本已经是激活状态");
        }
        
        if (version.getStatus() == VersionStatus.DEPRECATED) {
            throw new BizException("已废弃的版本不能激活");
        }
        
        // 先将同一意图的其他激活版本设为废弃
        IntentVersion activeVersion = versionGateway.findActiveVersionByIntentId(version.getIntentId());
        if (activeVersion != null && !activeVersion.getId().equals(versionId)) {
            deprecateVersion(activeVersion.getId());
        }
        
        long currentTime = System.currentTimeMillis();
        version.setStatus(VersionStatus.ACTIVE);
        version.setApprovedBy(approvedBy);
        version.setApprovedAt(currentTime);
        version.setUpdatedAt(currentTime);
        
        versionGateway.update(version);
    }
    
    /**
     * 废弃版本
     * @param versionId 版本ID
     */
    public void deprecateVersion(Long versionId) {
        IntentVersion version = versionGateway.findById(versionId);
        if (version == null) {
            throw new BizException("版本不存在");
        }
        
        version.setStatus(VersionStatus.DEPRECATED);
        version.setUpdatedAt(System.currentTimeMillis());
        
        versionGateway.update(version);
    }
    
    /**
     * 提交审核
     * @param versionId 版本ID
     */
    public void submitForReview(Long versionId) {
        IntentVersion version = versionGateway.findById(versionId);
        if (version == null) {
            throw new BizException("版本不存在");
        }
        
        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BizException("只有草稿状态的版本才能提交审核");
        }
        
        version.setStatus(VersionStatus.REVIEW);
        version.setUpdatedAt(System.currentTimeMillis());
        
        versionGateway.update(version);
    }
    
    /**
     * 更新样本数量
     * @param versionId 版本ID
     * @param sampleCount 样本数量
     */
    public void updateSampleCount(Long versionId, int sampleCount) {
        IntentVersion version = versionGateway.findById(versionId);
        if (version == null) {
            throw new BizException("版本不存在");
        }
        
        version.setSampleCount(sampleCount);
        version.setUpdatedAt(System.currentTimeMillis());
        
        versionGateway.update(version);
    }
}