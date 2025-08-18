package com.leyue.smartcs.intent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.intent.dataobject.IntentRouteDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 意图路由Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface IntentRouteMapper extends BaseMapper<IntentRouteDO> {
}