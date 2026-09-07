package ma.enset.chatbot.web;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRequestTest {
    @Test
    void shouldRejectBlankMessage() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var violations = validatorFactory.getValidator()
                    .validate(new ChatRequest("conversation-1", " "));
            assertThat(violations).isNotEmpty();
        }
    }
}
