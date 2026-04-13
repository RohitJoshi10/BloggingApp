package com.BloggingApp.BloggingApp.controllers;

import com.BloggingApp.BloggingApp.entities.Role;
import com.BloggingApp.BloggingApp.payloads.JwtAuthResponse;
import com.BloggingApp.BloggingApp.payloads.JwtUserShortDTO;
import com.BloggingApp.BloggingApp.payloads.UserDTO;
import com.BloggingApp.BloggingApp.security.JwtTokenHelper;
import com.BloggingApp.BloggingApp.services.interfaces.UserServiceInterface;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UserServiceInterface userService;
    private final JwtTokenHelper jwtTokenHelper;
    private final UserDetailsService userDetailsService;
    private final ModelMapper modelMapper;

    @GetMapping("/google/success")
    public ResponseEntity<JwtAuthResponse> handleGoogleSuccess(@AuthenticationPrincipal OAuth2User oAuth2User){

        // 1. Google se data nikalo
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 2. Database check/save logic
        // Agr user nhi hai toh naya banao with default 'about' and null 'phoneNumber'
        UserDTO userDTO = this.userService.getOrCreateSocialUser(email, name, "GOOGLE");

        // 3. JWT token generate kro just like we do at the time of normal user login.
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
        String token = this.jwtTokenHelper.generateToken(userDetails);

        JwtUserShortDTO shortUser = modelMapper.map(userDTO, JwtUserShortDTO.class);

        // --- MANUALLY SET ROLES ---
        // Kyunki UserDTO mein roles nahi hain, hum userDetails se nikal kar
        // shortUser mein manually set kar rahe hain.
// Controller mein ek naya Set generate karo roles ke liye
        Set<Role> roles = userDetails.getAuthorities().stream().map(authority -> {
            Role role = new Role();
            role.setName(authority.getAuthority());

            // Manual check for ID based on your DB (image_f5c7be.png)
            if(authority.getAuthority().equals("ROLE_ADMIN")) role.setId(1);
            else if(authority.getAuthority().equals("ROLE_NORMAL")) role.setId(2);

            return role;
        }).collect(Collectors.toSet());

        shortUser.setRoles(roles);

        // 4. Send the Response
        JwtAuthResponse response = new JwtAuthResponse();
        response.setToken(token);
        response.setUser(shortUser);

        // --- DONO KE LIYE CHECK ---
        // Hum check kar rahe hain ki kya user ka phone number wahi dummy hai jo humne Service mein dala tha
        if ("9123456789".equals(userDTO.getPhoneNumber())) {
            response.setMessage("Welcome! Please update your phone number and password in your profile to ensure you can recover your account in the future.");
        } else {
            response.setMessage("Welcome back! Login successful.");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
