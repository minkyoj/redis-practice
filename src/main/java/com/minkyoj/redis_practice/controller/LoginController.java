package com.minkyoj.redis_practice.controller;

import com.minkyoj.redis_practice.dto.LoginRequest;
import com.minkyoj.redis_practice.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    public String login(@RequestBody LoginRequest request) {
        return loginService.login(request.getUserId(), request.getPassword());
    }
}
