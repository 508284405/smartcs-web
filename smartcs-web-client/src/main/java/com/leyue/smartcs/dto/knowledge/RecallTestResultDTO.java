package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

/**
 * 召回测试结果
 */
@Data
public class RecallTestResultDTO {
  private Long chunkId;
  private Long contentId;
  private String content;
  private Float score;
  private Object metadata;
  private String docTitle;
  private Integer chunkIndex;
}


