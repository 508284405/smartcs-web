package com.leyue.smartcs.eval.convertor;

import com.leyue.smartcs.domain.eval.RagEvalDataset;
import com.leyue.smartcs.eval.dataobject.RagEvalDatasetDO;
import org.mapstruct.Mapper;

/**
 * RAG评估数据集转换器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface RagEvalDatasetConvertor {
    
    /**
     * DO转领域对象
     * 
     * @param datasetDO 数据对象
     * @return 领域对象
     */
    RagEvalDataset toDomain(RagEvalDatasetDO datasetDO);
    
    /**
     * 领域对象转DO
     * 
     * @param dataset 领域对象
     * @return 数据对象
     */
    RagEvalDatasetDO toDO(RagEvalDataset dataset);

    RagEvalDatasetDO toDataObject(RagEvalDataset dataset);

    RagEvalDataset toDomainObject(RagEvalDatasetDO datasetDO);
}