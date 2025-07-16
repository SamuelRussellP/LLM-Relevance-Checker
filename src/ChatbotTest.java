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
        String generationPrompt = "You are a user applying about a job! If chatbot response seems like the process is done, say thank you!";
        String chatbotReply = "Thanks for the info! You have completed the job application process! You can wait for further announcements";

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

    @Test
    public void testJobApplicationCompleted() {
        String message = "I am going to choose Software Engineer position. I have submitted my resume and cover letter.";
        String chatbotReply = "Thanks for the info! You have completed the job application process! You can wait for further announcements";

        try {
            boolean completed = RelevanceChecker.checkJobApplicationCompletion(message, chatbotReply);
            System.out.println("🧪 Job Application Completed Status: " + completed);
        } catch (Exception e) {
            System.err.println("❌ Error during job application check: " + e.getMessage());
        }
    }
}
