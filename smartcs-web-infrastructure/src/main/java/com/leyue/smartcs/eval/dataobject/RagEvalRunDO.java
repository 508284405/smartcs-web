package com.leyue.smartcs.eval.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * RAG评估运行记录数据对象
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_rag_eval_run", autoResultMap = true)
public class RagEvalRunDO extends BaseDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 运行唯一标识符
     */
    private String runId;
    
    /**
     * 使用的数据集ID
     */
    private String datasetId;
    
    /**
     * 关联的AI应用ID
     */
    private Long appId;
    
    /**
     * 使用的模型提供商ID
     */
    private Long providerId;
    
    /**
     * 使用的模型ID
     */
    private Long modelId;
    
    /**
     * 运行名称
     */
    private String runName;
    
    /**
     * 运行描述
     */
    private String runDescription;
    
    /**
     * 运行类型：retrieval, generation, e2e, ab_test
     */
    private String runType;
    
    /**
     * 评估模式：offline, online
     */
    private String evaluationMode;
    
    /**
     * RAG配置快照（检索、重排序、生成参数）
     */
    private String ragConfigSnapshot;
    
    /**
     * 模型配置快照
     */
    private String modelConfigSnapshot;
    
    /**
     * 选择的评估指标列表
     */
    private String selectedMetrics;
    
    /**
     * 总测试用例数
     */
    private Integer totalCases;
    
    /**
     * 已完成用例数
     */
    private Integer completedCases;
    
    /**
     * 失败用例数
     */
    private Integer failedCases;
    
    /**
     * 状态：pending, running, completed, failed, cancelled
     */
    private String status;
    
    /**
     * 开始时间（毫秒时间戳）
     */
    private Long startTime;
    
    /**
     * 结束时间（毫秒时间戳）
     */
    private Long endTime;
    
    /**
     * 运行时长（毫秒）
     */
    private Long durationMs;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 进度信息
     */
    private String progressInfo;
    
    /**
     * A/B测试时的基准运行ID
     */
    private String comparisonBaselineRunId;
    
    /**
     * 发起人用户ID
     */
    private Long initiatorId;
    
    /**
     * 发起人姓名
     */
    private String initiatorName;
    
    /**
     * 扩展元数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;
}