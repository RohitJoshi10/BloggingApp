package com.BloggingApp.BloggingApp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        // 1. Status set karo (401 Unauthorized)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 2. Content type batao ki hum JSON bhej rahe hain
        response.setContentType("application/json");

        // 3. JSON Body likho
        // Aap chaho toh yahan ek custom Map ya DTO ko ObjectMapper se convert karke bhi bhej sakte ho
        String jsonResponse = "{ \"message\": \"Access Denied !! " + authException.getMessage() + "\", \"success\": false }";

        response.getWriter().write(jsonResponse);
    }
}


// Ye commence method tab chlega jb ek unauthorized banda access krne ki kosish krega ek authenticated request ko.