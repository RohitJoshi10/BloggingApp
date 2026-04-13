package com.BloggingApp.BloggingApp.payloads;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private int commentId;

    @NotEmpty(message = "Content should not be empty or blank 💬")
    private String content;

    private UserShortDTO user; // Yahan UserDTO ki jagah UserShortDTO aayega
}
