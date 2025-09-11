package com.leyue.smartcs.rag.query.pipeline.stages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.dto.intent.SlotDefinitionDTO;
import com.leyue.smartcs.dto.intent.SlotTemplateDTO;
import com.leyue.smartcs.rag.metrics.SlotFillingMetricsCollector;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 槽位填充阶段
 * 负责识别查询中缺失的必填槽位并生成澄清问题
 * 
 * 主要功能：
 * 1. 根据意图识别结果获取对应的槽位模板
 * 2. 分析已抽取的槽位信息
 * 3. 识别缺失的必填槽位
 * 4. 生成澄清问题引导用户补全
 * 5. 控制是否阻断后续检索流程
 * 
 * @author Claude
 */
@Slf4j
@RequiredArgsConstructor
public class SlotFillingStage implements QueryTransformerStage {
    
    private final DictionaryService dictionaryService;
    private final ObjectMapper objectMapper;
    private final SlotFillingMetricsCollector metricsCollector;
    
    // 默认配置常量
    private static final int DEFAULT_MAX_CLARIFICATION_ATTEMPTS = 3;
    private static final double DEFAULT_COMPLETENESS_THRESHOLD = 0.8;
    
    // 槽位提取的正则模式
    private static final Map<String, Pattern> SLOT_EXTRACTION_PATTERNS = createSlotExtractionPatterns();
    
