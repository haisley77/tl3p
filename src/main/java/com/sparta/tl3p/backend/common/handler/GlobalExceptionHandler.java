package com.sparta.tl3p.backend.common.handler;

import com.sparta.tl3p.backend.common.dto.ErrorResponseDto;
import com.sparta.tl3p.backend.common.exception.BusinessException;
import com.sparta.tl3p.backend.common.type.ErrorCode;
import com.sparta.tl3p.backend.common.type.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {




    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponseDto> methodArgumentNotValidExceptionHandler(HttpServletRequest request) {
        printException(request, ErrorCode.ARGUMENT_NOT_VALID);
        return new ResponseEntity<>(ErrorResponseDto
                .builder()
                .code(ResponseCode.S)
                .message(ErrorCode.ARGUMENT_NOT_VALID.getMessage())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> businessExceptionHandler(
            HttpServletRequest request, BusinessException businessException) {
        printException(request, businessException.getErrorCode());
        return new ResponseEntity<>(ErrorResponseDto
                .builder()
                .code(ResponseCode.S)
                .message(businessException.getErrorCode().getMessage())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> runtimeExceptionHandler(HttpServletRequest request, RuntimeException runtimeException) {

        printDangerousException(request, ErrorCode.UNEXPECTED_ERROR);
        runtimeException.printStackTrace(); // 예상하지 못한 에러는 서버 쪽에서 stacktrace 확인 필요

        return new ResponseEntity<>(ErrorResponseDto
                .builder()
                .code(ResponseCode.S)
                .message(ErrorCode.UNEXPECTED_ERROR.getMessage())
                .build(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> exceptionHandler(HttpServletRequest request, Exception exception) {

        printDangerousException(request, ErrorCode.UNEXPECTED_ERROR);
        exception.printStackTrace(); // 예상하지 못한 에러는 서버 쪽에서 stacktrace 확인 필요

        return new ResponseEntity<>(ErrorResponseDto
                .builder()
                .code(ResponseCode.S)
                .message(ErrorCode.UNEXPECTED_ERROR.getMessage())
                .build(), INTERNAL_SERVER_ERROR);
    }

    // 예측한 예외 logging
    private void printException(HttpServletRequest request, ErrorCode errorCode) {
        log.info("[" + errorCode.getCode() + "] " + request.getRequestURI());
    }

    // 예측하지 못한 예외 logging
    private void printDangerousException(HttpServletRequest request, ErrorCode errorCode) {
        log.error("[" + errorCode.getCode() + "] " + request.getRequestURI());
    }

}
