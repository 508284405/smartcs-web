package com.leyue.smartcs.intent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.intent.dataobject.IntentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 意图Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface IntentMapper extends BaseMapper<IntentDO> {
}