package ma.enset.chatbot.config;

import ma.enset.chatbot.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConditionalOnProperty(name = "rag.index-on-startup", havingValue = "true", matchIfMissing = true)
public class DefaultDocumentsIndexer implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDocumentsIndexer.class);
    private final RagService ragService;
    private final Path documentsDirectory;

    public DefaultDocumentsIndexer(RagService ragService,
                                   @Value("${rag.documents-directory:./data/documents}") String directory) {
        this.ragService = ragService;
        this.documentsDirectory = Path.of(directory);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int chunks = ragService.indexDirectory(documentsDirectory);
        LOGGER.info("Indexation RAG terminée : {} fragment(s) depuis {}", chunks, documentsDirectory);
    }
}
