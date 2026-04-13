package com.BloggingApp.BloggingApp.repositories;

import com.BloggingApp.BloggingApp.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
