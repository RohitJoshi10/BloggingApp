package com.BloggingApp.BloggingApp.controllers;


import com.BloggingApp.BloggingApp.config.AppConstants;
import com.BloggingApp.BloggingApp.entities.Post;
import com.BloggingApp.BloggingApp.entities.PostMedia;
import com.BloggingApp.BloggingApp.exceptions.ResourceNotFoundException;
import com.BloggingApp.BloggingApp.payloads.ApiResponse;
import com.BloggingApp.BloggingApp.payloads.PostDTO;
import com.BloggingApp.BloggingApp.payloads.PostResponse;
import com.BloggingApp.BloggingApp.repositories.PostMediaRepository;
import com.BloggingApp.BloggingApp.repositories.PostRepository;
import com.BloggingApp.BloggingApp.services.interfaces.FileServiceInterface;
import com.BloggingApp.BloggingApp.services.interfaces.PostServiceInterface;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceInterface postService;
    private final PostMediaRepository postMediaRepository;

    @Value("${project.image}")
    private String path;

    // 1. Create Post: Isme PathVariable wala userId authentication se match hona chahiye
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('NORMAL', 'ADMIN') and #userId == authentication.principal.id)")
    @PostMapping("/user/{userId}/category/{categoryId}/posts")
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostDTO postDTO, @PathVariable Integer userId, @PathVariable Integer categoryId){
        PostDTO createdPost = postService.createPost(postDTO,userId,categoryId);
        return new ResponseEntity<PostDTO>(createdPost, HttpStatus.CREATED);
    }


    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<PostResponse> getPostByUser(@PathVariable Integer userId, @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber, @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize, @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir){
        PostResponse postResponse =  postService.getPostByUser(userId, pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<PostResponse>(postResponse,HttpStatus.OK);
    }


    @GetMapping("/category/{categoryId}/posts")
    public ResponseEntity<PostResponse> getPostByCategory(@PathVariable Integer categoryId, @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber, @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize, @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir){
        PostResponse postResponse =  postService.getPostByCategory(categoryId, pageNumber, pageSize, sortBy,sortDir);
        return new ResponseEntity<PostResponse>(postResponse,HttpStatus.OK);
    }


    @GetMapping("/posts")
    public ResponseEntity<PostResponse> getAllPost(@RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber, @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize, @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir){
        PostResponse postResponse = postService.getAllPost(pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<PostResponse>(postResponse,HttpStatus.OK);
    }


    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Integer postId){
        PostDTO post = postService.getPostById(postId);
        return new ResponseEntity<PostDTO>(post, HttpStatus.OK);
    }

    // 2. Update Post: Ismein logic thoda tricky hai.
    // Humein check karna padega ki ye postId ka owner kaun hai.
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> updatePost(@Valid @RequestBody PostDTO postDTO, @PathVariable Integer postId){
        PostDTO post = postService.updatePost(postDTO, postId);
        return ResponseEntity.ok(post);
    }


    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PutMapping("/posts/{postId}/category/{categoryId}")
    public ResponseEntity<PostDTO> movePostToNewCategory(@PathVariable Integer postId, @PathVariable Integer categoryId){
        PostDTO movedPost = postService.movePostToNewCategory(postId, categoryId);
        return new ResponseEntity<>(movedPost, HttpStatus.OK);
    }

    // 3. Delete Post: Same logic - Admin ya Post ka Malik
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Integer postId) throws IOException {
        postService.deletePost(postId);
        return new ResponseEntity(new ApiResponse("Post Deleted Successfully", true), HttpStatus.OK);
    }

    @GetMapping("/posts/search/{keywords}")
    public ResponseEntity<PostResponse> searchPosts(@PathVariable String keywords, @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber, @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize, @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy, @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir){
        PostResponse PostResponse = postService.searchPosts(keywords, pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(PostResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PostMapping("/post/{postId}/like/{userId}")
    public ResponseEntity<PostDTO> toggleLike(@PathVariable Integer postId, @PathVariable Integer userId){
        PostDTO updatedPost = postService.toggleLike(postId, userId);
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
    }

}
