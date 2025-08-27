package com.leyue.smartcs.dictionary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.dictionary.dataobject.DictionaryEntryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典条目Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface DictionaryEntryMapper extends BaseMapper<DictionaryEntryDO> {
    
    /**
     * 根据业务键查询字典条目
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户
     * @param channel 渠道
     * @param domain 领域
     * @param entryKey 条目键
     * @return 字典条目DO
     */
    @Select("SELECT * FROM t_dictionary_entry " +
            "WHERE dictionary_type = #{dictionaryType} " +
            "AND tenant = #{tenant} " +
            "AND channel = #{channel} " +
            "AND domain = #{domain} " +
            "AND entry_key = #{entryKey} " +
            "AND is_deleted = 0")
    DictionaryEntryDO selectByBusinessKey(@Param("dictionaryType") String dictionaryType,
                                         @Param("tenant") String tenant,
                                         @Param("channel") String channel,
                                         @Param("domain") String domain,
                                         @Param("entryKey") String entryKey);
    
    /**
     * 查询指定配置下的活跃字典条目
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户
     * @param channel 渠道  
     * @param domain 领域
     * @return 活跃字典条目列表
     */
    @Select("SELECT * FROM t_dictionary_entry " +
            "WHERE dictionary_type = #{dictionaryType} " +
            "AND tenant = #{tenant} " +
            "AND channel = #{channel} " +
            "AND domain = #{domain} " +
            "AND status = 'ACTIVE' " +
            "AND is_deleted = 0 " +
            "ORDER BY priority DESC, created_at ASC")
    List<DictionaryEntryDO> selectActiveEntries(@Param("dictionaryType") String dictionaryType,
                                               @Param("tenant") String tenant,
                                               @Param("channel") String channel,
                                               @Param("domain") String domain);
    
    /**
     * 查询指定配置下的所有字典条目（包含非活跃的）
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户
     * @param channel 渠道
     * @param domain 领域
     * @return 所有字典条目列表
     */
    @Select("SELECT * FROM t_dictionary_entry " +
            "WHERE dictionary_type = #{dictionaryType} " +
            "AND tenant = #{tenant} " +
            "AND channel = #{channel} " +
            "AND domain = #{domain} " +
            "AND is_deleted = 0 " +
            "ORDER BY priority DESC, created_at ASC")
    List<DictionaryEntryDO> selectAllEntries(@Param("dictionaryType") String dictionaryType,
                                            @Param("tenant") String tenant,
                                            @Param("channel") String channel,
                                            @Param("domain") String domain);
    
    /**
     * 检查业务键是否存在
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户
     * @param channel 渠道
     * @param domain 领域
     * @param entryKey 条目键
     * @param excludeId 排除的条目ID
     * @return 存在的数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_dictionary_entry " +
            "WHERE dictionary_type = #{dictionaryType} " +
            "AND tenant = #{tenant} " +
            "AND channel = #{channel} " +
            "AND domain = #{domain} " +
            "AND entry_key = #{entryKey} " +
            "AND is_deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    int countByBusinessKey(@Param("dictionaryType") String dictionaryType,
                          @Param("tenant") String tenant,
                          @Param("channel") String channel,
                          @Param("domain") String domain,
                          @Param("entryKey") String entryKey,
                          @Param("excludeId") Long excludeId);
    
    /**
     * 获取字典数据的最新更新时间戳
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户
     * @param channel 渠道
     * @param domain 领域
     * @return 最新更新时间戳
     */
    @Select("SELECT MAX(updated_at) FROM t_dictionary_entry " +
            "WHERE dictionary_type = #{dictionaryType} " +
            "AND tenant = #{tenant} " +
            "AND channel = #{channel} " +
            "AND domain = #{domain} " +
            "AND is_deleted = 0")
    Long selectLatestUpdateTimestamp(@Param("dictionaryType") String dictionaryType,
                                    @Param("tenant") String tenant,
                                    @Param("channel") String channel,
                                    @Param("domain") String domain);
    
    /**
     * 查询所有可用的配置组合
     * 
     * @param dictionaryType 字典类型（可选）
     * @return 配置组合列表
     */
    @Select("<script>" +
            "SELECT DISTINCT tenant, channel, domain FROM t_dictionary_entry " +
            "WHERE is_deleted = 0 " +
            "<if test='dictionaryType != null and dictionaryType != \"\"'>" +
            "AND dictionary_type = #{dictionaryType} " +
            "</if>" +
            "ORDER BY tenant, channel, domain" +
            "</script>")
    List<DictionaryEntryDO> selectDistinctConfigs(@Param("dictionaryType") String dictionaryType);
    
    /**
     * 查询所有使用的字典类型
     * 
     * @return 字典类型列表
     */
    @Select("SELECT DISTINCT dictionary_type FROM t_dictionary_entry " +
            "WHERE is_deleted = 0 " +
            "ORDER BY dictionary_type")
    List<String> selectDistinctDictionaryTypes();
    
    /**
     * 分页查询字典条目
     * 具体实现在对应的XML文件中
     */
    Page<DictionaryEntryDO> selectPageByConditions(@Param("page") Page<DictionaryEntryDO> page,
                                                  @Param("dictionaryType") String dictionaryType,
                                                  @Param("tenant") String tenant,
                                                  @Param("channel") String channel,
                                                  @Param("domain") String domain,
                                                  @Param("entryKey") String entryKey,
                                                  @Param("status") String status,
                                                  @Param("createdBy") String createdBy);
    
    /**
     * 按条件查询字典条目列表
     * 具体实现在对应的XML文件中
     */
    List<DictionaryEntryDO> selectListByConditions(@Param("dictionaryType") String dictionaryType,
                                                  @Param("tenant") String tenant,
                                                  @Param("channel") String channel,
                                                  @Param("domain") String domain,
                                                  @Param("entryKeyPattern") String entryKeyPattern,
                                                  @Param("status") String status,
                                                  @Param("limit") Integer limit);
}