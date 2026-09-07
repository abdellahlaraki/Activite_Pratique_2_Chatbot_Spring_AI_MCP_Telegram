package ma.enset.chatbot.telegram;

import ma.enset.chatbot.service.AiAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Comparator;

@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
public class TelegramAiBot extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramAiBot.class);
    private final AiAgentService agent;
    private final String username;

    public TelegramAiBot(AiAgentService agent,
                         @Value("${telegram.bot.token}") String token,
                         @Value("${telegram.bot.username}") String username) {
        super(token);
        this.agent = agent;
        this.username = username;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        try {
            sendTyping(chatId);
            String conversationId = "telegram:" + chatId;
            String answer;
            if (update.getMessage().hasPhoto()) {
                String caption = update.getMessage().getCaption();
                String question = caption == null || caption.isBlank()
                        ? "Décris précisément cette image." : caption;
                answer = agent.askWithImage(conversationId, question, downloadLargestPhoto(update));
            } else if (update.getMessage().hasText()) {
                answer = agent.ask(conversationId, update.getMessage().getText());
            } else {
                answer = "Envoyez une question textuelle ou une image avec une légende.";
            }
            execute(SendMessage.builder().chatId(chatId).text(answer).build());
        } catch (Exception exception) {
            LOGGER.error("Erreur de traitement Telegram pour le chat {}", chatId, exception);
            try {
                execute(SendMessage.builder().chatId(chatId)
                        .text("Une erreur est survenue. Réessayez dans quelques instants.")
                        .build());
            } catch (TelegramApiException sendException) {
                LOGGER.error("Impossible d'envoyer le message d'erreur Telegram", sendException);
            }
        }
    }

    private void sendTyping(long chatId) throws TelegramApiException {
        execute(SendChatAction.builder()
                .chatId(chatId)
                .action(ActionType.TYPING.toString())
                .build());
    }

    private byte[] downloadLargestPhoto(Update update) throws TelegramApiException, IOException {
        var largest = update.getMessage().getPhoto().stream()
                .max(Comparator.comparingInt(photo -> photo.getFileSize() == null ? 0 : photo.getFileSize()))
                .orElseThrow(() -> new IllegalArgumentException("Image Telegram absente"));
        var telegramFile = execute(GetFile.builder().fileId(largest.getFileId()).build());
        try (var input = downloadFileAsStream(telegramFile)) {
            return input.readAllBytes();
        }
    }
}
