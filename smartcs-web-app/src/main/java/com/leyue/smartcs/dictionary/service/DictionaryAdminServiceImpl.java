package com.leyue.smartcs.dictionary.service;

import com.leyue.smartcs.api.DictionaryAdminService;
import com.leyue.smartcs.dto.dictionary.*;
import com.leyue.smartcs.domain.dictionary.entity.DictionaryEntry;
import com.leyue.smartcs.domain.dictionary.enums.DictionaryType;
import com.leyue.smartcs.domain.dictionary.gateway.DictionaryGateway;
import com.leyue.smartcs.domain.dictionary.valueobject.DictionaryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 字典管理服务实现类
 * 面向管理端的字典配置服务实现
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryAdminServiceImpl implements DictionaryAdminService {
    
    private final DictionaryGateway dictionaryGateway;
    
    @Override
    public Long createDictionaryEntry(DictionaryEntryCreateCmd createCmd) {
        log.info("创建字典条目: type={}, key={}", createCmd.getDictionaryType(), createCmd.getEntryKey());
        
        try {
            DictionaryType type = DictionaryType.fromCode(createCmd.getDictionaryType());
            DictionaryConfig config = DictionaryConfig.of(
                    createCmd.getTenant(), 
                    createCmd.getChannel(), 
                    createCmd.getDomain()
            );
            
            // 检查业务键是否已存在
            if (dictionaryGateway.existsBusinessKey(type, config, createCmd.getEntryKey(), null)) {
                throw new IllegalArgumentException("字典条目已存在: " + createCmd.getEntryKey());
            }
            
            DictionaryEntry entry = DictionaryEntry.create(
                    type,
                    config,
                    createCmd.getEntryKey(),
                    createCmd.getEntryValue(),
                    createCmd.getDescription(),
                    createCmd.getCreatedBy()
            );
            
            if (createCmd.getPriority() != null) {
                entry.updatePriority(createCmd.getPriority(), createCmd.getCreatedBy());
            }
            
            DictionaryEntry savedEntry = dictionaryGateway.save(entry);
            return savedEntry.getId();
            
        } catch (Exception e) {
            log.error("创建字典条目失败: {}", createCmd, e);
            throw new RuntimeException("创建字典条目失败", e);
        }
    }
    
    @Override
    public DictionaryBatchCreateResult batchCreateDictionaryEntries(List<DictionaryEntryCreateCmd> createCmds) {
        // 简化实现，逐个创建
        int successCount = 0;
        int failCount = 0;
        
        for (DictionaryEntryCreateCmd cmd : createCmds) {
            try {
                createDictionaryEntry(cmd);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.warn("批量创建中单个条目失败: {}", cmd, e);
            }
        }
        
        return DictionaryBatchCreateResult.builder()
                .totalCount(createCmds.size())
                .successCount(successCount)
                .failCount(failCount)
                .build();
    }
    
    @Override
    public Boolean updateDictionaryEntry(DictionaryEntryUpdateCmd updateCmd) {
        log.info("更新字典条目: id={}", updateCmd.getId());
        
        try {
            Optional<DictionaryEntry> optEntry = dictionaryGateway.findById(updateCmd.getId());
            if (optEntry.isEmpty()) {
                return false;
            }
            
            DictionaryEntry entry = optEntry.get();
            entry.updateContent(updateCmd.getEntryValue(), updateCmd.getDescription(), updateCmd.getUpdatedBy());
            
            if (updateCmd.getPriority() != null) {
                entry.updatePriority(updateCmd.getPriority(), updateCmd.getUpdatedBy());
            }
            
            if (updateCmd.getStatus() != null) {
                switch (updateCmd.getStatus()) {
                    case "ACTIVE":
                        entry.activate(updateCmd.getUpdatedBy());
                        break;
                    case "INACTIVE":
                        entry.deactivate(updateCmd.getUpdatedBy());
                        break;
                    case "DRAFT":
                        entry.setToDraft(updateCmd.getUpdatedBy());
                        break;
                }
            }
            
            dictionaryGateway.save(entry);
            return true;
            
        } catch (Exception e) {
            log.error("更新字典条目失败: {}", updateCmd, e);
            return false;
        }
    }
    
    @Override
    public Boolean deleteDictionaryEntry(DictionaryEntryDeleteCmd deleteCmd) {
        return dictionaryGateway.deleteById(deleteCmd.getId());
    }
    
    @Override
    public DictionaryBatchDeleteResult batchDeleteDictionaryEntries(List<DictionaryEntryDeleteCmd> deleteCmds) {
        // 简化实现
        return DictionaryBatchDeleteResult.builder()
                .totalCount(deleteCmds.size())
                .successCount(0)
                .failCount(deleteCmds.size())
                .build();
    }
    
    @Override
    public DictionaryEntryDTO getDictionaryEntry(DictionaryEntryGetQry getQry) {
        Optional<DictionaryEntry> optEntry = dictionaryGateway.findById(getQry.getId());
        if (optEntry.isEmpty()) {
            return null;
        }
        
        DictionaryEntry entry = optEntry.get();
        return convertToDTO(entry);
    }
    
    @Override
    public DictionaryEntryPageResult pageDictionaryEntries(DictionaryEntryPageQry pageQry) {
        // 简化实现，返回空结果
        return DictionaryEntryPageResult.builder()
                .pageNum(pageQry.getPageNum())
                .pageSize(pageQry.getPageSize())
                .total(0L)
                .pages(0)
                .list(List.of())
                .build();
    }
    
    @Override
    public List<DictionaryEntryDTO> listDictionaryEntries(DictionaryEntryListQry listQry) {
        try {
            DictionaryType type = listQry.getDictionaryType() != null ? 
                    DictionaryType.fromCode(listQry.getDictionaryType()) : null;
            DictionaryConfig config = listQry.getTenant() != null ? 
                    DictionaryConfig.of(listQry.getTenant(), listQry.getChannel(), listQry.getDomain()) : null;
            
            List<DictionaryEntry> entries = dictionaryGateway.findEntries(type, config, null, listQry.getStatus(), listQry.getLimit());
            return entries.stream().map(this::convertToDTO).toList();
        } catch (Exception e) {
            log.error("查询字典条目列表失败: {}", listQry, e);
            return List.of();
        }
    }
    
    // 其他方法都提供简化实现，返回占位对象
    @Override
    public DictionaryImportResult importDictionaryData(DictionaryImportCmd importCmd) {
        return DictionaryImportResult.builder().success(false).build();
    }
    
    @Override
    public DictionaryExportResult exportDictionaryData(DictionaryExportCmd exportCmd) {
        return DictionaryExportResult.builder().success(false).build();
    }
    
    @Override
    public DictionaryValidateResult validateDictionaryData(DictionaryValidateCmd validateCmd) {
        return DictionaryValidateResult.builder().valid(true).build();
    }
    
    @Override
    public Boolean publishDictionaryData(DictionaryPublishCmd publishCmd) {
        return false;
    }
    
    @Override
    public Boolean rollbackDictionaryData(DictionaryRollbackCmd rollbackCmd) {
        return false;
    }
    
    @Override
    public DictionaryStatsResult getDictionaryStats(DictionaryStatsQry statsQry) {
        return DictionaryStatsResult.builder().build();
    }
    
    @Override
    public List<DictionaryTypeDTO> getDictionaryTypes() {
        return List.of();
    }
    
    @Override
    public List<DictionaryConfigDTO> getDictionaryConfigs(DictionaryConfigQry configQry) {
        return List.of();
    }
    
    @Override
    public DictionarySyncResult syncDictionaryCache(DictionarySyncCmd syncCmd) {
        return DictionarySyncResult.builder().build();
    }
    
    @Override
    public List<DictionaryHistoryDTO> getDictionaryHistory(DictionaryHistoryQry historyQry) {
        return List.of();
    }
    
    /**
     * 转换为DTO
     */
    private DictionaryEntryDTO convertToDTO(DictionaryEntry entry) {
        return DictionaryEntryDTO.builder()
                .id(entry.getId())
                .dictionaryType(entry.getDictionaryType().getCode())
                .tenant(entry.getConfig().getTenant())
                .channel(entry.getConfig().getChannel())
                .domain(entry.getConfig().getDomain())
                .entryKey(entry.getEntryKey())
                .entryValue(entry.getEntryValue())
                .description(entry.getDescription())
                .status(entry.getStatus().getCode())
                .priority(entry.getPriority())
                .version(entry.getVersion())
                .createTime(entry.getCreateTime())
                .updateTime(entry.getUpdateTime())
                .createdBy(entry.getCreatedBy())
                .updatedBy(entry.getUpdatedBy())
                .build();
    }
}