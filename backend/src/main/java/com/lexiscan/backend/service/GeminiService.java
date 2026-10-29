
package com.lexiscan.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GeminiService {

    private final RestClient client = RestClient.create();
    private final ObjectMapper mapper;
    private final String key;
    private final String model;

    public GeminiService(
            ObjectMapper mapper,
            @Value("${gemini.api-key:}") String key,
            @Value("${gemini.model:gemini-3.8-flash}") String model) {

        this.mapper = mapper;
        this.key = key;
        this.model = model;

        // Diagnostic information only; never print the API key.
        System.out.println(
                "GEMINI CONFIG CHECK: keyPresent="
                        + (key != null && !key.isBlank())
                        + ", keyLength="
                        + (key == null ? 0 : key.length())
                        + ", model=" + model
        );
    }

    public JsonNode analyze(String text) throws Exception {

        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is missing. Set GEMINI_API_KEY "
                            + "in the same terminal that starts Spring Boot."
            );
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Document text is empty; cannot analyze it."
            );
        }

        String prompt = """
                You are LexiScan AI, a contract and document analyzer.
                Do not provide legal advice.

                Analyze the document and return ONLY valid JSON with
                these keys:
                - summary: string
                - riskLevel: LOW, MEDIUM, or HIGH
                - risks: array of strings
                - importantClauses: array of objects containing
                  name, explanation, and risk
                - keyInformation: object containing parties,
                  effectiveDate, expiryDate, paymentTerms,
                  noticePeriod, and governingLaw
                - obligations: array of strings
                - disclaimer: string

                Use only facts supported by the document.
                Use "Not found" when information is absent.

                DOCUMENT:
                """ + "\n" + text;

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                },
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"
                )
        );

        String rawResponse = client.post()
                .uri(
                        "https://generativelanguage.googleapis.com/"
                                + "v1beta/models/{model}:generateContent",
                        model
                )
                .header("x-goog-api-key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty response."
            );
        }

        JsonNode response = mapper.readTree(rawResponse);

        JsonNode generatedText = response.at(
                "/candidates/0/content/parts/0/text"
        );

        if (generatedText.isMissingNode()
                || generatedText.isNull()
                || generatedText.asText().isBlank()) {

            // Avoid logging the API key or full request.
            throw new IllegalStateException(
                    "Gemini returned no analysis text. Check the API "
                            + "response and model availability."
            );
        }

        JsonNode analysis = mapper.readTree(generatedText.asText());

        if (!analysis.isObject()) {
            throw new IllegalStateException(
                    "Gemini analysis must be a JSON object, but received "
                            + analysis.getNodeType()
            );
        }

        return analysis;
    }
}
