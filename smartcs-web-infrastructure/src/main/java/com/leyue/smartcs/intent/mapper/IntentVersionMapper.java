package com.leyue.smartcs.intent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.intent.dataobject.IntentVersionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 意图版本Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface IntentVersionMapper extends BaseMapper<IntentVersionDO> {
    
    /**
     * 获取意图的下一个版本号
     * @param intentId 意图ID
     * @return 下一个版本号
     */
    String getNextVersionNumber(@Param("intentId") Long intentId);
}