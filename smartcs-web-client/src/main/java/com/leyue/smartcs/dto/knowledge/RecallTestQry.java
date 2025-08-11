package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 召回测试查询参数
 */
@Data
public class RecallTestQry {

  /** 知识库ID */
  @NotNull
  private Long knowledgeBaseId;

  /** 查询文本 */
  @NotBlank
  private String query;

  /** 检索方式：vector/full_text/hybrid */
  @NotBlank
  private String retrievalMethod;

  /** 返回数量，默认10 */
  @Min(1)
  @Max(100)
  private Integer topK;

  /** 分数阈值，默认0 */
  @Min(0)
  @Max(1)
  private Float scoreThreshold;

  /** 是否启用Rerank，仅在hybrid时生效 */
  private Boolean rerankEnabled;
}


