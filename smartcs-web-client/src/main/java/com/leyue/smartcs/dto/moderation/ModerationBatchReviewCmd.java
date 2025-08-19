package com.leyue.smartcs.dto.moderation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量审核命令
 *
 * @author Claude
 */
@Data
public class ModerationBatchReviewCmd {

    /**
     * 记录ID列表
     */
    @NotEmpty(message = "记录ID列表不能为空")
    private List<String> recordIds;

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
     * 是否覆盖已审核的记录
     */
    private Boolean overrideExisting = false;
}