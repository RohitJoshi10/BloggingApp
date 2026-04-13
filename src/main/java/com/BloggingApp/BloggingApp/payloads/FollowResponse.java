package com.BloggingApp.BloggingApp.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponse {
    private Integer targetUserId;
    private int followersCount;
    private int followingCount;
    private boolean followedByMe; // Frontend isi se button ka color/text change karega
    private String message;
}
