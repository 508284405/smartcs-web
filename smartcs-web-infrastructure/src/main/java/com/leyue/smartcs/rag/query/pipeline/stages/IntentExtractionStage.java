package com.leyue.smartcs.rag.query.pipeline.stages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.intent.ai.IntentClassificationAiService;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import dev.langchain4j.rag.query.Query;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 意图识别与结构化槽位提取阶段
 * 负责从查询中识别用户意图并提取结构化信息，包括：
 * 1. 意图识别与分类
 * 2. 实体抽取（命名实体识别）
 * 3. 结构化槽位提取（时间、地点、设备型号等）
 * 4. 查询类型判断（问答/对比/汇总等）
 * 5. 过滤条件抽取
 * 
 * @author Claude
 */
@Slf4j
@RequiredArgsConstructor
public class IntentExtractionStage implements QueryTransformerStage {
    
    private final IntentClassificationAiService intentClassificationAiService;
    private final ObjectMapper objectMapper;
    
    // 实体抽取模式
    private static final Map<String, Pattern> ENTITY_PATTERNS = createEntityPatterns();
    
    // 查询类型识别模式
    private static final Map<QueryType, Pattern> QUERY_TYPE_PATTERNS = createQueryTypePatterns();
    
    // 时间表达式模式
    private static final Map<String, Pattern> TIME_PATTERNS = createTimePatterns();
    
    // 数值与单位模式
    private static final Pattern VALUE_UNIT_PATTERN = Pattern.compile(
        "(\\d+(?:\\.\\d+)?)[\\s]*([a-zA-Z\\u4e00-\\u9fa5]+)(?:[\\s]*[以上|以下|左右|约]?)?"
    );
    
    // 比较运算符模式
    private static final Map<String, ComparisonOperator> COMPARISON_PATTERNS = createComparisonPatterns();
    
