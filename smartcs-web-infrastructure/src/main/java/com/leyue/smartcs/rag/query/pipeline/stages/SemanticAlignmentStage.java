package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 语义对齐阶段
 * 负责同义词归一化、实体规范化和语义标准化处理，包括：
 * 1. 领域同义词/别名归一化
 * 2. 单位/数值标准化  
 * 3. 时间表达式正则化
 * 4. 实体标准化和映射
 * 
 * @author Claude
 */
@Slf4j
public class SemanticAlignmentStage implements QueryTransformerStage {
    
    // 领域同义词映射表
    private static final Map<String, String> DOMAIN_SYNONYMS = createDomainSynonyms();
    
    // 单位换算映射表
    private static final Map<String, String> UNIT_MAPPINGS = createUnitMappings();
    
    // 时间表达式模式
    private static final Map<Pattern, TimeExpressionHandler> TIME_PATTERNS = createTimePatterns();
    
    // 数值单位模式
    private static final Pattern NUMERIC_UNIT_PATTERN = Pattern.compile(
        "(\\d+(?:\\.\\d+)?)\\s*(吨|kg|千克|公斤|斤|克|g|米|m|厘米|cm|毫米|mm|公里|km|小时|h|分钟|min|秒|s|天|日|周|月|年)"
    );
    
    // 时间相对表达式模式
    private static final Pattern RELATIVE_TIME_PATTERN = Pattern.compile(
        "(去年|今年|明年|上个月|这个月|下个月|上周|本周|这周|下周|昨天|今天|明天|最近)(\\d+)?(天|日|周|个月|月|年)?"
    );
    
