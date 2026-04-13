package com.BloggingApp.BloggingApp.controllers;

import com.BloggingApp.BloggingApp.exceptions.ApiException;
import com.BloggingApp.BloggingApp.exceptions.ResourceNotFoundException;
import com.BloggingApp.BloggingApp.payloads.*;
import com.BloggingApp.BloggingApp.security.JwtTokenHelper;
import com.BloggingApp.BloggingApp.services.interfaces.UserServiceInterface;
import com.cloudinary.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenHelper jwtTokenHelper;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final UserServiceInterface userService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> createToken(@RequestBody JwtAuthRequest request) throws Exception {

        // 1. Authenticate user
        this.authenticate(request.getUsername(), request.getPassword());

        // 2. Agar authentication successful raha toh user details load karo
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getUsername());

        // 3. Token generate karo
        String token = this.jwtTokenHelper.generateToken(userDetails);

        // 4. Response bhej do
        JwtAuthResponse response = new JwtAuthResponse();
        response.setToken(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDto) {
        UserDTO registeredUser = this.userService.registerNewUser(userDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    private void authenticate(String username, String password) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        try {
            this.authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            System.out.println("Invalid Details !!");
            // Ab hum naya exception throw karenge
            throw new ApiException("Invalid username or password !!");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> sendOtpForForgotPassword(@RequestParam String phoneNumber){
        userService.sendOtpForForgotPassword(phoneNumber);
        return new ResponseEntity<>(new ApiResponse("OTP sent successfully!", true), HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtpAndResetPassword(@RequestBody ResetPasswordRequest request){
        userService.verifyOtpAndResetPassword(request);
        return new ResponseEntity<>(new ApiResponse("Password reset successfully!", true), HttpStatus.OK);
    }
}