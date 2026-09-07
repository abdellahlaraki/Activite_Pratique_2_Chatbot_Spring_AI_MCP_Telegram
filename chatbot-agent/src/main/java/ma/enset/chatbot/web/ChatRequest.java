package ma.enset.chatbot.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank @Size(max = 100) String conversationId,
        @NotBlank @Size(max = 4000) String message) {
}
