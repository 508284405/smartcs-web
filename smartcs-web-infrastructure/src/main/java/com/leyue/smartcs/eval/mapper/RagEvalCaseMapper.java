package com.leyue.smartcs.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.eval.dataobject.RagEvalCaseDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG评估测试用例Mapper接口
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper
public interface RagEvalCaseMapper extends BaseMapper<RagEvalCaseDO> {
    
    /**
     * 根据数据集ID查询测试用例列表
     */
    List<RagEvalCaseDO> selectByDatasetId(@Param("datasetId") String datasetId);
    
    /**
     * 根据数据集ID和状态查询测试用例列表
     */
    List<RagEvalCaseDO> selectByDatasetIdAndStatus(@Param("datasetId") String datasetId, @Param("status") Integer status);
    
    /**
     * 根据难度标签查询测试用例列表
     */
    List<RagEvalCaseDO> selectByDifficultyTag(@Param("difficultyTag") String difficultyTag);
    
    /**
     * 根据类别查询测试用例列表
     */
    List<RagEvalCaseDO> selectByCategory(@Param("category") String category);
    
    /**
     * 根据查询类型查询测试用例列表
     */
    List<RagEvalCaseDO> selectByQueryType(@Param("queryType") String queryType);
    
    /**
     * 统计数据集的测试用例数量
     */
    Long countByDatasetId(@Param("datasetId") String datasetId);
    
    /**
     * 统计数据集的活跃测试用例数量
     */
    Long countActiveByDatasetId(@Param("datasetId") String datasetId);
    
    /**
     * 批量逻辑删除测试用例
     */
    int batchLogicDelete(@Param("caseIds") List<String> caseIds);
}