    @Override
    public String getName() {
        return "SlotFillingStage";
    }
    
    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnableSlotFilling();
    }
    
    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            log.debug("输入查询为空，跳过槽位填充处理");
            return Collections.emptyList();
        }
        
        log.debug("开始槽位填充阶段处理: inputCount={}", queries.size());
        
        try {
            List<Query> processedQueries = new ArrayList<>();
            
            for (Query query : queries) {
                try {
                    Query processedQuery = processSlotFilling(context, query);
                    processedQueries.add(processedQuery);
                } catch (Exception e) {
                    log.warn("处理查询槽位填充失败: query={}, error={}", query.text(), e.getMessage());
                    // 降级：返回原查询
                    processedQueries.add(query);
                }
            }
            
            log.debug("槽位填充阶段处理完成: outputCount={}", processedQueries.size());
            return processedQueries;
            
        } catch (Exception e) {
            log.error("槽位填充阶段处理异常", e);
            throw new QueryTransformationException("SlotFillingStage", "槽位填充阶段处理失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理单个查询的槽位填充
     */
    private Query processSlotFilling(QueryContext context, Query query) {
        long startTime = System.currentTimeMillis();
        
        // 1. 获取意图识别结果
        String intentCode = extractIntentCode(context, query);
        if (intentCode == null || intentCode.trim().isEmpty()) {
            // 尝试从会话态获取上一次意图
            intentCode = extractLastIntentFromContext(context);
            if (intentCode == null || intentCode.trim().isEmpty()) {
                log.debug("未找到意图识别结果，跳过槽位填充: query={}", query.text());
                return query;
            }
        }
        
        // 记录查询开始
        metricsCollector.recordQueryStart(intentCode);
        
        // 2. 获取槽位模板
        SlotTemplateDTO template = getSlotTemplate(context, intentCode);
        if (template == null || !template.isSlotFillingActive()) {
            log.debug("未找到槽位模板或未启用槽位填充: intent={}", intentCode);
            return query;
        }
        
        // 3. 提取已有槽位（融合会话态）
        Map<String, Object> extractedSlots = new HashMap<>();
        // 3.1 融合历史已知槽位
        Map<String, Object> historySlots = extractKnownSlotsFromContext(context);
        if (historySlots != null) {
            extractedSlots.putAll(historySlots);
        }
        // 3.2 当前轮从查询中提取
        extractedSlots.putAll(extractSlotsFromQuery(query, template));
        
        // 记录槽位填充激活
        metricsCollector.recordSlotFillingActivated(intentCode, 
                template.getSlotDefinitions() != null ? template.getSlotDefinitions().size() : 0, 0);
        
        // 4. 识别缺失槽位
        List<SlotDefinitionDTO> missingSlots = identifyMissingSlots(template, extractedSlots);
        
        // 5. 检查是否需要澄清
        if (!missingSlots.isEmpty()) {
            // 更新缺失槽位数
            metricsCollector.recordSlotFillingActivated(intentCode, 
                    template.getSlotDefinitions().size(), missingSlots.size());
            
            Query result = handleMissingSlots(context, query, template, missingSlots, extractedSlots);
            
            // 记录处理时间
            long processingTime = System.currentTimeMillis() - startTime;
            metricsCollector.recordProcessingTime(intentCode, processingTime);
            
            return result;
        }
        
        log.debug("所有必填槽位已完整，继续后续处理: intent={}", intentCode);
        
        // 记录处理时间
        long processingTime = System.currentTimeMillis() - startTime;
        metricsCollector.recordProcessingTime(intentCode, processingTime);
        
        return query;
    }
    
    /**
     * 从上下文中提取意图识别结果
     */
    @SuppressWarnings("unchecked")
    private String extractIntentCode(QueryContext context, Query query) {
        Map<String, Object> attributes = context.getAttributes();
        if (attributes == null) {
            return null;
        }
        
        // 尝试从意图识别阶段的结果中获取
        Map<String, Object> intentResults = (Map<String, Object>) attributes.get("intent_extraction");
        if (intentResults != null) {
            Object intent = intentResults.get(query.text());
            if (intent instanceof Map) {
                return (String) ((Map<?, ?>) intent).get("intentCode");
            }
        }
        
        return null;
    }

    /** 从会话上下文中提取上一次的意图编码 */
    @SuppressWarnings("unchecked")
    private String extractLastIntentFromContext(QueryContext context) {
        Map<String, Object> attributes = context.getAttributes();
        if (attributes == null) return null;
        Object intentObj = attributes.get("intent");
        if (intentObj instanceof Map) {
            Object last = ((Map<String, Object>) intentObj).get("lastCode");
            return last != null ? last.toString() : null;
        }
        return null;
    }

    /** 从会话上下文中提取已知槽位（历史） */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractKnownSlotsFromContext(QueryContext context) {
        Map<String, Object> attributes = context.getAttributes();
        if (attributes == null) return null;
        Object slotFilling = attributes.get("slot_filling");
        if (slotFilling instanceof Map) {
            Object extracted = ((Map<String, Object>) slotFilling).get("extracted");
            if (extracted instanceof Map) {
                return new HashMap<>((Map<String, Object>) extracted);
            }
        }
        return null;
    }
    
    /**
     * 获取槽位模板
     */
    private SlotTemplateDTO getSlotTemplate(QueryContext context, String intentCode) {
        try {
            String tenant = (String) context.getAttributes().getOrDefault("tenant", "default");
            String channel = (String) context.getAttributes().getOrDefault("channel", "default");
            String domain = (String) context.getAttributes().getOrDefault("domain", "default");
            
            return dictionaryService.getSlotTemplateByIntent(intentCode, tenant, channel, domain);
        } catch (Exception e) {
            log.warn("获取槽位模板失败: intentCode={}, error={}", intentCode, e.getMessage());
            return null;
        }
    }
    
    /**
     * 从查询中提取槽位信息
     */
    private Map<String, Object> extractSlotsFromQuery(Query query, SlotTemplateDTO template) {
        Map<String, Object> slots = new HashMap<>();
        String queryText = query.text();
        
        if (template.getSlotDefinitions() == null) {
            return slots;
        }
        
        for (SlotDefinitionDTO slotDef : template.getSlotDefinitions()) {
            Object value = extractSlotValue(queryText, slotDef);
            if (value != null) {
                slots.put(slotDef.getName(), value);
            }
        }
        
        return slots;
    }
    
    /**
     * 提取单个槽位的值
     */
    private Object extractSlotValue(String queryText, SlotDefinitionDTO slotDef) {
        // 使用模式匹配提取槽位值
        Pattern pattern = SLOT_EXTRACTION_PATTERNS.get(slotDef.getType());
        if (pattern != null) {
            var matcher = pattern.matcher(queryText);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        // 检查是否在示例中匹配
        if (slotDef.getExamples() != null) {
            for (String example : slotDef.getExamples()) {
                if (queryText.contains(example)) {
                    return example;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 识别缺失的必填槽位
     */
    private List<SlotDefinitionDTO> identifyMissingSlots(SlotTemplateDTO template, Map<String, Object> extractedSlots) {
        return template.getSlotDefinitions().stream()
                .filter(slot -> Boolean.TRUE.equals(slot.getRequired()))
                .filter(slot -> !extractedSlots.containsKey(slot.getName()))
                .sorted(Comparator.comparingInt(slot -> slot.getOrder() != null ? slot.getOrder() : 999))
                .collect(Collectors.toList());
    }
    
    /**
     * 处理缺失槽位的情况
     */
    private Query handleMissingSlots(QueryContext context, Query originalQuery, SlotTemplateDTO template, 
                                   List<SlotDefinitionDTO> missingSlots, Map<String, Object> extractedSlots) {
        
        // 检查澄清次数限制
        int clarificationCount = getClarificationCount(context);
        int maxAttempts = template.getMaxClarificationAttempts() != null ? 
                template.getMaxClarificationAttempts() : DEFAULT_MAX_CLARIFICATION_ATTEMPTS;
        
        if (clarificationCount >= maxAttempts) {
            log.warn("已达到最大澄清次数限制: count={}, max={}", clarificationCount, maxAttempts);
            return originalQuery; // 不再澄清，继续处理
        }
        
        // 生成澄清问题
        List<String> clarificationQuestions = generateClarificationQuestions(template, missingSlots);
        
        // 记录需要澄清
        metricsCollector.recordClarificationRequired(template.getIntentCode(), 
                missingSlots.size(), clarificationQuestions.size());
        
        // 将槽位填充信息写入上下文
        Map<String, Object> slotFillingInfo = new HashMap<>();
        slotFillingInfo.put("required", true);
        slotFillingInfo.put("intent", template.getIntentCode());
        slotFillingInfo.put("missing", missingSlots.stream().map(SlotDefinitionDTO::getName).collect(Collectors.toList()));
        slotFillingInfo.put("questions", clarificationQuestions);
        slotFillingInfo.put("extracted", extractedSlots);
        slotFillingInfo.put("template", template.getTemplateId());
        
        // 检查是否阻断检索
        boolean blockRetrieval = Boolean.TRUE.equals(template.getBlockRetrievalOnMissing());
        if (blockRetrieval) {
            slotFillingInfo.put("block_retrieval", true);
            metricsCollector.recordRetrievalBlocked(template.getIntentCode(), "缺失必填槽位");
            log.info("槽位缺失，阻断后续检索: intent={}, missing={}", template.getIntentCode(), 
                    missingSlots.stream().map(SlotDefinitionDTO::getName).collect(Collectors.toList()));
        }
        
        context.getAttributes().put("slot_filling", slotFillingInfo);
        
        // 更新澄清计数
        setClarificationCount(context, clarificationCount + 1);
        
        log.info("识别到缺失槽位，需要澄清: intent={}, missing={}, questions={}", 
                template.getIntentCode(), missingSlots.size(), clarificationQuestions.size());
        
        return originalQuery; // 返回原查询，由对话层处理澄清逻辑
    }
    
    /**
     * 生成澄清问题
     */
    private List<String> generateClarificationQuestions(SlotTemplateDTO template, List<SlotDefinitionDTO> missingSlots) {
        List<String> questions = new ArrayList<>();
        
        for (SlotDefinitionDTO slot : missingSlots) {
            String question = generateSlotQuestion(template, slot);
            if (question != null && !question.trim().isEmpty()) {
                questions.add(question);
            }
        }
        
        return questions;
    }
    
    /**
     * 为单个槽位生成问题
     */
    private String generateSlotQuestion(SlotTemplateDTO template, SlotDefinitionDTO slot) {
        // 1. 优先使用模板中的澄清问题
        if (template.getClarificationTemplates() != null) {
            String customQuestion = template.getClarificationTemplates().get(slot.getName());
            if (customQuestion != null && !customQuestion.trim().isEmpty()) {
                return customQuestion;
            }
        }
        
        // 2. 使用槽位定义中的提示信息
        if (slot.getHint() != null && !slot.getHint().trim().isEmpty()) {
            return "请提供" + slot.getLabel() + "：" + slot.getHint();
        }
        
        // 3. 根据槽位类型生成默认问题
        String baseQuestion = "请提供" + (slot.getLabel() != null ? slot.getLabel() : slot.getName());
        
        if (slot.getExamples() != null && !slot.getExamples().isEmpty()) {
            String examples = String.join("、", slot.getExamples().subList(0, Math.min(3, slot.getExamples().size())));
            return baseQuestion + "（例如：" + examples + "）";
        }
        
        return baseQuestion;
    }
    
    /**
     * 获取当前会话的澄清次数
     */
    @SuppressWarnings("unchecked")
    private int getClarificationCount(QueryContext context) {
        Map<String, Object> slotFillingInfo = (Map<String, Object>) context.getAttributes().get("slot_filling");
        if (slotFillingInfo != null) {
            Object count = slotFillingInfo.get("clarification_count");
            if (count instanceof Integer) {
                return (Integer) count;
            }
        }
        return 0;
    }
    
    /**
     * 设置澄清次数
     */
    @SuppressWarnings("unchecked")
    private void setClarificationCount(QueryContext context, int count) {
        Map<String, Object> slotFillingInfo = (Map<String, Object>) context.getAttributes().get("slot_filling");
        if (slotFillingInfo == null) {
            slotFillingInfo = new HashMap<>();
            context.getAttributes().put("slot_filling", slotFillingInfo);
        }
        slotFillingInfo.put("clarification_count", count);
    }
    
    /**
     * 创建槽位提取的正则表达式模式
     */
    private static Map<String, Pattern> createSlotExtractionPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // 数字类型
        patterns.put("NUMBER", Pattern.compile("(\\d+(?:\\.\\d+)?)"));
        patterns.put("INTEGER", Pattern.compile("(\\d+)"));
        
        // 日期时间类型
        patterns.put("DATE", Pattern.compile("(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2})"));
        patterns.put("TIME", Pattern.compile("(\\d{1,2}:\\d{2}(?::\\d{2})?)"));
        
        // 邮箱
        patterns.put("EMAIL", Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"));
        
        // 手机号
        patterns.put("PHONE", Pattern.compile("(1[3-9]\\d{9})"));
        
        // URL
        patterns.put("URL", Pattern.compile("(https?://[^\\s]+)"));
        
        return patterns;
    }
}
