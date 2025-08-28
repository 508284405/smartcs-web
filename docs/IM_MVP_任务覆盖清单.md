# 企业内部 IM 系统 MVP 实现覆盖清单

本清单对照《docs/企业内部IM系统MVP架构设计.md》，罗列当前代码实现覆盖情况、接口与数据结构落地、以及待办任务与验证步骤。

## 覆盖总览

- [x] WebSocket/STOMP 端点、握手鉴权、用户路由与在线态
- [x] 私聊消息发送链路（校验→落库→跨节点分发→在线直推/离线落库）
- [x] 会话与历史消息查询 REST 接口
- [x] 离线消息与未读计数（DDL/Mapper/Gateway/Service/REST）
- [x] 群聊（DDL/Mapper/Gateway/Service/REST + WS 群发入口）
- [x] Kafka 跨节点分发与事件审计（im.direct/im.group/im.event）
- [x] 用户中心鉴权接入（WS 握手校验 token 并绑定 Principal）
- [ ] 生产级 Kafka 主题与分区/副本/保留策略配置、应用级开关
- [ ] 监控与压测用指标与告警（分发耗时、WS QPS、队列堆积等）

## 模块覆盖情况（DDD）

### smartcs-web-client（DTO/API 契约）
- [x] DTO：
  - `com.leyue.smartcs.dto.chat.ws.*`（`ChatMessage`/`AckMessage`/`WebSocketMessage`/`SessionStatusMessage`/`SystemMessage`）
  - `com.leyue.smartcs.dto.chat.group.*`（`GroupCreateCmd`/`UpdateGroupCmd`/`GroupMessageCmd`/`AddGroupMemberCmd`/`RemoveGroupMemberCmd`/`GroupDto`/`GroupMemberDto`）
  - `com.leyue.smartcs.dto.chat.offline.*`（`OfflineMessageAckCmd`/`OfflineMessagesDto`/`OfflineMessageSummaryDto`）
- [x] API：
  - `MessageService`、`MessageSendService`、`MessageValidatorService`
  - `GroupChatService`、`OfflineMessageService`、`UnreadCounterService`

### smartcs-web-domain（领域模型与网关接口）
- [x] 私聊/会话聚合：`Message`、`Session`，及 `MessageGateway`、`SessionGateway`
- [x] 离线与未读：`OfflineMessage`、`UnreadCounter`；`OfflineMessageGateway`、`UnreadCounterGateway`
- [x] 群聊：`Group`、`GroupMember`；`GroupGateway`

### smartcs-web-infrastructure（持久化/缓存/WS/Kafka/Redis）
- [x] WebSocket：
  - `WebSocketConfig`、`WebSocketHandshakeInterceptor`、`WebSocketAuthInterceptor`、`CustomHandshakeHandler`
  - 会话管理 `WebSocketSessionManager`（本地 Map + Redisson，键前缀：`ws:session:*`；在线集合：`ws:agents`/`ws:customers`）
  - 事件监听 `WebSocketEventListener`（连接成功系统消息、上线触发离线摘要推送、断开清理）
- [x] MyBatis-Plus + Mapper XML：
  - 会话/消息：`t_cs_session`、`t_cs_message`（既有）
  - 离线消息：`OfflineMessageMapper(.xml)` → 表 `t_im_offline_message`
  - 未读计数：`UnreadCounterMapper(.xml)` → 表 `t_im_unread_counter`
  - 群组/成员：`GroupMapper(.xml)`、`GroupMemberMapper(.xml)` → 表 `t_im_group`、`t_im_group_member`
- [x] Gateway 实现：
  - `MessageGatewayImpl`、`SessionGatewayImpl`
  - `OfflineMessageGatewayImpl`、`UnreadCounterGatewayImpl`
  - `GroupGatewayImpl`
