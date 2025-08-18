package com.leyue.smartcs.dto.moderation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核命令
 *
 * @author Claude
 */
@Data
public class ModerationReviewCmd {

    /**
     * 记录ID
     */
    @NotBlank(message = "记录ID不能为空")
    private String recordId;

    /**
     * 审核结果
     */
    @NotBlank(message = "审核结果不能为空")
    private String result;

    /**
     * 处理动作
     */
    @NotBlank(message = "处理动作不能为空")
    private String action;

    /**
     * 审核备注
     */
    private String reviewNotes;

    /**
     * 审核员ID
     */
    private String reviewerId;

    /**
     * 参考案例ID
     */
    private String referenceCase;
}