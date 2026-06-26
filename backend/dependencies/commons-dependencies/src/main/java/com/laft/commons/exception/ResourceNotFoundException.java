package com.laft.commons.exception;

import com.laft.commons.response.ResponseCode;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ResponseCode.ERR_002, message);
    }
}