- [x] SQL DDL（路径：`smartcs-web-infrastructure/src/main/resources/sql/modules/chat`）
  - `02_cs_session.sql`、`03_cs_message.sql`
  - `04_offline_message.sql`（含 `t_im_offline_message` 与 `t_im_unread_counter`）
  - `05_group.sql`（`t_im_group`、`t_im_group_member`）

### smartcs-web-app（应用服务与编排）
- [x] 私聊：
  - `MessageValidatorServiceImpl`、`MessageSendServiceImpl`
  - `MessageDistributionService`（Kafka 发布/消费，在线直推/离线落库，事件审计）
- [x] 会话：`SessionServiceImpl`
- [x] 离线与未读：`OfflineMessageServiceImpl`、`UnreadCounterServiceImpl`
- [x] 群聊：`GroupChatServiceImpl`（建群、改名、解散、加/退群、发消息、成员/权限校验）
- [x] 用户：`UserServiceImpl`（鉴权，透传到 `UserContext`）

### smartcs-web-adapter（REST/WebSocket 控制器）
- [x] WebSocket：`ChatWebSocketController`
  - 发送私聊：`/app/chat.sendMessage`
  - 发送群聊：`/app/group.sendMessage`
  - ACK：`/app/chat.ack`
  - 心跳：`/app/chat.heartbeat`
- [x] 历史查询：`ChatMessageController`
  - `GET /api/chat/messages/session/{sessionId}`
  - `GET /api/chat/messages/session/{sessionId}/page`
- [x] 离线与未读：`OfflineMessageController`
  - `GET /api/im/offline/summary`
  - `GET /api/im/offline/{conversationId}?userId=...&limit=...`
  - `POST /api/im/offline/ack`
  - `DELETE /api/im/offline/{conversationId}?userId=...`
  - `GET /api/im/offline/unread-counts?userId=...`
  - `GET /api/im/offline/unread-count?userId=...&conversationId=...`
  - `POST /api/im/offline/reset-unread?userId=...&conversationId=...`
- [x] 群聊管理：`GroupChatController`
  - `POST /api/im/groups`（建群） / `PUT /api/im/groups/{groupId}`（改名） / `DELETE /api/im/groups/{groupId}`（解散）
  - `GET /api/im/groups/{groupId}`（群信息）
  - `POST /api/im/groups/{groupId}/members`（加人） / `DELETE /api/im/groups/{groupId}/members/{memberId}`（移除/退群）
  - `GET /api/im/groups/{groupId}/members`（成员列表） / `GET /api/im/groups/{groupId}/members/{userId}/exists`
  - `POST /api/im/groups/{groupId}/messages`（REST 群发）
  - `GET /api/im/groups/created?userId=...`、`GET /api/im/groups/joined?userId=...`

### start（配置/启动）
- [x] Kafka/Redis/Cache 基础配置：`start/src/main/resources/application.yaml`、`redisson.yaml`
- [ ] IM 专用 Kafka 主题集中配置与开关（建议补充 `smartcs.im.kafka.topics` 与开关）

## 接口契约落地（节选）

WebSocket（STOMP）
- 端点：`/ws/chat`、`/ws/agent`（SockJS 支持）
- 订阅：用户点对点 `/user/queue/messages`、`/user/queue/heartbeat`、`/user/queue/session`、`/user/queue/offline-summary`
- 发送：`/app/chat.sendMessage`、`/app/group.sendMessage`、`/app/chat.ack`、`/app/chat.heartbeat`

REST（主要）
- 历史：`GET /api/chat/messages/session/{sessionId}`、`GET /api/chat/messages/session/{sessionId}/page`
- 离线：`GET /api/im/offline/summary`、`GET /api/im/offline/{conversationId}`、`POST /api/im/offline/ack`、`DELETE /api/im/offline/{conversationId}`、未读计数相关 3 个接口
- 群组：建群/成员管理/群发/查询（见上文 Adapter 小节）

## 存储与缓存

