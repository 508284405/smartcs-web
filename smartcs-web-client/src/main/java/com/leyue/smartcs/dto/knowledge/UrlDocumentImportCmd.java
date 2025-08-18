package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通过URL导入文档命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UrlDocumentImportCmd extends Command {

  /** 知识库ID */
  @NotNull(message = "知识库ID不能为空")
  private Long knowledgeBaseId;

  /** 嵌入模型ID（可选，为空将根据知识库默认设置填充） */
  private Long modelId;

  /** 文档URL，仅支持http/https */
  @NotBlank(message = "URL不能为空")
  private String url;

  /** 文档标题（可选，不填则基于文件名推断） */
  private String title;

  /** 文件类型（可选，content-type或扩展名推断失败时可由前端传入） */
  private String fileType;

  /** 文件大小（可选，单位：字节） */
  private Long fileSize;

  /** 原始文件名（可选） */
  private String originalFileName;

  /** 分段模式：general 或 parent_child（为空则使用知识库默认设置） */
  private String segmentMode;

  /** 通用分段设置（可选） */
  @Valid
  private DocumentProcessCmd.SegmentSettings segmentSettings;

  /** 父子分段设置（可选） */
  @Valid
  private DocumentProcessCmd.ParentChildSettings parentChildSettings;

  /** 索引方式（可选） */
  private String indexMethod;

  /** 检索设置（可选） */
  @Valid
  private DocumentProcessCmd.RetrievalSettings retrievalSettings;

  /** 是否使用知识库默认设置补齐缺省项（默认true） */
  private Boolean useKbDefaults = true;
}


