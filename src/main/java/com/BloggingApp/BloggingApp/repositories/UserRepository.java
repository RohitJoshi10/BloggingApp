package com.BloggingApp.BloggingApp.repositories;

import com.BloggingApp.BloggingApp.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"followers", "following", "roles"})
    Optional<User> findById(Integer id);

    @EntityGraph(attributePaths = {"followers", "following", "roles"})
    List<User> findAll();

    Optional<User> findByPhoneNumber(String phoneNumber);
}

/*

N+1 Query Problem:

Zara in logs ko dhyan se dekh, jab tune GET /api/users/2 hit kiya tha, toh Hibernate ne kya-kya kiya:

Pehli Query: User ko fetch kiya.

Dusri Query (Extra): Sachin (User 3) ke saare followers ko fetch karne ke liye alag query maari.

Teesri Query (Extra): Sachin ki following list nikaalne ke liye fir se ek aur query maari.

Abhi sirf 1-1 entries hain toh 3 queries dikh rahi hain, par agar Sachin ke 1,000 followers hote, toh tera console queries se bhar jata. Isse Database Load badhta hai aur app slow ho jati hai.

Solution: The @EntityGraph Magic
Hum Hibernate ko bolenge: "Bhai, jab tu User ko uthaye, toh uske Followers aur Following ko alag-alag queries mein mat mangwa, balki ek hi baar 'JOIN' maar kar le aa."

attributePaths: Isme humne wo fields likhi hain jo "Lazy" load hoti hain. Ab ye sab ek hi SQL Query mein aa jayengi.


 */
