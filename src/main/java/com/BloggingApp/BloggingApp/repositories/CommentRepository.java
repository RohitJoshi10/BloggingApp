package com.BloggingApp.BloggingApp.repositories;

import com.BloggingApp.BloggingApp.entities.Comment;
import com.BloggingApp.BloggingApp.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // @EntityGraph lagane se Comment ke saath uska Post aur User ek hi JOIN query mein aa jayenge
    @EntityGraph(attributePaths = {"user", "post"})
    List<Comment> findByUser(User user);
}
