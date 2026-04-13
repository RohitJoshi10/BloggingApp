package com.BloggingApp.BloggingApp.payloads;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    private Integer id;

    @NotEmpty
    @Size(min=2, message = "Username must be at least of 2 character 👤")
    private String name;

    @Email(message = "Email address is not valid 📧")
    private String email;

    @NotEmpty
    @Size(min = 8, max = 16,  message = "- " +
            "At least one lowercase letter, " +
            "At least one uppercase letter, " +
            "At least one digit, " +
            "At least one special character, " +
            "Password must be minimum of 8 characters and maximum of 16 characters 🔑")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotEmpty(message = "Message should not be empty or blank 💬")
    private String about;

    @NotEmpty
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits 📱")
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    private String phoneNumber;

    private Integer followersCount;

    private Integer followingCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isFollowedByMe;

//    private Set<UserShortDTO> followers = new HashSet<>();
//    private Set<UserShortDTO> following = new HashSet<>();
}


// NotEmpty: NotBlank, NotNull