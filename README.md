# SmartCS - 智能客服系统

SmartCS是一个基于COLA架构的智能客服系统，集成了大语言模型能力，支持知识库检索问答（RAG）。

## 模块结构

- **Knowledge-Service**: 负责知识库管理和检索
- **Bot-Service**: 负责LLM集成和智能问答
- **Session-Service**: 负责会话管理
- **Chat-Service**: 负责聊天界面和交互

## 开发环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.0+

## 配置与启动

### 1. 配置环境变量

```bash
# 配置OpenAI API密钥
export OPENAI_API_KEY=your-api-key
# 可选：配置OpenAI API基础URL（如果使用代理）
export OPENAI_BASE_URL=https://your-proxy-url/v1
```

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS smartcs DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 编译项目

```bash
cd smartcs-web
mvn clean install -DskipTests
```

### 4. 启动项目

```bash
cd start
mvn spring-boot:run
```

## API接口

### Bot-Service接口

- **POST /api/bot/chat**: 智能问答接口
- **GET /api/bot/context/{sessionId}**: 获取会话上下文
- **DELETE /api/bot/context/{sessionId}**: 删除会话上下文

### Knowledge-Service接口

- **POST /api/knowledge/search/text**: 关键词搜索
- **POST /api/knowledge/search/vector**: 向量搜索
- **POST /api/knowledge/faq**: 创建/更新FAQ
- **GET /api/knowledge/faq**: 查询FAQ列表
- **POST /api/knowledge/doc**: 上传文档
- **GET /api/knowledge/doc**: 查询文档列表

## 技术栈

- Spring Boot 3.4.4
- LangChain4j 1.0.0-beta3 (LLM集成框架)
- MyBatis-Plus 3.5.12
- Redisson (Redis客户端)
- COLA架构 (5.0.0)
- MapStruct 1.5.5 (对象映射)
- Nacos 2.2.5 (服务发现与配置)

## 架构特点

- **分层架构**: 采用COLA架构，清晰的分层设计
- **LLM集成**: 基于LangChain4j，支持多种大语言模型
- **RAG支持**: 支持知识库检索增强生成
- **向量存储**: 集成Redis向量存储，支持语义搜索
- **聊天记忆**: 支持会话上下文管理
- **工具调用**: 支持Function Calling和工具集成

## 配置说明

### LangChain4j配置

在`application.yaml`中配置LangChain4j相关参数：

```yaml
# LangChain4j OpenAI配置
langchain4j:
  openai:
    api-key: ${OPENAI_API_KEY}
    base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
    embedding:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
      model: text-embedding-ada-002
```

### 向量存储配置

```yaml
# Redis配置（用于向量存储）
spring:
  redis:
    host: localhost
    port: 6379
    password: your-password
    database: 0
```

## 开发指南

### 添加新的AI功能

1. 在`domain`层定义Gateway接口
2. 在`infrastructure`层实现Gateway，集成LangChain4j
3. 在`app`层编写业务逻辑
4. 在`adapter`层暴露API接口

### 扩展LLM模型

1. 在`ModelBeanManagerService`中添加新的模型创建逻辑
2. 更新`BotProfile`配置支持新的厂商类型
3. 在配置文件中添加相应的API配置

## 注意事项

- 确保OpenAI API密钥配置正确
- 向量存储需要Redis支持向量操作
- 大模型调用可能存在延迟，建议使用流式响应
- 生产环境建议配置API代理以提高稳定性 