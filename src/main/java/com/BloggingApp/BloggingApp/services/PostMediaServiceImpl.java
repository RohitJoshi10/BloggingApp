package com.BloggingApp.BloggingApp.services;

import com.BloggingApp.BloggingApp.entities.Post;
import com.BloggingApp.BloggingApp.entities.PostMedia;
import com.BloggingApp.BloggingApp.exceptions.ApiException;
import com.BloggingApp.BloggingApp.exceptions.ResourceNotFoundException;
import com.BloggingApp.BloggingApp.payloads.MediaDownloadResource;
import com.BloggingApp.BloggingApp.payloads.PostDTO;
import com.BloggingApp.BloggingApp.repositories.PostMediaRepository;
import com.BloggingApp.BloggingApp.repositories.PostRepository;
import com.BloggingApp.BloggingApp.services.interfaces.PostMediaServiceInterface;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostMediaServiceImpl implements PostMediaServiceInterface {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final Cloudinary cloudinary;
    private final FileServiceImpl fileService;
    private final ModelMapper modelMapper;

    // 1. ADD MEDIA (Append Only)
    @Override
    public PostDTO addMedia(Integer postId, List<MultipartFile> files) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Note: validateOwnership method ko common utility mein daal dena ya yahan copy kar lena
        validateOwnership(post);

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                Map uploadResult = fileService.uploadImage(null, file);
                PostMedia media = new PostMedia();
                media.setFileUrl(uploadResult.get("url").toString());
                media.setPublicId(uploadResult.get("public_id").toString());

                String resType = uploadResult.get("resource_type").toString();
                String format = uploadResult.get("format") != null ? uploadResult.get("format").toString() : "raw";
                media.setFileType(resType + "/" + format);

                media.setPost(post);
                post.getMediaFiles().add(media);
            }
        }
        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, PostDTO.class);
    }

    // 2. DELETE SPECIFIC MEDIA
    @Override
    public void deleteMedia(Integer mediaId) {
        PostMedia media = postMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", "id", mediaId));

        validateOwnership(media.getPost());

        try {
            // 1. Content Type ko lowercase karke check karo
            String cType = media.getFileType().toLowerCase();
            String resType = "image"; // Default

            if (cType.contains("video")) {
                resType = "video";
            }
            // IMPORTANT: PDF ke liye 'raw' resource type hona chahiye
            else if (cType.contains("pdf") || cType.contains("application")) {
                resType = "raw";
            }

            // 2. Cloudinary se delete karo
            Map result = cloudinary.uploader().destroy(media.getPublicId(),
                    ObjectUtils.asMap("resource_type", resType));

            System.out.println("DEBUG: Deleting from Cloudinary | ID: " + media.getPublicId() + " | Type: " + resType);
            System.out.println("DEBUG: Cloudinary Response: " + result);

            // 3. Agar abhi bhi "not found" aaye, toh ek last try 'image' type ke saath kar lo (Just in case)
            if ("not found".equals(result.get("result")) && !resType.equals("image")) {
                cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.asMap("resource_type", "image"));
            }

        } catch (IOException e) {
            throw new ApiException("Cloudinary cleanup failed!");
        }

        postMediaRepository.delete(media);
    }

    // 3. UPDATE/REPLACE SPECIFIC MEDIA
    @Override
    public PostDTO updateMedia(Integer mediaId, MultipartFile file) throws IOException {
        PostMedia oldMedia = postMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", "id", mediaId));

        validateOwnership(oldMedia.getPost());

        // Delete old from Cloudinary
        String oldResType = oldMedia.getFileType().contains("video") ? "video" :
                (oldMedia.getFileType().contains("pdf") ? "raw" : "image");
        cloudinary.uploader().destroy(oldMedia.getPublicId(), ObjectUtils.asMap("resource_type", oldResType));

        // Upload new
        Map uploadResult = fileService.uploadImage(null, file);
        oldMedia.setFileUrl(uploadResult.get("url").toString());
        oldMedia.setPublicId(uploadResult.get("public_id").toString());

        String resType = uploadResult.get("resource_type").toString();
        String format = uploadResult.get("format") != null ? uploadResult.get("format").toString() : "raw";
        oldMedia.setFileType(resType + "/" + format);

        postMediaRepository.save(oldMedia);
        return modelMapper.map(oldMedia.getPost(), PostDTO.class);
    }

    @Override
    public MediaDownloadResource downloadMedia(Integer mediaId) throws IOException {
        // 1. DB Fetch
        PostMedia media = postMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", "id", mediaId));

        // 2. Cloudinary se bytes nikalna
        URL url = new URL(media.getFileUrl());
        byte[] data;
        try (InputStream is = url.openStream()) {
            data = StreamUtils.copyToByteArray(is);
        }

        // 3. Filename aur Type ka logic yahi rakho
        String extension = media.getFileType().contains("/") ? media.getFileType().split("/")[1] : "bin";
        String fileName = "media_" + mediaId + "." + extension;

        // 4. Sab pack karke return karo
        return new MediaDownloadResource(data, fileName, media.getFileType());
    }

    private void validateOwnership(Post post) {
        // Current logged-in user ki details nikalna
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = "";

        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else {
            currentUsername = principal.toString();
        }

        // 1. Agar login wala banda Post ka owner hai (Email match)
        // 2. Ya phir login wala banda ADMIN hai
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (!post.getUser().getEmail().equals(currentUsername) && !isAdmin) {
            // Agar dono nahi hain, toh dhakka maar ke bahar nikalo
            throw new ApiException("Unauthorised! You are not the owner of this post and you are not an Admin.");
        }
    }
}
