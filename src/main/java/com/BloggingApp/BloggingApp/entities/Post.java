package com.BloggingApp.BloggingApp.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    @Column(name = "post_title", length = 100, nullable = false)
    private String title;

    @Column(length = 10000)
    private String content;

//    private String imageName;
//    private String fileUrl; // Isme URL save hoga
//    private String publicId; // Cloudinary ki unique ID delete karne ke liye
//    private String fileType; // Format handle karne ke liye (jpg, png, pdf, mp4)

    private Date addedDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "post_likes",
            joinColumns =  @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes = new HashSet<>(); // Unidirectional

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostMedia> mediaFiles = new ArrayList<>();


}


/*

Note: cascade = CascadeType.ALL ka matlab hai ki jab tum Post save karoge, toh uski saari images/videos apne aap save ho jayengi. Aur orphanRemoval = true ka matlab hai agar tum list se koi image hataoge, toh wo DB se bhi delete ho jayegi.
 */