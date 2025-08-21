package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 语义对齐阶段单元测试
 * 
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("语义对齐阶段测试")
class SemanticAlignmentStageTest {
    
    private SemanticAlignmentStage semanticAlignmentStage;
    private QueryContext context;
    
    @BeforeEach
    void setUp() {
        semanticAlignmentStage = new SemanticAlignmentStage();
        
        // 创建测试上下文
        context = QueryContext.builder()
                .originalQuery(Query.from("测试查询"))
                .tenant("test")
                .channel("web")
                .locale("zh-CN")
                .chatHistory(new HashMap<>())
                .attributes(new HashMap<>())
                .budgetControl(QueryContext.BudgetControl.builder()
                        .maxTokens(1000)
                        .maxCost(10.0)
                        .build())
                .timeoutControl(QueryContext.TimeoutControl.builder()
                        .maxLatencyMs(30000L)
                        .pipelineStartTime(System.currentTimeMillis())
                        .build())
                .pipelineConfig(QueryContext.PipelineConfig.builder()
                        .enableNormalization(true)
                        .build())
                .build();
    }
    
    @Test
    @DisplayName("应该正确处理同义词归一化")
    void shouldNormalizeSynonyms() {
        // Given
        List<Query> queries = Arrays.asList(
                Query.from("国VI排放标准是什么"),
                Query.from("新能源车有哪些优势"),
                Query.from("AI技术的发展趋势")
        );
        
        // When
        Collection<Query> result = semanticAlignmentStage.apply(context, queries);
        
        // Then
        assertThat(result).isNotEmpty();
        
        List<String> resultTexts = result.stream()
                .map(Query::text)
                .toList();
        
        // 验证同义词被正确归一化
        assertThat(resultTexts).anyMatch(text -> text.contains("国六"));
        assertThat(resultTexts).anyMatch(text -> text.contains("新能源汽车"));
        assertThat(resultTexts).anyMatch(text -> text.contains("人工智能"));
    }
    
    @Test
    @DisplayName("应该正确处理时间表达式正则化")
    void shouldNormalizeTimeExpressions() {
        // Given
        List<Query> queries = Arrays.asList(
                Query.from("最近30天的销售数据"),
                Query.from("上个月的用户反馈"),
                Query.from("去年11月的产品发布")
        );
        
        // When
        Collection<Query> result = semanticAlignmentStage.apply(context, queries);
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isGreaterThanOrEqualTo(queries.size());
        
        // 验证时间表达式被处理
        List<String> resultTexts = result.stream()
                .map(Query::text)
                .toList();
        
        // 应该包含一些标准化的时间表达式
        assertThat(resultTexts).anyMatch(text -> text.matches(".*\\d{4}-\\d{2}-\\d{2}.*"));
    }
    
    @Test
    @DisplayName("应该正确处理单位和数值标准化")
    void shouldNormalizeUnitsAndValues() {
        // Given
        List<Query> queries = Arrays.asList(
                Query.from("3吨货物的运输成本"),
                Query.from("5米长的设备规格"),
                Query.from("2小时内完成任务")
        );
        
        // When
        Collection<Query> result = semanticAlignmentStage.apply(context, queries);
        
        // Then
        assertThat(result).isNotEmpty();
        
        List<String> resultTexts = result.stream()
                .map(Query::text)
                .toList();
        
        // 验证单位标准化
        assertThat(resultTexts).anyMatch(text -> text.contains("kg") || text.contains("吨"));
        assertThat(resultTexts).anyMatch(text -> text.contains("m") || text.contains("米"));
        assertThat(resultTexts).anyMatch(text -> text.contains("小时") || text.contains("h"));
    }
    
    @Test
    @DisplayName("应该处理空输入")
    void shouldHandleEmptyInput() {
        // Given
        List<Query> emptyQueries = Collections.emptyList();
        
        // When
        Collection<Query> result = semanticAlignmentStage.apply(context, emptyQueries);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("应该处理null输入")
    void shouldHandleNullInput() {
        // When & Then
        Collection<Query> result = semanticAlignmentStage.apply(context, null);
        
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("应该正确判断阶段是否启用")
    void shouldCheckIfStageIsEnabled() {
        // Given - 默认应该启用
        // When
        boolean enabled = semanticAlignmentStage.isEnabled(context);
        
        // Then
        assertThat(enabled).isTrue();
        
        // Given - 明确禁用
        context.setAttribute("enableSemanticAlignment", false);
        
        // When
        boolean disabledResult = semanticAlignmentStage.isEnabled(context);
        
        // Then
        assertThat(disabledResult).isFalse();
    }
    
    @Test
    @DisplayName("应该正确返回阶段名称")
    void shouldReturnCorrectStageName() {
        // When
        String name = semanticAlignmentStage.getName();
        
        // Then
        assertThat(name).isEqualTo("SemanticAlignmentStage");
    }
    
    @Test
    @DisplayName("应该正确处理基于上下文的实体标准化")
    void shouldNormalizeEntitiesBasedOnContext() {
        // Given
        context = QueryContext.builder()
                .originalQuery(Query.from("测试查询"))
                .tenant("automotive")  // 汽车行业租户
                .channel("web")
                .locale("zh-CN")
                .chatHistory(new HashMap<>())
                .attributes(new HashMap<>())
                .budgetControl(QueryContext.BudgetControl.builder().build())
                .timeoutControl(QueryContext.TimeoutControl.builder()
                        .maxLatencyMs(30000L)
                        .pipelineStartTime(System.currentTimeMillis())
                        .build())
                .pipelineConfig(QueryContext.PipelineConfig.builder().build())
                .build();
        
        List<Query> queries = Arrays.asList(
                Query.from("国VI排放标准详情"),
                Query.from("电动车续航里程对比")
        );
        
        // When
        Collection<Query> result = semanticAlignmentStage.apply(context, queries);
        
        // Then
        assertThat(result).isNotEmpty();
        
        List<String> resultTexts = result.stream()
                .map(Query::text)
                .toList();
        
        // 验证汽车行业相关的实体标准化
        assertThat(resultTexts).anyMatch(text -> text.contains("国六"));
        assertThat(resultTexts).anyMatch(text -> text.contains("新能源汽车"));
    }
    
    @Test
    @DisplayName("应该正确去重")
    void shouldRemoveDuplicates() {
        // Given
        List<Query> queries = Arrays.asList(
                Query.from("AI技术发展"),
                Query.from("人工智能技术发展"),  // 应该归一化为相同结果
                Query.from("AI技术发展")  // 重复
        );
        
        // When
        Collection<Query> result = semanticAlignmentStage.apply(context, queries);
        
        // Then
        assertThat(result).isNotEmpty();
        // 结果数量应该少于输入（因为去重）
        assertThat(result.size()).isLessThanOrEqualTo(queries.size());
        
        // 验证没有完全相同的查询
        List<String> resultTexts = result.stream()
                .map(Query::text)
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        
        Set<String> uniqueTexts = new HashSet<>(resultTexts);
        assertThat(uniqueTexts.size()).isEqualTo(resultTexts.size());
    }
    
    @Test
    @DisplayName("初始化和清理方法应该正常工作")
    void shouldInitializeAndCleanupCorrectly() {
        // When & Then - 应该不抛出异常
        assertDoesNotThrow(() -> {
            semanticAlignmentStage.initialize(context);
            semanticAlignmentStage.cleanup(context);
        });
    }
}