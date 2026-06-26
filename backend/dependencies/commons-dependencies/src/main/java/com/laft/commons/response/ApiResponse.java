package com.laft.commons.response;

public class ApiResponse<T> {

    private String code;
    private String type;
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String code, String type, String message, T data) {
        this.code = code;
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, ResponseCode.OK_000.getDefaultMessage());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(
                ResponseCode.OK_000.getCode(),
                ResponseType.SUCCESS.getValue(),
                message,
                data
        );
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        return error(responseCode, responseCode.getDefaultMessage());
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
        return new ApiResponse<>(
                responseCode.getCode(),
                responseCode.getType().getValue(),
                message,
                null
        );
    }

    public static <T> ApiResponse<T> warning(ResponseCode responseCode, String message, T data) {
        return new ApiResponse<>(
                responseCode.getCode(),
                ResponseType.WARNING.getValue(),
                message,
                data
        );
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
