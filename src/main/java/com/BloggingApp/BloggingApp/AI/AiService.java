package com.BloggingApp.BloggingApp.AI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    // ChatClient.Builder SpringAI provide karata hai
    public AiService(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }

    public String generateSmartReply(String comment) {
        // List of personalities (AI prompt mein hi pass kar denge)
        String prompt = """
        You are the Smart Assistant for a Blogging Platform. 
        Analyze the user's comment and reply in ONE of these three personalities (choose the most suitable one):
        
        1. TECH_BRO: Chill, uses 'Bhai', 'Mast', 'Khatarnaak'. Reply in Hinglish.
        2. MENTOR: Professional, deep technical insights, encouraging.
        3. CRITIC: Witty and slightly sarcastic if the comment is lazy.
        
        COMMENT: %s
        
        REPLY:
        """.formatted(comment);

        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            return "Bro, AI is busy right now 🅱️";
        }
    }

}
