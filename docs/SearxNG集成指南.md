# SearxNG 集成指南

本文档介绍如何在 SmartCS 项目中集成 SearxNG 搜索引擎，基于 LangChain4j 1.1.0 + langchain4j-community-web-search-engine-searxng。

## 1. 依赖配置

### 1.1 Maven 依赖

已在 `smartcs-web-infrastructure/pom.xml` 中添加：

```xml
<!-- SearxNG 搜索引擎依赖 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-community-web-search-engine-searxng</artifactId>
</dependency>
```

### 1.2 应用配置

在 `application.yaml` 中配置：

```yaml
smartcs:
  ai:
    web-search:
      enabled: true
      searxng:
        base-url: "https://searx.be"  # 或本地实例 http://localhost:8080
        timeout: 10
        max-results: 8
        user-agent: "SmartCS-Web/1.0.0"
        result-language: "zh-CN"
        safe-content: true
```

## 2. 本地 SearxNG 部署

### 2.1 使用 Docker Compose

```bash
# 启动本地 SearxNG 实例
docker-compose -f docker-compose.searxng.yml up -d

# 验证服务
curl http://localhost:8080
```

### 2.2 手动 Docker 部署

```bash
docker run -d \
  --name smartcs-searxng \
  -p 8080:8080 \
  -v searxng_data:/etc/searxng \
  searxng/searxng:latest
```

## 3. 在 RAG 中使用

### 3.1 自动集成

SearxNG 已通过 `RagChatServiceConfig` 自动集成到 RAG 流程中：

- **知识库检索**: 使用向量存储
- **Web 搜索**: 使用 SearxNG
- **数据库查询**: 使用 SQL 查询

### 3.2 查询路由

系统会根据查询内容自动选择合适的检索器：

```java
@Bean
public QueryRouter queryRouter(ContentRetriever contentRetriever, 
                              ContentRetriever webContentRetriever, 
                              ContentRetriever sqlQueryContentRetriever) {
    return LanguageModelQueryRouter.builder()
            .chatModel(chatModel)
            .retrieverToDescription(Map.of(
                contentRetriever, "知识库检索", 
                webContentRetriever, "Web搜索", 
                sqlQueryContentRetriever, "数据库查询"
            ))
            .build();
}
```

## 4. 测试验证

### 4.1 运行集成测试

```bash
# 确保本地 SearxNG 运行中
docker-compose -f docker-compose.searxng.yml up -d

# 运行测试
mvn test -Dtest=SearxngIntegrationTest
```

### 4.2 手动测试

```bash
# 测试 SearxNG 连接
curl "http://localhost:8080/search?q=Spring%20Boot&format=json"
```

## 5. 生产环境配置

### 5.1 自托管 SearxNG

推荐在生产环境自托管 SearxNG 实例：

```yaml
smartcs:
  ai:
    web-search:
      searxng:
        base-url: "https://your-searxng.example.com"
        timeout: 15
        max-results: 10
        user-agent: "SmartCS-Production/1.0.0"
```

### 5.2 高可用配置

```yaml
smartcs:
  ai:
    web-search:
      searxng:
        # 主实例
        primary:
          base-url: "https://searxng-1.example.com"
        # 备用实例
        fallback:
          base-url: "https://searxng-2.example.com"
```

## 6. 常见问题

### 6.1 HTTP 403/429 错误

**原因**: 公共实例限流
**解决**: 使用自托管实例或增加请求间隔

### 6.2 中文搜索结果为空

**原因**: 语言配置问题
**解决**: 确保 `result-language: "zh-CN"` 并启用中文搜索源

### 6.3 连接超时

**原因**: 网络问题或实例不可用
**解决**: 检查网络连接，增加超时时间

## 7. 监控和日志

### 7.1 日志配置

```yaml
logging:
  level:
    com.leyue.smartcs.rag.config: DEBUG
    dev.langchain4j.web.search: DEBUG
```

### 7.2 性能监控

```java
// 在 SearxngWebSearchConfig 中添加监控
@Bean
public SearxngWebSearchEngine searxngWebSearchEngine() {
    log.info("初始化 SearxNG 搜索引擎，配置: {}", searxngProperties);
    
    return SearxngWebSearchEngine.builder()
            .baseUrl(searxngProperties.getBaseUrl())
            .timeout(Duration.ofSeconds(searxngProperties.getTimeout()))
            .maxResults(searxngProperties.getMaxResults())
            .userAgent(searxngProperties.getUserAgent())
            .resultLanguage(searxngProperties.getResultLanguage())
            .safeContent(searxngProperties.isSafeContent())
            .build();
}
```

## 8. 最佳实践

1. **使用自托管实例**: 避免公共实例限流
2. **合理设置超时**: 根据网络情况调整
3. **监控使用量**: 避免过度调用
4. **错误处理**: 实现降级机制
5. **缓存策略**: 对重复查询进行缓存

## 9. 版本兼容性

- **LangChain4j**: 1.1.0
- **SearxNG**: 最新版本
- **Java**: 17+
- **Spring Boot**: 3.4.4

---

通过以上配置，您的 SmartCS 项目已成功集成 SearxNG 搜索引擎，可以在 RAG 流程中提供实时 Web 搜索能力。 