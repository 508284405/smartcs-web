package com.leyue.smartcs.domain.dictionary.gateway;

import com.leyue.smartcs.domain.dictionary.entity.DictionaryEntry;
import com.leyue.smartcs.domain.dictionary.enums.DictionaryType;
import com.leyue.smartcs.domain.dictionary.valueobject.DictionaryConfig;

import java.util.List;
import java.util.Optional;

/**
 * 字典领域网关接口
 * 定义字典实体的数据访问契约，遵循DDD架构原则
 * 
 * @author Claude
 */
public interface DictionaryGateway {
    
    /**
     * 保存字典条目
     * 
     * @param entry 字典条目
     * @return 保存后的条目（包含ID）
     */
    DictionaryEntry save(DictionaryEntry entry);
    
    /**
     * 批量保存字典条目
     * 
     * @param entries 字典条目列表
     * @return 保存后的条目列表
     */
    List<DictionaryEntry> saveBatch(List<DictionaryEntry> entries);
    
    /**
     * 根据ID查找字典条目
     * 
     * @param id 条目ID
     * @return 字典条目Optional
     */
    Optional<DictionaryEntry> findById(Long id);
    
    /**
     * 根据业务键查找字典条目
     * 
     * @param dictionaryType 字典类型
     * @param config 配置信息
     * @param entryKey 条目键
     * @return 字典条目Optional
     */
    Optional<DictionaryEntry> findByBusinessKey(DictionaryType dictionaryType, 
                                              DictionaryConfig config, 
                                              String entryKey);
    
    /**
     * 查找指定配置下的所有活跃字典条目
     * 
     * @param dictionaryType 字典类型
     * @param config 配置信息
     * @return 活跃字典条目列表
     */
    List<DictionaryEntry> findActiveEntries(DictionaryType dictionaryType, DictionaryConfig config);
    
    /**
     * 查找指定配置下的所有字典条目（包含非活跃的）
     * 
     * @param dictionaryType 字典类型
     * @param config 配置信息
     * @return 所有字典条目列表
     */
    List<DictionaryEntry> findAllEntries(DictionaryType dictionaryType, DictionaryConfig config);
    
    /**
     * 查找指定条件下的字典条目列表
     * 
     * @param dictionaryType 字典类型（可选）
     * @param config 配置信息（可选）
     * @param entryKeyPattern 条目键模式（支持通配符，可选）
     * @param status 状态（可选）
     * @param limit 限制数量（可选）
     * @return 匹配的字典条目列表
     */
    List<DictionaryEntry> findEntries(DictionaryType dictionaryType,
                                    DictionaryConfig config,
                                    String entryKeyPattern,
                                    String status,
                                    Integer limit);
    
    /**
     * 统计指定条件下的字典条目数量
     * 
     * @param dictionaryType 字典类型（可选）
     * @param config 配置信息（可选）
     * @param status 状态（可选）
     * @return 条目数量
     */
    long countEntries(DictionaryType dictionaryType, DictionaryConfig config, String status);
    
    /**
     * 检查业务键是否已存在
     * 
     * @param dictionaryType 字典类型
     * @param config 配置信息
     * @param entryKey 条目键
     * @param excludeId 排除的条目ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsBusinessKey(DictionaryType dictionaryType, 
                             DictionaryConfig config, 
                             String entryKey, 
                             Long excludeId);
    
    /**
     * 删除字典条目
     * 
     * @param id 条目ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
    
    /**
     * 批量删除字典条目
     * 
     * @param ids 条目ID列表
     * @return 删除成功的数量
     */
    int deleteBatch(List<Long> ids);
    
    /**
     * 删除指定配置下的所有字典条目
     * 
     * @param dictionaryType 字典类型
     * @param config 配置信息
     * @return 删除的数量
     */
    int deleteByConfig(DictionaryType dictionaryType, DictionaryConfig config);
    
    /**
     * 获取字典数据的最新版本时间戳
     * 
     * @param dictionaryType 字典类型
     * @param config 配置信息
     * @return 最新版本时间戳（毫秒）
     */
    Long getLatestVersionTimestamp(DictionaryType dictionaryType, DictionaryConfig config);
    
    /**
     * 获取所有可用的配置列表
     * 
     * @param dictionaryType 字典类型（可选）
     * @return 配置列表
     */
    List<DictionaryConfig> findAllConfigs(DictionaryType dictionaryType);
    
    /**
     * 获取所有使用的字典类型列表
     * 
     * @return 字典类型列表
     */
    List<DictionaryType> findAllUsedDictionaryTypes();
}