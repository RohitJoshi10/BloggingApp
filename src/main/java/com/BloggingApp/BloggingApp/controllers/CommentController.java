package com.BloggingApp.BloggingApp.controllers;

import com.BloggingApp.BloggingApp.payloads.ApiResponse;
import com.BloggingApp.BloggingApp.payloads.CommentDTO;
import com.BloggingApp.BloggingApp.repositories.CommentRepository;
import com.BloggingApp.BloggingApp.services.interfaces.CommentServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentServiceInterface commentService;

    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PostMapping("/post/{postId}/user/{userId}/comment")
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO, @PathVariable Integer postId, @PathVariable Integer userId){
        CommentDTO createdComment = commentService.createComment(commentDTO, postId, userId);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable  Integer commentId){
        commentService.deleteComment(commentId);
        return new ResponseEntity<ApiResponse>(new ApiResponse("Comment deleted successfully", true), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@Valid @RequestBody CommentDTO commentDTO, @PathVariable Integer commentId){
        CommentDTO updatedComment = commentService.updateComment(commentDTO, commentId);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @GetMapping("/comment/{userId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByUser(@PathVariable Integer userId){
        List<CommentDTO> commentDTOList = commentService.getCommentsByUser(userId);
        return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
    }

}
