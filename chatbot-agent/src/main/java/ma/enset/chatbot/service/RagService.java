package ma.enset.chatbot.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RagService {
    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter = new TokenTextSplitter();
    private final AtomicInteger indexedChunks = new AtomicInteger();

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int indexUploadedPdf(MultipartFile file) throws IOException {
        if (file.isEmpty() || !isPdf(file)) {
            throw new IllegalArgumentException("Le fichier doit être un PDF non vide.");
        }

        Path temporaryFile = Files.createTempFile("rag-", ".pdf");
        try (var input = file.getInputStream()) {
            Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
            return indexPdf(temporaryFile, file.getOriginalFilename());
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    public int indexDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return 0;
        }

        int total = 0;
        try (var files = Files.list(directory)) {
            for (Path path : files.filter(Files::isRegularFile).toList()) {
                String name = path.getFileName().toString().toLowerCase();
                if (name.endsWith(".pdf")) {
                    total += indexPdf(path, path.getFileName().toString());
                } else if (name.endsWith(".txt") || name.endsWith(".md")) {
                    total += indexText(path);
                }
            }
        }
        return total;
    }

    public List<Document> search(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(Math.max(1, Math.min(topK, 10)))
                .similarityThreshold(0.60)
                .build());
    }

    public int indexedChunks() {
        return indexedChunks.get();
    }

    private int indexPdf(Path path, String sourceName) {
        var reader = new PagePdfDocumentReader(new FileSystemResource(path));
        List<Document> documents = withSource(reader.get(), sourceName);
        return addChunks(documents);
    }

    private int indexText(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        Document document = new Document(content, Map.of(
                "source", path.getFileName().toString(),
                "documentId", UUID.randomUUID().toString()
        ));
        return addChunks(List.of(document));
    }

    private List<Document> withSource(List<Document> input, String sourceName) {
        List<Document> result = new ArrayList<>(input.size());
        for (Document document : input) {
            Document enriched = document.mutate()
                    .metadata("source", sourceName == null ? "document.pdf" : sourceName)
                    .metadata("documentId", UUID.randomUUID().toString())
                    .build();
            result.add(enriched);
        }
        return result;
    }

    private int addChunks(List<Document> documents) {
        List<Document> chunks = splitter.apply(documents);
        vectorStore.add(chunks);
        indexedChunks.addAndGet(chunks.size());
        return chunks.size();
    }

    private boolean isPdf(MultipartFile file) {
        String type = file.getContentType();
        String name = file.getOriginalFilename();
        return "application/pdf".equalsIgnoreCase(type)
                || (name != null && name.toLowerCase().endsWith(".pdf"));
    }
}
