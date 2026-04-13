package com.BloggingApp.BloggingApp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_media")
@Getter
@Setter
@NoArgsConstructor
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mediaId;

    private String fileUrl;
    private String publicId;
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post; // FK jo post table se link hogi
}
