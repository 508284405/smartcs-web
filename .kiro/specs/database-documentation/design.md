# Design Document

## Overview

This design outlines the approach for generating comprehensive markdown documentation of the SmartCS project's database schema. The solution will parse existing SQL files, extract table definitions, and generate structured markdown documentation suitable for knowledge base integration and AI agent reference.

## Architecture

The documentation generation system will follow a simple pipeline architecture:

1. **SQL File Discovery**: Scan the project for SQL files containing table definitions
2. **SQL Parsing**: Extract CREATE TABLE statements and parse table structures
3. **Data Processing**: Organize table information, relationships, and metadata
4. **Markdown Generation**: Format the extracted data into structured markdown
5. **Output Generation**: Create the final documentation file

## Components and Interfaces

### SQL File Scanner
- **Purpose**: Locate and read all SQL files in the project
- **Input**: Project directory path
- **Output**: List of SQL file contents
- **Location**: `smartcs-web-infrastructure/src/main/resources/sql/`

### SQL Parser
- **Purpose**: Parse CREATE TABLE statements and extract schema information
- **Input**: Raw SQL content
- **Output**: Structured table definitions
- **Key Functions**:
  - Parse table names and comments
  - Extract column definitions with types, constraints, and comments
  - Identify indexes and their types
  - Extract foreign key relationships

### Table Analyzer
- **Purpose**: Analyze parsed tables and identify relationships
- **Input**: Structured table definitions
- **Output**: Enhanced table data with relationship mappings
- **Key Functions**:
  - Identify foreign key relationships between tables
  - Group tables by functional modules
  - Validate data consistency

### Markdown Generator
- **Purpose**: Convert structured data to markdown format
- **Input**: Enhanced table data
- **Output**: Formatted markdown documentation
- **Key Functions**:
  - Generate table of contents
  - Format table definitions as markdown tables
  - Create relationship diagrams in text format
  - Organize content by modules

## Data Models

### Table Definition Model
```
TableDefinition {
  name: string
  comment: string
  engine: string
  charset: string
  columns: Column[]
  indexes: Index[]
  primaryKey: string[]
  foreignKeys: ForeignKey[]
}
```

### Column Model
```
Column {
  name: string
  dataType: string
  length: number?
  nullable: boolean
  defaultValue: string?
  autoIncrement: boolean
  comment: string
}
```

### Index Model
```
Index {
  name: string
  type: string (PRIMARY, UNIQUE, INDEX, FULLTEXT)
  columns: string[]
}
```

### Foreign Key Model
```
ForeignKey {
  columnName: string
  referencedTable: string
  referencedColumn: string
}
```

## Implementation Approach

### Phase 1: SQL File Processing
1. Scan the `smartcs-web-infrastructure/src/main/resources/sql/` directory
2. Read all `.sql` files
3. Extract CREATE TABLE statements using regex patterns
4. Handle both `CREATE TABLE` and `CREATE TABLE IF NOT EXISTS` syntax

### Phase 2: Schema Parsing
1. Parse table names and extract table-level comments
2. Parse column definitions including:
   - Data types with length specifications
   - Constraints (NOT NULL, DEFAULT, AUTO_INCREMENT)
   - Column comments
3. Parse index definitions including:
   - Primary keys
   - Unique indexes
   - Regular indexes
   - Full-text indexes
4. Identify foreign key relationships from column names and comments

### Phase 3: Documentation Generation
1. Create markdown structure with:
   - Document header and introduction
   - Table of contents
   - Module-based table groupings
   - Individual table documentation
   - Relationship summary
2. Format each table as:
   - Table name and description
   - Column definitions table
   - Index information
   - Relationships
3. Generate cross-references between related tables

## Module Organization

Based on the existing SQL files, tables will be organized into these modules:

### Knowledge Base Module
- `t_kb_knowledge_base` - 知识库表
- `t_kb_content` - 知识内容表
- `t_kb_chunk` - 内容切片表
- `t_kb_vector` - 向量表
- `t_kb_user_kb_rel` - 用户知识库权限关系表
- `t_cs_faq` - 常见问题FAQ表

### Bot Management Module
- `t_cs_bot_profile` - Bot Agent详细配置表
- `t_bot_prompt_template` - Bot Prompt模板表

### Session Management Module
- `t_cs_user` - 用户数据表
- `t_cs_session` - 会话数据表
- `t_cs_message` - 消息数据表

## Output Format

The generated markdown will follow this structure:

```markdown
# SmartCS Database Schema Documentation

## Table of Contents
[Generated TOC with links]

## Overview
[Project description and schema summary]

## Module: Knowledge Base
### Table: t_kb_knowledge_base
[Table documentation]

## Module: Bot Management
[Module tables]

## Module: Session Management
[Module tables]

## Table Relationships
[Cross-reference diagram and descriptions]

## Index Summary
[All indexes organized by table]
```

## Error Handling

1. **File Reading Errors**: Log missing or unreadable SQL files and continue processing
2. **SQL Parsing Errors**: Skip malformed CREATE TABLE statements with warning logs
3. **Data Validation**: Validate extracted schema data and report inconsistencies
4. **Output Generation**: Ensure valid markdown syntax even with incomplete data

## Testing Strategy

1. **Unit Tests**: Test SQL parsing functions with various CREATE TABLE formats
2. **Integration Tests**: Test complete pipeline with sample SQL files
3. **Output Validation**: Verify generated markdown is valid and complete
4. **Schema Coverage**: Ensure all existing tables are documented
5. **Relationship Accuracy**: Validate foreign key relationship detection