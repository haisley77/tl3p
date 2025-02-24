package com.sparta.tl3p.backend.common.type;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Member
    MEMBER_NOT_FOUND("M-001", "Member Not Found"),
    EMAIL_NOT_FOUND("M-002", "Member Email Not Found"),
    ROLE_NOT_FOUND("M-003", "Member Role Not Found"),
    USERNAME_DUPLICATE("M-004", "Username already exists"),
    PASSWORD_MISMATCH("M-005", "비밀번호가 일치하지 않습니다"),
    INVALID_JWT_TOKEN("M-006", "유효하지 않은 JWT 토큰입니다."),
    INVALID_MEMBER("M-007", "Invalid Member Request"),

    // Store
    STORE_NOT_FOUND("S-001", "Store Not Found"),
    // Review
    REVIEW_NOT_FOUND("R-001", "Review Not Found"),
    REVIEW_ALREADY_DELETED("R-002", "Review Already Deleted"),
    // Item
    ITEM_NOT_FOUND("I-001", "Item Not Found"),
    // Order
    ORDER_NOT_FOUND("O-001", "Order Not Found"),
    ORDER_TIME_OUT("O-002", "Order Time Out"),
    // Payment
    PAYMENT_NOT_FOUND("P-001", "Payment Not Found"),
    PAYMENT_FAILED("P-002", "Payment Failed"),

    // Valid
    ARGUMENT_NOT_VALID("V-001", "Method Argument Not Valid"),
    PARAM_NOT_VALID("V-002", "Param Not Valid"),

    // API
    API_UNEXPECTED_ERROR("A-001","API Error"),
    API_CALL_ERROR("A-002", "API Call Error"),
    REST_TEMPLATE_ERROR("A-003", "REST Template Error"),
    API_STATUS_ERROR("A-004", "API Status Error"),
    API_RESPONSE_PARSE_ERROR("A-005", "API Response Parse Error"),

    // Unexpected Exception
    UNEXPECTED_ERROR("D-001", "Unexpected error "),
    // Unauthorized
    ACCESS_DENIED("D-002", "Access Denied"),;


    private final String code;
    private final String message;

    ErrorCode(final String code, final String message) {
        this.code = code;
        this.message = message;
    }
}
