package com.leyue.smartcs.ltm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.ltm.dataobject.EpisodicMemoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EpisodicMemoryMapper extends BaseMapper<EpisodicMemoryDO> {

    List<Long> selectNeedingConsolidationUserIds(@Param("minImportanceScore") double minImportanceScore,
                                                 @Param("startingAfterUserId") Long startingAfterUserId,
                                                 @Param("limit") int limit);
}
