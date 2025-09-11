package com.leyue.smartcs.intent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.intent.dataobject.IntentCatalogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 意图目录Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface IntentCatalogMapper extends BaseMapper<IntentCatalogDO> {
    
    /**
     * 统计指定目录下的意图数量
     * @param catalogId 目录ID
     * @return 意图数量
     */
    @Select("SELECT COUNT(*) FROM t_intent WHERE catalog_id = #{catalogId} AND is_deleted = 0")
    int countIntentsByCatalogId(@Param("catalogId") Long catalogId);
}