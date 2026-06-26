package com.laft.remittances.controller;

import com.laft.commons.exception.BusinessException;
import com.laft.commons.response.ApiResponse;
import com.laft.commons.response.ResponseCode;
import com.laft.commons.grpc.account.*;
import com.laft.commons.grpc.client.*;
import com.laft.commons.grpc.movement.*;
import com.laft.composite.infrastructure.security.JwtUtil;
import io.jsonwebtoken.Claims;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/accounts", produces = "application/json")
public class AccountController {

    @GrpcClient("account-service")
    private AccountServiceGrpcGrpc.AccountServiceGrpcBlockingStub accountStub;

    @GrpcClient("client-service")
    private ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub clientStub;

    @GrpcClient("movement-service")
    private MovementServiceGrpcGrpc.MovementServiceGrpcBlockingStub movementStub;

    private final JwtUtil jwtUtil;

    public AccountController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public static class AccountRestResponse {
        public String accountId;
        public String accountNumber;
        public String accountType;
        public double initialBalance;
        public boolean status;
        public long clientId;
        public String clientName;

        public static AccountRestResponse from(AccountResponse acc) {
            AccountRestResponse dto = new AccountRestResponse();
            dto.accountId = acc.getAccountId();
            dto.accountNumber = acc.getAccountNumber();
            dto.accountType = acc.getAccountType();
            dto.initialBalance = acc.getInitialBalance();
            dto.status = acc.getStatus();
            dto.clientId = acc.getClientId();
            dto.clientName = acc.getClientName();
            return dto;
        }
    }

    private AccountResponse enrichWithClientName(AccountResponse acc) {
        try {
            ClientResponse client = clientStub.getClient(GetClientRequest.newBuilder().setClientId(acc.getClientId()).build());
            
            return acc.toBuilder()
                    .setClientName(client.getName())
                    .build();
        } catch (Exception e) {
            return acc;
        }
    }

    private AccountResponse maskAccount(AccountResponse acc) {
        String raw = acc.getAccountNumber();
        if (raw != null && raw.length() >= 3) {
            String masked = raw.charAt(0) + "X".repeat(raw.length() - 2) + raw.charAt(raw.length() - 1);
            return acc.toBuilder().setAccountNumber(masked).build();
        }
        return acc;
    }

    private AccountResponse enrichWithBalance(AccountResponse acc) {
        try {
            MovementListResponse movementsResponse = movementStub.getMovementsByAccount(
                GetMovementsByAccountRequest.newBuilder().setAccountNumber(acc.getAccountNumber()).build()
            );
            if (movementsResponse.getMovementsCount() > 0) {
                // The first movement is the latest because of OrderByDateDesc
                double currentBalance = movementsResponse.getMovements(0).getBalance();
                return acc.toBuilder().setInitialBalance(currentBalance).build();
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch balance for account: " + acc.getAccountNumber() + " - " + e.getMessage());
        }
        return acc;
    }

    private long resolveClientId(AccountCreateDTO dto, ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        Long tokenClientId = null;
        String role = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Claims claims = jwtUtil.getClaimsFromToken(authHeader.substring(7));
            tokenClientId = claims.get("clientId", Long.class);
            role = claims.get("role", String.class);
        }

        if (!"admin".equals(role) && tokenClientId != null && tokenClientId > 0) {
            return tokenClientId;
        }

        if (dto.clientId != null && dto.clientId > 0) {
            return dto.clientId;
        }

        if (tokenClientId != null && tokenClientId > 0) {
            return tokenClientId;
        }

        throw new BusinessException(ResponseCode.ERR_004, "Valid clientId is required to create an account");
    }

