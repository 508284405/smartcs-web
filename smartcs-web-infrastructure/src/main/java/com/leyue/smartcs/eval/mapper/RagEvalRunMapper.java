package com.leyue.smartcs.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.eval.dataobject.RagEvalRunDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG评估运行记录Mapper接口
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper
public interface RagEvalRunMapper extends BaseMapper<RagEvalRunDO> {
    
    /**
     * 根据数据集ID查询运行列表
     */
    List<RagEvalRunDO> selectByDatasetId(@Param("datasetId") String datasetId);
    
    /**
     * 根据应用ID查询运行列表
     */
    List<RagEvalRunDO> selectByAppId(@Param("appId") Long appId);
    
    /**
     * 根据模型ID查询运行列表
     */
    List<RagEvalRunDO> selectByModelId(@Param("modelId") Long modelId);
    
    /**
     * 根据运行类型查询运行列表
     */
    List<RagEvalRunDO> selectByRunType(@Param("runType") String runType);
    
    /**
     * 根据状态查询运行列表
     */
    List<RagEvalRunDO> selectByStatus(@Param("status") String status);
    
    /**
     * 根据发起人ID查询运行列表
     */
    List<RagEvalRunDO> selectByInitiatorId(@Param("initiatorId") Long initiatorId);
    
    /**
     * 查询正在运行的评估列表
     */
    List<RagEvalRunDO> selectRunningRuns();
    
    /**
     * 查询数据集的最近运行记录
     */
    List<RagEvalRunDO> selectRecentRunsByDatasetId(@Param("datasetId") String datasetId, @Param("limit") int limit);
    
    /**
     * 查询应用的最近运行记录
     */
    List<RagEvalRunDO> selectRecentRunsByAppId(@Param("appId") Long appId, @Param("limit") int limit);
    
    /**
     * 统计数据集的运行次数
     */
    Long countByDatasetId(@Param("datasetId") String datasetId);
    
    /**
     * 统计模型的运行次数
     */
    Long countByModelId(@Param("modelId") Long modelId);
    
    /**
     * 更新运行状态
     */
    int updateStatus(@Param("runId") String runId, @Param("status") String status);
    
    /**
     * 更新运行进度
     */
    int updateProgress(@Param("runId") String runId, 
                      @Param("completedCases") Integer completedCases, 
                      @Param("failedCases") Integer failedCases);
}