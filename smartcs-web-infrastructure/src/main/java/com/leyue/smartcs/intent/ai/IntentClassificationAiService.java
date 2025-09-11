package com.leyue.smartcs.intent.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 意图分类AI服务接口
 * 基于LangChain4j框架的声明式AI服务，提供意图分类能力
 * 
 * @author Claude
 */
public interface IntentClassificationAiService {
    
    /**
     * 单文本意图分类
     * 
     * @param text 待分类文本
     * @param intentList 可用意图列表，格式："{intentCode}:{intentName}"
     * @param channel 渠道信息
     * @param tenant 租户信息
     * @return JSON格式分类结果
     */
    @SystemMessage("""
        你是一个专业的两级意图分类系统。根据用户输入的文本，进行层次化意图识别。
        
        **分类流程：**
        1. 首先识别用户文本属于哪个意图目录（CATALOG_xxx）
        2. 然后在该目录下识别具体意图
        3. 计算两级分类的置信度分数
        4. 如果没有合适的意图匹配，返回"UNKNOWN"
        
        **意图层次结构：**
        {{intentList}}
        
        **分类规则：**
        - 目录级别：CATALOG_xxx代表意图分类（如CATALOG_customer_service:客服服务）
        - 意图级别：具体意图编码（如greeting:问候、complaint:投诉）
        - 优先考虑用户文本的主要目的和上下文场景
        - 置信度应该综合考虑目录匹配和具体意图匹配的准确性
        
        **渠道：** {{channel}}
        **租户：** {{tenant}}
        
        **输出格式（严格JSON）：**
        {
          "intentCode": "匹配的具体意图编码或UNKNOWN",
          "intentName": "具体意图名称",
          "catalogCode": "匹配的目录编码",
          "catalogName": "目录名称",
          "confidenceScore": 0.95,
          "catalogConfidence": 0.98,
          "intentConfidence": 0.92,
          "reasonCode": "MATCH|LOW_CONFIDENCE|NO_MATCH",
          "reasoning": "两级分类的详细推理过程，包括目录选择和意图识别依据"
        }
        """)
    @UserMessage("{{text}}")
    String classifyIntent(@V("text") String text, 
                         @V("intentList") String intentList,
                         @V("channel") String channel, 
                         @V("tenant") String tenant);

    /**
     * 单文本意图分类（历史感知）
     * 增强：引入上一轮意图/已知槽位/最近消息，做多轮消歧。
     */
    @SystemMessage("""
        你是一个历史感知的两级意图分类系统。根据用户输入和会话上下文，进行层次化意图识别。
        
        **上下文线索（可为空）：**
        - 上一轮意图：{{lastIntentCode}}
        - 已知槽位（JSON）：{{lastSlots}}
        - 最近消息（拼接）：{{recentMessages}}
        
        **分类流程：**
        1. 首先识别用户文本属于哪个意图目录（CATALOG_xxx）
        2. 然后在该目录下识别具体意图
        3. 结合历史线索提高稳定性（连续话题应倾向于同一意图，除非当前文本明显不同）
        4. 计算两级分类的置信度分数
        5. 如果没有合适的意图匹配，返回"UNKNOWN"
        
        **意图层次结构：**
        {{intentList}}
        
        **输出格式（严格JSON）：**
        {
          "intentCode": "...",
          "intentName": "...",
          "catalogCode": "...",
          "catalogName": "...",
          "confidenceScore": 0.95,
          "catalogConfidence": 0.98,
          "intentConfidence": 0.92,
          "reasonCode": "MATCH|LOW_CONFIDENCE|NO_MATCH",
          "reasoning": "结合历史线索的推理过程"
        }
        """)
    @UserMessage("{{text}}")
    String classifyIntentWithHistory(@V("text") String text,
                                     @V("intentList") String intentList,
                                     @V("channel") String channel,
                                     @V("tenant") String tenant,
                                     @V("lastIntentCode") String lastIntentCode,
                                     @V("lastSlots") String lastSlots,
                                     @V("recentMessages") String recentMessages);
    
    /**
     * 批量文本意图分类
     * 
     * @param textList 待分类文本列表，格式：文本1\n文本2\n...
     * @param intentList 可用意图列表
     * @param channel 渠道信息
     * @param tenant 租户信息
     * @return JSON格式批量分类结果
     */
    @SystemMessage("""
        你是一个专业的两级意图分类系统。对输入的多个文本进行批量层次化意图识别。
        
        **分类流程：**
        1. 对每个文本首先识别意图目录（CATALOG_xxx）
        2. 然后识别该目录下的具体意图
        3. 计算两级分类的置信度分数
        4. 如果没有合适的意图匹配，返回"UNKNOWN"
        
        **意图层次结构：**
        {{intentList}}
        
        **分类规则：**
        - 目录级别：CATALOG_xxx代表意图分类（如CATALOG_customer_service:客服服务）
        - 意图级别：具体意图编码（如greeting:问候、complaint:投诉）
        - 保持批量分类的一致性标准
        - 置信度应该综合考虑目录匹配和具体意图匹配的准确性
        
        **渠道：** {{channel}}
        **租户：** {{tenant}}
        
        **输出格式（严格JSON数组）：**
        [
          {
            "text": "原始文本",
            "intentCode": "匹配的具体意图编码或UNKNOWN",
            "intentName": "具体意图名称",
            "catalogCode": "匹配的目录编码",
            "catalogName": "目录名称",
            "confidenceScore": 0.95,
            "catalogConfidence": 0.98,
            "intentConfidence": 0.92,
            "reasonCode": "MATCH|LOW_CONFIDENCE|NO_MATCH",
            "reasoning": "两级分类推理过程"
          }
        ]
        """)
    @UserMessage("{{textList}}")
    String classifyIntentsBatch(@V("textList") String textList,
                               @V("intentList") String intentList,
                               @V("channel") String channel,
                               @V("tenant") String tenant);
    
    /**
     * 意图相似度计算
     * 
     * @param text 输入文本
     * @param intentCode 意图编码
     * @param intentExamples 意图示例文本
     * @return 相似度分数（0.0-1.0）
     */
    @SystemMessage("""
        你是一个意图相似度计算系统。计算输入文本与指定意图的相似度分数。
        
        **任务要求：**
        1. 分析输入文本的语义特征
        2. 与意图示例进行语义相似度比较
        3. 输出0.0-1.0之间的相似度分数
        
        **意图编码：** {{intentCode}}
        **意图示例：**
        {{intentExamples}}
        
        **输出格式（严格JSON）：**
        {
          "similarityScore": 0.85,
          "reasoning": "相似度计算依据"
        }
        """)
    @UserMessage("{{text}}")
    String calculateSimilarity(@V("text") String text,
                             @V("intentCode") String intentCode,
                             @V("intentExamples") String intentExamples);
}
