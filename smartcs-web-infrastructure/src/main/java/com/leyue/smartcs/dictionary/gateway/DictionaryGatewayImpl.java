package com.leyue.smartcs.dictionary.gateway;

import com.leyue.smartcs.dictionary.convertor.DictionaryConvertor;
import com.leyue.smartcs.dictionary.dataobject.DictionaryEntryDO;
import com.leyue.smartcs.dictionary.mapper.DictionaryEntryMapper;
import com.leyue.smartcs.domain.dictionary.entity.DictionaryEntry;
import com.leyue.smartcs.domain.dictionary.enums.DictionaryType;
import com.leyue.smartcs.domain.dictionary.gateway.DictionaryGateway;
import com.leyue.smartcs.domain.dictionary.valueobject.DictionaryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 字典领域网关实现
 * 负责字典实体的数据访问实现
 * 
 * @author Claude
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DictionaryGatewayImpl implements DictionaryGateway {
    
    private final DictionaryEntryMapper dictionaryEntryMapper;
    private final DictionaryConvertor convertor = DictionaryConvertor.INSTANCE;
    
    @Override
    public DictionaryEntry save(DictionaryEntry entry) {
        log.debug("保存字典条目: type={}, config={}, key={}", 
                 entry.getDictionaryType(), entry.getConfigIdentifier(), entry.getEntryKey());
        
        DictionaryEntryDO dataObject = convertor.toDataObject(entry);
        
        if (entry.getId() == null) {
            // 新增
            dictionaryEntryMapper.insert(dataObject);
        } else {
            // 更新
            dictionaryEntryMapper.updateById(dataObject);
        }
        
        return convertor.toDomainEntity(dataObject);
    }
    
    @Override
    public List<DictionaryEntry> saveBatch(List<DictionaryEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        
        log.debug("批量保存字典条目: count={}", entries.size());
        
        List<DictionaryEntryDO> dataObjects = convertor.toDataObjectList(entries);
        
        // 分离新增和更新
        List<DictionaryEntryDO> insertList = dataObjects.stream()
                .filter(item -> item.getId() == null)
                .collect(Collectors.toList());
        
        List<DictionaryEntryDO> updateList = dataObjects.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toList());
        
        // 批量插入新数据
        if (!insertList.isEmpty()) {
            for (DictionaryEntryDO item : insertList) {
                dictionaryEntryMapper.insert(item);
            }
        }
        
        // 批量更新已有数据
        if (!updateList.isEmpty()) {
            for (DictionaryEntryDO item : updateList) {
                dictionaryEntryMapper.updateById(item);
            }
        }
        
        return convertor.toDomainEntityList(dataObjects);
    }
    
    @Override
    public Optional<DictionaryEntry> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        DictionaryEntryDO dataObject = dictionaryEntryMapper.selectById(id);
        if (dataObject == null) {
            return Optional.empty();
        }
        
        return Optional.of(convertor.toDomainEntity(dataObject));
    }
    
    @Override
    public Optional<DictionaryEntry> findByBusinessKey(DictionaryType dictionaryType, 
                                                     DictionaryConfig config, 
                                                     String entryKey) {
        if (dictionaryType == null || config == null || entryKey == null || entryKey.isEmpty()) {
            return Optional.empty();
        }
        
        DictionaryEntryDO dataObject = dictionaryEntryMapper.selectByBusinessKey(
                dictionaryType.getCode(),
                config.getTenant(),
                config.getChannel(), 
                config.getDomain(),
                entryKey
        );
        
        if (dataObject == null) {
            return Optional.empty();
        }
        
        return Optional.of(convertor.toDomainEntity(dataObject));
    }
    
    @Override
    public List<DictionaryEntry> findActiveEntries(DictionaryType dictionaryType, DictionaryConfig config) {
        if (dictionaryType == null || config == null) {
            return List.of();
        }
        
        List<DictionaryEntryDO> dataObjects = dictionaryEntryMapper.selectActiveEntries(
                dictionaryType.getCode(),
                config.getTenant(),
                config.getChannel(),
                config.getDomain()
        );
        
        return convertor.toDomainEntityList(dataObjects);
    }
    
    @Override
    public List<DictionaryEntry> findAllEntries(DictionaryType dictionaryType, DictionaryConfig config) {
        if (dictionaryType == null || config == null) {
            return List.of();
        }
        
        List<DictionaryEntryDO> dataObjects = dictionaryEntryMapper.selectAllEntries(
                dictionaryType.getCode(),
                config.getTenant(),
                config.getChannel(),
                config.getDomain()
        );
        
        return convertor.toDomainEntityList(dataObjects);
    }
    
    @Override
    public List<DictionaryEntry> findEntries(DictionaryType dictionaryType,
                                           DictionaryConfig config,
                                           String entryKeyPattern,
                                           String status,
                                           Integer limit) {
        String dictionaryTypeCode = dictionaryType != null ? dictionaryType.getCode() : null;
        String tenant = config != null ? config.getTenant() : null;
        String channel = config != null ? config.getChannel() : null;
        String domain = config != null ? config.getDomain() : null;
        
        List<DictionaryEntryDO> dataObjects = dictionaryEntryMapper.selectListByConditions(
                dictionaryTypeCode, tenant, channel, domain, entryKeyPattern, status, limit
        );
        
        return convertor.toDomainEntityList(dataObjects);
    }
    
    @Override
    public long countEntries(DictionaryType dictionaryType, DictionaryConfig config, String status) {
        // 这里需要在Mapper中添加对应的方法，暂时使用简单实现
        List<DictionaryEntry> entries = findEntries(dictionaryType, config, null, status, null);
        return entries.size();
    }
    
    @Override
    public boolean existsBusinessKey(DictionaryType dictionaryType, 
                                   DictionaryConfig config, 
                                   String entryKey, 
                                   Long excludeId) {
        if (dictionaryType == null || config == null || entryKey == null || entryKey.isEmpty()) {
            return false;
        }
        
        int count = dictionaryEntryMapper.countByBusinessKey(
                dictionaryType.getCode(),
                config.getTenant(),
                config.getChannel(),
                config.getDomain(),
                entryKey,
                excludeId
        );
        
        return count > 0;
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        
        int result = dictionaryEntryMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        int deletedCount = 0;
        for (Long id : ids) {
            if (deleteById(id)) {
                deletedCount++;
            }
        }
        
        return deletedCount;
    }
    
    @Override
    public int deleteByConfig(DictionaryType dictionaryType, DictionaryConfig config) {
        if (dictionaryType == null || config == null) {
            return 0;
        }
        
        // 这里需要在Mapper中添加对应的方法，暂时使用简单实现
        List<DictionaryEntry> entries = findAllEntries(dictionaryType, config);
        List<Long> ids = entries.stream().map(DictionaryEntry::getId).collect(Collectors.toList());
        
        return deleteBatch(ids);
    }
    
    @Override
    public Long getLatestVersionTimestamp(DictionaryType dictionaryType, DictionaryConfig config) {
        if (dictionaryType == null || config == null) {
            return null;
        }
        
        return dictionaryEntryMapper.selectLatestUpdateTimestamp(
                dictionaryType.getCode(),
                config.getTenant(),
                config.getChannel(),
                config.getDomain()
        );
    }
    
    @Override
    public List<DictionaryConfig> findAllConfigs(DictionaryType dictionaryType) {
        String dictionaryTypeCode = dictionaryType != null ? dictionaryType.getCode() : null;
        
        List<DictionaryEntryDO> distinctConfigs = dictionaryEntryMapper.selectDistinctConfigs(dictionaryTypeCode);
        
        return distinctConfigs.stream()
                .map(item -> DictionaryConfig.of(item.getTenant(), item.getChannel(), item.getDomain()))
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DictionaryType> findAllUsedDictionaryTypes() {
        List<String> typeCodes = dictionaryEntryMapper.selectDistinctDictionaryTypes();
        
        return typeCodes.stream()
                .map(code -> {
                    try {
                        return DictionaryType.fromCode(code);
                    } catch (IllegalArgumentException e) {
                        log.warn("发现未识别的字典类型代码: {}", code);
                        return null;
                    }
                })
                .filter(type -> type != null)
                .collect(Collectors.toList());
    }
}