package com.leyue.smartcs.domain.knowledge;


import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
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
     * 状态 uploaded/parsed/vectorized
     */
    private ContentStatusEnum status;

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