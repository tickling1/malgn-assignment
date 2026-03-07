package com.malgn.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. 시큐리티 인증 실패 (로그인 시 아이디/비번 틀림)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e) {
        return getResponse("아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
    }

    // 2. 비즈니스 로직 에러 (수정/삭제 권한 없음 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        return getResponse(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    // 3. 데이터 없음 (잘못된 ID로 조회 등)
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(jakarta.persistence.EntityNotFoundException e) {
        return getResponse("요청하신 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

    // 공통 응답 포맷 생성기
    private ResponseEntity<Map<String, Object>> getResponse(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("code", status.value());
        return new ResponseEntity<>(body, status);
    }
}
