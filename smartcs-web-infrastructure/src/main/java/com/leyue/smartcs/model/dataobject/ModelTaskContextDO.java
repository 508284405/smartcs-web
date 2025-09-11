package com.leyue.smartcs.model.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型上下文数据对象，对应t_model_context表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_model_context")
public class ModelTaskContextDO extends BaseDO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 上下文消息列表（JSON格式）
     */
    private String messages;

    /**
     * 上下文窗口大小
     */
    private Integer contextWindow;

    /**
     * 当前上下文长度
     */
    private Integer currentLength;
}