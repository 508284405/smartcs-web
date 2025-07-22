# PDF多模态解析器增强说明

## 概述

基于多模态RAG最佳实践，对 `PdfDocumentParser` 进行了全面增强，使其能够智能处理PDF文档中的文本、图像和表格等多种模态内容。

## 🚀 主要功能增强

### 1. 多模态内容提取
- **文本内容**: 智能分段，区分标题和段落
- **图像内容**: 高级图像提取，支持OCR和图像描述
- **表格内容**: 自动检测和结构化提取
- **文档结构**: 生成文档大纲和层次结构

### 2. 智能内容分析
- **标题检测**: 使用正则表达式识别各种标题格式
- **表格识别**: 支持多种表格格式（管道分隔、Tab分隔、编号列表）
- **图像筛选**: 自动过滤装饰性小图标
- **置信度评估**: 为检测结果提供置信度评分

### 3. 可配置的处理选项
- **OCR开关**: 可选择启用图像文字识别
- **图像描述**: 可选择启用AI图像描述生成
- **表格检测**: 可配置表格检测敏感度
- **尺寸过滤**: 可设置最小图像尺寸阈值

## 📋 生成的文档类型

### 文档级别
| 文档类型 | 描述 | 元数据字段 |
|---------|------|-----------|
| `pdf_metadata` | 文档基本信息 | pageCount, totalCharacters, hasImages, estimatedTables, titleCount |
| `pdf_outline` | 文档结构大纲 | structureType |

### 页面级别
| 文档类型 | 描述 | 元数据字段 |
|---------|------|-----------|
| `pdf_title` | 标题内容 | pageNumber, segmentIndex, segmentType, confidence |
| `pdf_text` | 文本段落 | pageNumber, segmentIndex, segmentType, confidence |
| `pdf_image` | 图像内容 | pageNumber, imageIndex, imageFormat, width, height, hasOCR, hasDescription |
| `pdf_table` | 表格内容 | pageNumber, tableIndex, rowCount, columnCount, confidence |

## ⚙️ 配置参数

### application.yml 配置示例

```yaml
pdf:
  multimodal:
    # 是否启用OCR识别（需要集成Tesseract等OCR库）
    enable-ocr: false
    
    # 是否启用表格检测
    enable-table-detection: true
    
    # 是否启用图像描述生成（需要集成视觉AI模型）
    enable-image-description: false
    
    # 最小图像尺寸（像素），小于此尺寸的图像将被过滤
    min-image-size: 100
```

### 环境变量配置

```bash
# OCR功能开关
PDF_MULTIMODAL_ENABLE_OCR=false

# 表格检测开关
PDF_MULTIMODAL_ENABLE_TABLE_DETECTION=true

# 图像描述生成开关
PDF_MULTIMODAL_ENABLE_IMAGE_DESCRIPTION=false

# 最小图像尺寸
PDF_MULTIMODAL_MIN_IMAGE_SIZE=100
```

## 🔧 使用方式

### 基本使用

```java
@Autowired
private PdfDocumentParser pdfParser;

public void processPdfDocument(Resource pdfResource, String fileName) {
    try {
        List<Document> documents = pdfParser.parse(pdfResource, fileName);
        
        // 按类型处理不同的文档
        for (Document doc : documents) {
            String type = doc.metadata().toMap().get("type").toString();
            
            switch (type) {
                case "pdf_metadata":
                    handleDocumentMetadata(doc);
                    break;
                case "pdf_title":
                    handleTitleContent(doc);
                    break;
                case "pdf_text":
                    handleTextContent(doc);
                    break;
                case "pdf_image":
                    handleImageContent(doc);
                    break;
                case "pdf_table":
                    handleTableContent(doc);
                    break;
                case "pdf_outline":
                    handleDocumentOutline(doc);
                    break;
            }
        }
    } catch (IOException e) {
        log.error("PDF解析失败", e);
    }
}
```

### 与分块策略集成

```java
// 使用多模态分块策略
KnowledgeGeneralChunkCmd cmd = new KnowledgeGeneralChunkCmd();
cmd.setFileUrl("https://example.com/document.pdf");
cmd.setChunkingStrategies(Arrays.asList(
    "IMAGE_PROCESSING",    // 图像处理策略
    "TABLE_PROCESSING",    // 表格处理策略  
    "TEXT_CONTENT"         // 文本内容策略
));

MultiResponse<ChunkDTO> result = chunkExecutor.execute(cmd);
```

## 📊 处理流程

### 1. 文档级别分析
```
PDF文档 → 页数统计 → 字符统计 → 图像检测 → 表格估算 → 标题计数
```

### 2. 页面级别处理
```
每一页 → 文本提取 → 图像提取 → 表格检测 → 内容分类 → 文档生成
```

