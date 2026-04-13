package com.BloggingApp.BloggingApp.infrastructure.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Token ko blacklist mai daalne k liye
    public void saveTokenToBlacklist(String token, long expirationInSeconds){
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofSeconds(expirationInSeconds));
    }

    // Check krne k liye ki token blacklisted hai ya nhi h
    public boolean isTokenBlacklisted(String token){
        return  Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}
