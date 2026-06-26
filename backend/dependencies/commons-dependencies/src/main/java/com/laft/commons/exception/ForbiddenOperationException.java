package com.laft.commons.exception;

import com.laft.commons.response.ResponseCode;

public class ForbiddenOperationException extends BusinessException {

    public ForbiddenOperationException(String message) {
        super(ResponseCode.ERR_003, message);
    }
}
