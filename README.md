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
- Spring AI 1.0.0-M6 (OpenAI集成)
- MyBatis-Plus 3.5.12
- Redisson (Redis客户端)
- COLA架构 (5.0.0) 