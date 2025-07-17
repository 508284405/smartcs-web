package com.leyue.smartcs.knowledge.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * 文本预处理工具类
 */
@Component
@Slf4j
public class TextPreprocessor {

    /**
     * URL正则表达式
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+|" +
                    "ftp://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+|" +
                    "www\\.[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    /**
     * Q&A分段提示词模板 - 中文
     */
    private static final String QA_PROMPT_CHINESE = """
            请将以下文本按照问答（Q&A）格式进行分段。每个分段应该包含一个问题和对应的答案。

            要求：
            1. 识别文本中的关键信息点
            2. 将每个信息点转换为问答格式
            3. 问题要简洁明了
            4. 答案要完整准确
            5. 每个Q&A分段用"---"分隔
            6. 格式：Q: 问题内容 A: 答案内容

            文本内容：
            {text}

            请按照上述要求进行Q&A分段：
            """;

    /**
     * Q&A分段提示词模板 - 英文
     */
    private static final String QA_PROMPT_ENGLISH = """
            Please segment the following text into Q&A (Question and Answer) format. Each segment should contain a question and its corresponding answer.

            Requirements:
            1. Identify key information points in the text
            2. Convert each information point into Q&A format
            3. Questions should be concise and clear
            4. Answers should be complete and accurate
            5. Separate each Q&A segment with "---"
            6. Format: Q: Question content A: Answer content

            Text content:
            {text}

            Please segment according to the above requirements:
            """;

    /**
     * 移除URL和邮箱地址
     *
     * @param text 原始文本
     * @return 处理后的文本
     */
    public String removeUrlsAndEmails(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        log.debug("开始移除URL和邮箱，原始文本长度: {}", text.length());

        // 移除URL
        String processedText = URL_PATTERN.matcher(text).replaceAll("");

        // 移除邮箱
        processedText = EMAIL_PATTERN.matcher(processedText).replaceAll("");

        log.debug("移除URL和邮箱完成，处理后文本长度: {}", processedText.length());

        return processedText;
    }

    /**
     * 使用Q&A分段
     *
     * @param text      原始文本
     * @param language  语言（Chinese/English）
     * @param chatModel 聊天模型
     * @return Q&A分段结果列表
     */
    public List<String> segmentWithQA(String text, String language, ChatModel chatModel) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        if (chatModel == null) {
            log.warn("ChatModel为空，无法进行Q&A分段，返回原始文本");
            return List.of(text);
        }

        log.info("开始Q&A分段，语言: {}, 文本长度: {}", language, text.length());

        try {
            // 选择提示词模板
            String promptTemplate = "English".equalsIgnoreCase(language) ? QA_PROMPT_ENGLISH : QA_PROMPT_CHINESE;

            // 创建提示词
            PromptTemplate template = new PromptTemplate(promptTemplate);
            Prompt prompt = template.create(Map.of("text", text));

            // 调用大模型
            String response = chatModel.call(prompt).getResult().getOutput().getText();

            // 解析响应结果
            List<String> segments = parseQAResponse(response);

            log.info("Q&A分段完成，共生成 {} 个分段", segments.size());

            return segments;

        } catch (Exception e) {
            log.error("Q&A分段失败", e);
            // 失败时返回原始文本
            return List.of(text);
        }
    }

    /**
     * 解析Q&A响应结果
     *
     * @param response 大模型响应
     * @return 分段列表
     */
    private List<String> parseQAResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> segments = new ArrayList<>();

        // 按照"---"分隔符分割
        String[] parts = response.split("---");

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                segments.add(trimmed);
            }
        }

        // 如果没有找到分隔符，尝试按段落分割
        if (segments.isEmpty()) {
            String[] paragraphs = response.split("\n\n");
            for (String paragraph : paragraphs) {
                String trimmed = paragraph.trim();
                if (!trimmed.isEmpty()) {
                    segments.add(trimmed);
                }
            }
        }

        return segments;
    }

    /**
     * 综合文本预处理
     *
     * @param removeUrls        是否移除URL和邮箱
     * @param useQASegmentation 是否使用Q&A分段
     * @param qaLanguage        Q&A分段语言
     * @param chatModel         聊天模型
     * @return 处理后的文本或分段列表
     */
    public List<Document> preprocessText(List<Document> documents, Boolean removeUrls, Boolean useQASegmentation,
            String qaLanguage, ChatModel chatModel) {

        return documents.stream().map(document -> {
            String processedText = document.getText();

            // 移除URL和邮箱
            if (Boolean.TRUE.equals(removeUrls)) {
                processedText = removeUrlsAndEmails(processedText);
            }
            return Document.builder()
                    .id(document.getId())
                    .text(processedText)
                    .metadata(document.getMetadata())
                    .media(document.getMedia())
                    .score(document.getScore())
                    .build();
        }).collect(Collectors.toList());
    }
}