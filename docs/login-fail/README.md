# 로그인 실패 차단 로직 (Redis + TTL)

## 1. 개요

사용자가 일정 횟수 이상 로그인 실패 시, Redis에 실패 횟수를 저장하고 TTL(만료 시간)과 함께 계정을 일정 시간 동안 차단하는 기능 구현.

실패 횟수 저장: Redis String

자동 만료: TTL 사용

특정 횟수 이상 실패 시 로그인 차단

## 2. Redis Key 전략

login_fail:{userId}

예시
```bash
login_fail:min
login_fail:user123
```

Value: 로그인 실패 횟수
TTL: 마지막 실패 후 10분

## 3. 디렉토리 구조
```text
src/main/java/com/example/redispractice
├── config
│   └── RedisConfig.java
├── controller
│   └── LoginController.java
└── service
    └── LoginService.java
```

## 4. 코드

### 4.1 LoginService.java

```java
@Service
@RequiredArgsConstructor
public class LoginService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_FAIL = 5;
    private static final int BLOCK_MINUTE = 10;

    public String login(String userId, String password) {

        String redisKey = "login_fail:" + userId;

        // 차단 여부 확인
        String failCount = redisTemplate.opsForValue().get(redisKey);
        if (failCount != null && Integer.parseInt(failCount) >= MAX_FAIL) {
            return "해당 계정은 현재 로그인 차단 상태입니다.";
        }

        // 비밀번호 검증 (샘플: 정답은 1234)
        if (!"1234".equals(password)) {

            Long count = redisTemplate.opsForValue().increment(redisKey);

            // 첫 실패 시 TTL 설정
            if (count == 1) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(BLOCK_MINUTE));
            }

            if (count >= MAX_FAIL) {
                return "로그인 5회 실패. 10분간 차단됩니다.";
            }

            return "로그인 실패 (" + count + "회)";
        }

        // 성공 시 실패 기록 삭제
        redisTemplate.delete(redisKey);
        return "로그인 성공";
    }
}
```

### 4.2 LoginController.java
```java
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    public String login(@RequestParam String userId, @RequestParam String password) {
        return loginService.login(userId, password);
    }
}
```

## 5. 테스트 방법

### 5.1 요청

GET /login?userId=min&password=1111
→ 실패 횟수 증가

GET /login?userId=min&password=1234
→ 성공, 실패 기록 삭제

## 6. Redis에서 확인하기
키 조회
```bash
127.0.0.1:6379> keys *
1) "login_fail:min"
```

실패 횟수
```bash
127.0.0.1:6379> get login_fail:min
"3"
```

남은 TTL
```bash
127.0.0.1:6379> ttl login_fail:min
(integer) 558
```
