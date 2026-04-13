package com.BloggingApp.BloggingApp.payloads;

import lombok.Data;

@Data
public class JwtAuthResponse {
    private String token;
    // private UserDTO user;
    private JwtUserShortDTO user;
    private String message;
}
