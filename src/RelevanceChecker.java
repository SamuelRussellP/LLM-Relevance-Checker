import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import models.PromptResponse;
import models.ResponseResult;

public class RelevanceChecker {

    public static double checkRelevance(String prompt, String response) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        PromptResponse payloadObj = new PromptResponse(prompt, response);
        String payloadJson = new Gson().toJson(payloadObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8000/check-relevance"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> responseObj = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (responseObj.statusCode() != 200) {
            System.out.println("❌ Server returned error.");
            return -1;
        }

        ResponseResult result = new Gson().fromJson(responseObj.body(), ResponseResult.class);

        System.out.println("Relevance Score: " + result.relevance_score);
        System.out.println("Confidence: " + result.confidence);
        System.out.println(result.is_relevant ? "✅ Relevant" : "❌ Not Relevant");

        return result.relevance_score;
    }

    public static double checkRelevanceByMistral(String message, String chatbotReply) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        PromptResponse payload = new PromptResponse(message, chatbotReply);
        String jsonPayload = new Gson().toJson(payload);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/check-relevance-mistral"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Mistral Relevance Status: " + response.statusCode());
            System.out.println("📥 Response: " + response.body());

            if (response.statusCode() != 200) {
                System.err.println("❌ Mistral server error.");
                return -1;
            }

            ResponseResult result = new Gson().fromJson(response.body(), ResponseResult.class);

            System.out.println("✅ Mistral Relevance Score: " + result.relevance_score);
            System.out.println(result.is_relevant ? "✅ Relevant" : "❌ Not Relevant");

            // ✅ Show reasoning if available
            if (result.reasoning != null && !result.reasoning.isBlank()) {
                System.out.println("🧠 Reasoning: " + result.reasoning);
            }

            return result.relevance_score;

        } catch (JsonSyntaxException e) {
            System.err.println("❌ JSON parsing error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ HTTP request error: " + e.getMessage());
        }

        return -1;
    }
}
