package com.skillbarter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Gemini AI Service — AI Chat Assistant feature.
 *
 * Uses Google Gemini 2.0 Flash via REST API (no SDK needed).
 * Provides skill suggestions, message drafting, and FAQ answers
 * tailored to the SkillBarter platform context.
 *
 * Design Pattern: Strategy — the system prompt can be swapped
 * to change AI behaviour without modifying callers.
 *
 * SOLID – SRP: only handles Gemini API communication.
 * SOLID – DIP: callers depend on this abstraction, not raw HTTP.
 */
@Service
@Slf4j
public class GeminiAiService {

    private static final String SYSTEM_CONTEXT = """
            You are SkillBot, the AI assistant for SkillBarter — a peer-to-peer skill exchange platform
            where users trade "Time Credits" (1 hour of teaching = 1 credit, no real money involved).
            
            Your role:
            - Help users find the right skills to learn or teach
            - Suggest how to write a good skill description or bio
            - Answer questions about how the platform works (sessions, escrow, disputes, badges)
            - Give learning tips and study advice
            - Help draft polite messages to teachers or learners
            - Explain the credit/escrow system clearly
            
            Platform facts:
            - Credits are earned by teaching, spent by learning
            - Sessions go: REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED
            - Credits are held in escrow until both parties confirm completion
            - Disputes are resolved by a Tribunal Verifier
            - Badges are awarded for verified skills and streaks
            - Categories: TECHNOLOGY, LANGUAGES, MUSIC, ARTS, FITNESS, COOKING, ACADEMICS, BUSINESS, CRAFTS, OTHER
            
            Keep responses concise, friendly, and practical. Use bullet points when listing things.
            Never discuss real money, payments, or anything outside the platform scope.
            """;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Send a user message and get a Gemini response.
     *
     * @param userMessage the user's question or request
     * @return AI response text, or a friendly error message
     */
    public String chat(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Please type a message first!";
        }

        try {
            String requestBody = buildRequestBody(userMessage);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                log.warn("Gemini API returned status {}: {}", response.statusCode(), response.body());
                return "I'm having trouble connecting right now. Please try again in a moment.";
            }

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            return "Sorry, I couldn't process that request. Please try again.";
        }
    }

    /**
     * Specialized: suggest skills based on user interests.
     */
    public String suggestSkills(String interests, String currentSkills) {
        String prompt = String.format(
                "A SkillBarter user has these interests: %s. " +
                "They already know: %s. " +
                "Suggest 5 specific skills they could learn on the platform, " +
                "with a one-line reason for each. Format as a numbered list.",
                interests, currentSkills.isBlank() ? "nothing listed yet" : currentSkills);
        return chat(prompt);
    }

    /**
     * Specialized: help write a skill description.
     */
    public String improveSkillDescription(String skillName, String rawDescription) {
        String prompt = String.format(
                "Help me write a compelling skill description for '%s' on SkillBarter. " +
                "My rough notes: %s. " +
                "Write a polished 2-3 sentence description that would attract learners. " +
                "Be specific about what learners will gain.",
                skillName, rawDescription);
        return chat(prompt);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private String buildRequestBody(String userMessage) throws Exception {
        ObjectNode root = mapper.createObjectNode();

        // System instruction
        ObjectNode systemInstruction = mapper.createObjectNode();
        ObjectNode systemPart = mapper.createObjectNode();
        systemPart.put("text", SYSTEM_CONTEXT);
        ArrayNode systemParts = mapper.createArrayNode();
        systemParts.add(systemPart);
        systemInstruction.set("parts", systemParts);
        root.set("systemInstruction", systemInstruction);

        // User message
        ObjectNode userContent = mapper.createObjectNode();
        userContent.put("role", "user");
        ObjectNode userPart = mapper.createObjectNode();
        userPart.put("text", userMessage);
        ArrayNode userParts = mapper.createArrayNode();
        userParts.add(userPart);
        userContent.set("parts", userParts);

        ArrayNode contents = mapper.createArrayNode();
        contents.add(userContent);
        root.set("contents", contents);

        // Generation config
        ObjectNode genConfig = mapper.createObjectNode();
        genConfig.put("maxOutputTokens", 512);
        genConfig.put("temperature", 0.7);
        root.set("generationConfig", genConfig);

        return mapper.writeValueAsString(root);
    }

    private String parseResponse(String responseBody) throws Exception {
        JsonNode root = mapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText("No response generated.");
            }
        }
        return "I couldn't generate a response. Please try rephrasing your question.";
    }
}
