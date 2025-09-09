# API Key加密存储实现指南

## 概述

本实现为模型提供商的API Key提供了强加密（AES-GCM）存储方案，确保敏感信息的安全性。采用"无KMS环境"的本地密钥托管方案，支持后期平滑迁移至外部密钥管理服务。

## 核心特性

### 安全特性
- **强加密算法**: AES-256-GCM，提供机密性和完整性保护
- **随机IV**: 每次加密使用不同的96位初始化向量
- **密钥轮换**: 支持多密钥共存，便于密钥轮换
- **即时解密**: 仅在模型调用时解密，使用后立即释放内存引用
- **全链脱敏**: 请求、响应、异常和审计日志全面脱敏

### 功能特性
- **前端友好**: 支持"留空不修改"和脱敏显示
- **向后兼容**: 过渡期支持明文和密文并存
- **自动迁移**: 提供数据迁移工具和脚本
- **易于扩展**: 支持后期迁移至KeyStore/Jasypt/Vault

## 实现架构

### 数据库层
```sql
ALTER TABLE t_model_provider ADD COLUMN (
    api_key_cipher VARBINARY(1024) COMMENT 'API Key 密文',
    api_key_iv VARBINARY(32) COMMENT '加密初始化向量', 
    api_key_kid VARCHAR(64) COMMENT '密钥ID'
);
```

### 配置层
```yaml
smartcs:
  secrets:
    activeKid: "prod-key-2024"
    algorithm: "AES"
    transformation: "AES/GCM/NoPadding"
    keys:
      "prod-key-2024": "Base64编码的256位密钥"
```

### 服务层组件

1. **SecretConfig** - 密钥管理配置
2. **SecretCryptoService** - 加密解密服务
3. **LogDesensitizationUtil** - 日志脱敏工具
4. **ApiKeyMigrationUtil** - 数据迁移工具

## 部署指南

### 1. 密钥生成和配置

#### 生成加密密钥
```java
// 使用工具类生成测试密钥
String key = SecretCryptoService.generateTestKey();
System.out.println("Generated key: " + key);
```

#### 配置密钥（生产环境）
```yaml
smartcs:
  secrets:
    activeKid: "prod-key-2024"
    keys:
      "prod-key-2024": "你生成的实际密钥"
```

### 2. 数据库迁移

#### 执行DDL
```sql
-- 添加加密字段
ALTER TABLE t_model_provider ADD COLUMN (
    api_key_cipher VARBINARY(1024) COMMENT 'API Key 密文（AES-GCM加密）',
    api_key_iv VARBINARY(32) COMMENT 'API Key 加密初始化向量',
    api_key_kid VARCHAR(64) COMMENT 'API Key 加密密钥ID'
);
```

#### 执行数据迁移
```yaml
# 启用迁移开关
smartcs:
  migration:
    encrypt-api-keys: true
```

启动应用程序，系统会自动执行API Key加密迁移。

### 3. 验证部署

#### 检查加密状态
```sql
SELECT 
    id, provider_type,
    CASE 
        WHEN api_key_cipher IS NOT NULL THEN '已加密'
        WHEN api_key IS NOT NULL THEN '待加密'
        ELSE '无密钥'
    END as encryption_status
FROM t_model_provider 
WHERE is_deleted = 0;
```

#### 测试模型调用
- 确保现有模型调用功能正常
- 检查日志中无明文API Key泄露
- 验证前端UI正确显示脱敏状态

### 4. 清理和优化

#### 关闭迁移开关
```yaml
smartcs:
  migration:
    encrypt-api-keys: false  # 迁移完成后关闭
```

#### 可选：清理明文字段
```sql
-- 验证所有API Key已加密后执行
UPDATE t_model_provider 
SET api_key = NULL 
WHERE api_key_cipher IS NOT NULL 
  AND LENGTH(api_key_cipher) > 0;
```

## 使用说明

### 前端操作

#### 创建提供商
- 输入完整的API Key
- 系统自动加密存储
- 不在响应中返回明文

