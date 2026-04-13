package com.BloggingApp.BloggingApp.services.interfaces;

import com.BloggingApp.BloggingApp.payloads.PostDTO;
import com.BloggingApp.BloggingApp.payloads.PostResponse;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PostServiceInterface {

    PostDTO createPost(PostDTO postDTO, Integer userId, Integer categoryId);

    PostDTO updatePost(PostDTO postDTO, Integer postId);

    PostDTO movePostToNewCategory(Integer postId, Integer categoryId);

    void deletePost(Integer postId) throws IOException;

    PostResponse getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    PostDTO getPostById(Integer postId);

    PostResponse getPostByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    PostResponse getPostByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    PostResponse searchPosts(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    PostDTO toggleLike(Integer postId, Integer userId);

}
