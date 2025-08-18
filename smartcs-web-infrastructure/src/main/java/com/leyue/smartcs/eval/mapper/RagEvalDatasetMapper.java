package com.leyue.smartcs.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.eval.dataobject.RagEvalDatasetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG评估数据集Mapper接口
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper
public interface RagEvalDatasetMapper extends BaseMapper<RagEvalDatasetDO> {
    
    /**
     * 根据创建者ID查询数据集列表
     */
    List<RagEvalDatasetDO> selectByCreatorId(@Param("creatorId") Long creatorId);
    
    /**
     * 根据领域和状态查询数据集列表
     */
    List<RagEvalDatasetDO> selectByDomainAndStatus(@Param("domain") String domain, @Param("status") Integer status);
    
    /**
     * 根据标签查询数据集列表
     */
    List<RagEvalDatasetDO> selectByTagsContaining(@Param("tag") String tag);
    
    /**
     * 查询活跃数据集列表（状态为1且未删除）
     */
    List<RagEvalDatasetDO> selectActiveDatasets();
    
    /**
     * 更新数据集的用例计数
     */
    int updateCaseCounts(@Param("datasetId") String datasetId, 
                        @Param("totalCases") Integer totalCases, 
                        @Param("activeCases") Integer activeCases,
                        @Param("updatedAt") Long updatedAt);
}