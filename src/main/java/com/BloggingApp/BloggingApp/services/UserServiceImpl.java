package com.BloggingApp.BloggingApp.services;

import com.BloggingApp.BloggingApp.config.AppConstants;
import com.BloggingApp.BloggingApp.entities.Role;
import com.BloggingApp.BloggingApp.entities.User;
import com.BloggingApp.BloggingApp.exceptions.ApiException;
import com.BloggingApp.BloggingApp.exceptions.ResourceNotFoundException;
import com.BloggingApp.BloggingApp.infrastructure.email.EmailService;
import com.BloggingApp.BloggingApp.infrastructure.sms.SmsService;
import com.BloggingApp.BloggingApp.payloads.*;
import com.BloggingApp.BloggingApp.repositories.RoleRepository;
import com.BloggingApp.BloggingApp.repositories.UserRepository;
import com.BloggingApp.BloggingApp.services.interfaces.UserServiceInterface;
import com.BloggingApp.BloggingApp.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserServiceInterface {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final OtpUtils otpUtils;
    private final SmsService smsService;

//    @Override
//    public UserDTO createUser(UserDTO userDTO) {
//
//        // 1. Convert DTO to Entity
//        User user = modelMapper.map(userDTO, User.class);
//
//        // 2. Save Entity to DB
//        User savedUser = userRepository.save(user);
//
//        // 3. Convert saved Entity back to DTO and return
//        return modelMapper.map(savedUser, UserDTO.class);
//    }

    @Override
    public UserDTO updateUser(UpdateUserDTO updateUserDTO, Integer userId) {
        validateUserOwnership(userId);
        // 1. Take out the user from DB having this id
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User", "Id", userId));

        // 2. Update the old user with new values which are present in userDTO
//        user.setName(userDTO.getName());
//        user.setEmail(userDTO.getEmail());
//        user.setAbout(userDTO.getAbout());
//        user.setPassword(userDTO.getPassword());
        modelMapper.map(updateUserDTO, user);

        // 3. Now save the updated user
        User updatedUser = userRepository.save(user);

       // 4. Return complete UserDTO (with counts)
        UserDTO responseDto = modelMapper.map(updatedUser, UserDTO.class);
        responseDto.setFollowersCount(updatedUser.getFollowers().size());
        responseDto.setFollowingCount(updatedUser.getFollowing().size());

        return responseDto;
    }

    @Override
    public void updatePasswordByEmail(String email, String newPassword) {
        // 1. Token se mili email se user dhundo (No Clash!)
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email: " + email, 0));

        // 2. Jo UUID wala password tha, use naye password se replace aur encode karo
        user.setPassword(this.passwordEncoder.encode(newPassword));

        // 3. Save kar do
        this.userRepository.save(user);
    }

    @Override
    public UserDTO getUserById(Integer userId) {
        validateUserOwnership(userId);
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User", "Id", userId));
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        // --- NEW: List Mapping (Taaki profile fetch pe followers dikhen) ---
//        userDTO.setFollowers(user.getFollowers().stream()
//                .map(u -> modelMapper.map(u, UserShortDTO.class)).collect(Collectors.toSet()));
//        userDTO.setFollowing(user.getFollowing().stream()
//                .map(u -> modelMapper.map(u, UserShortDTO.class)).collect(Collectors.toSet()));

        // YE ZARURI HAI: Taaki profile fetch karte waqt counts dikhen
        userDTO.setFollowersCount(user.getFollowers().size());
        userDTO.setFollowingCount(user.getFollowing().size());

        // Check if the current logged-in user follows this profile
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Boolean isFollowed = user.getFollowers().stream()
                .anyMatch(f -> f.getEmail().equals(loggedInUserEmail));
        userDTO.setIsFollowedByMe(isFollowed);
        return userDTO;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        // 1. Pehle logged-in user ki email nikaal lo (ek hi baar loop se pehle)
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    // Normal mapping
                    UserDTO userDTO = modelMapper.map(user, UserDTO.class);

                    // Counts set karo
                    userDTO.setFollowersCount(user.getFollowers().size());
                    userDTO.setFollowingCount(user.getFollowing().size());

                    // 🔥 YE MISSING THA: Check if 'Me' follows 'This User'
                    // Hum check kar rahe hain ki kya current user ki email target user ke followers ki list mein hai
                    Boolean isFollowed = user.getFollowers().stream()
                            .anyMatch(f -> f.getEmail().equals(loggedInUserEmail));

                    userDTO.setIsFollowedByMe(isFollowed);

                    return userDTO;
                })
                .toList();
    }

    @Override
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User", "Id", userId));
        userRepository.delete(user);
    }

    @Override
    public UserDTO registerNewUser(UserDTO userDTO) {
        // 1. Convert UserDto into Entity
        User user = modelMapper.map(userDTO, User.class);

        // 2. Encode the Password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // SET PROVIDER AS LOCAL (Add this line)
        user.setProvider("LOCAL");

        // 3. To a new user give a default role i.e. NORMAL
        Role role = roleRepository.findById(AppConstants.NORMAL_USER).get();
        user.getRoles().add(role);

        // 4. Save the user
        User newUser = userRepository.save(user);

        // SMTP Welcome email
        String userEmail = newUser.getEmail();
        String userName = newUser.getName();
        emailService.sendWelcomeEmail(userEmail, userName);

        return modelMapper.map(newUser, UserDTO.class);
    }

    @Override
    public UserDTO assignAdminRole(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));
        Role adminRole = roleRepository.findById(AppConstants.ADMIN_USER).orElseThrow(()-> new ApiException("Admin Role not found in Database"));
        user.getRoles().add(adminRole);
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public FollowResponse toggleFollow(Integer targetUserId) {
        // 1. Security Check: Get the email of the currently logged-in user from SecurityContext
        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Fetch Both Users: Current user (from email) and Target user (from ID)
        User currentUser = userRepository.findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new ApiException("Please Login to perform this action!"));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", targetUserId));

        // 3. Self-Follow Guard: Prevent users from following their own profile
        if (targetUser.getId().equals(currentUser.getId())) {
            throw new ApiException("You cannot follow yourself! 🤔");
        }

        // 4. INDUSTRY LOGIC: Bi-directional Relationship Management
        // We check if the current user is already following the target user to toggle the state
        boolean isFollowing;
        if (currentUser.getFollowing().contains(targetUser)) {
            currentUser.getFollowing().remove(targetUser);
            targetUser.getFollowers().remove(currentUser);
            isFollowing = false;
        } else {
            currentUser.getFollowing().add(targetUser);
            targetUser.getFollowers().add(currentUser);
            isFollowing = true;
        }

        userRepository.save(currentUser);
        User savedTargetUser = userRepository.save(targetUser);

        // REAL WORLD: Sirf counts aur status bhejo
        return new FollowResponse(
                savedTargetUser.getId(),
                savedTargetUser.getFollowers().size(),
                savedTargetUser.getFollowing().size(),
                isFollowing,
                isFollowing ? "Followed Successfully" : "Unfollowed Successfully"
        );
    }

    @Override
    public void sendOtpForForgotPassword(String phoneNumber) {
        // 1. Find user by using PhoneNumber
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() ->  new ResourceNotFoundException("USer", "Phone Number", phoneNumber));

        // 2. Generate the OTP
        String otp = otpUtils.generateOtp();

        // 3. Save the OTP and Expiry Time(5 mins)
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // 4. Send the SMS
        smsService.sendOtpSms(phoneNumber, otp);

    }

    @Override
    public void verifyOtpAndResetPassword(ResetPasswordRequest request) {
        // 1. Find the User.
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElseThrow(() -> new ResourceNotFoundException("User", "Phone Number", request.getPhoneNumber()));

        // 2. Check if OTP is not NULL and is matching with the one which is generated.
        if(user.getOtp() == null || !user.getOtp().equals(request.getOtp())){
            throw new ApiException("Invalid OTP ❌");
        }

        // 3. Check the Expiry.
        if(user.getOtpExpiry().isBefore(LocalDateTime.now())){
            throw new ApiException("OTP has expired! Please request a new one. ⌛");
        }

        // 4. Now update the password.
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 5. Clear the OTP so that same OTP can be use further.
        user.setOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
    }

    @Override
    public UserDTO getOrCreateSocialUser(String email, String name, String provider) {

        //1. Check if the use is present in the Database or not
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if(optionalUser.isPresent())
        {
            // If present use that user only
            user = optionalUser.get();
            // Option: Yahan provider update kar sakte ho agar pehle LOCAL tha ab GOOGLE hai
            // Agar pehle local tha aur ab Google se aaya hai, toh provider update kar do
            if(user.getProvider() == null || !user.getProvider().equals(provider)) {
                user.setProvider(provider);
                user = userRepository.save(user);
            }
        }
        else
        {
            // Not Present so Crate a new User
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setAbout("Hey! I am a Blogger🧑‍💻");
            user.setProvider(provider); // eg: GOOGLE
            user.setPhoneNumber("9123456789");
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setEnabled(true); // Google verified hai toh true rakho

            // Logic to handle Role properly
            Role role = this.roleRepository.findById(AppConstants.NORMAL_USER)
                    .orElseGet(() -> {
                        // Agar DB mein role nahi hai, toh naya banao (First time setup handle karne ke liye)
                        Role newRole = new Role();
                        newRole.setId(AppConstants.NORMAL_USER);
                        newRole.setName("ROLE_NORMAL");
                        return this.roleRepository.save(newRole);
                    });

            user.getRoles().add(role);
            user = userRepository.save(user);

        }
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public List<UserShortDTO> getFollowersList(Integer userId) {
        // 1. Pehle check karo ki kya Admin hai ya khud ki profile hai
        validateUserOwnership(userId);

        // 2. User fetch karo (Jiske followers dekhne hain)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return user.getFollowers().stream()
                .map(follower -> modelMapper.map(follower, UserShortDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserShortDTO> getFollowingList(Integer userId) {
        validateUserOwnership(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return user.getFollowing().stream()
                .map(followed -> modelMapper.map(followed, UserShortDTO.class))
                .collect(Collectors.toList());
    }

    private void validateUserOwnership(Integer userId){
        Object principal  = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = ((UserDetails) principal).getUsername();

        // Check if admin
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        // Fetch the target user to get their email
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if(!user.getEmail().equals(currentUsername) && !isAdmin){
            throw  new ApiException("Access Denied! You can only access your own profile.");
        }
    }
}

//Agar aapka userDTO aur User entity ke fields bilkul same hain, toh aap manually set karne ki jagah ModelMapper ko bhi bol sakte ho ki wo values copy kar de:
//        modelMapper.map(userDTO, user);
//(Isse userDTO ki values existing user object mein chali jayengi).