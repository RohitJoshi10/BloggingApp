package com.BloggingApp.BloggingApp.repositories;

import com.BloggingApp.BloggingApp.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