    @Override
    public String getName() {
        return "SemanticAlignmentStage";
    }
    
    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig() != null &&
               context.getAttribute("enableSemanticAlignment") != Boolean.FALSE;
    }
    
    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            log.debug("输入查询为空，跳过语义对齐处理");
            return Collections.emptyList();
        }
        
        log.debug("开始语义对齐处理: inputCount={}", queries.size());
        
        try {
            List<Query> alignedQueries = new ArrayList<>();
            
            for (Query query : queries) {
                try {
                    Query alignedQuery = applySemanticAlignment(context, query);
                    if (alignedQuery != null && !alignedQuery.text().trim().isEmpty()) {
                        alignedQueries.add(alignedQuery);
                    }
                } catch (Exception e) {
                    log.warn("单个查询语义对齐失败，保留原查询: query={}", query.text(), e);
                    alignedQueries.add(query);
                }
            }
            
            // 去重处理
            alignedQueries = removeDuplicates(alignedQueries);
            
            log.debug("语义对齐处理完成: inputCount={}, outputCount={}", 
                    queries.size(), alignedQueries.size());
            
            return alignedQueries;
            
        } catch (Exception e) {
            log.error("语义对齐处理失败: inputCount={}", queries.size(), e);
            throw new QueryTransformationException(getName(), "语义对齐处理失败", e, true);
        }
    }
    
    /**
     * 应用语义对齐处理
     */
    private Query applySemanticAlignment(QueryContext context, Query query) {
        String text = query.text();
        if (text == null || text.trim().isEmpty()) {
            return query;
        }
        
        // 1. 领域同义词归一化
        text = applyDomainSynonyms(text);
        
        // 2. 单位/数值标准化
        text = standardizeUnitsAndValues(text);
        
        // 3. 时间表达式正则化
        text = normalizeTimeExpressions(text);
        
        // 4. 实体标准化（基于上下文）
        text = normalizeEntities(context, text);
        
        return Query.from(text.trim());
    }
    
    /**
     * 应用领域同义词映射
     */
    private String applyDomainSynonyms(String text) {
        String result = text;
        
        // 按长度倒序排列，优先匹配长词组
        List<String> sortedKeys = DOMAIN_SYNONYMS.keySet().stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .collect(Collectors.toList());
        
        for (String synonym : sortedKeys) {
            String standardForm = DOMAIN_SYNONYMS.get(synonym);
            // 使用词边界匹配，避免部分匹配
            result = result.replaceAll("(?i)\\b" + Pattern.quote(synonym) + "\\b", standardForm);
        }
        
        return result;
    }
    
    /**
     * 标准化单位和数值
     */
    private String standardizeUnitsAndValues(String text) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = NUMERIC_UNIT_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String number = matcher.group(1);
            String unit = matcher.group(2);
            
            // 查找标准化单位
            String standardUnit = UNIT_MAPPINGS.getOrDefault(unit, unit);
            
            // 进行单位换算（如果需要）
            String standardizedValue = convertToStandardUnit(number, unit, standardUnit);
            
            matcher.appendReplacement(result, standardizedValue);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 单位换算
     */
    private String convertToStandardUnit(String number, String originalUnit, String standardUnit) {
        try {
            double value = Double.parseDouble(number);
            double convertedValue = value;
            
            // 重量单位换算到kg
            if (isWeightUnit(originalUnit)) {
                switch (originalUnit) {
                    case "吨": convertedValue = value * 1000; break;
                    case "斤": convertedValue = value * 0.5; break;
                    case "克": case "g": convertedValue = value / 1000; break;
                    default: convertedValue = value; // kg保持不变
                }
                return String.format("%.1fkg", convertedValue);
            }
            
            // 长度单位换算到m
            if (isLengthUnit(originalUnit)) {
                switch (originalUnit) {
                    case "公里": case "km": convertedValue = value * 1000; break;
                    case "厘米": case "cm": convertedValue = value / 100; break;
                    case "毫米": case "mm": convertedValue = value / 1000; break;
                    default: convertedValue = value; // m保持不变
                }
                return String.format("%.1fm", convertedValue);
            }
            
            // 时间单位换算到标准形式
            if (isTimeUnit(originalUnit)) {
                switch (originalUnit) {
                    case "小时": case "h": return value + "小时";
                    case "分钟": case "min": return value + "分钟";
                    case "秒": case "s": return value + "秒";
                    default: return number + standardUnit;
                }
            }
            
            return number + standardUnit;
            
        } catch (NumberFormatException e) {
            log.debug("数值转换失败，保持原样: {}{}", number, originalUnit);
            return number + standardUnit;
        }
    }
    
    /**
     * 时间表达式正则化
     */
    private String normalizeTimeExpressions(String text) {
        String result = text;
        
        // 处理相对时间表达式
        for (Map.Entry<Pattern, TimeExpressionHandler> entry : TIME_PATTERNS.entrySet()) {
            Pattern pattern = entry.getKey();
            TimeExpressionHandler handler = entry.getValue();
            
            Matcher matcher = pattern.matcher(result);
            StringBuffer buffer = new StringBuffer();
            
            while (matcher.find()) {
                String replacement = handler.handle(matcher);
                matcher.appendReplacement(buffer, replacement);
            }
            matcher.appendTail(buffer);
            result = buffer.toString();
        }
        
        return result;
    }
    
    /**
     * 实体标准化（基于上下文信息）
     */
    private String normalizeEntities(QueryContext context, String text) {
        // 这里可以基于租户、渠道等上下文信息进行实体标准化
        // 例如：根据不同租户的业务领域，应用不同的实体映射规则
        
        String tenant = context.getTenant();
        if ("automotive".equals(tenant)) {
            // 汽车行业特定的实体标准化
            text = text.replaceAll("(?i)国VI|国6|China\\s*6", "国六");
            text = text.replaceAll("(?i)新能源车|NEV|电动车", "新能源汽车");
        } else if ("logistics".equals(tenant)) {
            // 物流行业特定的实体标准化
            text = text.replaceAll("(?i)CN|菜鸟|菜鸟网络", "菜鸟网络");
            text = text.replaceAll("(?i)快递|快运|配送", "快递服务");
        }
        
        return text;
    }
    
    /**
     * 去重处理
     */
    private List<Query> removeDuplicates(List<Query> queries) {
        Set<String> seen = new LinkedHashSet<>();
        List<Query> uniqueQueries = new ArrayList<>();
        
        for (Query query : queries) {
            String normalized = query.text().trim().toLowerCase();
            if (seen.add(normalized)) {
                uniqueQueries.add(query);
            }
        }
        
        return uniqueQueries;
    }
    
    // 辅助方法
    private boolean isWeightUnit(String unit) {
        return Arrays.asList("吨", "kg", "千克", "公斤", "斤", "克", "g").contains(unit);
    }
    
    private boolean isLengthUnit(String unit) {
        return Arrays.asList("米", "m", "厘米", "cm", "毫米", "mm", "公里", "km").contains(unit);
    }
    
    private boolean isTimeUnit(String unit) {
        return Arrays.asList("小时", "h", "分钟", "min", "秒", "s", "天", "日", "周", "月", "年").contains(unit);
    }
    
    // 静态初始化方法
    private static Map<String, String> createDomainSynonyms() {
        Map<String, String> synonyms = new HashMap<>();
        
        // 汽车行业同义词
        synonyms.put("国VI", "国六");
        synonyms.put("国6", "国六");
        synonyms.put("China 6", "国六");
        synonyms.put("新能源车", "新能源汽车");
        synonyms.put("NEV", "新能源汽车");
        synonyms.put("电动车", "新能源汽车");
        
        // 物流行业同义词
        synonyms.put("CN", "菜鸟网络");
        synonyms.put("菜鸟", "菜鸟网络");
        synonyms.put("快递", "快递服务");
        synonyms.put("快运", "快递服务");
        synonyms.put("配送", "快递服务");
        
        // 技术领域同义词
        synonyms.put("AI", "人工智能");
        synonyms.put("ML", "机器学习");
        synonyms.put("DL", "深度学习");
        synonyms.put("NLP", "自然语言处理");
        
        // 时间同义词
        synonyms.put("半个月", "两周");
        synonyms.put("半月", "两周");
        
        return synonyms;
    }
    
    private static Map<String, String> createUnitMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // 重量单位标准化到kg
        mappings.put("千克", "kg");
        mappings.put("公斤", "kg");
        mappings.put("克", "g");
        
        // 长度单位标准化到m
        mappings.put("米", "m");
        mappings.put("厘米", "cm");
        mappings.put("毫米", "mm");
        mappings.put("公里", "km");
        
        // 时间单位标准化
        mappings.put("小时", "h");
        mappings.put("分钟", "min");
        mappings.put("秒", "s");
        
        return mappings;
    }
    
    private static Map<Pattern, TimeExpressionHandler> createTimePatterns() {
        Map<Pattern, TimeExpressionHandler> patterns = new HashMap<>();
        
        // 相对时间表达式
        patterns.put(
            Pattern.compile("去年(\\d+)月"),
            matcher -> {
                int month = Integer.parseInt(matcher.group(1));
                int year = LocalDate.now().getYear() - 1;
                return year + "-" + String.format("%02d", month);
            }
        );
        
        patterns.put(
            Pattern.compile("最近(\\d+)天"),
            matcher -> {
                int days = Integer.parseInt(matcher.group(1));
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minus(days, ChronoUnit.DAYS);
                return startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "至" + 
                       endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
        );
        
        patterns.put(
            Pattern.compile("上周"),
            matcher -> {
                LocalDate today = LocalDate.now();
                LocalDate lastWeekStart = today.minus(1, ChronoUnit.WEEKS)
                                              .minus(today.getDayOfWeek().getValue() - 1, ChronoUnit.DAYS);
                LocalDate lastWeekEnd = lastWeekStart.plus(6, ChronoUnit.DAYS);
                return lastWeekStart.format(DateTimeFormatter.ISO_LOCAL_DATE) + "至" +
                       lastWeekEnd.format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
        );
        
        patterns.put(
            Pattern.compile("上个月"),
            matcher -> {
                LocalDate lastMonth = LocalDate.now().minus(1, ChronoUnit.MONTHS);
                return lastMonth.getYear() + "-" + String.format("%02d", lastMonth.getMonthValue());
            }
        );
        
        return patterns;
    }
    
    /**
     * 时间表达式处理器接口
     */
    @FunctionalInterface
    private interface TimeExpressionHandler {
        String handle(Matcher matcher);
    }
    
    @Override
    public void initialize(QueryContext context) {
        log.debug("初始化语义对齐阶段: tenant={}, channel={}", 
                context.getTenant(), context.getChannel());
    }
    
    @Override
    public void cleanup(QueryContext context) {
        // 无需清理资源
    }
}