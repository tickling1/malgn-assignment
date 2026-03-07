package com.malgn.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        return createErrorResponse(errorCode, e.getMessage(), request);
    }

    // [404] 존재하지 않는 API 경로 (NoHandlerFoundException)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException e, HttpServletRequest request) {
        return createErrorResponse(ErrorCode.API_NOT_FOUND, ErrorCode.API_NOT_FOUND.getMessage(), request);
    }

    // [400] @Valid 검증 실패는 스프링 자체 예외라 따로 관리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return createErrorResponse(ErrorCode.INVALID_INPUT_VALUE, message, request);
    }

    // 공통 응답 생성 메서드
    private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode, String message, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, errorCode.getStatus());
    }
}
