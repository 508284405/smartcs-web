package com.leyue.smartcs.domain.knowledge;


import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.enums.SegmentMode;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识内容领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 所属知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容类型 document/audio/video
     */
    private String contentType;

    /**
     * 原始文件地址
     */
    private String fileUrl;

    /**
     * 文件扩展名
     */
    private String fileType;

    /**
     * 提取后的原始文本
     */
    private String textExtracted;

    /**
     * 状态 enabled/disabled
     */
    private ContentStatusEnum status;

    /**
     * 分段模式 general/parent_child
     */
    private SegmentMode segmentMode;

    /**
     * 字符数
     */
    private Long charCount;

    /**
     * 召回次数
     */
    private Long recallCount;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 元数据信息（JSON格式）
     */
    private String metadata;

    /**
     * 原始文件名称
     */
    private String originalFileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 来源 upload/api/import
     */
    private String source;

    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;

    /**
     * 向量化时间（毫秒）
     */
    private Long embeddingTime;

    /**
     * 嵌入成本（tokens）
     */
    private Long embeddingCost;

    /**
     * 平均段落长度
     */
    private Integer averageChunkLength;

    /**
     * 段落数量
     */
    private Integer chunkCount;

    /**
     * 处理状态 processing/success/failed
     */
    private String processingStatus;

    /**
     * 处理错误信息
     */
    private String processingErrorMessage;

    /**
     * 检查内容是否可以处理
     *
     * @return 是否可处理
     */
    public boolean canProcess() {
        return this.fileUrl != null && !this.fileUrl.isEmpty();
    }

    /**
     * 检查内容是否已处理完成（启用或禁用状态）
     *
     * @return 是否已处理完成
     */
    public boolean isProcessed() {
        return this.status == ContentStatusEnum.ENABLED || this.status == ContentStatusEnum.DISABLED;
    }

    /**
     * 检查内容是否已启用
     *
     * @return 是否已启用
     */
    public boolean isEnabled() {
        return this.status == ContentStatusEnum.ENABLED;
    }

    /**
     * 检查内容是否已禁用
     *
     * @return 是否已禁用
     */
    public boolean isDisabled() {
        return this.status == ContentStatusEnum.DISABLED;
    }


    /**
     * 启用内容
     */
    public void enable() {
        if (this.status != null && this.status.canTransitionTo(ContentStatusEnum.ENABLED)) {
            this.status = ContentStatusEnum.ENABLED;
        } else {
            throw new IllegalStateException("当前状态不能转换为启用状态");
        }
    }

    /**
     * 禁用内容
     */
    public void disable() {
        if (this.status != null && this.status.canTransitionTo(ContentStatusEnum.DISABLED)) {
            this.status = ContentStatusEnum.DISABLED;
        } else {
            throw new IllegalStateException("当前状态不能转换为禁用状态");
        }
    }

    /**
     * 增加召回次数
     */
    public void incrementRecallCount() {
        this.recallCount = (this.recallCount == null ? 0 : this.recallCount) + 1;
    }

    /**
     * 检查是否为文档类型
     *
     * @return 是否为文档
     */
    public boolean isDocument() {
        return "document".equals(this.contentType);
    }

    /**
     * 检查是否为音频类型
     *
     * @return 是否为音频
     */
    public boolean isAudio() {
        return "audio".equals(this.contentType);
    }

    /**
     * 检查是否为视频类型
     *
     * @return 是否为视频
     */
    public boolean isVideo() {
        return "video".equals(this.contentType);
    }

    /**
     * 检查标题是否有效
     *
     * @return 是否有效
     */
    public boolean isValidTitle() {
        return this.title != null && !this.title.trim().isEmpty() && this.title.length() <= 256;
    }

    /**
     * 设置处理成功状态
     */
    public void setProcessingSuccess() {
        this.processingStatus = "success";
        this.processingErrorMessage = null;
    }

    /**
     * 设置处理失败状态
     */
    public void setProcessingFailed(String errorMessage) {
        this.processingStatus = "failed";
        this.processingErrorMessage = errorMessage;
    }

    /**
     * 设置处理中状态
     */
    public void setProcessingInProgress() {
        this.processingStatus = "processing";
        this.processingErrorMessage = null;
    }

    /**
     * 计算召回率百分比
     *
     * @return 召回率百分比
     */
    public Double getRecallRate() {
        if (this.chunkCount == null || this.chunkCount == 0) {
            return 0.0;
        }
        return (this.recallCount != null ? this.recallCount : 0) * 100.0 / this.chunkCount;
    }
} 