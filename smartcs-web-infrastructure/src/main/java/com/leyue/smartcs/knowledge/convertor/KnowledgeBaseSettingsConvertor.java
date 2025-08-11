package com.leyue.smartcs.knowledge.convertor;

import com.leyue.smartcs.domain.knowledge.KnowledgeBaseSettings;
import com.leyue.smartcs.knowledge.dataobject.KnowledgeBaseSettingsDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 知识库设置数据转换器
 */
@Mapper(componentModel = "spring")
public interface KnowledgeBaseSettingsConvertor {
    
    /**
     * DO转Domain
     * @param settingsDO 数据对象
     * @return 领域对象
     */
    @Mapping(target = "vectorSearch", expression = "java(buildVectorSearchSettings(settingsDO))")
    @Mapping(target = "fullTextSearch", expression = "java(buildFullTextSearchSettings(settingsDO))")
    @Mapping(target = "hybridSearch", expression = "java(buildHybridSearchSettings(settingsDO))")
    KnowledgeBaseSettings toDomain(KnowledgeBaseSettingsDO settingsDO);
    
    /**
     * Domain转DO
     * @param settings 领域对象
     * @return 数据对象
     */
    @Mapping(target = "vectorEnabled", expression = "java(settings.getVectorSearch() != null ? settings.getVectorSearch().getEnabled() : null)")
    @Mapping(target = "vectorTopK", expression = "java(settings.getVectorSearch() != null ? settings.getVectorSearch().getTopK() : null)")
    @Mapping(target = "vectorScoreThreshold", expression = "java(settings.getVectorSearch() != null ? settings.getVectorSearch().getScoreThreshold() : null)")
    @Mapping(target = "fullTextEnabled", expression = "java(settings.getFullTextSearch() != null ? settings.getFullTextSearch().getEnabled() : null)")
    @Mapping(target = "hybridEnabled", expression = "java(settings.getHybridSearch() != null ? settings.getHybridSearch().getEnabled() : null)")
    @Mapping(target = "hybridRerankEnabled", expression = "java(settings.getHybridSearch() != null ? settings.getHybridSearch().getRerankEnabled() : null)")
    @Mapping(target = "isDeleted", ignore = true)
    KnowledgeBaseSettingsDO toDO(KnowledgeBaseSettings settings);
    
    /**
     * 构建向量搜索设置
     */
    default KnowledgeBaseSettings.VectorSearchSettings buildVectorSearchSettings(KnowledgeBaseSettingsDO settingsDO) {
        if (settingsDO == null) {
            return null;
        }
        return KnowledgeBaseSettings.VectorSearchSettings.builder()
            .enabled(settingsDO.getVectorEnabled())
            .topK(settingsDO.getVectorTopK())
            .scoreThreshold(settingsDO.getVectorScoreThreshold())
            .build();
    }
    
    /**
     * 构建全文搜索设置
     */
    default KnowledgeBaseSettings.FullTextSearchSettings buildFullTextSearchSettings(KnowledgeBaseSettingsDO settingsDO) {
        if (settingsDO == null) {
            return null;
        }
        return KnowledgeBaseSettings.FullTextSearchSettings.builder()
            .enabled(settingsDO.getFullTextEnabled())
            .build();
    }
    
    /**
     * 构建混合搜索设置
     */
    default KnowledgeBaseSettings.HybridSearchSettings buildHybridSearchSettings(KnowledgeBaseSettingsDO settingsDO) {
        if (settingsDO == null) {
            return null;
        }
        return KnowledgeBaseSettings.HybridSearchSettings.builder()
            .enabled(settingsDO.getHybridEnabled())
            .rerankEnabled(settingsDO.getHybridRerankEnabled())
            .build();
    }
}