    @GetMapping
    public Mono<ApiResponse<List<AccountRestResponse>>> getAllAccounts() {
        return Mono.fromCallable(() -> {
            List<AccountResponse> accounts = accountStub.getAllAccounts(EmptyAccountRequest.newBuilder().build()).getAccountsList();
            return accounts.stream()
                    .map(this::enrichWithClientName)
                    .map(this::enrichWithBalance)
                    .map(this::maskAccount)
                    .map(AccountRestResponse::from)
                    .collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    public static class AccountCreateDTO {
        public String accountNumber;
        public String accountType;
        public double initialBalance;
        public boolean status;
        public Long clientId;
    }

    @PostMapping
    public Mono<ApiResponse<AccountRestResponse>> createAccount(@RequestBody AccountCreateDTO dto, ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            long clientId = resolveClientId(dto, request);
            AccountRequest accountRequest = AccountRequest.newBuilder()
                    .setAccountNumber(dto.accountNumber != null ? dto.accountNumber : "")
                    .setAccountType(dto.accountType != null ? dto.accountType : "")
                    .setInitialBalance(0.0)
                    .setStatus(dto.status)
                    .setClientId(clientId)
                    .build();
            AccountResponse createdAccount = accountStub.createAccount(accountRequest);
            if (dto.initialBalance > 0) {
                try {
                    MovementRequest movRequest = MovementRequest.newBuilder()
                            .setAccountNumber(createdAccount.getAccountNumber())
                            .setMovementType("DEPOSITO INICIAL")
                            .setValue(dto.initialBalance)
                            .build();
                    movementStub.createMovement(movRequest);
                } catch (Exception e) {
                    System.err.println("Failed to create initial movement: " + e.getMessage());
                }
            }
            AccountResponse enriched = enrichWithClientName(createdAccount);
            enriched = enrichWithBalance(enriched);
            enriched = maskAccount(enriched);
            return AccountRestResponse.from(enriched);
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @GetMapping("/{accountNumber}")
    public Mono<ApiResponse<AccountRestResponse>> getAccount(@PathVariable String accountNumber) {
        return Mono.fromCallable(() -> {
            AccountResponse account = accountStub.getAccount(GetAccountRequest.newBuilder().setAccountNumber(accountNumber).build());
            return AccountRestResponse.from(maskAccount(enrichWithBalance(enrichWithClientName(account))));
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @GetMapping("/client/{clientId}")
    public Mono<ApiResponse<List<AccountRestResponse>>> getAccountsByClient(@PathVariable Long clientId) {
        return Mono.fromCallable(() -> {
            List<AccountResponse> accounts = accountStub.getAccountsByClient(GetAccountsByClientRequest.newBuilder().setClientId(clientId).build()).getAccountsList();
            return accounts.stream()
                    .map(this::enrichWithClientName)
                    .map(this::enrichWithBalance)
                    .map(this::maskAccount)
                    .map(AccountRestResponse::from)
                    .collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @DeleteMapping("/{accountNumber}")
    public Mono<ApiResponse<DeleteAccountResponse>> deleteAccount(@PathVariable String accountNumber) {
        return Mono.fromCallable(() -> accountStub.deleteAccount(DeleteAccountRequest.newBuilder().setAccountNumber(accountNumber).build()))
                .subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    public static class AccountUpdateDTO {
        public String accountType;
        public boolean status;
        public Long clientId;
    }

    @PutMapping("/{accountNumber}")
    public Mono<ApiResponse<AccountRestResponse>> updateAccount(@PathVariable String accountNumber, @RequestBody AccountUpdateDTO dto) {
        return Mono.fromCallable(() -> {
            AccountResponse existing = accountStub.getAccount(GetAccountRequest.newBuilder().setAccountNumber(accountNumber).build());
            AccountRequest request = AccountRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .setAccountType(dto.accountType != null ? dto.accountType : existing.getAccountType())
                    .setInitialBalance(existing.getInitialBalance())
                    .setStatus(dto.status)
                    .setClientId(dto.clientId != null ? dto.clientId : existing.getClientId())
                    .setAccountId(existing.getAccountId())
                    .build();
            AccountResponse updated = accountStub.updateAccount(request);
            return AccountRestResponse.from(maskAccount(enrichWithBalance(enrichWithClientName(updated))));
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }
}
