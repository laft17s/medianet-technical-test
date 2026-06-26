package com.laft.composite.infrastructure.web;

import com.laft.commons.exception.BusinessException;
import com.laft.commons.exception.InsufficientBalanceException;
import com.laft.commons.exception.ResourceNotFoundException;
import com.laft.commons.response.ApiResponse;
import com.laft.commons.response.ResponseCode;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return buildResponse(ResponseCode.ERR_001, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(ResponseCode.ERR_002, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return buildResponse(ex.getResponseCode(), ex.getMessage());
    }

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleGrpc(StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();
        String message = safeGrpcMessage(ex);

        if (code == Status.Code.FAILED_PRECONDITION || isInsufficientBalance(message)) {
            return buildResponse(ResponseCode.ERR_001, message);
        }
        if (code == Status.Code.PERMISSION_DENIED) {
            return buildResponse(ResponseCode.ERR_003, message);
        }
        if (code == Status.Code.NOT_FOUND) {
            return buildResponse(ResponseCode.ERR_002, message);
        }
        if (code == Status.Code.INVALID_ARGUMENT) {
            return buildResponse(ResponseCode.ERR_004, message);
        }
        if (code == Status.Code.UNAUTHENTICATED) {
            return buildResponse(ResponseCode.ERR_006, message);
        }
        return buildResponse(ResponseCode.ERR_999, ResponseCode.ERR_999.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof StatusRuntimeException statusRuntimeException) {
            return handleGrpc(statusRuntimeException);
        }
        return buildResponse(ResponseCode.ERR_999, ResponseCode.ERR_999.getDefaultMessage());
    }

    private static boolean isInsufficientBalance(String message) {
        return message != null && message.toLowerCase().contains("saldo");
    }

    private static String safeGrpcMessage(StatusRuntimeException ex) {
        String description = ex.getStatus().getDescription();
        if (description != null && !description.isBlank() && !"UNKNOWN".equalsIgnoreCase(description)) {
            return description;
        }
        return "No se pudo procesar la solicitud";
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ResponseCode responseCode, String message) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.error(responseCode, message));
    }
}
