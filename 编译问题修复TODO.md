# SmartCS-Web 编译问题修复 TODO

## 编译错误 (15个) - ✅ 已修复

### 1. KnowledgeSearchTool.java - Metadata API 变化 ✅
**文件**: `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/app/tools/KnowledgeSearchTool.java:185`
**错误**: `找不到符号: 方法 forEach((key,value[...]\"\\n\"))`
**问题**: langchain4j 1.1.0版本中Metadata API发生变化，`metadata().forEach()`方法不存在
**修复**: ✅ 已修复 - 使用`metadata().toMap().forEach()`替代

### 2. EnhancedAppChatAssistant.java - 方法签名不匹配 (6个错误) ✅
**文件**: `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/app/ai/EnhancedAppChatAssistant.java`

#### 2.1 第60行 - chat方法 ✅
**错误**: 无法将接口方法chat应用到给定类型
**问题**: 方法签名不匹配，需要`ChatRequest`参数，但传入了多个独立参数
**修复**: ✅ 已修复 - 构建`ChatRequest`对象

#### 2.2 第81行 - chatStream方法 ✅
**错误**: 无法将接口方法chatStream应用到给定类型
**问题**: 方法签名不匹配，需要`ChatRequest`和`StreamingHandler`参数
**修复**: ✅ 已修复 - 构建`ChatRequest`对象和`StreamingHandler`

#### 2.3 第87行 - TokenStream API变化 ✅
**错误**: 找不到符号: 方法 onNext((token)->{[...]n); })
**问题**: langchain4j 1.1.0中TokenStream API发生变化
**修复**: ✅ 已修复 - 更新TokenStream的使用方式

#### 2.4 第127行 - chatWithRag方法 ✅
**错误**: 无法将接口方法chatWithRag应用到给定类型
**问题**: 方法签名不匹配，需要`ChatRequest`参数
**修复**: ✅ 已修复 - 构建`ChatRequest`对象

#### 2.5 第162行 - chatWithRagStream方法 ✅
**错误**: 无法将接口方法chatWithRagStream应用到给定类型
**问题**: 方法签名不匹配，需要`ChatRequest`和`StreamingHandler`参数
**修复**: ✅ 已修复 - 构建`ChatRequest`对象和`StreamingHandler`

#### 2.6 第168行 - TokenStream API变化 ✅
**错误**: 找不到符号: 方法 onNext((token)->{[...]n); })
**问题**: langchain4j 1.1.0中TokenStream API发生变化
**修复**: ✅ 已修复 - 更新TokenStream的使用方式

#### 2.7 第203行 - chatOnce方法不存在 ✅
**错误**: 找不到符号: 方法 chatOnce(java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)
**问题**: 接口中没有定义chatOnce方法
**修复**: ✅ 已修复 - 删除chatOnce方法，使用现有方法

#### 2.8 第233行 - chatOnceStream方法不存在 ✅
**错误**: 找不到符号: 方法 chatOnceStream(java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)
**问题**: 接口中没有定义chatOnceStream方法
**修复**: ✅ 已修复 - 删除chatOnceStream方法，使用现有方法

#### 2.9 第239行 - TokenStream API变化 ✅
**错误**: 找不到符号: 方法 onNext((token)->{[...]n); })
**问题**: langchain4j 1.1.0中TokenStream API发生变化
**修复**: ✅ 已修复 - 更新TokenStream的使用方式

### 3. AiAppChatServiceFactory.java - Builder API变化 (3个错误) ✅
**文件**: `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/app/service/AiAppChatServiceFactory.java`

#### 3.1 第127行 - Builder类不存在 ✅
**错误**: 找不到符号: 类 Builder
**问题**: langchain4j 1.1.0中AiServices.Builder API发生变化
**修复**: ✅ 已修复 - 使用`var builder`替代`AiServices.Builder<AiAppChatService>`

#### 3.2 第174行 - Builder类不存在 ✅
**错误**: 找不到符号: 类 Builder
**问题**: langchain4j 1.1.0中AiServices.Builder API发生变化
**修复**: ✅ 已修复 - 使用`var builder`替代`AiServices.Builder<AiAppChatService>`

#### 3.3 第203行 - Builder类不存在 ✅
**错误**: 找不到符号: 类 Builder
**问题**: langchain4j 1.1.0中AiServices.Builder API发生变化
**修复**: ✅ 已修复 - 使用`var builder`替代`AiServices.Builder<AiAppChatService>`

### 4. RedisChatMemoryStore.java - ChatMessage API变化 (2个错误) ✅
**文件**: `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/app/memory/RedisChatMemoryStore.java`

#### 4.1 第130行 - text()方法不存在 ✅
**错误**: 找不到符号: 方法 text()
**问题**: langchain4j 1.1.0中ChatMessage API发生变化，text()方法不存在
**修复**: ✅ 已修复 - 使用类型检查获取文本内容

#### 4.2 第130行 - text()方法不存在 ✅
**错误**: 找不到符号: 方法 text()
**问题**: langchain4j 1.1.0中ChatMessage API发生变化，text()方法不存在
**修复**: ✅ 已修复 - 使用类型检查获取文本内容

## 编译警告 (30个) - 待处理

### 1. Lombok警告 (2个)
- `OrderQueryDTO.java:9` - 需要添加`@EqualsAndHashCode(callSuper=false)`
- `CustomerListByNameQry.java:6` - 需要添加`@EqualsAndHashCode(callSuper=false)`

### 2. MapStruct映射警告 (28个)
多个Convertor类中存在未映射的目标属性，需要检查并修复映射配置：

#### ChunkConvertor.java
- 第18行: "vectorId, createTime, updateTime" 未映射
- 第25行: "isDeleted, createdBy, updatedBy, createdAt, updatedAt" 未映射

#### ModelConvertor.java
- 第26行: "featuresList, modelTypeStrings" 未映射
- 第36行: "featuresList, modelTypeStrings" 未映射

#### ProviderConvertor.java
- 第16行: "supportedModelTypesList" 未映射

#### 其他Convertor类
- 多个类存在类似的未映射属性问题

## 修复总结

✅ **高优先级问题已全部修复**：
1. ✅ KnowledgeSearchTool.java - Metadata API变化
2. ✅ EnhancedAppChatAssistant.java - 方法签名不匹配 (9个错误)
3. ✅ AiAppChatServiceFactory.java - Builder API变化 (3个错误)
4. ✅ RedisChatMemoryStore.java - ChatMessage API变化 (2个错误)

🔄 **待处理问题**：
- 编译警告 (30个) - 中低优先级，不影响编译

## 修复详情

### LangChain4j 1.1.0 API 适配
1. **Metadata API**: 使用`metadata().toMap().forEach()`替代`metadata().forEach()`
2. **ChatMessage API**: 使用类型检查获取文本内容
3. **TokenStream API**: 更新流式处理方式
4. **AiServices.Builder**: 使用`var builder`替代具体类型声明
5. **StreamingHandler**: 正确使用StreamingHandler API

### 方法签名修复
1. **ChatRequest构建**: 为所有chat方法构建正确的ChatRequest对象
2. **StreamingHandler使用**: 正确创建和使用StreamingHandler
3. **删除不存在的方法**: 移除接口中不存在的chatOnce和chatOnceStream方法

### 编译顺序
通过正确的Maven编译顺序（client -> domain -> infrastructure -> app -> adapter -> start）成功解决了所有编译错误。 