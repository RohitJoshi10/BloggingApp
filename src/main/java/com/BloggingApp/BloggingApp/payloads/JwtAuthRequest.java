package com.BloggingApp.BloggingApp.payloads;

import lombok.Data;

@Data
public class JwtAuthRequest {
    private String username; // Email
    private String password;
}
