package com.BloggingApp.BloggingApp.payloads;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryDTO {
    private Integer categoryId;

    @NotEmpty(message = "categoryTitle should not be empty or blank 💬")
    private String categoryTitle;

    @NotEmpty(message = "categoryDescription should not be empty or blank 💬")
    private String categoryDescription;
}
