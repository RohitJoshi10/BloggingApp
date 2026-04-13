package com.BloggingApp.BloggingApp.services;

import com.BloggingApp.BloggingApp.services.interfaces.FileServiceInterface;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileServiceInterface {

    private final Cloudinary cloudinary;

    @Override
    public Map uploadImage(String path, MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "blog_posts", "resource_type", "auto")); // resource_type: "auto" -> Image, Video, PDF sab handle karega

        return uploadResult;// Ye URL return karega (e.g., https://res.cloudinary.com/...)
//        // File Name
//        String name = file.getOriginalFilename(); // Ex: abc.png
//
//        // Random name generate file
//        String randomID = UUID.randomUUID().toString();
//        String fileName1 = randomID.concat(name.substring(name.lastIndexOf(".")));
//
//        // FullPath
//        String filePath = path + File.separator + fileName1;
//
//
//        // Create folder if not created
//        File f = new File(path);
//        if(!f.exists()) f.mkdirs();
//
//        // File Copy
//        Files.copy(file.getInputStream(), Paths.get(filePath));
//
//        return fileName1;
    }

    @Override
    public InputStream getResources(String path, String fileName) throws FileNotFoundException {
        // Cloudinary mein iski zaroorat nahi padti, direct URL frontend pe chalta hai
        throw new UnsupportedOperationException("No needed with Cloudinary !");

//        String fullPath = path + File.separator + fileName;
//        InputStream is = new FileInputStream(fullPath);
//        return is;
    }
}
