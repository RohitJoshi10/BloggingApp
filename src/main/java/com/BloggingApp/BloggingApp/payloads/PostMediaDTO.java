package com.BloggingApp.BloggingApp.payloads;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostMediaDTO {
    private Integer mediaId;
    private String fileUrl;
    private String fileType;
    // publicID we don't generally give to frontend.
}
