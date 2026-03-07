package com.malgn.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,      // 404, 403 등
        String code,     // "C004", "M001"
        String message,
        String path
) {}