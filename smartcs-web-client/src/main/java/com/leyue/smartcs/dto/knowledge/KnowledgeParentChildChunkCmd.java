package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 父子文档分块命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeParentChildChunkCmd extends Command {
    
    /**
     * 上传文件
     */
    @NotNull(message = "文件不能为空")
    private String fileUrl;
    
    /**
     * 父块大小（token数）
     */
    @NotNull(message = "父块大小不能为空")
    @Min(value = 100, message = "父块大小不能小于100")
    @Max(value = 10000, message = "父块大小不能大于10000")
    private Integer parentChunkSize = 1000;
    
    /**
     * 子块大小（token数）
     */
    @NotNull(message = "子块大小不能为空")
    @Min(value = 50, message = "子块大小不能小于50")
    @Max(value = 5000, message = "子块大小不能大于5000")
    private Integer childChunkSize = 500;
    
    /**
     * 父块用作上下文的段落数
     */
    @Min(value = 1, message = "上下文段落数不能小于1")
    @Max(value = 10, message = "上下文段落数不能大于10")
    private Integer contextParagraphs = 3;
    
    /**
     * 父块重叠大小（token数）
     */
    @Min(value = 0, message = "父块重叠大小不能小于0")
    @Max(value = 2000, message = "父块重叠大小不能大于2000")
    private Integer parentOverlapSize = 200;
    
    /**
     * 子块重叠大小（token数）
     */
    @Min(value = 0, message = "子块重叠大小不能小于0")
    @Max(value = 1000, message = "子块重叠大小不能大于1000")
    private Integer childOverlapSize = 100;
    
    /**
     * 分块标识符
     */
    private String chunkSeparator = "\n\n";
    
    /**
     * 最小分块大小
     */
    @Min(value = 10, message = "最小分块大小不能小于10")
    private Integer minChunkSize = 10;
    
    /**
     * 最大分块大小
     */
    @Min(value = 100, message = "最大分块大小不能小于100")
    private Integer maxChunkSize = 10000;
    
    /**
     * 是否保留分隔符
     */
    private Boolean keepSeparator = true;
    
    /**
     * 是否替换连续的空格、换行符和制表符
     */
    private Boolean stripWhitespace = true;
    
    /**
     * 是否删除所有URL和电子邮件地址
     */
    private Boolean removeAllUrls = false;
} 