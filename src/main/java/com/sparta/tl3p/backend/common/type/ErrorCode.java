package com.sparta.tl3p.backend.common.type;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Member
    MEMBER_NOT_FOUND("M-001", "Member Not Found"),
    EMAIL_NOT_FOUND("M-002", "Member Email Not Found"),
    ROLE_NOT_FOUND("M-003", "Member Role Not Found"),
    USERNAME_DUPLICATE("M-004", "Username already exists"),

    // Store
    STORE_NOT_FOUND("S-001", "Store Not Found"),
    // Review
    REVIEW_NOT_FOUND("R-001", "Review Not Found"),
    // Item
    ITEM_NOT_FOUND("I-001", "Item Not Found"),
    // Order
    ORDER_NOT_FOUND("O-001", "Order Not Found"),
    // Payment

    // Valid
    ARGUMENT_NOT_VALID("V-001", "Method Argument Not Valid"),

    // Unexpected Exception
    UNEXPECTED_ERROR("D-001", "Unexpected error ");


    private final String code;
    private final String message;

    ErrorCode(final String code, final String message) {
        this.code = code;
        this.message = message;
    }
}
