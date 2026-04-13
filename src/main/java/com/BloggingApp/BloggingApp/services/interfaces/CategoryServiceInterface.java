package com.BloggingApp.BloggingApp.services.interfaces;

import com.BloggingApp.BloggingApp.payloads.CategoryDTO;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.List;

public interface CategoryServiceInterface {

    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO updateCategory(CategoryDTO categoryDTO, Integer categoryId);

    void deleteCategory(Integer categoryId);

    CategoryDTO getCategory(Integer categoryId);

    List<CategoryDTO> getCategories();


}
