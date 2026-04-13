package com.BloggingApp.BloggingApp.controllers;


import com.BloggingApp.BloggingApp.entities.User;
import com.BloggingApp.BloggingApp.exceptions.ApiException;
import com.BloggingApp.BloggingApp.exceptions.ResourceNotFoundException;
import com.BloggingApp.BloggingApp.payloads.*;
import com.BloggingApp.BloggingApp.repositories.UserRepository;
import com.BloggingApp.BloggingApp.services.UserServiceImpl;
import com.BloggingApp.BloggingApp.services.interfaces.UserServiceInterface;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceInterface userService;
    private final UserRepository userRepository;

//    @PostMapping("/")
//    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO){
//        UserDTO createdUser = userService.createUser(userDTO);
//        //return ResponseEntity.ok(createdUser);
//        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
//    }

    // @PreAuthorize("hasRole('ADMIN') or (hasRole('NORMAL') and #userId == authentication.principal.id)")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UpdateUserDTO updateUserDTO, @PathVariable Integer userId){
        UserDTO updatedUser = userService.updateUser(updateUserDTO, userId);
        return ResponseEntity.ok(updatedUser);
    }


    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PatchMapping("/profile/set-password")
    public ResponseEntity<ApiResponse> updateMyPassword(
            @RequestBody Map<String, String> request,
            Principal principal) {

        // Token se email nikal raha hai (Isme kabhi clash nahi hoga)
        String email = principal.getName();
        String newPassword = request.get("password");

        if (newPassword == null || newPassword.length() < 8) {
            throw new ApiException("Password must be at least 8 characters long!");
        }

        userService.updatePasswordByEmail(email, newPassword);

        return ResponseEntity.ok(new ApiResponse("Password updated successfully!", true));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllUser")
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer userId){
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Integer userId){
        userService.deleteUser(userId);
        return new ResponseEntity(new ApiResponse("User Deleted Successfully", true), HttpStatus.OK);
        // return ResponseEntity.noContent().build(); // 204
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}/assign-admin")
    public ResponseEntity<UserDTO> promoteToAdmin(@PathVariable Integer userId){
        UserDTO updatedUser = userService.assignAdminRole(userId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('NORMAL','ADMIN')")
    @PostMapping("/follow/{targetUserId}")
    public ResponseEntity<FollowResponse> toggleFollow(@PathVariable Integer targetUserId){
        FollowResponse response = userService.toggleFollow(targetUserId);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("/{userId}/followers")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResponseEntity<List<UserShortDTO>> getFollowers(@PathVariable Integer userId) {
        return ResponseEntity.ok(this.userService.getFollowersList(userId));
    }

    @GetMapping("/{userId}/following")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResponseEntity<List<UserShortDTO>> getFollowing(@PathVariable Integer userId) {
        return ResponseEntity.ok(this.userService.getFollowingList(userId));
    }
}



/*

1. Updated UserController with ID Ownership Check
Aap apne UserController ko is tarah update kijiye. Yahan hum ye check karenge ki:

Ya toh request karne wala banda ADMIN hai.

Ya phir request karne wale ki Email/Username wahi hai jo target user ki hai

Iske liye ya toh valiateOwnership wala code likho jaise humne post k liye likha hai jo ek best approach hai ya fir

ye laga do: @PreAuthorize("hasRole('ADMIN') or (hasRole('NORMAL') and #userId == authentication.principal.id)")

In do method mai getUserById and UpdateUser
.
 */