- MySQL 表：
  - 已有：`t_cs_session`、`t_cs_message`
  - 新增：`t_im_offline_message`、`t_im_unread_counter`、`t_im_group`、`t_im_group_member`
- Redis 键空间：
  - 在线态与路由：`ws:session:{userId}`（Hash: `sessionId`/`userType`）
  - 在线集合：`ws:agents`、`ws:customers`
  - 未读计数：以 DB 存储实现；如需高并发可扩展 Redis 主存、DB 兜底（后续可选）

## Kafka 主题与消费

- 主题：
  - `im.direct`：私聊跨节点转发（key=`receiverId`，用户内有序）
  - `im.group`：群聊分发（key=`groupId`，群内有序）
  - `im.event`：审计/统计/系统事件
- 消费组：
  - `im-dispatcher`：处理 direct 与 group 分发
  - `im-audit`：处理 event 审计

## 验证清单

1) WebSocket 建连与鉴权
- [ ] 使用有效 token 连接 `/ws/chat`，校验 Principal 绑定与系统消息 `CONNECT_SUCCESS`
- [ ] 订阅 `/user/queue/messages`、发送 `/app/chat.heartbeat`，收到心跳回执

2) 私聊消息流
- [ ] 客户/客服发送 `/app/chat.sendMessage`，另一端在线收到 `/user/queue/messages`
- [ ] 接收方离线时消息入库 `t_im_offline_message` 且 `t_im_unread_counter` 累加

3) 离线消息与未读
- [ ] 连接成功后收到 `/user/queue/offline-summary` 摘要
- [ ] `GET /api/im/offline/summary` 与 `/{conversationId}` 返回正确
- [ ] `POST /api/im/offline/ack` 后相应离线记录删除、未读计数减少

4) 群聊
- [ ] `POST /api/im/groups` 建群并自动加入群主与成员
- [ ] `POST /api/im/groups/{groupId}/messages` 或 `/app/group.sendMessage` 群发，在线直推/离线落库生效
- [ ] 成员管理与权限（群主/管理员）生效

5) 跨节点分发（需要 Kafka）
- [ ] 观察 `MessageDistributionService` 日志，`im.direct`/`im.group` 发布与消费成功
- [ ] 压测下消息局部有序与不重复

## 待办与建议

- [ ] Kafka 主题初始化与生产配置（分区/副本/保留），在 `start` 模块集中化 `smartcs.im.kafka.topics` 与开关
- [ ] 观测性：消息分发耗时、WS 在线数/订阅数、离线堆积量、Kafka Lag 等指标与告警
- [ ] 幂等/重试：端到端去重策略与失败重试、DLQ
- [ ] Redis 键空间可选扩展：`im:unread:{userId}`（Hash）与 `im:route:{userId}`（String）

## 参考文件路径（节选）

- 架构文档：`docs/企业内部IM系统MVP架构设计.md`
- WS 配置：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/config/websocket/`
- WS 控制器：`smartcs-web-adapter/src/main/java/com/leyue/smartcs/websocket/ChatWebSocketController.java`
- 历史消息 REST：`smartcs-web-adapter/src/main/java/com/leyue/smartcs/web/message/ChatMessageController.java`
- 离线/未读 REST：`smartcs-web-adapter/src/main/java/com/leyue/smartcs/web/message/OfflineMessageController.java`
- 群聊 REST：`smartcs-web-adapter/src/main/java/com/leyue/smartcs/web/group/GroupChatController.java`
- 应用服务：`smartcs-web-app/src/main/java/com/leyue/smartcs/chat/serviceimpl/`
- 网关实现：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/chat/`
- SQL DDL：`smartcs-web-infrastructure/src/main/resources/sql/modules/chat/`

---

构建与运行参考：
- 编译：`mvn -q -DskipTests compile`
- 运行：`cd start && mvn spring-boot:run`
- 测试单模：`mvn -pl smartcs-web-infrastructure test`

