# 项目升级说明

## 升级内容

本次升级主要包含以下内容：

1. **JDK 升级**：
   - 从 JDK 8 升级到 JDK 17
   - 更新了Maven编译器配置以支持Java 17特性

2. **Spring Boot 升级**：
   - 从 Spring Boot 2.7.2 升级到 Spring Boot 3.4.4
   - 更新了父POM为Spring Boot Starter Parent 3.4.4

3. **依赖包命名空间迁移**：
   - 将 javax.* 包迁移到了 jakarta.* 命名空间
   - 更新了相关的验证API依赖

4. **数据库连接升级**：
   - 将 mysql-connector-java 替换为 mysql-connector-j
   - 对数据库连接URL添加了更多参数以提高兼容性

5. **MyBatis 替换**：
   - 使用 MyBatis Plus 3.x 替代了原来的 MyBatis

6. **新增微服务功能**：
   - 添加了 Spring Cloud 2023.0.1 支持
   - 添加了 Spring Cloud Alibaba 2022.0.0.0 支持
   - 添加了 Nacos 服务注册发现和配置中心支持

## 注意事项

1. **需要JDK 17环境**：
   - 确保开发环境和生产环境已安装JDK 17

2. **Nacos配置**：
   - 需要安装Nacos服务器，默认连接地址为 127.0.0.1:8848
   - Nacos配置示例位于 `/start/src/main/resources/nacos_config_export` 目录

3. **COLA框架兼容性**：
   - 保留了 COLA 5.0.0 框架，并做了兼容性修改

4. **数据库连接**：
   - 检查数据库连接配置是否正确，特别是URL中的参数部分

## 部署说明

1. **本地开发部署**：
   ```bash
   # 编译项目
   mvn clean package -DskipTests
   
   # 运行应用
   java -jar start/target/start-1.0.0-SNAPSHOT.jar
   ```

2. **使用Nacos**：
   - 启动Nacos服务
   - 将配置文件导入到Nacos配置中心
   - 启动应用，将自动连接到Nacos
