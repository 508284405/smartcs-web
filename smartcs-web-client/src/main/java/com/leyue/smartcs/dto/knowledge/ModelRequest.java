package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

@Data
public class ModelRequest {
    /** 模型ID */
    private Long modelId;
    /** 模型名称 */
    private String modelName;
    /** 温度 */
    private Double temperature = 0.5;
    /** topP */
    private Double topP;
    /** topK */
    private Integer topK;
    /** 频率惩罚 */
    private Double frequencyPenalty = 0.0;
    /** 存在惩罚 */
    private Double presencePenalty = 0.0;
    /** 最大输出token */
    private Integer maxOutputTokens = 1024;
}
