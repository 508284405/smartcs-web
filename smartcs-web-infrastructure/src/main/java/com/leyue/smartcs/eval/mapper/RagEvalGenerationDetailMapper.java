package com.leyue.smartcs.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.eval.dataobject.RagEvalGenerationDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG评估生成详情Mapper接口
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper
public interface RagEvalGenerationDetailMapper extends BaseMapper<RagEvalGenerationDetailDO> {
    
    /**
     * 根据运行ID查询生成详情列表
     */
    List<RagEvalGenerationDetailDO> selectByRunId(@Param("runId") String runId);
    
    /**
     * 根据运行ID和用例ID查询生成详情
     */
    RagEvalGenerationDetailDO selectByRunIdAndCaseId(@Param("runId") String runId, @Param("caseId") String caseId);
    
    /**
     * 根据用例ID查询生成详情列表
     */
    List<RagEvalGenerationDetailDO> selectByCaseId(@Param("caseId") String caseId);
    
    /**
     * 查询有错误的生成详情列表
     */
    List<RagEvalGenerationDetailDO> selectByHasError(@Param("runId") String runId, @Param("hasError") Integer hasError);
    
    /**
     * 查询检测到幻觉的生成详情列表
     */
    List<RagEvalGenerationDetailDO> selectByHallucinationDetected(@Param("runId") String runId, @Param("hallucinationDetected") Integer hallucinationDetected);
    
    /**
     * 删除运行的所有生成详情
     */
    int deleteByRunId(@Param("runId") String runId);
    
    /**
     * 删除指定运行ID和用例ID的生成详情
     */
    int deleteByRunIdAndCaseId(@Param("runId") String runId, @Param("caseId") String caseId);
    
    /**
     * 统计运行的生成详情数量
     */
    Long countByRunId(@Param("runId") String runId);
    
    /**
     * 统计运行中有错误的生成详情数量
     */
    Long countErrorsByRunId(@Param("runId") String runId);
    
    /**
     * 统计运行中检测到幻觉的生成详情数量
     */
    Long countHallucinationsByRunId(@Param("runId") String runId);
}