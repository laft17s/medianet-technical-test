package com.laft.commons.exception;

import com.laft.commons.response.ResponseCode;

public class BusinessException extends RuntimeException {

    private final ResponseCode responseCode;

    public BusinessException(String message) {
        super(message);
        this.responseCode = ResponseCode.ERR_005;
    }

    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    /** @deprecated Use {@link #getResponseCode()} instead. */
    @Deprecated
    public String getCode() {
        return responseCode.getCode();
    }
}
