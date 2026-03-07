package com.malgn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 공통 (Common - C)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "해당 리소스를 찾을 수 없습니다."),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "존재하지 않는 API 경로입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C004", "접근 권한이 없습니다."),

    // 회원 (Member - M)
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "M001", "이미 사용 중인 아이디입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "M002", "아이디 또는 비밀번호가 일치하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M003", "이미 사용 중인 이메일입니다."),
    INVALID_ADMIN_TOKEN(HttpStatus.FORBIDDEN, "M004", "관리자 인증 토큰이 유효하지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M005", "존재하지 않는 회원입니다."),

    // 게시글/댓글 (Board/Content - B)
    NOT_AUTHOR(HttpStatus.FORBIDDEN, "B001", "작성자만 수정/삭제할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
