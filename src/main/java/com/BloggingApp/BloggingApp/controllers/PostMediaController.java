package com.BloggingApp.BloggingApp.controllers;

import com.BloggingApp.BloggingApp.entities.PostMedia;
import com.BloggingApp.BloggingApp.payloads.ApiResponse;
import com.BloggingApp.BloggingApp.payloads.MediaDownloadResource;
import com.BloggingApp.BloggingApp.payloads.PostDTO;
import com.BloggingApp.BloggingApp.repositories.PostMediaRepository;
import com.BloggingApp.BloggingApp.services.interfaces.PostMediaServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class PostMediaController {
    private final PostMediaServiceInterface postMediaService;
    private final PostMediaRepository postMediaRepository;

    // Add more media to a post
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PostMapping("/upload/{postId}")
    public ResponseEntity<PostDTO> addMedia(@PathVariable Integer postId, @RequestParam("media") List<MultipartFile> files) throws IOException {
        return ResponseEntity.ok(postMediaService.addMedia(postId, files));
    }

    // Replace a specific media file
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PutMapping("/update/{mediaId}")
    public ResponseEntity<PostDTO> updateMedia(@PathVariable Integer mediaId, @RequestParam("media") MultipartFile file) throws IOException {
        return ResponseEntity.ok(postMediaService.updateMedia(mediaId, file));
    }

    // Delete a specific media file
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @DeleteMapping("/delete/{mediaId}")
    public ResponseEntity<ApiResponse> deleteMedia(@PathVariable Integer mediaId) {
        postMediaService.deleteMedia(mediaId);
        return ResponseEntity.ok(new ApiResponse("Media deleted successfully", true));
    }

    // DOWNLOAD MEDIA
    @GetMapping("/download/{mediaId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Integer mediaId) throws IOException {

        // Service se pura resource bundle mango
        MediaDownloadResource resource = postMediaService.downloadMedia(mediaId);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFileName() + "\"");
        headers.setContentType(MediaType.parseMediaType(resource.getContentType()));

        return new ResponseEntity<>(resource.getData(), headers, HttpStatus.OK);
    }
}
