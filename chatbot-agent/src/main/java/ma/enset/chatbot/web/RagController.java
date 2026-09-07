package ma.enset.chatbot.web;

import ma.enset.chatbot.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {
    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping(value = "/index", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> index(@RequestPart("file") MultipartFile file) throws IOException {
        int chunks = ragService.indexUploadedPdf(file);
        return Map.of("file", file.getOriginalFilename(), "indexedChunks", chunks);
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query,
                                 @RequestParam(defaultValue = "4") int topK) {
        return ragService.search(query, topK);
    }

    @GetMapping("/status")
    public Map<String, Integer> status() {
        return Map.of("indexedChunks", ragService.indexedChunks());
    }
}
