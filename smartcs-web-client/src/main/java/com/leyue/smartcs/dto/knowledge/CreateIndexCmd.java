package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 创建RediSearch索引命令
 */
@Data
public class CreateIndexCmd {
    /**
     * 索引名称
     */
    @NotEmpty(message = "索引名称不能为空")
    private String indexName;
    
    /**
     * 前缀
     */
    @NotEmpty(message = "索引前缀不能为空")
    private String prefix;
    
    /**
     * 字段定义
     * 键为字段名，值为字段类型和属性配置
     */
    private Map<String, String> schema;
    
    /**
     * 是否替换现有索引（如存在）
     */
    private boolean replaceIfExists;
} 