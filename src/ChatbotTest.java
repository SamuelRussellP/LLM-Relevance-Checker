import org.junit.Test;

public class ChatbotTest {

    @Test
    public void testRelevanceChecker() throws Exception {
        String prompt = "Is Java a good programming language?";
        String chatbotReply = "Java is not a good programming language anymore";

        double score = RelevanceChecker.checkRelevance(prompt, chatbotReply);

        System.out.println("🧪 Relevance Score: " + score);
        // You can add assertions if needed:
        // Assertions.assertTrue(score >= 0);
    }

    @Test
    public void testResponseGenerator() {
        String generationPrompt = "You are a user asking about Java. If the chatbot says something negative, respond in a positive and natural way.";
        String chatbotReply = "Java is not a good programming language anymore";

        String generated = ResponseGenerator.generateResponse(generationPrompt, chatbotReply);

        System.out.println("🎯 Generated Follow-up: " + generated);
        // Optional:
        // Assertions.assertNotNull(generated);
    }

    @Test
    public void testRelevanceCheckerByMistral() {
        String message = "Is Java a good programming language?";
        String chatbotReply = "Java is not a good programming language anymore. However, it has a large ecosystem and is widely used in enterprise applications.";

        try {
            double score = RelevanceChecker.checkRelevanceByMistral(message, chatbotReply);
            System.out.println("🧪 Mistral Relevance Score: " + score);
        } catch (Exception e) {
            System.err.println("❌ Error during Mistral relevance check: " + e.getMessage());
        }
    }

}
