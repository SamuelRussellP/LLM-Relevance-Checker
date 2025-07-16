package models;

public class MessageChatbotResponse {
    public String message;
    public String chatbot_response;

    public MessageChatbotResponse(String message, String chatbot_response) {
        this.message = message;
        this.chatbot_response = chatbot_response;
    }
}
