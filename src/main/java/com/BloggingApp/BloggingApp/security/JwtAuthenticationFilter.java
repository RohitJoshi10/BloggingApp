package com.BloggingApp.BloggingApp.security;

import com.BloggingApp.BloggingApp.infrastructure.redis.RedisService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenHelper jwtTokenHelper;
    private  final UserDetailsService userDetailsService;
    private final RedisService redisService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // 🔥 In paths par filter ko bilkul mat chalao
        if (path.contains("/api/v1/auth/") || path.contains("/api/v1/payments/webhook")) {
            filterChain.doFilter(request, response);
            return;
        }


        // 1. Get Token from the Request: Token comes in Header
        String requestToken = request.getHeader("Authorization");
        System.out.println(requestToken);

        String username = null;
        String token = null;

        if(requestToken != null && requestToken.startsWith("Bearer "))
        {
            token = requestToken.substring(7);

            // 🔥 1. Sabse pehle Redis check: Kya token blacklisted hai?
            if (redisService.isTokenBlacklisted(token)) {
                System.out.println("Token is blacklisted (Logged out user)");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is blacklisted. Please login again.");
                return; // Yahin se request khatam
            }

            try {
                username = jwtTokenHelper.getUsernameFromToken(token);
            }
            catch (IllegalArgumentException e){
                System.out.println("Unable to get JWT Token");
            }
            catch (ExpiredJwtException e){
                System.out.println("JWT Token is expired !!!");
            }
            catch (MalformedJwtException e){
                System.out.println("Invalid JWT Exception");
            }
        }
        else
        {
            System.out.println("Jwt Token does not begin with Bearer");
        }

        // 2. Validate the token
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if(jwtTokenHelper.validateToken(token, userDetails))
            {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            else
            {
                System.out.println("Invalid jwt Token");
            }
        }
        else
        {
            System.out.println("Username is null or context is not null");
        }

        filterChain.doFilter(request, response);

    }
}
