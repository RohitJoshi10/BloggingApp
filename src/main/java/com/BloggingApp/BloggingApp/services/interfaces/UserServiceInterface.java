package com.BloggingApp.BloggingApp.services.interfaces;

import com.BloggingApp.BloggingApp.payloads.*;

import java.util.List;

public interface UserServiceInterface {

//    UserDTO createUser(UserDTO user);
    UserDTO updateUser(UpdateUserDTO user, Integer userId);
    void updatePasswordByEmail(String email, String newPassword);
    UserDTO getUserById(Integer userId);
    List<UserDTO> getAllUsers();
    void deleteUser(Integer userId);
    UserDTO registerNewUser(UserDTO user);
    UserDTO assignAdminRole(Integer userId);
    FollowResponse toggleFollow(Integer targetUserId);
    void sendOtpForForgotPassword(String phoneNumber);
    void verifyOtpAndResetPassword(ResetPasswordRequest request);
    UserDTO getOrCreateSocialUser(String email, String name, String provider);
    List<UserShortDTO> getFollowersList(Integer userId);
    List<UserShortDTO> getFollowingList(Integer userId);
}
