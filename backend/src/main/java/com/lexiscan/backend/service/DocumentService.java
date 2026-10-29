
package com.lexiscan.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexiscan.backend.entity.Document;
import com.lexiscan.backend.entity.User;
import com.lexiscan.backend.repository.DocumentRepository;
import com.lexiscan.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository docs;
    private final UserRepository users;
    private final PdfService pdf;
    private final GeminiService gemini;
    private final ObjectMapper mapper;
    private final Path dir;

    public DocumentService(
            DocumentRepository d,
            UserRepository u,
            PdfService p,
            GeminiService g,
            ObjectMapper m,
            @Value("${app.storage-dir}") String s) {

        docs = d;
        users = u;
        pdf = p;
        gemini = g;
        mapper = m;
        dir = Paths.get(s);
    }

    public Document upload(MultipartFile f, String email) throws Exception {

        if (f.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (f.getSize() > 10_000_000) {
            throw new IllegalArgumentException(
                    "Maximum file size is 10MB");
        }

        String name = f.getOriginalFilename() == null
                ? "file.pdf"
                : f.getOriginalFilename();

        if (!name.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "MVP accepts PDF only");
        }

        Files.createDirectories(dir);

        String stored = UUID.randomUUID() + ".pdf";
        Path storedPath = dir.resolve(stored);

        Files.copy(
                f.getInputStream(),
                storedPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        User u = users.findByEmail(email).orElseThrow();

        Document d = new Document();
        d.setOriginalFileName(name);
        d.setStoredFileName(stored);
        d.setContentType("application/pdf");
        d.setFileSize(f.getSize());
        d.setUser(u);
        d.setStatus("EXTRACTING");

        docs.save(d);

        try {
            // Step 1: Extract text from the PDF.
            String text = pdf.extract(f);

            if (text.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text found in PDF");
            }

            d.setExtractedText(text);
            d.setStatus("ANALYZING");
            docs.save(d);

            // Step 2: Analyze extracted text using Gemini.
            JsonNode analysis = gemini.analyze(text);

            if (analysis == null || !analysis.isObject()) {
                throw new IllegalStateException(
                        "Gemini returned an invalid analysis object");
            }

            // Step 3: Serialize the analysis as JSON.
            String analysisJson =
                    mapper.writeValueAsString(analysis);

            d.setAnalysisJson(analysisJson);
            d.setStatus("ANALYZED");

            // Step 4: Save the completed document.
            return docs.save(d);

        } catch (Exception e) {

            log.error(
                    "Document processing failed for document ID {}",
                    d.getId(),
                    e
            );

            d.setStatus("FAILED");

            try {
                docs.save(d);
            } catch (Exception saveError) {
                log.error(
                        "Could not save FAILED status for document ID {}",
                        d.getId(),
                        saveError
                );
            }

            throw e;
        }
    }

    public List<Document> list(String email) {
        return docs.findByUserOrderByCreatedAtDesc(
                users.findByEmail(email).orElseThrow()
        );
    }

    public Document get(Long id, String email) {
        return docs.findByIdAndUser(
                id,
                users.findByEmail(email).orElseThrow()
        ).orElseThrow(
                () -> new IllegalArgumentException("Document not found")
        );
    }

    public void delete(Long id, String email) throws Exception {
        Document d = get(id, email);
        Files.deleteIfExists(dir.resolve(d.getStoredFileName()));
        docs.delete(d);
    }
}