### 3. 结构化输出
```
原始PDF → 多模态解析 → 分类文档 → 元数据丰富 → 索引就绪
```

## 🎯 核心算法

### 表格检测正则表达式

```java
Pattern TABLE_PATTERN = Pattern.compile(
    "(?:\\s*\\|[^\\n]*\\|\\s*\\n){2,}|" +  // 管道分隔: |列1|列2|
    "(?:\\s*[^\\n]*\\t[^\\n]*\\n){2,}|" +  // Tab分隔: 列1\t列2
    "(?:\\s*\\d+[\\s.]+[^\\n]*\\n){3,}"    // 编号列表: 1. 项目
);
```

### 标题检测正则表达式

```java
Pattern TITLE_PATTERN = Pattern.compile(
    "^\\s*(?:第?[一二三四五六七八九十\\d]+[章节部分条]|" +  // 第一章、第1节
    "\\d+\\.\\d*|[A-Z]\\.|\\d+\\)|" +                    // 1.1、A.、1)
    "[\\u4e00-\\u9fa5]{1,20}：?)\\s*" +                 // 中文标题：
    "([\\u4e00-\\u9fa5A-Za-z\\d\\s]{2,50})\\s*$",       // 标题内容
    Pattern.MULTILINE
);
```

### 表格置信度计算

```java
private double calculateTableConfidence(String tableContent) {
    String[] lines = tableContent.split("\\n");
    if (lines.length < 2) return 0.3;
    
    // 检查行结构一致性
    int consistentRows = 0;
    String pattern = null;
    
    for (String line : lines) {
        String currentPattern = line.replaceAll("[^|\\t]", "X");
        if (pattern == null) {
            pattern = currentPattern;
            consistentRows = 1;
        } else if (pattern.equals(currentPattern)) {
            consistentRows++;
        }
    }
    
    return (double) consistentRows / lines.length;
}
```

## 🔮 扩展能力

### OCR集成示例

```java
// 集成Tesseract OCR
private String performOCR(BufferedImage image) {
    try {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("/path/to/tessdata");
        tesseract.setLanguage("chi_sim+eng"); // 中英文识别
        return tesseract.doOCR(image);
    } catch (TesseractException e) {
        log.error("OCR识别失败", e);
        return null;
    }
}
```

### 图像描述生成示例

```java
// 集成Gemini Vision API
private String generateImageDescription(BufferedImage image) {
    try {
        // 将图像转换为Base64
        String base64Image = imageToBase64(image);
        
        // 调用Gemini Vision API
        GeminiVisionRequest request = new GeminiVisionRequest()
            .setImage(base64Image)
            .setPrompt("请描述这张图片的主要内容，重点关注图表、文字和关键信息");
            
        return geminiVisionClient.generateDescription(request);
    } catch (Exception e) {
        log.error("图像描述生成失败", e);
        return null;
    }
}
```

## 📈 性能优化

### 1. 图像处理优化
- **尺寸过滤**: 跳过小于阈值的装饰性图像
- **格式转换**: 统一转换为PNG格式以提高兼容性
- **并行处理**: 图像和文本处理可并行执行

### 2. 表格检测优化
- **正则缓存**: 预编译正则表达式以提高性能
- **置信度过滤**: 只处理高置信度的表格候选
- **结构验证**: 验证表格结构的合理性

### 3. 内存管理
- **流式处理**: 大文档分页处理以控制内存使用
- **资源释放**: 及时释放PDF和图像资源
- **缓存策略**: 合理缓存处理结果

## 🧪 测试验证

### 单元测试覆盖
- ✅ 接口方法实现测试
- ✅ 配置参数验证测试
- ✅ 表格检测正则测试
- ✅ 标题检测正则测试
- ✅ 图像尺寸过滤测试
- ✅ 元数据结构测试

### 集成测试建议
- 使用真实PDF文件测试
- 验证多模态内容提取准确性
- 性能基准测试
- 错误处理和边界情况测试

## 🔄 迁移指南

### 从旧版本迁移
1. **配置更新**: 添加多模态相关配置参数
2. **依赖检查**: 确保PDFBox版本兼容
3. **测试验证**: 运行测试确保功能正常
4. **性能调优**: 根据实际文档调整参数

### 向后兼容性
- 保持了原有的接口签名
- 配置参数都有合理的默认值
- 不会破坏现有的调用代码

## 🎉 总结

增强后的PDF多模态解析器提供了：

- **🎯 精确提取**: 智能识别文本、图像、表格等多种内容
- **🔧 灵活配置**: 可根据需求开启/关闭各种处理功能
- **📊 丰富元数据**: 提供详细的内容分析和结构信息
- **🚀 高性能**: 优化的处理流程和资源管理
- **🔗 易集成**: 与现有分块策略无缝集成

这为构建强大的多模态RAG系统奠定了坚实的基础！