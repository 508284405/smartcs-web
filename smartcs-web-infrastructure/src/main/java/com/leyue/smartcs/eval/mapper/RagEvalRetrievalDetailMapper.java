package com.leyue.smartcs.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.eval.dataobject.RagEvalRetrievalDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG评估检索详情Mapper接口
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper
public interface RagEvalRetrievalDetailMapper extends BaseMapper<RagEvalRetrievalDetailDO> {
    
    /**
     * 根据运行ID查询检索详情列表
     */
    List<RagEvalRetrievalDetailDO> selectByRunId(@Param("runId") String runId);
    
    /**
     * 根据运行ID和用例ID查询检索详情
     */
    RagEvalRetrievalDetailDO selectByRunIdAndCaseId(@Param("runId") String runId, @Param("caseId") String caseId);
    
    /**
     * 根据用例ID查询检索详情列表
     */
    List<RagEvalRetrievalDetailDO> selectByCaseId(@Param("caseId") String caseId);
    
    /**
     * 查询有错误的检索详情列表
     */
    List<RagEvalRetrievalDetailDO> selectByHasError(@Param("runId") String runId, @Param("hasError") Integer hasError);
    
    /**
     * 删除运行的所有检索详情
     */
    int deleteByRunId(@Param("runId") String runId);
    
    /**
     * 删除指定运行ID和用例ID的检索详情
     */
    int deleteByRunIdAndCaseId(@Param("runId") String runId, @Param("caseId") String caseId);
    
    /**
     * 统计运行的检索详情数量
     */
    Long countByRunId(@Param("runId") String runId);
    
    /**
     * 统计运行中有错误的检索详情数量
     */
    Long countErrorsByRunId(@Param("runId") String runId);
}