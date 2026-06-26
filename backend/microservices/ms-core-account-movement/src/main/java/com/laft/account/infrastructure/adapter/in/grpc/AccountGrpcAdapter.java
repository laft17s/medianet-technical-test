package com.laft.account.infrastructure.adapter.in.grpc;

import com.laft.account.application.port.in.AccountUseCase;
import com.laft.account.domain.AccountDomain;
import com.laft.commons.grpc.account.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class AccountGrpcAdapter extends AccountServiceGrpcGrpc.AccountServiceGrpcImplBase {

    private final AccountUseCase accountUseCase;

    public AccountGrpcAdapter(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }

    @Override
    public void createAccount(AccountRequest request, StreamObserver<AccountResponse> responseObserver) {
        if (request.getClientId() <= 0) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Valid clientId is required to create an account")
                    .asRuntimeException());
            return;
        }
        AccountDomain domain = toDomain(request);
        AccountDomain saved = accountUseCase.createAccount(domain);
        responseObserver.onNext(toResponse(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void getAccount(GetAccountRequest request, StreamObserver<AccountResponse> responseObserver) {
        AccountDomain found = accountUseCase.getAccount(request.getAccountNumber());
        responseObserver.onNext(toResponse(found));
        responseObserver.onCompleted();
    }

    @Override
    public void getAccountById(GetAccountByIdRequest request, StreamObserver<AccountResponse> responseObserver) {
        AccountDomain found = accountUseCase.getAccountById(request.getAccountId());
        responseObserver.onNext(toResponse(found));
        responseObserver.onCompleted();
    }

    @Override
    public void updateAccount(AccountRequest request, StreamObserver<AccountResponse> responseObserver) {
        AccountDomain domain = toDomain(request);
        AccountDomain updated = accountUseCase.updateAccount(domain);
        responseObserver.onNext(toResponse(updated));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        accountUseCase.deleteAccount(request.getAccountNumber());
        responseObserver.onNext(DeleteAccountResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAccountsByClient(GetAccountsByClientRequest request, StreamObserver<AccountListResponse> responseObserver) {
        List<AccountDomain> accounts = accountUseCase.getAccountsByClient(request.getClientId());
        AccountListResponse response = AccountListResponse.newBuilder()
                .addAllAccounts(accounts.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllAccounts(EmptyAccountRequest request, StreamObserver<AccountListResponse> responseObserver) {
        List<AccountDomain> accounts = accountUseCase.getAllAccounts();
        AccountListResponse response = AccountListResponse.newBuilder()
                .addAllAccounts(accounts.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private AccountDomain toDomain(AccountRequest request) {
        AccountDomain domain = new AccountDomain();
        domain.setAccountNumber(request.getAccountNumber());
        if (!request.getAccountId().isBlank()) {
            domain.setAccountId(request.getAccountId());
        }
        domain.setAccountType(request.getAccountType());
        domain.setInitialBalance(BigDecimal.valueOf(request.getInitialBalance()));
        domain.setStatus(request.getStatus());
        domain.setClientId(request.getClientId());
        return domain;
    }

    private AccountResponse toResponse(AccountDomain domain) {
        return AccountResponse.newBuilder()
                .setAccountNumber(domain.getAccountNumber() != null ? domain.getAccountNumber() : "")
                .setAccountId(domain.getAccountId() != null ? domain.getAccountId() : "")
                .setAccountType(domain.getAccountType() != null ? domain.getAccountType() : "")
                .setInitialBalance(domain.getInitialBalance() != null ? domain.getInitialBalance().doubleValue() : 0.0)
                .setStatus(domain.getStatus() != null ? domain.getStatus() : false)
                .setClientId(domain.getClientId() != null ? domain.getClientId() : 0L)
                .build();
    }
}
