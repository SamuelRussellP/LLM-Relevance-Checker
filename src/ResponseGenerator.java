import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import models.ResponseResult;

public class ResponseGenerator {

    public static String generateResponse(String prompt, String chatbotReply) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        // Create request payload object
        GenerateRequest requestBody = new GenerateRequest(prompt, chatbotReply);
        String payloadJson = new Gson().toJson(requestBody);

        System.out.println("📤 Payload to /generate-response:\n" + payloadJson);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8001/generate-response"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Status Code: " + response.statusCode());
            System.out.println("📥 Raw Response: " + response.body());

            if (response.statusCode() != 200) {
                System.err.println("❌ Server returned error.");
                return null;
            }

            GenerateResult result = new Gson().fromJson(response.body(), GenerateResult.class);
            System.out.println("✅ Generated Response: " + result.generated_response);

            return result.generated_response;

        } catch (JsonSyntaxException e) {
            System.err.println("❌ JSON parse error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Request failed: " + e.getMessage());
        }

        return null;
    }

    // Request body class
    static class GenerateRequest {
        String prompt;
        String chatbot_reply;

        GenerateRequest(String prompt, String chatbotReply) {
            this.prompt = prompt;
            this.chatbot_reply = chatbotReply;
        }
    }

    // Response body class
    static class GenerateResult {
        String generated_response;
    }
}
