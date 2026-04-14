package com.BloggingApp.BloggingApp.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);

        // 🔥 Railway connectivity issues ke liye ye mandatory hai
        config.put("connection_timeout", "20000");
        config.put("read_timeout", "20000");

        return new Cloudinary(config);
    }
}


/*
In pom add cloudinary dependecy
Ab ek config package bana aur usmein CloudinaryConfig.java naam ki class bana.
Ye class tere API keys ko read karke Cloudinary ka "Bean" ready karegi.

 */