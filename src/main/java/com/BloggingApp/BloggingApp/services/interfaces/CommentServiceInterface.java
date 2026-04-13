package com.BloggingApp.BloggingApp.services.interfaces;

import com.BloggingApp.BloggingApp.payloads.CommentDTO;
import com.BloggingApp.BloggingApp.payloads.UserDTO;

import java.util.List;

public interface CommentServiceInterface {
    CommentDTO createComment(CommentDTO commentDTO, Integer postId, Integer userId);
    void deleteComment(Integer commentId);
    CommentDTO updateComment(CommentDTO commentDTO, Integer commentId);
    List<CommentDTO> getCommentsByUser(Integer userId);
}
