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
     * 状态 uploaded/parsed/vectorized/enabled/disabled
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
     * 检查内容是否可以处理
     *
     * @return 是否可处理
     */
    public boolean canProcess() {
        return this.fileUrl != null && !this.fileUrl.isEmpty() && "uploaded".equals(this.status);
    }

    /**
     * 检查内容是否已解析
     *
     * @return 是否已解析
     */
    public boolean isParsed() {
        return this.status == ContentStatusEnum.PARSED || this.status == ContentStatusEnum.VECTORIZED;
    }

    /**
     * 检查内容是否已向量化
     *
     * @return 是否已向量化
     */
    public boolean isVectorized() {
        return this.status == ContentStatusEnum.VECTORIZED;
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
     * 标记为已解析状态
     */
    public void markAsParsed() {
        this.status = ContentStatusEnum.PARSED;
    }

    /**
     * 标记为已向量化状态
     */
    public void markAsVectorized() {
        this.status = ContentStatusEnum.VECTORIZED;
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
} 