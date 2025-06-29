package com.example.backend.user.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserFilter implements Filter {

    // ✅ 이메일별 로그인 시도 기록 Map
    private final Map<String, LoginAttempt> loginFailures = new ConcurrentHashMap<>();
    // ✅ 차단 기준
    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = 5 * 60 * 1000;

    // ✅ 로그인 시도 정보
    private static class LoginAttempt {
        int count;
        long lastAttemptTime;

        LoginAttempt(int count, long lastAttemptTime) {
            this.count = count;
            this.lastAttemptTime = lastAttemptTime;
        }
    }

    // ✅ login POST 요청 감시
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        if ("/login".equals(req.getRequestURI()) && "POST".equalsIgnoreCase(req.getMethod())) {
            String email = req.getParameter("email");
            if (email != null && isBlocked(email)) {
                throw new IllegalArgumentException("로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.");
            }
        }

        chain.doFilter(request, response);
    }

    // ✅ 로그인 실패 기록
    public void recordLoginFailure(String email) {
        LoginAttempt attempt = loginFailures.getOrDefault(email, new LoginAttempt(0, System.currentTimeMillis()));
        attempt.count++;
        attempt.lastAttemptTime = System.currentTimeMillis();
        loginFailures.put(email, attempt);
    }

    // ✅ 로그인 실패 기록 초기화
    public void resetLoginFailures(String email) {
        loginFailures.remove(email);
    }

    // ✅ 차단 여부 확인
    private boolean isBlocked(String email) {
        LoginAttempt attempt = loginFailures.get(email);
        if (attempt == null) return false;

        long now = System.currentTimeMillis();

        if (now - attempt.lastAttemptTime > BLOCK_DURATION_MS) {
            loginFailures.remove(email);
            return false;
        }

        return attempt.count >= MAX_ATTEMPTS;
    }
}