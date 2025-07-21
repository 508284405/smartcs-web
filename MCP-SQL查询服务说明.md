# MCP SQL查询服务说明

## 概述
本项目实现了基于MCP (Model Context Protocol) 的SQL查询服务，使用Spring AI MCP框架，提供安全的数据库查询能力。

## 功能特性

### 1. 安全的SQL查询
- **仅支持SELECT语句**：确保数据安全，防止数据被意外修改
- **SQL注入防护**：通过关键词过滤和模式匹配防止恶意SQL注入
- **结果限制**：默认显示前10条记录，防止返回过大数据集

### 2. 支持的工具

#### `executeSelectQuery(String sqlQuery)`
- **功能**：执行SELECT查询语句
- **参数**：SQL查询语句
- **返回**：格式化的查询结果
- **示例**：
  ```sql
  SELECT * FROM cs_user WHERE user_type = 1 LIMIT 5
  ```

#### `getTableSchema(String tableName)`
- **功能**：获取指定表的结构信息
- **参数**：表名
- **返回**：表的字段、类型、约束等信息
- **示例**：
  ```
  getTableSchema("cs_user")
  ```

#### `getTableList()`
- **功能**：获取当前数据库中所有表的列表
- **返回**：数据库表名列表

## 架构实现

### 1. 分层架构
```
adapter/         # REST API层
├── SqlQueryController.java

infrastructure/  # 基础设施层
├── mcp/
│   ├── SqlQueryToolsService.java      # SQL查询工具服务
│   └── config/
│       └── SqlMcpServerConfiguration.java  # MCP服务器配置
```

### 2. MCP集成
- **传输协议**：使用WebMVC SSE (Server-Sent Events)
- **端点**：`/mcp/sql/message` 和 `/mcp/sql/sse`
- **工具注册**：通过`ToolCallbackProvider`注册SQL查询工具

### 3. LangChain4j集成
- SQL查询工具同时集成到LangChain4j AI Services中
- 支持通过聊天机器人调用SQL查询功能

## 安全措施

### 1. SQL语句验证
```java
// 只允许SELECT语句
private static final Pattern SELECT_PATTERN = Pattern.compile(
    "^\\s*SELECT\\s+.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

// 危险关键词黑名单
private static final String[] DANGEROUS_KEYWORDS = {
    "DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "CREATE", "TRUNCATE", 
    "EXEC", "EXECUTE", "DECLARE", "UNION", "INFORMATION_SCHEMA"
};
```

### 2. 表名验证
- 只允许字母、数字和下划线组合的表名
- 防止SQL注入攻击

### 3. 错误处理
- 统一的异常处理和错误消息返回
- 详细的日志记录便于问题排查

## 使用方式

### 1. REST API测试
```bash
# 执行SQL查询
curl -X POST http://localhost:8080/api/mcp/sql/query \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT * FROM cs_user LIMIT 5"}'

# 查询表结构
curl http://localhost:8080/api/mcp/sql/schema/cs_user

# 获取表列表
curl http://localhost:8080/api/mcp/sql/tables
```

### 2. MCP客户端连接
- **消息端点**：`http://localhost:8080/mcp/sql/message`
- **SSE端点**：`http://localhost:8080/mcp/sql/sse`
- **服务器信息**：SQL-Query-MCP v1.0

### 3. AI聊天集成
通过聊天机器人可以直接使用自然语言查询数据库：
```
用户：查询所有客服用户
AI：我来为您查询客服用户信息
[调用SQL查询工具：SELECT * FROM cs_user WHERE user_type = 1]
```

## 配置说明

### 1. 数据库配置
确保application.yaml中配置了正确的数据库连接：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smartcs
    username: your_username
    password: your_password
```

### 2. MCP配置
```yaml
mcp:
  server:
    sql:
      enabled: true
      endpoint: "/mcp/sql"
      description: "SQL Query MCP Server"
```

## 依赖说明
```xml
<!-- MCP Server支持 -->
<dependency>
    <groupId>org.springframework.experimental.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    <version>1.0.0-M6</version>
</dependency>

<!-- MCP Client支持 -->
<dependency>
    <groupId>org.springframework.experimental.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
    <version>1.0.0-M6</version>
</dependency>
```

## 注意事项

1. **安全性**：请确保在生产环境中设置适当的数据库权限
2. **性能**：大数据集查询会被限制显示条数，避免影响系统性能
3. **监控**：建议启用查询日志监控，便于审计和问题排查
4. **权限**：根据业务需要限制可查询的表和字段

## 扩展建议

1. **权限控制**：可以基于用户角色限制可查询的表
2. **查询缓存**：对于重复查询可以增加缓存机制
3. **查询优化**：可以添加查询性能监控和优化建议
4. **数据脱敏**：对敏感数据字段进行脱敏处理