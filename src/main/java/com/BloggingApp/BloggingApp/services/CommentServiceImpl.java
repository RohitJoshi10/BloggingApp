package com.BloggingApp.BloggingApp.services;

import com.BloggingApp.BloggingApp.AI.AiReplyService;
import com.BloggingApp.BloggingApp.AI.AiService;
import com.BloggingApp.BloggingApp.entities.Comment;
import com.BloggingApp.BloggingApp.entities.Post;
import com.BloggingApp.BloggingApp.entities.User;
import com.BloggingApp.BloggingApp.exceptions.ApiException;
import com.BloggingApp.BloggingApp.exceptions.ResourceNotFoundException;
import com.BloggingApp.BloggingApp.infrastructure.email.EmailService;
import com.BloggingApp.BloggingApp.payloads.CommentDTO;
import com.BloggingApp.BloggingApp.repositories.CommentRepository;
import com.BloggingApp.BloggingApp.repositories.PostRepository;
import com.BloggingApp.BloggingApp.repositories.UserRepository;
import com.BloggingApp.BloggingApp.services.interfaces.CommentServiceInterface;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentServiceInterface {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final AiReplyService aiReplyService;


    @Override
    public CommentDTO createComment(CommentDTO commentDTO, Integer postId, Integer userId) {
        validateUserOwnership(userId);

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Save User Comment
        Comment comment = modelMapper.map(commentDTO, Comment.class);
        comment.setPost(post);
        comment.setUser(user);
        Comment savedComment = commentRepository.save(comment);

        // Send Email Turant
        String postOwnerEmail = post.getUser().getEmail();
        if(!postOwnerEmail.equalsIgnoreCase(user.getEmail())) {
            emailService.sendCommentNotification(postOwnerEmail, post.getUser().getName(), user.getName(), post.getTitle(), savedComment.getContent());
        }

        // 2. Call Async service method (Ab ye sach mein alag thread mein chalega)
        aiReplyService.handleAiReply(savedComment, post);

        return modelMapper.map(savedComment, CommentDTO.class);
    }

//    @Override
//    public CommentDTO createComment(CommentDTO commentDTO, Integer postId, Integer userId) {
//        // Security Check: Kya logged-in user wahi hai jo userId bhej raha hai?
//        validateUserOwnership(userId);
//        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post", "id", postId));
//        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "id", userId));
//        Comment comment = modelMapper.map(commentDTO, Comment.class);
//        comment.setPost(post);
//        comment.setUser(user);
//        Comment savedComment = commentRepository.save(comment);
//
//        // Email Notification Logic
//        String postOwnerEmail = post.getUser().getEmail();
//        String postOwnerName = post.getUser().getName();
//        String postTitle = post.getTitle();
//        String commenterName = user.getName();
//        String commentContent = savedComment.getContent();
//
//        // Jiski post hai wo khud ki he post pe comment kre toh usse mail na jaye
//        if(!postOwnerEmail.equalsIgnoreCase(user.getEmail()))
//        {
//            emailService.sendCommentNotification(postOwnerEmail, postOwnerName, commenterName, postTitle, commentContent);
//        }
//
//        return modelMapper.map(savedComment, CommentDTO.class);
//    }

    @Override
    public void deleteComment(Integer commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new ResourceNotFoundException("Comment", "id", commentId));
        // SECURITY CHECK: Owner or Admin?
        validateCommentDeletion(comment);
        commentRepository.delete(comment);
    }

    @Override
    public CommentDTO updateComment(CommentDTO commentDTO, Integer commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new ResourceNotFoundException("Comment", "id", commentId));
        // SECURITY CHECK: Owner or Admin?
        validateCommentOwnership(comment);
        comment.setContent(commentDTO.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return modelMapper.map(updatedComment, CommentDTO.class);
    }

    @Override
    public List<CommentDTO> getCommentsByUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "id", userId));
        List<Comment> comments = commentRepository.findByUser(user);
        List<CommentDTO> commentDTOS = comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).toList();
        return commentDTOS;
    }

    private void validateCommentDeletion(Comment comment){
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Kya logged-in banda ADMIN hai?
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        // 2. Kya ye comment isi bande ne likha hai  ?
        boolean isCommentOwner = comment.getUser().getEmail().equals(currentUsername);

        // 3. Kya ye banda us POST ka owner hai jispar comment hua hai ?
        boolean isPostOwner = comment.getPost().getUser().getEmail().equals(currentUsername);

        // Agar teeno he condition false hai toh error thorw kr denge
        if(!isCommentOwner && !isPostOwner && !isAdmin){
            throw new ApiException("Access Denied: You are not authorized to delete this comment as you are neither the comment author, the post owner, nor an admin");
        }
    }

    private void validateUserOwnership(Integer userId){
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User", "id", userId));

        if(!user.getEmail().equals(currentUsername) && !isAdmin){
            throw new ApiException("Unauthorised access to this user ID!");
        }
    }

    private void validateCommentOwnership(Comment comment) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        // Comment ke user ki email aur logged-in user ki email match honi chahiye
        if (!comment.getUser().getEmail().equals(currentUsername) && !isAdmin) {
            throw new ApiException("Unauthorised! You can't modify this comment.");
        }
    }
}
