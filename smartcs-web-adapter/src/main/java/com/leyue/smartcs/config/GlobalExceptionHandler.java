package com.leyue.smartcs.config;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.SocketException;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientException;
import java.sql.SQLNonTransientException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Response handleBizException(BizException e) {
        log.error("业务异常: ", e);
        return Response.buildFailure(e.getErrCode(), e.getMessage());
    }
    
//    @ExceptionHandler(AccessDeniedException.class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    public Response handleAccessDeniedException(AccessDeniedException e) {
//        log.warn("权限不足: {}", e.getMessage());
//        return Response.buildFailure("ACCESS_DENIED", "权限不足");
//    }
    
    /**
     * 处理SSE客户端异步请求不可用异常（客户端断开连接）
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.warn("SSE客户端断开连接: {}", e.getMessage());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 处理客户端中止异常（SSE连接断开）
     */
    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> handleClientAbortException(org.apache.catalina.connector.ClientAbortException e) {
        log.warn("客户端中止连接: {}", e.getMessage());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 处理Socket异常（网络连接中断）
     */
    @ExceptionHandler(SocketException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> handleSocketException(SocketException e) {
        log.warn("网络连接异常: {}", e.getMessage());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 处理IO异常（可能是SSE连接断开）
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> handleIOException(IOException e, HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        // 只有SSE相关的请求才特殊处理
        if (requestPath != null && (requestPath.contains("/chat") || requestPath.contains("/sse"))) {
            log.warn("SSE连接IO异常: path={}, message={}", requestPath, e.getMessage());
            return ResponseEntity.noContent().build();
        }
        // 其他IO异常交给通用异常处理器
        throw new RuntimeException("IO异常", e);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Response.buildFailure("UNAUTHORIZED", "认证失败");
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("参数校验失败: {}", message);
        return Response.buildFailure("INVALID_PARAMETER", message);
    }
    
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定失败: {}", message);
        return Response.buildFailure("INVALID_PARAMETER", message);
    }
    
    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleSQLException(SQLException e) {
        log.error("数据库SQL异常", e);
        
        // 处理SQL超时异常
        if (e instanceof SQLTimeoutException) {
            return Response.buildFailure("DB_TIMEOUT_ERROR", "数据库查询超时");
        }
        
        // 处理可重试的SQL异常
        if (e instanceof SQLTransientException) {
            return Response.buildFailure("DB_TRANSIENT_ERROR", "数据库临时异常，请稍后重试");
        }
        
        // 处理不可重试的SQL异常
        if (e instanceof SQLNonTransientException) {
            return Response.buildFailure("DB_NON_TRANSIENT_ERROR", "数据库永久异常");
        }
        
        // 根据SQL状态码分类处理
        String sqlState = e.getSQLState();
        if (sqlState != null) {
            switch (sqlState) {
                case "28000": // 认证失败
                    return Response.buildFailure("DB_AUTH_ERROR", "数据库认证失败");
                case "42000": // 语法错误
                    return Response.buildFailure("DB_SYNTAX_ERROR", "数据库查询语法错误");
                case "23000": // 约束违反
                    return Response.buildFailure("DB_CONSTRAINT_ERROR", "数据约束违反");
                case "08001": // 连接失败
                    return Response.buildFailure("DB_CONNECTION_ERROR", "数据库连接失败");
                case "08003": // 连接断开
                    return Response.buildFailure("DB_CONNECTION_LOST", "数据库连接已断开");
                case "08004": // 连接被拒绝
                    return Response.buildFailure("DB_CONNECTION_REFUSED", "数据库连接被拒绝");
                case "08006": // 连接失败
                    return Response.buildFailure("DB_CONNECTION_FAILED", "数据库连接失败");
                case "08007": // 连接异常断开
                    return Response.buildFailure("DB_CONNECTION_ABORTED", "数据库连接异常断开");
                case "08S01": // 通信链路失败
                    return Response.buildFailure("DB_COMMUNICATION_ERROR", "数据库通信失败");
                case "21S01": // 插入值列表不匹配
                    return Response.buildFailure("DB_INSERT_ERROR", "数据插入格式错误");
                case "21S02": // 派生表列数不匹配
                    return Response.buildFailure("DB_COLUMN_MISMATCH", "数据库列数不匹配");
                case "22001": // 字符串数据右截断
                    return Response.buildFailure("DB_DATA_TRUNCATED", "数据长度超出限制");
                case "22003": // 数值超出范围
                    return Response.buildFailure("DB_NUMERIC_OVERFLOW", "数值超出范围");
                case "23001": // 外键约束违反
                    return Response.buildFailure("DB_FOREIGN_KEY_ERROR", "外键约束违反");
                case "23505": // 唯一约束违反
                    return Response.buildFailure("DB_UNIQUE_CONSTRAINT_ERROR", "数据重复，违反唯一约束");
                case "23503": // 外键约束违反
                    return Response.buildFailure("DB_FOREIGN_KEY_ERROR", "外键约束违反");
                case "23514": // 检查约束违反
                    return Response.buildFailure("DB_CHECK_CONSTRAINT_ERROR", "数据检查约束违反");
                case "42S02": // 表不存在
                    return Response.buildFailure("DB_TABLE_NOT_FOUND", "数据库表不存在");
                case "42S22": // 列不存在
                    return Response.buildFailure("DB_COLUMN_NOT_FOUND", "数据库列不存在");
                case "42001": // 语法错误或访问规则违反
                    return Response.buildFailure("DB_SYNTAX_ERROR", "数据库语法错误");
                case "42501": // 权限不足
                    return Response.buildFailure("DB_PERMISSION_ERROR", "数据库权限不足");
                case "42601": // 语法错误
                    return Response.buildFailure("DB_SYNTAX_ERROR", "数据库语法错误");
                case "42703": // 未定义的列
                    return Response.buildFailure("DB_COLUMN_UNDEFINED", "数据库列未定义");
                case "42704": // 未定义的对象
                    return Response.buildFailure("DB_OBJECT_UNDEFINED", "数据库对象未定义");
                case "42804": // 数据类型不匹配
                    return Response.buildFailure("DB_TYPE_MISMATCH", "数据类型不匹配");
                case "42883": // 未定义的函数
                    return Response.buildFailure("DB_FUNCTION_UNDEFINED", "数据库函数未定义");
                case "42P01": // 未定义的表
                    return Response.buildFailure("DB_TABLE_UNDEFINED", "数据库表未定义");
                case "42P02": // 未定义的参数
                    return Response.buildFailure("DB_PARAMETER_UNDEFINED", "数据库参数未定义");
                default:
                    return Response.buildFailure("DB_ERROR", "数据库操作异常");
            }
        }
        
        return Response.buildFailure("DB_ERROR", "数据库操作异常");
    }
    
    /**
     * 处理Spring数据访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleDataAccessException(DataAccessException e) {
        log.error("数据访问异常", e);
        
        // 检查是否为SQL语法错误
        if (e instanceof BadSqlGrammarException) {
            return Response.buildFailure("DB_SYNTAX_ERROR", "数据库查询语法错误");
        }
        
        // 检查是否为未分类的SQL异常
        if (e instanceof UncategorizedSQLException) {
            return Response.buildFailure("DB_UNCATEGORIZED_ERROR", "数据库操作异常");
        }
        
        return Response.buildFailure("DB_ACCESS_ERROR", "数据访问异常");
    }
    
    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据完整性违反异常", e);
        return Response.buildFailure("DB_INTEGRITY_ERROR", "数据完整性违反");
    }
    
    /**
     * 处理重复键异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("重复键异常", e);
        return Response.buildFailure("DB_DUPLICATE_KEY", "数据重复，违反唯一约束");
    }
    
    /**
     * 处理事务异常
     */
    @ExceptionHandler(TransactionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleTransactionException(TransactionException e) {
        log.error("事务异常", e);
        return Response.buildFailure("DB_TRANSACTION_ERROR", "数据库事务异常");
    }
    
    /**
     * 处理事务系统异常
     */
    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleTransactionSystemException(TransactionSystemException e) {
        log.error("事务系统异常", e);
        return Response.buildFailure("DB_TRANSACTION_SYSTEM_ERROR", "数据库事务系统异常");
    }
    
    /**
     * 处理MyBatis异常
     */
    @ExceptionHandler(org.apache.ibatis.exceptions.PersistenceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleMyBatisException(org.apache.ibatis.exceptions.PersistenceException e) {
        log.error("MyBatis持久化异常", e);
        return Response.buildFailure("DB_PERSISTENCE_ERROR", "数据持久化异常");
    }
    
    /**
     * 处理MyBatis-Plus异常
     */
    @ExceptionHandler(com.baomidou.mybatisplus.core.exceptions.MybatisPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleMyBatisPlusException(com.baomidou.mybatisplus.core.exceptions.MybatisPlusException e) {
        log.error("MyBatis-Plus异常", e);
        return Response.buildFailure("DB_MYBATIS_PLUS_ERROR", "数据库操作异常");
    }
    
    /**
     * 处理通用异常，区分数据库异常和其他系统异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常", e);
        
        // 检查是否为SSE请求，避免转换器冲突
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getContentType();
        String requestPath = request.getRequestURI();
        
        if ((acceptHeader != null && acceptHeader.contains("text/event-stream")) ||
            (contentType != null && contentType.contains("text/event-stream")) ||
            (requestPath != null && (requestPath.contains("/chat") || requestPath.contains("/sse")))) {
            log.warn("SSE请求发生异常，返回无内容响应以避免转换器冲突: path={}, error={}", requestPath, e.getMessage());
            return ResponseEntity.noContent().build();
        }
        
        // 检查是否为数据库相关异常
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SQLException) {
                return handleSQLException((SQLException) cause);
            }
            if (cause instanceof DataAccessException) {
                return handleDataAccessException((DataAccessException) cause);
            }
            if (cause instanceof TransactionException) {
                return handleTransactionException((TransactionException) cause);
            }
            cause = cause.getCause();
        }
        
        // 检查异常消息是否包含数据库相关关键词
        String message = e.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            if (lowerMessage.contains("sql") || lowerMessage.contains("database") || 
                lowerMessage.contains("mysql") || lowerMessage.contains("connection") ||
                lowerMessage.contains("table") || lowerMessage.contains("column") ||
                lowerMessage.contains("constraint") || lowerMessage.contains("foreign key") ||
                lowerMessage.contains("unique") || lowerMessage.contains("duplicate") ||
                lowerMessage.contains("transaction") || lowerMessage.contains("timeout") ||
                lowerMessage.contains("connection pool") || lowerMessage.contains("datasource")) {
                
                // 进一步细分连接池相关异常
                if (lowerMessage.contains("connection pool") || lowerMessage.contains("connection timeout")) {
                    return Response.buildFailure("DB_CONNECTION_POOL_ERROR", "数据库连接池异常");
                }
                if (lowerMessage.contains("connection refused") || lowerMessage.contains("connection failed")) {
                    return Response.buildFailure("DB_CONNECTION_ERROR", "数据库连接失败");
                }
                
                return Response.buildFailure("DB_ERROR", "数据库操作异常");
            }
        }
        
        return Response.buildFailure("SYSTEM_ERROR", "系统异常");
    }
}