# bot模块删除清单

本清单列出删除bot模块功能所需移除的所有主要代码、配置、SQL、文档等，供架构重构和代码迁移参考。

---

## 1. smartcs-web-adapter
- `src/main/java/com/leyue/smartcs/web/bot/`
  - BotController.java
  - AdminBotPromptTemplateController.java
  - AdminBotProfileController.java

## 2. smartcs-web-app
- `src/main/java/com/leyue/smartcs/bot/`
  - executor/ 目录下所有文件（如 ChatCmdExe.java、ContextQryExe.java、BotProfileCreateCmdExe.java 等）
  - service/ 目录下所有文件（如 BotServiceImpl.java、BotSSEServiceImpl.java 等）
  - serviceimpl/ 目录下所有文件（如 BotProfileServiceImpl.java、BotPromptTemplateServiceImpl.java 等）

## 3. smartcs-web-client
- `src/main/java/com/leyue/smartcs/api/`
  - BotService.java
  - BotProfileService.java
  - BotSSEService.java
- `src/main/java/com/leyue/smartcs/dto/bot/`
  - 目录下所有DTO（如 BotChatRequest.java、BotProfileDTO.java、BotProfileCreateCmd.java 等）

## 4. smartcs-web-domain
- `src/main/java/com/leyue/smartcs/domain/bot/`
  - 目录下所有领域模型、枚举、Gateway、DomainService等

## 5. smartcs-web-infrastructure
- `src/main/java/com/leyue/smartcs/bot/`
  - gatewayimpl/ 目录下所有实现（如 LLMGatewayImpl.java 等）
  - 其他bot相关目录（如 advisor/、convertor/、mapper/、dataobject/ 等）
- `src/main/resources/mapper/bot/`
  - 目录下所有MyBatis XML
- `src/main/resources/sql/`
  - bot_profile.sql 及其他bot相关SQL脚本

## 6. 其他
- 相关测试代码、文档（如 Bot模块.md、集成测试说明.md 等）

---

> 注：如有遗漏文件，请结合实际目录补充。删除前请做好备份和依赖分析。 