package com.leyue.smartcs.dto.moderation;

import lombok.Data;

/**
 * 审核记录导出命令
 *
 * @author Claude
 */
@Data
public class ModerationRecordExportCmd {

    /**
     * 导出格式
     */
    private String format = "EXCEL";

    /**
     * 查询条件（复用分页查询对象）
     */
    private ModerationRecordPageQry queryCondition;

    /**
     * 导出字段列表
     */
    private String[] fields;

    /**
     * 文件名前缀
     */
    private String filenamePrefix;

    /**
     * 是否包含敏感信息
     */
    private Boolean includeSensitive = false;

    /**
     * 最大导出数量限制
     */
    private Integer maxRecords = 10000;
}