package com.BloggingApp.BloggingApp.payloads;

import com.BloggingApp.BloggingApp.entities.Role;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class JwtUserShortDTO {
    private int id;
    private String name;
    private String email;
    private Set<Role> roles = new HashSet<>();
}
