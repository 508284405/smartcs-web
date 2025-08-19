package com.leyue.smartcs.rag.database.service;

import com.leyue.smartcs.domain.database.entity.DatabaseTableSchema;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.rag.database.service.NlpToSqlService.SqlGenerationResult;
import com.leyue.smartcs.rag.database.service.SchemaRetrievalService.SchemaMatch;
import com.leyue.smartcs.rag.database.service.SchemaRetrievalService.SchemaRetrievalResult;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NLP到SQL转换服务测试
 * 
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
class NlpToSqlServiceTest {
    
    @Mock
    private SchemaRetrievalService schemaRetrievalService;
    
    @Mock
    private DynamicModelManager dynamicModelManager;
    
    @Mock
    private ChatModel chatModel;
    
    @Mock
    private ChatResponse chatResponse;
    
    @Mock
    private AiMessage aiMessage;
    
    private NlpToSqlService nlpToSqlService;
    
    @BeforeEach
    void setUp() {
        nlpToSqlService = new NlpToSqlService(schemaRetrievalService, dynamicModelManager);
        
        // 设置配置值
        ReflectionTestUtils.setField(nlpToSqlService, "chatModelId", 1L);
        ReflectionTestUtils.setField(nlpToSqlService, "maxTablesForSql", 5);
        ReflectionTestUtils.setField(nlpToSqlService, "enableComplexQueries", true);
        ReflectionTestUtils.setField(nlpToSqlService, "similarityThreshold", 0.6);
    }
    
