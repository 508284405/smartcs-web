package com.leyue.smartcs.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.eval.dataobject.RagEvalMetricDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG评估指标汇总Mapper接口
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper
public interface RagEvalMetricMapper extends BaseMapper<RagEvalMetricDO> {
    
    /**
     * 根据运行ID查询所有指标
     */
    List<RagEvalMetricDO> selectByRunId(@Param("runId") String runId);
    
    /**
     * 根据运行ID和指标类别查询指标
     */
    RagEvalMetricDO selectByRunIdAndCategory(@Param("runId") String runId, @Param("category") String category);
    
    /**
     * 根据指标类别查询指标列表
     */
    List<RagEvalMetricDO> selectByCategory(@Param("category") String category);
    
    /**
     * 查询时间范围内的指标列表
     */
    List<RagEvalMetricDO> selectByTimeRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);
    
    /**
     * 删除运行的所有指标
     */
    int deleteByRunId(@Param("runId") String runId);
    
    /**
     * 删除指定运行ID和类别的指标
     */
    int deleteByRunIdAndCategory(@Param("runId") String runId, @Param("category") String category);
}