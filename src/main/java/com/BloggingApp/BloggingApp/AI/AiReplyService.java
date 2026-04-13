package com.BloggingApp.BloggingApp.AI;

import com.BloggingApp.BloggingApp.entities.Comment;
import com.BloggingApp.BloggingApp.entities.Post;
import com.BloggingApp.BloggingApp.entities.User;
import com.BloggingApp.BloggingApp.repositories.CommentRepository;
import com.BloggingApp.BloggingApp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiReplyService {

    private final AiService aiService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Async
    public void handleAiReply(Comment userComment, Post post) {
        try {
            // AI Response generation
            String aiReplyContent = aiService.generateSmartReply(userComment.getContent());

            // AI Bot User fetch
            User aiUser = userRepository.findByEmail("ai-bot@blogapp.com")
                    .orElseGet(() -> userRepository.findAll().get(0));

            Comment aiReply = new Comment();
            aiReply.setContent(aiReplyContent);
            aiReply.setPost(post);
            aiReply.setUser(aiUser);

            commentRepository.save(aiReply);
        } catch (Exception e) {
            System.err.println("AI background task failed: " + e.getMessage());
        }
    }
}