    @Override
    public String getName() {
        return "IntentExtractionStage";
    }
    
    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnableIntentRecognition();
    }
    
    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            log.debug("输入查询为空，跳过意图识别处理");
            return Collections.emptyList();
        }
        
        log.debug("开始意图识别与结构化抽取: inputCount={}", queries.size());
        
        try {
            List<Query> enrichedQueries = new ArrayList<>();
            
            for (Query query : queries) {
                try {
                    QueryIntent queryIntent = extractIntentAndSlots(context, query);
                    Query enrichedQuery = enrichQueryWithIntent(query, queryIntent);
                    enrichedQueries.add(enrichedQuery);
                    
                    // 将意图信息存储到上下文中供后续阶段使用
                    storeIntentInContext(context, query.text(), queryIntent);
                    
                } catch (Exception e) {
                    log.warn("单个查询意图识别失败，保留原查询: query={}", query.text(), e);
                    enrichedQueries.add(query);
                }
            }
            
            log.debug("意图识别与结构化抽取完成: inputCount={}, outputCount={}", 
                    queries.size(), enrichedQueries.size());
            
            return enrichedQueries;
            
        } catch (Exception e) {
            log.error("意图识别与结构化抽取失败: inputCount={}", queries.size(), e);
            throw new QueryTransformationException(getName(), "意图识别与结构化抽取失败", e, true);
        }
    }
    
    /**
     * 提取意图和槽位信息
     */
    private QueryIntent extractIntentAndSlots(QueryContext context, Query query) {
        String text = query.text();
        
        // 1. 意图分类
        IntentClassificationResult intentResult = classifyIntent(context, text);
        
        // 2. 查询类型识别
        QueryType queryType = recognizeQueryType(text);
        
        // 3. 实体抽取
        Map<String, List<String>> entities = extractEntities(text);
        
        // 4. 结构化槽位提取
        Map<String, Object> slots = extractStructuredSlots(text);
        
        // 5. 过滤条件提取
        List<FilterCondition> filterConditions = extractFilterConditions(text);
        
        return QueryIntent.builder()
                .originalText(text)
                .intentCode(intentResult.getIntentCode())
                .intentName(intentResult.getIntentName())
                .catalogCode(intentResult.getCatalogCode())
                .catalogName(intentResult.getCatalogName())
                .confidence(intentResult.getConfidence())
                .queryType(queryType)
                .entities(entities)
                .slots(slots)
                .filterConditions(filterConditions)
                .extractedAt(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 调用意图分类服务
     */
    private IntentClassificationResult classifyIntent(QueryContext context, String text) {
        try {
            // 构建意图列表（这里简化处理，实际应从配置或数据库获取）
            String intentList = buildIntentList(context);
            
            // 调用AI服务进行意图分类
            String result = intentClassificationAiService.classifyIntent(
                text, intentList, context.getChannel(), context.getTenant()
            );
            
            // 解析JSON结果
            return parseIntentClassificationResult(result);
            
        } catch (Exception e) {
            log.warn("意图分类失败，使用默认分类: text={}", text, e);
            return IntentClassificationResult.builder()
                    .intentCode("UNKNOWN")
                    .intentName("未知意图")
                    .catalogCode("CATALOG_unknown")
                    .catalogName("未知分类")
                    .confidence(0.0)
                    .build();
        }
    }
    
    /**
     * 构建意图列表
     */
    private String buildIntentList(QueryContext context) {
        // 根据租户和渠道构建可用意图列表
        // 这里提供一个基础示例，实际应从配置服务获取
        StringBuilder intentList = new StringBuilder();
        
        intentList.append("CATALOG_customer_service:客服服务\n");
        intentList.append("  greeting:问候\n");
        intentList.append("  complaint:投诉\n");
        intentList.append("  consultation:咨询\n");
        intentList.append("  feedback:反馈\n");
        
        intentList.append("CATALOG_technical_support:技术支持\n");
        intentList.append("  troubleshooting:故障排查\n");
        intentList.append("  installation:安装指导\n");
        intentList.append("  configuration:配置说明\n");
        intentList.append("  upgrade:升级指南\n");
        
        intentList.append("CATALOG_product_inquiry:产品咨询\n");
        intentList.append("  product_info:产品信息\n");
        intentList.append("  feature_comparison:功能对比\n");
        intentList.append("  pricing:价格询问\n");
        intentList.append("  compatibility:兼容性\n");
        
        return intentList.toString();
    }
    
    /**
     * 解析意图分类结果
     */
    private IntentClassificationResult parseIntentClassificationResult(String jsonResult) {
        try {
            JsonNode node = objectMapper.readTree(jsonResult);
            
            return IntentClassificationResult.builder()
                    .intentCode(node.path("intentCode").asText("UNKNOWN"))
                    .intentName(node.path("intentName").asText("未知意图"))
                    .catalogCode(node.path("catalogCode").asText("CATALOG_unknown"))
                    .catalogName(node.path("catalogName").asText("未知分类"))
                    .confidence(node.path("confidence").asDouble(0.0))
                    .reasoning(node.path("reasoning").asText(""))
                    .build();
                    
        } catch (JsonProcessingException e) {
            log.error("解析意图分类结果失败: {}", jsonResult, e);
            return IntentClassificationResult.builder()
                    .intentCode("UNKNOWN")
                    .intentName("解析失败")
                    .catalogCode("CATALOG_unknown")
                    .catalogName("未知分类")
                    .confidence(0.0)
                    .build();
        }
    }
    
    /**
     * 识别查询类型
     */
    private QueryType recognizeQueryType(String text) {
        for (Map.Entry<QueryType, Pattern> entry : QUERY_TYPE_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(text).find()) {
                return entry.getKey();
            }
        }
        return QueryType.QUESTION_ANSWER; // 默认问答类型
    }
    
    /**
     * 抽取命名实体
     */
    private Map<String, List<String>> extractEntities(String text) {
        Map<String, List<String>> entities = new HashMap<>();
        
        for (Map.Entry<String, Pattern> entry : ENTITY_PATTERNS.entrySet()) {
            String entityType = entry.getKey();
            Pattern pattern = entry.getValue();
            List<String> matches = new ArrayList<>();
            
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String match = matcher.group(1);
                if (!matches.contains(match)) {
                    matches.add(match);
                }
            }
            
            if (!matches.isEmpty()) {
                entities.put(entityType, matches);
            }
        }
        
        return entities;
    }
    
    /**
     * 提取结构化槽位
     */
    private Map<String, Object> extractStructuredSlots(String text) {
        Map<String, Object> slots = new HashMap<>();
        
        // 提取时间信息
        extractTimeSlots(text, slots);
        
        // 提取数值和单位
        extractValueUnitSlots(text, slots);
        
        // 提取比较条件
        extractComparisonSlots(text, slots);
        
        return slots;
    }
    
    /**
     * 提取时间槽位
     */
    private void extractTimeSlots(String text, Map<String, Object> slots) {
        for (Map.Entry<String, Pattern> entry : TIME_PATTERNS.entrySet()) {
            String timeType = entry.getKey();
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(text);
            
            while (matcher.find()) {
                String timeExpression = matcher.group();
                slots.put(timeType, timeExpression);
                log.debug("提取时间槽位: {}={}", timeType, timeExpression);
            }
        }
    }
    
    /**
     * 提取数值单位槽位
     */
    private void extractValueUnitSlots(String text, Map<String, Object> slots) {
        Matcher matcher = VALUE_UNIT_PATTERN.matcher(text);
        List<Map<String, String>> valueUnits = new ArrayList<>();
        
        while (matcher.find()) {
            Map<String, String> valueUnit = new HashMap<>();
            valueUnit.put("value", matcher.group(1));
            valueUnit.put("unit", matcher.group(2));
            valueUnits.add(valueUnit);
        }
        
        if (!valueUnits.isEmpty()) {
            slots.put("valueUnits", valueUnits);
        }
    }
    
    /**
     * 提取比较条件槽位
     */
    private void extractComparisonSlots(String text, Map<String, Object> slots) {
        for (Map.Entry<String, ComparisonOperator> entry : COMPARISON_PATTERNS.entrySet()) {
            String pattern = entry.getKey();
            ComparisonOperator operator = entry.getValue();
            
            if (text.contains(pattern)) {
                slots.put("comparisonOperator", operator);
                slots.put("comparisonPattern", pattern);
                break;
            }
        }
    }
    
    /**
     * 提取过滤条件
     */
    private List<FilterCondition> extractFilterConditions(String text) {
        List<FilterCondition> conditions = new ArrayList<>();
        
        // 地区过滤
        Pattern regionPattern = Pattern.compile("(\\w+省|\\w+市|\\w+区|\\w+县)");
        Matcher regionMatcher = regionPattern.matcher(text);
        while (regionMatcher.find()) {
            conditions.add(FilterCondition.builder()
                    .field("region")
                    .operator(ComparisonOperator.EQUALS)
                    .value(regionMatcher.group(1))
                    .build());
        }
        
        // 设备类型过滤
        Pattern devicePattern = Pattern.compile("(\\w+设备|\\w+终端|\\w+网关|\\w+传感器)");
        Matcher deviceMatcher = devicePattern.matcher(text);
        while (deviceMatcher.find()) {
            conditions.add(FilterCondition.builder()
                    .field("deviceType")
                    .operator(ComparisonOperator.EQUALS)
                    .value(deviceMatcher.group(1))
                    .build());
        }
        
        // 版本过滤
        Pattern versionPattern = Pattern.compile("([vV]\\d+\\.\\d+(?:\\.\\d+)?)");
        Matcher versionMatcher = versionPattern.matcher(text);
        while (versionMatcher.find()) {
            conditions.add(FilterCondition.builder()
                    .field("version")
                    .operator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                    .value(versionMatcher.group(1))
                    .build());
        }
        
        return conditions;
    }
    
    /**
     * 使用意图信息丰富查询
     */
    private Query enrichQueryWithIntent(Query originalQuery, QueryIntent intent) {
        // 这里可以根据意图信息对查询进行增强
        // 例如：添加意图相关的关键词、调整查询权重等
        
        String enrichedText = originalQuery.text();
        
        // 基于意图类型添加上下文关键词
        if (intent.getQueryType() == QueryType.TROUBLESHOOTING) {
            enrichedText = "故障排查 " + enrichedText;
        } else if (intent.getQueryType() == QueryType.FEATURE_COMPARISON) {
            enrichedText = "功能对比 " + enrichedText;
        }
        
        return Query.from(enrichedText);
    }
    
    /**
     * 将意图信息存储到上下文
     */
    private void storeIntentInContext(QueryContext context, String queryText, QueryIntent intent) {
        String key = "intent_" + queryText.hashCode();
        context.setAttribute(key, intent);
        
        // 也可以存储全局意图统计
        @SuppressWarnings("unchecked")
        Map<String, Integer> intentStats = (Map<String, Integer>) context.getAttribute("intentStats");
        if (intentStats == null) {
            intentStats = new HashMap<>();
            context.setAttribute("intentStats", intentStats);
        }
        
        intentStats.merge(intent.getIntentCode(), 1, Integer::sum);
    }
    
    // 静态初始化方法
    
    private static Map<String, Pattern> createEntityPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // 人名实体
        patterns.put("PERSON", Pattern.compile("([\\u4e00-\\u9fa5]{2,4}(?:先生|女士|同学|老师|经理|总监)?|[A-Z][a-z]+ [A-Z][a-z]+)"));
        
        // 地名实体
        patterns.put("LOCATION", Pattern.compile("([\\u4e00-\\u9fa5]+(?:省|市|区|县|街道|路|号))"));
        
        // 组织机构
        patterns.put("ORGANIZATION", Pattern.compile("([\\u4e00-\\u9fa5]+(?:公司|集团|机构|部门|科技|网络))"));
        
        // 产品名称
        patterns.put("PRODUCT", Pattern.compile("([A-Za-z0-9\\u4e00-\\u9fa5]+(?:设备|终端|网关|传感器|系统|平台|软件))"));
        
        // 型号规格
        patterns.put("MODEL", Pattern.compile("([A-Z]{2,}[\\-_]?\\d{2,}[A-Z]?|\\w+[\\-_]v?\\d+(?:\\.\\d+)?)"));
        
        return patterns;
    }
    
    private static Map<QueryType, Pattern> createQueryTypePatterns() {
        Map<QueryType, Pattern> patterns = new HashMap<>();
        
        patterns.put(QueryType.QUESTION_ANSWER, 
            Pattern.compile(".*(什么|如何|怎么|为什么|哪里|谁|when|what|how|why|where|who).*"));
        
        patterns.put(QueryType.FEATURE_COMPARISON, 
            Pattern.compile(".*(对比|比较|区别|差异|优缺点|和.*的区别|vs|versus).*"));
            
        patterns.put(QueryType.SUMMARY_REPORT, 
            Pattern.compile(".*(汇总|统计|报告|总结|概述|总计|合计).*"));
            
        patterns.put(QueryType.TROUBLESHOOTING, 
            Pattern.compile(".*(故障|问题|错误|异常|不工作|无法|失败|报错).*"));
            
        patterns.put(QueryType.MANUAL_LOOKUP, 
            Pattern.compile(".*(手册|文档|说明书|指南|教程|操作步骤).*"));
            
        patterns.put(QueryType.CODE_EXAMPLE, 
            Pattern.compile(".*(代码|示例|例子|demo|样例|代码片段).*"));
        
        return patterns;
    }
    
    private static Map<String, Pattern> createTimePatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        patterns.put("absoluteDate", 
            Pattern.compile("\\d{4}[年\\-/]\\d{1,2}[月\\-/]\\d{1,2}[日]?"));
            
        patterns.put("relativeTime", 
            Pattern.compile("(今天|昨天|明天|上周|本周|下周|上个月|这个月|下个月|去年|今年|明年)"));
            
        patterns.put("timeRange", 
            Pattern.compile("最近(\\d+)(天|周|个?月|年)"));
            
        patterns.put("timePoint", 
            Pattern.compile("(\\d{1,2})[点时:]?(\\d{1,2})?[分]?"));
        
        return patterns;
    }
    
    private static Map<String, ComparisonOperator> createComparisonPatterns() {
        Map<String, ComparisonOperator> patterns = new HashMap<>();
        
        patterns.put("大于", ComparisonOperator.GREATER_THAN);
        patterns.put("小于", ComparisonOperator.LESS_THAN);
        patterns.put("等于", ComparisonOperator.EQUALS);
        patterns.put("不等于", ComparisonOperator.NOT_EQUALS);
        patterns.put("大于等于", ComparisonOperator.GREATER_THAN_OR_EQUAL);
        patterns.put("小于等于", ComparisonOperator.LESS_THAN_OR_EQUAL);
        patterns.put("超过", ComparisonOperator.GREATER_THAN);
        patterns.put("低于", ComparisonOperator.LESS_THAN);
        patterns.put("至少", ComparisonOperator.GREATER_THAN_OR_EQUAL);
        patterns.put("最多", ComparisonOperator.LESS_THAN_OR_EQUAL);
        patterns.put("包含", ComparisonOperator.CONTAINS);
        patterns.put("不包含", ComparisonOperator.NOT_CONTAINS);
        
        return patterns;
    }
    
    // 内部数据结构
    
    /**
     * 查询类型枚举
     */
    public enum QueryType {
        QUESTION_ANSWER,      // 问答
        FEATURE_COMPARISON,   // 功能对比
        SUMMARY_REPORT,       // 汇总报告
        TROUBLESHOOTING,      // 故障排查
        MANUAL_LOOKUP,        // 手册查找
        CODE_EXAMPLE         // 代码示例
    }
    
    /**
     * 比较运算符枚举
     */
    public enum ComparisonOperator {
        EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, 
        GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
        CONTAINS, NOT_CONTAINS, IN, NOT_IN
    }
    
    /**
     * 意图分类结果
     */
    @Data
    @lombok.Builder
    public static class IntentClassificationResult {
        private String intentCode;
        private String intentName;
        private String catalogCode;
        private String catalogName;
        private double confidence;
        private String reasoning;
    }
    
    /**
     * 过滤条件
     */
    @Data
    @lombok.Builder
    public static class FilterCondition {
        private String field;
        private ComparisonOperator operator;
        private Object value;
    }
    
    /**
     * 查询意图综合信息
     */
    @Data
    @lombok.Builder
    public static class QueryIntent {
        private String originalText;
        private String intentCode;
        private String intentName;
        private String catalogCode;
        private String catalogName;
        private double confidence;
        private QueryType queryType;
        private Map<String, List<String>> entities;
        private Map<String, Object> slots;
        private List<FilterCondition> filterConditions;
        private long extractedAt;
    }
    
    @Override
    public void initialize(QueryContext context) {
        log.debug("初始化意图识别阶段: tenant={}, channel={}", 
                context.getTenant(), context.getChannel());
        
        // 初始化意图统计
        context.setAttribute("intentStats", new HashMap<String, Integer>());
    }
    
    @Override
    public void cleanup(QueryContext context) {
        // 打印意图统计信息
        @SuppressWarnings("unchecked")
        Map<String, Integer> intentStats = (Map<String, Integer>) context.getAttribute("intentStats");
        if (intentStats != null && !intentStats.isEmpty()) {
            log.info("本次查询意图统计: {}", intentStats);
        }
    }
}