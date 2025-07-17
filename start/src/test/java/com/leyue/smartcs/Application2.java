package com.leyue.smartcs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication
public class Application2 {

    public static void main(String[] args) {
        SpringApplication.run(Application2.class, args);
    }

    @Bean
    public CommandLineRunner chatbot() {
        log.info("Starting chatbot...");
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/")
                // .baseUrl("http://localhost:11434")
                .apiKey("sk-1e81f65835674d82828a329b398518fc")
                .build();

        // 使用builder模式构建OpenAiChatModel实例
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model("deepseek-r1").build())
                .build();


        return args -> {

            var chatClient = ChatClient.builder(chatModel)
//					.defaultSystem("You are useful assistant and can perform web searches Brave's search API to reply to your questions.")
//					.defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
//					.defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
                    .build();

            String content = chatClient.prompt("what day is tomorrow?") // Get the user input
                    .tools(new DateTimeTools())
                    .call()
                    .content();
            System.out.println(" ----------------- " + content);

        };
    }
}

class DateTimeTools {
    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}