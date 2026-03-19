package com.minkyoj.redis_practice.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Cacheable(value = "user", key = "#id")
    public String getUser(Long id) {
        try {
            Thread.sleep(3000); // 일부러 지연
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "user-" + id;
    }
}
