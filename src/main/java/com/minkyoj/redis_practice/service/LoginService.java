package com.minkyoj.redis_practice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_FAIL_COUNT = 5;
    private static final Duration FAIL_TTL = Duration.ofMinutes(10);

    public String login(String userId, String password) {
        String key = "login_fail:" + userId;

        // 1️⃣ 차단 상태인지 확인
        String current = redisTemplate.opsForValue().get(key);
        if (current != null && Integer.parseInt(current) >= MAX_FAIL_COUNT) {
            return "로그인 차단 상태입니다. 10분 후 다시 시도하세요.";
        }

        // 2️⃣ 로그인 성공 가정 (비밀번호 검증 생략)
        if ("1234".equals(password)) {
            redisTemplate.delete(key);
            return "로그인 성공!";
        }

        // 3️⃣ 실패 → count 증가
        Long failCount = redisTemplate.opsForValue().increment(key);

        // 처음 증가한 경우 TTL 설정
        if (failCount != null && failCount == 1) {
            redisTemplate.expire(key, FAIL_TTL);
        }

        if (failCount != null && failCount >= MAX_FAIL_COUNT) {
            return "로그인 5회 실패, 10분간 차단됩니다.";
        }

        return "로그인 실패 (" + failCount + "회)";
    }
}
