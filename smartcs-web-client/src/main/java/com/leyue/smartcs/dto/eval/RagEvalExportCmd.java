package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估结果导出命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalExportCmd {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 导出格式：excel, csv, json, pdf
     */
    private String exportFormat;
    
    /**
     * 是否包含详细数据
     */
    private Boolean includeDetailedData;
    
    /**
     * 是否包含图表
     */
    private Boolean includeCharts;
    
    /**
     * 是否包含原始查询和回答
     */
    private Boolean includeRawData;
    
    /**
     * 导出的数据类型列表
     */
    private List<String> dataTypes;
    
    /**
     * 文件名称模板
     */
    private String fileNameTemplate;
    
    /**
     * 是否压缩文件
     */
    private Boolean compressFile;
}
