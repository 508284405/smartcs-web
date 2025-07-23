# QWEN.md

This file provides guidance to Qwen Code when working with code in this repository.

## Build & Development Commands

### Maven Commands
```bash
# Clean and build the entire project
mvn clean install -DskipTests

# Run the application (from start module)
cd start && mvn spring-boot:run

# Run with specific profile
cd start && mvn spring-boot:run -Dspring.profiles.active=dev

# Run tests for specific module
mvn test -pl smartcs-web-app

# Package the application
mvn clean package -DskipTests
```

### Environment Setup
```bash
# Required environment variables
export OPENAI_API_KEY=your-api-key
export OPENAI_BASE_URL=https://your-proxy-url/v1  # Optional, for proxy

# Create database
CREATE DATABASE IF NOT EXISTS smartcs DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## Architecture Overview

This is a Spring Boot 3.4.4 application using COLA (Clean Object-oriented and Layered Architecture) with the following modules:

### Module Structure
- **smartcs-web-adapter**: Web layer - REST controllers, filters, configuration
- **smartcs-web-client**: API definitions and DTOs for external contracts
- **smartcs-web-app**: Application layer - business logic, command/query executors
- **smartcs-web-domain**: Domain layer - core business entities and gateway interfaces
- **smartcs-web-infrastructure**: Infrastructure layer - database, external APIs, implementations
- **start**: Spring Boot application entry point and configuration

### Key Technologies
- **LangChain4j 1.1.0**: LLM integration framework (migrated from Spring AI)
- **COLA 5.0.0**: Clean Architecture framework
- **MyBatis-Plus 3.5.12**: ORM framework
- **Redis + Redisson**: Caching and vector storage
- **Spring State Machine**: Session state management
- **MapStruct 1.5.5**: Object mapping
- **WebSocket**: Real-time chat functionality

## Development Guidelines

### Code Standards (from .cursor/rules)
- Use UTF-8 encoding with Chinese comments
- Use `@Slf4j` for logging
- Prefer constructor injection with `@RequiredArgsConstructor` and `final` fields
- Use Lombok annotations: `@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`
- Throw `BizException` for business logic interruptions
- Avoid magic numbers - use constants or enums
- All timestamps should be in milliseconds (`long` type)

### Layer Responsibilities
- **Adapter**: Handle HTTP requests, WebSocket connections, filters
- **Client**: Define service interfaces and DTOs
- **App**: Business logic implementation, command/query executors
- **Domain**: Core entities, value objects, domain services, gateway interfaces
- **Infrastructure**: Database operations, external service calls, gateway implementations

### Naming Conventions
- Controllers: `AdminXxxController` for admin endpoints, `XxxController` for client
- Command executors: `XxxCmdExe`
- Query executors: `XxxQryExe`
- Gateway implementations: `XxxGatewayImpl`
- Data objects: `XxxDO` (in infrastructure layer)
- Converters: `XxxConvertor` (using MapStruct)

## Key Features & Components

### AI/LLM Integration
- Uses LangChain4j for LLM operations
- RAG (Retrieval-Augmented Generation) with knowledge base
- Vector search using Redis as vector store
- MCP (Model Context Protocol) tool integration
- Streaming responses via SSE

### Knowledge Management
- Document parsing and chunking
- Vector embeddings generation
- FAQ search functionality
- Content status management
- Multiple chunking strategies (general, parent-child)

### Chat System
- Real-time WebSocket chat
- Session state management with Spring State Machine
- Message persistence with horizontal partitioning
- SSE-based streaming responses
- Multi-channel support

### Testing
- Use JUnit 5 + Mockito for unit tests
- Integration tests in `src/test/java`
- No specific test runner found - use standard Maven test commands

## Configuration Notes

### Application Configuration
- Main config: `start/src/main/resources/application.yaml`
- Bootstrap: `start/src/main/resources/bootstrap.yaml`
- LangChain4j configuration uses environment variables for API keys
- Redis configuration supports vector operations
- Database connection uses MySQL 8.0+

### Profiles
- Development profile available (exact profiles need verification from bootstrap.yaml)
- Configuration can be environment-specific

## Important Files to Check
- Database schema: `smartcs-web-infrastructure/src/main/resources/sql/`
- Mapper XML files: `smartcs-web-infrastructure/src/main/resources/mapper/`
- Migration documentation: `LangChain4j迁移完成报告.md`
- Architecture documentation: `架构文档.md`
- Module-specific guides: `Bot模块.md`, `知识模块.md`

## Development Workflow
1. Follow DDD principles and COLA architecture
2. Check existing implementations before creating new classes
3. Use the established patterns for command/query separation
4. Ensure proper layer dependencies (no circular dependencies)
5. Add proper error handling with BizException
6. Write unit tests for business logic
7. Use MapStruct for object conversions between layers