#### 编辑提供商
- 留空API Key字段表示不修改
- 输入新值则覆盖原密钥
- 显示"已设置"/"未设置"状态

#### 查看列表
- 显示脱敏的API Key状态
- 显示"••••••••••••••••"掩码
- 不在前端缓存明文信息

### API接口

#### ProviderDTO响应
```json
{
  "id": 1,
  "providerType": "OPENAI",
  "hasApiKey": true,
  "apiKeyMasked": "••••••••••••••••",
  "endpoint": "https://api.openai.com/v1"
}
```

#### 更新请求
```json
{
  "id": 1,
  "providerType": "OPENAI", 
  "apiKey": "",  // 空值表示不修改
  "endpoint": "https://api.openai.com/v1"
}
```

## 安全考虑

### 密钥管理
- **生产环境**: 使用环境变量或外部密钥管理服务
- **密钥长度**: 256位AES密钥（Base64编码约44字符）
- **密钥轮换**: 定期更新activeKid，保留历史密钥用于解密
- **访问控制**: 限制密钥配置的访问权限

### 数据保护
- **传输安全**: HTTPS加密传输
- **存储安全**: 数据库加密，备份加密
- **日志安全**: 全链路日志脱敏
- **内存安全**: 即时解密，使用后立即清理

### 监控和审计
- **加密状态监控**: 定期检查加密状态
- **解密操作审计**: 记录解密操作（脱敏）
- **异常监控**: 监控解密失败和密钥错误
- **性能监控**: 监控加密解密性能影响

## 故障排除

### 常见问题

#### 1. 加密失败
**现象**: 创建或更新提供商时报错"API Key加密失败"

**排查步骤**:
- 检查密钥配置是否正确
- 验证activeKid是否存在于keys中
- 确认密钥格式为有效的Base64编码

#### 2. 解密失败
**现象**: 模型调用时报错"API Key解密失败"

**排查步骤**:
- 检查密钥ID(kid)对应的密钥是否存在
- 验证数据库中的密文数据完整性
- 确认IV和密文长度正确

#### 3. 迁移失败
**现象**: 数据迁移过程中部分记录失败

**排查步骤**:
- 查看详细错误日志
- 检查原始API Key数据完整性
- 验证数据库事务状态
- 必要时进行单条记录手动迁移

### 恢复方案

#### 密钥丢失恢复
1. 如果有密钥备份，恢复密钥配置
2. 如果密钥完全丢失，需要重新设置所有API Key
3. 紧急情况下可以临时启用明文兼容模式

#### 数据损坏恢复
1. 从最近的数据库备份恢复
2. 重新执行数据迁移流程
3. 验证恢复后的加密状态

## 性能影响评估

### 加密性能
- **加密时间**: 单次加密约1-2ms
- **解密时间**: 单次解密约1-2ms  
- **内存开销**: 每个提供商额外约1KB存储
- **CPU开销**: AES-GCM硬件加速下影响可忽略

### 优化建议
- 模型实例缓存减少重复解密
- 批量操作时考虑并行处理
- 定期清理不用的密钥降低内存占用

## 后期扩展

### 外部密钥管理集成
支持后期迁移至以下服务：
- AWS KMS / Azure Key Vault
- HashiCorp Vault
- Kubernetes Secrets
- 企业级密钥管理系统

### 集成步骤
1. 实现对应的KeyProvider接口
2. 更新SecretConfig支持多种密钥源
3. 修改SecretCryptoService支持动态密钥获取
4. 逐步迁移存量数据

### 高级特性
- 自动密钥轮换
- 密钥版本管理  
- 分级加密（不同敏感等级使用不同密钥）
- 审计日志增强

## 总结

本实现提供了完整的API Key加密存储解决方案，具有以下优势：
- **安全性高**: AES-GCM强加密，全链路脱敏
- **易于使用**: 前端友好，操作简单
- **向后兼容**: 平滑过渡，不影响现有功能
- **扩展性强**: 支持后期升级到外部密钥管理
- **维护友好**: 完整的迁移工具和监控机制

通过遵循本指南，可以安全有效地部署和维护API Key加密存储功能。