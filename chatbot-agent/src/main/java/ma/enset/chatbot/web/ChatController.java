package ma.enset.chatbot.web;

import jakarta.validation.Valid;
import ma.enset.chatbot.service.AiAgentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final AiAgentService agent;

    public ChatController(AiAgentService agent) {
        this.agent = agent;
    }

    @PostMapping
    public ChatResponseDto chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponseDto(request.conversationId(),
                agent.ask(request.conversationId(), request.message()));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String conversationId,
                               @RequestParam String message) {
        return agent.stream(conversationId, message);
    }
}
