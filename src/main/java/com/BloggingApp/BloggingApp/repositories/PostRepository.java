package com.BloggingApp.BloggingApp.repositories;

import com.BloggingApp.BloggingApp.entities.Category;
import com.BloggingApp.BloggingApp.entities.Post;
import com.BloggingApp.BloggingApp.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {

    // 1. Get Post by ID with Joins (Taaki likes/user/category ek baar mein aayein)
    @EntityGraph(attributePaths = {"user", "category", "likes"})
    Optional<Post> findById(Integer id);

    // 2. All Posts with Pagination (Eager Loading)
    @EntityGraph(attributePaths = {"user", "category", "likes"})
    Page<Post> findAll(Pageable pageable);

    // 3. User specific posts
    @EntityGraph(attributePaths = {"user", "category", "likes"})
    Page<Post> findByUser(User user, Pageable pageable);

    // 4. Category specific posts
    @EntityGraph(attributePaths = {"user", "category", "likes"})
    Page<Post> findByCategory(Category category, Pageable pageable);

    // 5. Professional Search with ILIKE (PostgreSQL) + Join Fetch
    // Note: Query mein JOIN FETCH use karna better hota hai custom queries ke liye
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.title ILIKE %:key% OR p.content ILIKE %:key%")
    Page<Post> searchByKeyword(@Param("key") String keyword, Pageable pageable);
}



//PostgreSQL mein LIKE operator Case-Sensitive hota hai. Matlab agar title mein "Java" likha hai aur user ne "java" (small 'j') search kiya, toh aapka current code results nahi dikhayega.
//
//PostgreSQL ke liye Pro-Fix:
//Aapko repository mein LIKE ki jagah ILIKE (In-sensitive LIKE) use karna chahiye, ya phir dono sides ko lower() mein convert karna chahiye.

// Use this for postgres: @Query("select p from Post p where p.title ILIKE %:key% or p.content ILIKE %:key%")
// Use this for MySQL: @Query("select p from Post p where p.title like %:key% or p.content like %:key%")