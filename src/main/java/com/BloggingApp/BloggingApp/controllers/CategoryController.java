package com.BloggingApp.BloggingApp.controllers;

import com.BloggingApp.BloggingApp.payloads.ApiResponse;
import com.BloggingApp.BloggingApp.payloads.CategoryDTO;
import com.BloggingApp.BloggingApp.services.interfaces.CategoryServiceInterface;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryServiceInterface categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO, @PathVariable Integer categoryId){
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryDTO, categoryId);
        return ResponseEntity.ok(updatedCategory);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Integer categoryId){
        categoryService.deleteCategory(categoryId);
        return new ResponseEntity(new ApiResponse("Category Deleted Successfully", true), HttpStatus.OK);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Integer categoryId){
        return new ResponseEntity<>(categoryService.getCategory(categoryId), HttpStatus.OK);
    }

    @GetMapping("/getCategories")
    public ResponseEntity<List<CategoryDTO>> getCategories(){
        return ResponseEntity.ok(categoryService.getCategories());
    }

}
