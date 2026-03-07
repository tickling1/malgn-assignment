package com.malgn.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "전역 에러 응답 규격")
public record ErrorResponse(
        @Schema(description = "에러 발생 시각", example = "2026-03-07T23:00:00")
        LocalDateTime timestamp,
        @Schema(description = "HTTP 상태 코드", example = "403")
        int status,      // 404, 403 등
        @Schema(description = "비즈니스 에러 코드", example = "INVALID_ADMIN_TOKEN")
        String code,     // "C004", "M001"
        @Schema(description = "에러 상세 메시지", example = "관리자 인증 토큰이 유효하지 않습니다.")
        String message,
        @Schema(description = "요청 경로", example = "/api/members/join")
        String path
) {}