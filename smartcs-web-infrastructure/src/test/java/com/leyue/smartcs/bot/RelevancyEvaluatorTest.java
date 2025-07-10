// package com.leyue.smartcs.bot;

// import org.junit.jupiter.api.Test;
// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
// import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreDocumentRetriever;
// import org.springframework.ai.chat.model.ChatModel;
// import org.springframework.ai.evaluation.EvaluationRequest;
// import org.springframework.ai.evaluation.EvaluationResponse;
// import org.springframework.ai.evaluation.RelevancyEvaluator;
// import org.springframework.ai.vectorstore.VectorStore;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest
// public class RelevancyEvaluatorTest {

//     @Autowired
//     private VectorStore vectorStore;

//     @Autowired
//     private ChatModel chatModel;

//     @Test
//     void evaluateRelevancy() {
//         String question = "Where does the adventure of Anacletus and Birba take place?";

//         RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
//                 .documentRetriever(VectorStoreDocumentRetriever.builder()
//                         .vectorStore(vectorStore)
//                         .build())
//                 .build();

//         ChatClient chatClient = ChatClient.builder(chatModel).build();
//         var chatResponse = chatClient
//                 .prompt(question)
//                 .advisors(ragAdvisor)
//                 .call()
//                 .chatResponse();

//         EvaluationRequest evaluationRequest = new EvaluationRequest(
//                 question,
//                 chatResponse.getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT),
//                 chatResponse.getResult().getOutput().getText()
//         );

//         RelevancyEvaluator evaluator = new RelevancyEvaluator(ChatClient.builder(chatModel));
//         EvaluationResponse evaluationResponse = evaluator.evaluate(evaluationRequest);

//         assertThat(evaluationResponse.isPass()).isTrue();
//     }
// } 