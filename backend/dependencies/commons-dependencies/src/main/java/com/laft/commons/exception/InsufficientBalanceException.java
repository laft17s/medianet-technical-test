package com.laft.commons.exception;

import com.laft.commons.response.ResponseCode;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(ResponseCode.ERR_001, message);
    }
}
