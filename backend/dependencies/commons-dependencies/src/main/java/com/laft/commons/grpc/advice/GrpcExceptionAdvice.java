package com.laft.commons.grpc.advice;

import com.laft.commons.exception.BusinessException;
import com.laft.commons.exception.ForbiddenOperationException;
import com.laft.commons.exception.InsufficientBalanceException;
import com.laft.commons.exception.ResourceNotFoundException;
import com.laft.commons.response.ResponseCode;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(InsufficientBalanceException.class)
    public Status handleInsufficientBalance(InsufficientBalanceException ex) {
        return Status.FAILED_PRECONDITION.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(ForbiddenOperationException.class)
    public Status handleForbidden(ForbiddenOperationException ex) {
        return Status.PERMISSION_DENIED.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(ResourceNotFoundException.class)
    public Status handleNotFound(ResourceNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(BusinessException.class)
    public Status handleBusiness(BusinessException ex) {
        if (ex.getResponseCode() == ResponseCode.ERR_003) {
            return Status.PERMISSION_DENIED.withDescription(ex.getMessage());
        }
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleGeneric(Exception ex) {
        return Status.INTERNAL.withDescription(ResponseCode.ERR_999.getDefaultMessage());
    }
}
