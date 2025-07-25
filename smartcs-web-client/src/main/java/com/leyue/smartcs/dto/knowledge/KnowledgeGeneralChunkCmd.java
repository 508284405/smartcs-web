package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 通用文档分块命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeGeneralChunkCmd extends Command {
    
    /**
     * 上传文件
     */
    @NotNull(message = "文件不能为空")
    private String fileUrl;
    
    /**
     * 分块大小（token数）
     */
    @NotNull(message = "分块大小不能为空")
    @Min(value = 50, message = "分块大小不能小于50")
    @Max(value = 5000, message = "分块大小不能大于5000")
    private Integer chunkSize = 500;
    
    /**
     * 分块重叠大小（token数）
     */
    @Min(value = 0, message = "重叠大小不能小于0")
    @Max(value = 1000, message = "重叠大小不能大于1000")
    private Integer overlapSize = 100;
    
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
    private Integer maxChunkSize = 5000;
    
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
    
    /**
     * 是否使用Q&A分段
     */
    private Boolean useQASegmentation = false;
    
    /**
     * Q&A分段语言
     */
    private String qaLanguage = "Chinese";
    
    /**
     * 模型请求
     */
    private ModelRequest modelRequest;
} 