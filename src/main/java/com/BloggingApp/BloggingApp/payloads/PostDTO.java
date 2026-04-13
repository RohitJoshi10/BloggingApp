package com.BloggingApp.BloggingApp.payloads;

import com.BloggingApp.BloggingApp.entities.Category;
import com.BloggingApp.BloggingApp.entities.Comment;
import com.BloggingApp.BloggingApp.entities.PostMedia;
import com.BloggingApp.BloggingApp.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({ "postId", "title", "content", "fileUrl", "fileType", "addedDate", "category", "user" })
public class PostDTO {

    private Integer postId;

    @NotEmpty(message = "Post title cannot be empty !!")
    @Size(min = 4, max = 100, message = "Title must be between 4 to 100 characters")
    private String title;

    @NotEmpty(message = "Post content cannot be empty !!")
    @Size(min = 10, message = "Content must be at least 10 characters long")
    private String content;

//    private String imageName;
//    private String fileUrl; // Isme URL save hoga
//    private String publicId; // Cloudinary ki unique ID delete karne ke liye
//    private String fileType; // Format handle karne ke liye (jpg, png, pdf, mp4)

    private List<PostMediaDTO> mediaFiles = new ArrayList<>();

    private Date addedDate;

    private CategoryDTO category;

    private UserShortDTO user;

    private Set<CommentDTO> comments = new HashSet<>();

    private boolean isLikedByMe; // Kya login wale user ne like kiya hai?
    private int likesCount; // Total kitne likes hain
}
