package com.BloggingApp.BloggingApp.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserDTO {
    @NotEmpty
    @Size(min=2, message = "Username must be at least of 2 character 👤")
    private String name;

    @NotEmpty
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits 📱")
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    private String phoneNumber;

    @NotEmpty(message = "Message should not be empty or blank 💬")
    private String about;
}
