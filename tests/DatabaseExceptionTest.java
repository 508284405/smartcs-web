package com.leyue.smartcs;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库异常处理测试
 */
public class DatabaseExceptionTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void testSQLExceptionHandling() {
        // 测试SQL语法错误
        SQLException syntaxError = new SQLException("You have an error in your SQL syntax", "42000");
        Response response = handler.handleSQLException(syntaxError);
        
        assertEquals("DB_SYNTAX_ERROR", response.getErrCode());
        assertEquals("数据库查询语法错误", response.getErrMessage());
    }

    @Test
    public void testConnectionExceptionHandling() {
        // 测试连接失败
        SQLException connectionError = new SQLException("Connection refused", "08001");
        Response response = handler.handleSQLException(connectionError);
        
        assertEquals("DB_CONNECTION_ERROR", response.getErrCode());
        assertEquals("数据库连接失败", response.getErrMessage());
    }

    @Test
    public void testDuplicateKeyExceptionHandling() {
        // 测试重复键异常
        DuplicateKeyException duplicateKeyException = new DuplicateKeyException("Duplicate entry");
        Response response = handler.handleDuplicateKeyException(duplicateKeyException);
        
        assertEquals("DB_DUPLICATE_KEY", response.getErrCode());
        assertEquals("数据重复，违反唯一约束", response.getErrMessage());
    }

    @Test
    public void testDataAccessExceptionHandling() {
        // 测试数据访问异常
        DataAccessException dataAccessException = new DataAccessException("Data access failed") {};
        Response response = handler.handleDataAccessException(dataAccessException);
        
        assertEquals("DB_ACCESS_ERROR", response.getErrCode());
        assertEquals("数据访问异常", response.getErrMessage());
    }

    @Test
    public void testGenericExceptionWithDatabaseKeywords() {
        // 测试包含数据库关键词的通用异常
        Exception dbException = new Exception("SQL query failed: table not found");
        Response response = handler.handleException(dbException);
        
        assertEquals("DB_ERROR", response.getErrCode());
        assertEquals("数据库操作异常", response.getErrMessage());
    }

    @Test
    public void testConnectionPoolExceptionHandling() {
        // 测试连接池异常
        Exception poolException = new Exception("Connection pool timeout");
        Response response = handler.handleException(poolException);
        
        assertEquals("DB_CONNECTION_POOL_ERROR", response.getErrCode());
        assertEquals("数据库连接池异常", response.getErrMessage());
    }

    @Test
    public void testNonDatabaseExceptionHandling() {
        // 测试非数据库异常
        Exception nonDbException = new Exception("File not found");
        Response response = handler.handleException(nonDbException);
        
        assertEquals("SYSTEM_ERROR", response.getErrCode());
        assertEquals("系统异常", response.getErrMessage());
    }
} 