    @Test
    void testIsNaturalLanguage_WithNaturalLanguage() {
        // Given
        String naturalQuery = "查询销售额最高的产品";
        
        // When
        boolean result = nlpToSqlService.isNaturalLanguage(naturalQuery);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testIsNaturalLanguage_WithSqlQuery() {
        // Given
        String sqlQuery = "SELECT * FROM products WHERE price > 100";
        
        // When
        boolean result = nlpToSqlService.isNaturalLanguage(sqlQuery);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testGenerateSql_Success() {
        // Given
        String nlpQuery = "查询用户表中的所有活跃用户";
        
        // 模拟表结构检索结果
        DatabaseTableSchema userSchema = createMockUserSchema();
        SchemaMatch schemaMatch = SchemaMatch.builder()
                .schema(userSchema)
                .score(0.85)
                .matchedText("用户表相关信息")
                .matchReason("表名匹配")
                .build();
        
        SchemaRetrievalResult schemaResult = SchemaRetrievalResult.builder()
                .query(nlpQuery)
                .matches(Collections.singletonList(schemaMatch))
                .totalResults(1)
                .build();
        
        // 模拟LLM响应
        String llmResponse = """
                ```sql
                SELECT * FROM t_cs_user WHERE status = 'ACTIVE'
                ```
                
                解释: 查询用户表中状态为活跃的所有用户记录
                """;
        
        // 设置Mock行为
        when(schemaRetrievalService.retrieveRelevantSchemas(eq(nlpQuery), anyInt(), anyDouble()))
                .thenReturn(schemaResult);
        when(dynamicModelManager.getChatModel(1L)).thenReturn(chatModel);
        when(chatModel.chat(any(UserMessage.class))).thenReturn(chatResponse);
        when(chatResponse.aiMessage()).thenReturn(aiMessage);
        when(aiMessage.text()).thenReturn(llmResponse);
        
        // When
        SqlGenerationResult result = nlpToSqlService.generateSql(nlpQuery);
        
        // Then
        assertTrue(result.getSuccess());
        assertNotNull(result.getGeneratedSql());
        assertTrue(result.getGeneratedSql().contains("SELECT"));
        assertTrue(result.getGeneratedSql().contains("t_cs_user"));
        assertTrue(result.getConfidence() > 0.0);
        assertEquals("查询用户表中的所有活跃用户", result.getOriginalQuery());
        assertNotNull(result.getExplanation());
    }
    
    @Test
    void testGenerateSql_NoRelevantTables() {
        // Given
        String nlpQuery = "查询不存在的表";
        
        SchemaRetrievalResult emptyResult = SchemaRetrievalResult.builder()
                .query(nlpQuery)
                .matches(Collections.emptyList())
                .totalResults(0)
                .build();
        
        when(schemaRetrievalService.retrieveRelevantSchemas(eq(nlpQuery), anyInt(), anyDouble()))
                .thenReturn(emptyResult);
        
        // When
        SqlGenerationResult result = nlpToSqlService.generateSql(nlpQuery);
        
        // Then
        assertFalse(result.getSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("未找到相关的表结构信息"));
    }
    
    @Test
    void testGenerateSql_DangerousKeywords() {
        // Given
        String nlpQuery = "删除所有用户数据";
        
        DatabaseTableSchema userSchema = createMockUserSchema();
        SchemaMatch schemaMatch = SchemaMatch.builder()
                .schema(userSchema)
                .score(0.75)
                .build();
        
        SchemaRetrievalResult schemaResult = SchemaRetrievalResult.builder()
                .query(nlpQuery)
                .matches(Collections.singletonList(schemaMatch))
                .totalResults(1)
                .build();
        
        // 模拟LLM返回危险SQL
        String dangerousResponse = """
                ```sql
                DELETE FROM t_cs_user
                ```
                """;
        
        when(schemaRetrievalService.retrieveRelevantSchemas(eq(nlpQuery), anyInt(), anyDouble()))
                .thenReturn(schemaResult);
        when(dynamicModelManager.getChatModel(1L)).thenReturn(chatModel);
        when(chatModel.chat(any(UserMessage.class))).thenReturn(chatResponse);
        when(chatResponse.aiMessage()).thenReturn(aiMessage);
        when(aiMessage.text()).thenReturn(dangerousResponse);
        
        // When
        SqlGenerationResult result = nlpToSqlService.generateSql(nlpQuery);
        
        // Then
        assertFalse(result.getSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("SQL安全验证失败"));
    }
    
    @Test
    void testGenerateSql_InvalidSqlResponse() {
        // Given
        String nlpQuery = "查询用户信息";
        
        DatabaseTableSchema userSchema = createMockUserSchema();
        SchemaMatch schemaMatch = SchemaMatch.builder()
                .schema(userSchema)
                .score(0.8)
                .build();
        
        SchemaRetrievalResult schemaResult = SchemaRetrievalResult.builder()
                .query(nlpQuery)
                .matches(Collections.singletonList(schemaMatch))
                .totalResults(1)
                .build();
        
        // 模拟LLM返回无效响应
        String invalidResponse = "抱歉，我无法理解您的查询";
        
        when(schemaRetrievalService.retrieveRelevantSchemas(eq(nlpQuery), anyInt(), anyDouble()))
                .thenReturn(schemaResult);
        when(dynamicModelManager.getChatModel(1L)).thenReturn(chatModel);
        when(chatModel.chat(any(UserMessage.class))).thenReturn(chatResponse);
        when(chatResponse.aiMessage()).thenReturn(aiMessage);
        when(aiMessage.text()).thenReturn(invalidResponse);
        
        // When
        SqlGenerationResult result = nlpToSqlService.generateSql(nlpQuery);
        
        // Then
        assertFalse(result.getSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("生成的SQL语句无效"));
    }
    
    /**
     * 创建模拟的用户表结构
     */
    private DatabaseTableSchema createMockUserSchema() {
        DatabaseTableSchema.ColumnInfo idColumn = DatabaseTableSchema.ColumnInfo.builder()
                .columnName("id")
                .dataType("BIGINT")
                .isPrimaryKey(true)
                .isAutoIncrement(true)
                .columnDescription("用户ID")
                .build();
        
        DatabaseTableSchema.ColumnInfo usernameColumn = DatabaseTableSchema.ColumnInfo.builder()
                .columnName("username")
                .dataType("VARCHAR")
                .maxLength(50)
                .columnDescription("用户名")
                .build();
        
        DatabaseTableSchema.ColumnInfo statusColumn = DatabaseTableSchema.ColumnInfo.builder()
                .columnName("status")
                .dataType("VARCHAR")
                .maxLength(20)
                .columnDescription("用户状态")
                .enumValues(Arrays.asList("ACTIVE", "INACTIVE", "DELETED"))
                .build();
        
        return DatabaseTableSchema.builder()
                .tableName("t_cs_user")
                .tableDescription("用户表")
                .businessDescription("存储用户基本信息和状态")
                .columns(Arrays.asList(idColumn, usernameColumn, statusColumn))
                .build();
    }
}