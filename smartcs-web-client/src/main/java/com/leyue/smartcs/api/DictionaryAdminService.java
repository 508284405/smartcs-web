package com.leyue.smartcs.api;

import com.leyue.smartcs.dto.dictionary.*;

import java.util.List;

/**
 * 字典管理服务接口 - 面向管理端的字典配置服务
 * 提供字典数据的CRUD操作和管理功能
 * 
 * 核心特性：
 * - 支持字典条目的完整生命周期管理
 * - 提供批量导入导出功能
 * - 支持版本控制和历史追溯
 * - 支持多租户配置管理
 * - 提供数据校验和冲突检测
 * 
 * @author Claude
 */
public interface DictionaryAdminService {
    
    /**
     * 创建字典条目
     * 
     * @param createCmd 创建命令
     * @return 创建的字典条目ID
     */
    Long createDictionaryEntry(DictionaryEntryCreateCmd createCmd);
    
    /**
     * 批量创建字典条目
     * 
     * @param createCmds 批量创建命令列表
     * @return 批量创建结果
     */
    DictionaryBatchCreateResult batchCreateDictionaryEntries(List<DictionaryEntryCreateCmd> createCmds);
    
    /**
     * 更新字典条目
     * 
     * @param updateCmd 更新命令
     * @return 是否更新成功
     */
    Boolean updateDictionaryEntry(DictionaryEntryUpdateCmd updateCmd);
    
    /**
     * 删除字典条目
     * 
     * @param deleteCmd 删除命令
     * @return 是否删除成功
     */
    Boolean deleteDictionaryEntry(DictionaryEntryDeleteCmd deleteCmd);
    
    /**
     * 批量删除字典条目
     * 
     * @param deleteCmds 批量删除命令列表
     * @return 批量删除结果
     */
    DictionaryBatchDeleteResult batchDeleteDictionaryEntries(List<DictionaryEntryDeleteCmd> deleteCmds);
    
    /**
     * 根据ID查询字典条目
     * 
     * @param getQry 查询命令
     * @return 字典条目DTO
     */
    DictionaryEntryDTO getDictionaryEntry(DictionaryEntryGetQry getQry);
    
    /**
     * 分页查询字典条目
     * 
     * @param pageQry 分页查询命令
     * @return 分页结果
     */
    DictionaryEntryPageResult pageDictionaryEntries(DictionaryEntryPageQry pageQry);
    
    /**
     * 列表查询字典条目
     * 
     * @param listQry 列表查询命令
     * @return 字典条目列表
     */
    List<DictionaryEntryDTO> listDictionaryEntries(DictionaryEntryListQry listQry);
    
    /**
     * 导入字典数据
     * 支持从文件或外部数据源导入字典条目
     * 
     * @param importCmd 导入命令
     * @return 导入结果
     */
    DictionaryImportResult importDictionaryData(DictionaryImportCmd importCmd);
    
    /**
     * 导出字典数据
     * 支持导出到文件或返回数据结构
     * 
     * @param exportCmd 导出命令
     * @return 导出结果
     */
    DictionaryExportResult exportDictionaryData(DictionaryExportCmd exportCmd);
    
    /**
     * 校验字典数据
     * 检查数据格式、完整性和一致性
     * 
     * @param validateCmd 校验命令
     * @return 校验结果
     */
    DictionaryValidateResult validateDictionaryData(DictionaryValidateCmd validateCmd);
    
    /**
     * 发布字典数据
     * 将草稿状态的字典数据发布为生效状态
     * 
     * @param publishCmd 发布命令
     * @return 是否发布成功
     */
    Boolean publishDictionaryData(DictionaryPublishCmd publishCmd);
    
    /**
     * 回滚字典数据
     * 回滚到指定版本的字典数据
     * 
     * @param rollbackCmd 回滚命令
     * @return 是否回滚成功
     */
    Boolean rollbackDictionaryData(DictionaryRollbackCmd rollbackCmd);
    
    /**
     * 获取字典配置统计信息
     * 
     * @param statsQry 统计查询命令
     * @return 统计结果
     */
    DictionaryStatsResult getDictionaryStats(DictionaryStatsQry statsQry);
    
    /**
     * 获取字典类型列表
     * 返回系统支持的所有字典类型
     * 
     * @return 字典类型列表
     */
    List<DictionaryTypeDTO> getDictionaryTypes();
    
    /**
     * 获取租户渠道领域配置列表
     * 返回指定条件下的配置组合
     * 
     * @param configQry 配置查询命令
     * @return 配置列表
     */
    List<DictionaryConfigDTO> getDictionaryConfigs(DictionaryConfigQry configQry);
    
    /**
     * 同步字典缓存
     * 手动触发字典数据的缓存同步
     * 
     * @param syncCmd 同步命令
     * @return 同步结果
     */
    DictionarySyncResult syncDictionaryCache(DictionarySyncCmd syncCmd);
    
    /**
     * 获取字典变更历史
     * 
     * @param historyQry 历史查询命令
     * @return 变更历史列表
     */
    List<DictionaryHistoryDTO> getDictionaryHistory(DictionaryHistoryQry historyQry);
}