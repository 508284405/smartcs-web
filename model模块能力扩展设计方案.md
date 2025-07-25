# model模块能力扩展设计方案

本方案用于指导将bot模块的推理/调用、任务管理、Prompt管理等能力迁移并统一到model模块，支撑未来模型推理、训练、评估等全流程能力。

---

## 1. 能力目标
- 统一承载模型推理/调用、推理任务管理、模型训练、模型评估、Prompt管理、上下文管理等能力。

## 2. 主要扩展点

### 2.1 API设计
- 新增/扩展REST接口：
  - `/api/model/infer`：模型推理（同步/异步/流式）
  - `/api/model/task`：推理/训练/评估任务管理
  - `/api/model/prompt`：Prompt模板管理
  - `/api/model/context`：上下文管理

### 2.2 Service与实现
- 在model模块下实现推理、训练、评估等Service（如 ModelInferenceService、ModelTrainingService、ModelEvaluationService 等）。
- 迁移并重构bot模块的推理、Prompt、上下文等实现到model模块。

### 2.3 DTO与命令
- 新增/扩展DTO（如 ModelInferRequest、ModelInferResponse、ModelTaskDTO、PromptTemplateDTO 等）。
- 复用或扩展现有的ModelDTO、ModelCreateCmd等。

### 2.4 Domain与Gateway
- 在domain层定义模型推理、训练、评估等核心领域服务与Gateway接口。
- 统一模型元数据、能力、状态、属性等的领域建模。

### 2.5 Infrastructure
- 实现模型推理、训练、评估等的基础设施支持（如任务队列、日志、缓存、上下文存储等）。
- 统一Prompt模板、上下文等的存储与管理。

### 2.6 数据库与表结构
- 合并/迁移bot_profile、bot_prompt_template等表结构到model相关表，或设计兼容映射。
- 新增训练、评估任务表（如 model_task、model_evaluation 等）。

### 2.7 依赖与适配
- 适配原有bot相关依赖，统一为model模块依赖。
- 适配LangChain4j、Spring AI等模型调用框架。

### 2.8 文档与测试
- 更新API文档、数据库文档、架构文档。
- 补充/调整单元测试、集成测试，确保新架构下功能完整。

---

> 注：本方案为初稿，具体迁移和实现细节需结合实际代码进一步细化。 