package ma.enset.chatbot.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

@Service
public class AiAgentService {
    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor ragAdvisor;

    public AiAgentService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.60)
                        .topK(4)
                        .build())
                .build();
    }

    public String ask(String conversationId, String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, conversationId)
                        .advisors(ragAdvisor))
                .call()
                .content();
    }

    public Flux<String> stream(String conversationId, String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, conversationId)
                        .advisors(ragAdvisor))
                .stream()
                .chatResponse()
                .map(ChatResponse::getResult)
                .map(result -> result.getOutput().getText());
    }

    public String askWithImage(String conversationId, String question, byte[] jpegBytes) {
        return chatClient.prompt()
                .user(user -> user
                        .text(question)
                        .media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(jpegBytes)))
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, conversationId)
                        .advisors(ragAdvisor))
                .call()
                .content();
    }
}
