package com.BloggingApp.BloggingApp.services.interfaces;

import com.BloggingApp.BloggingApp.payloads.MediaDownloadResource;
import com.BloggingApp.BloggingApp.payloads.PostDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PostMediaServiceInterface {
    PostDTO addMedia(Integer postId, List<MultipartFile> files) throws IOException;
    void deleteMedia(Integer mediaId);
    PostDTO updateMedia(Integer mediaId, MultipartFile file) throws IOException;
    MediaDownloadResource downloadMedia(Integer mediaId) throws IOException;
}
