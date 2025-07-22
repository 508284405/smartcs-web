package com.leyue.smartcs.knowledge.parser;

import com.leyue.smartcs.knowledge.parser.impl.HtmlDocumentParser;
import com.leyue.smartcs.knowledge.parser.impl.TxtDocumentParser;
import com.leyue.smartcs.knowledge.parser.impl.WordDocumentParser;
import dev.langchain4j.data.document.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 文档解析器测试
 * 验证三个完善的解析器实现
 */
class DocumentParserTest {

    private TxtDocumentParser txtParser;
    private HtmlDocumentParser htmlParser;
    private WordDocumentParser wordParser;

    @BeforeEach
    void setUp() {
        txtParser = new TxtDocumentParser();
        htmlParser = new HtmlDocumentParser();
        wordParser = new WordDocumentParser();
    }

    @Test
    void testTxtDocumentParserInterface() {
        // 测试接口方法实现
        assertThat(txtParser.getSupportedTypes()).contains("txt", "text");
        assertTrue(txtParser.supports("txt"));
        assertTrue(txtParser.supports("text"));
        assertFalse(txtParser.supports("html"));
        assertFalse(txtParser.supports(null));
    }

    @Test
    void testTxtDocumentParserParsing() throws Exception {
        // 创建测试文本内容
        String testContent = "第一段内容\n这是第一段的第二行\n\n第二段内容\n这是第二段的内容\n\n第三段内容";
        Resource resource = new ByteArrayResource(testContent.getBytes(StandardCharsets.UTF_8));
        
        // 解析文档
        List<Document> documents = txtParser.parse(resource, "test.txt");
        
        // 验证结果
        assertThat(documents).isNotEmpty();
        
        // 应该有段落和完整文档
        boolean hasParagraphs = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("paragraph"));
        boolean hasFullDocument = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("full_document"));
        
        assertTrue(hasParagraphs, "应该包含段落文档");
        assertTrue(hasFullDocument, "应该包含完整文档");
    }

    @Test
    void testHtmlDocumentParserInterface() {
        // 测试接口方法实现
        assertThat(htmlParser.getSupportedTypes()).contains("html", "htm", "xhtml");
        assertTrue(htmlParser.supports("html"));
        assertTrue(htmlParser.supports("htm"));
        assertTrue(htmlParser.supports("xhtml"));
        assertFalse(htmlParser.supports("txt"));
        assertFalse(htmlParser.supports(null));
    }

    @Test
    void testHtmlDocumentParserParsing() throws Exception {
        // 创建测试HTML内容
        String testHtml = """
                <html>
                <head><title>测试页面</title></head>
                <body>
                    <h1>主标题</h1>
                    <p>这是第一个段落内容。</p>
                    <h2>副标题</h2>
                    <p>这是第二个段落内容。</p>
                    <table>
                        <tr><th>列1</th><th>列2</th></tr>
                        <tr><td>数据1</td><td>数据2</td></tr>
                    </table>
                    <img src="test.jpg" alt="测试图片"/>
                </body>
                </html>
                """;
        Resource resource = new ByteArrayResource(testHtml.getBytes(StandardCharsets.UTF_8));
        
        // 解析文档
        List<Document> documents = htmlParser.parse(resource, "test.html");
        
        // 验证结果
        assertThat(documents).isNotEmpty();
        
        // 验证不同类型的文档
        boolean hasTitle = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("title"));
        boolean hasHeadings = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("heading"));
        boolean hasParagraphs = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("paragraph"));
        boolean hasTables = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("table"));
        boolean hasImages = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("image"));
        boolean hasFullDocument = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("full_document"));
        
        assertTrue(hasTitle, "应该提取标题");
        assertTrue(hasHeadings, "应该提取标题层次");
        assertTrue(hasParagraphs, "应该提取段落");
        assertTrue(hasTables, "应该提取表格");
        assertTrue(hasImages, "应该提取图片信息");
        assertTrue(hasFullDocument, "应该包含完整文档");
    }

    @Test
    void testWordDocumentParserInterface() {
        // 测试接口方法实现
        assertThat(wordParser.getSupportedTypes()).contains("docx");
        assertTrue(wordParser.supports("docx"));
        assertFalse(wordParser.supports("doc")); // 只支持docx
        assertFalse(wordParser.supports("html"));
        assertFalse(wordParser.supports(null));
    }

    @Test
    void testTxtParserEmptyContent() throws Exception {
        // 测试空内容
        String emptyContent = "";
        Resource resource = new ByteArrayResource(emptyContent.getBytes(StandardCharsets.UTF_8));
        
        List<Document> documents = txtParser.parse(resource, "empty.txt");
        
        // 应该至少有一个文档（空文档情况下是占位文档）
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).metadata().toMap().get("type")).isEqualTo("empty_document");
    }

    @Test
    void testHtmlParserMinimalContent() throws Exception {
        // 测试最小HTML内容
        String minimalHtml = "<html><body><p>简单段落</p></body></html>";
        Resource resource = new ByteArrayResource(minimalHtml.getBytes(StandardCharsets.UTF_8));
        
        List<Document> documents = htmlParser.parse(resource, "minimal.html");
        
        // 应该至少有段落和完整文档
        assertThat(documents).hasSizeGreaterThan(1);
        
        boolean hasParagraph = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("paragraph"));
        boolean hasFullDocument = documents.stream()
                .anyMatch(doc -> doc.metadata().toMap().get("type").equals("full_document"));
        
        assertTrue(hasParagraph, "应该提取段落");
        assertTrue(hasFullDocument, "应该包含完整文档");
    }

    @Test
    void testDocumentMetadata() throws Exception {
        // 测试元数据是否正确设置
        String testContent = "测试内容\n\n第二段";
        Resource resource = new ByteArrayResource(testContent.getBytes(StandardCharsets.UTF_8));
        
        List<Document> documents = txtParser.parse(resource, "metadata-test.txt");
        
        // 验证所有文档都有fileName元数据
        documents.forEach(doc -> {
            assertThat(doc.metadata().toMap()).containsKey("fileName");
            assertThat(doc.metadata().toMap().get("fileName")).isEqualTo("metadata-test.txt");
            assertThat(doc.metadata().toMap()).containsKey("type");
        });
    }

    @Test
    void testParserSelection() {
        // 测试解析器选择逻辑
        assertTrue(txtParser.supports("txt"));
        assertFalse(txtParser.supports("html"));
        
        assertTrue(htmlParser.supports("html"));
        assertFalse(htmlParser.supports("docx"));
        
        assertTrue(wordParser.supports("docx"));
        assertFalse(wordParser.supports("txt"));
    }

    @Test
    void testDeprecatedMethods() {
        // 验证已弃用的方法仍然存在（向后兼容）
        // 这些方法应该存在但标记为@Deprecated
        try {
            // 通过反射检查方法是否存在且被标记为弃用
            java.lang.reflect.Method parseContentMethod = txtParser.getClass()
                    .getDeclaredMethod("parseContent", String.class);
            assertTrue(parseContentMethod.isAnnotationPresent(Deprecated.class));
            
            parseContentMethod = htmlParser.getClass()
                    .getDeclaredMethod("parseContent", String.class);
            assertTrue(parseContentMethod.isAnnotationPresent(Deprecated.class));
            
            parseContentMethod = wordParser.getClass()
                    .getDeclaredMethod("parseContent", String.class);
            assertTrue(parseContentMethod.isAnnotationPresent(Deprecated.class));
            
        } catch (NoSuchMethodException e) {
            fail("已弃用的 parseContent 方法应该仍然存在以保持向后兼容性");
        }
    }
}