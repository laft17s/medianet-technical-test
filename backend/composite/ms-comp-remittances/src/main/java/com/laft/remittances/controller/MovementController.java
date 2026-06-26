package com.laft.remittances.controller;

import com.laft.commons.exception.ForbiddenOperationException;
import com.laft.commons.response.ApiResponse;
import com.laft.commons.grpc.movement.*;
import com.laft.commons.grpc.account.*;
import com.laft.commons.grpc.client.*;
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
@RequestMapping(value = "/movements", produces = "application/json")
public class MovementController {

    @GrpcClient("movement-service")
    private MovementServiceGrpcGrpc.MovementServiceGrpcBlockingStub movementStub;

    @GrpcClient("account-service")
    private AccountServiceGrpcGrpc.AccountServiceGrpcBlockingStub accountStub;

    @GrpcClient("client-service")
    private ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub clientStub;

    private final JwtUtil jwtUtil;

    public MovementController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private MovementResponse enrichWithClientName(MovementResponse mov) {
        try {
            AccountResponse acc = accountStub.getAccount(GetAccountRequest.newBuilder().setAccountNumber(mov.getAccountNumber()).build());
            ClientResponse client = clientStub.getClient(GetClientRequest.newBuilder().setClientId(acc.getClientId()).build());
            
            return mov.toBuilder()
                    .setClientName(client.getName())
                    .setAccountType(acc.getAccountType())
                    .build();
        } catch (Exception e) {
            return mov;
        }
    }

    public static class MovementRestResponse {
        public Long movementId;
        public String accountNumber;
        public String maskedAccountNumber;
        public String movementType;
        public double value;
        public double balance;
        public String date;
        public String clientName;
        public String accountType;

        public static MovementRestResponse from(MovementResponse mov) {
            MovementRestResponse dto = new MovementRestResponse();
            dto.movementId = mov.getMovementId();
            dto.accountNumber = mov.getAccountNumber();
            
            String rawAccount = mov.getAccountNumber();
            String maskedAccount = rawAccount;
            if (rawAccount != null && rawAccount.length() >= 3) {
                maskedAccount = rawAccount.charAt(0) + "X".repeat(rawAccount.length() - 2) + rawAccount.charAt(rawAccount.length() - 1);
            }
            dto.maskedAccountNumber = maskedAccount;
            
            dto.movementType = mov.getMovementType();
            dto.value = mov.getValue();
            dto.balance = mov.getBalance();
            dto.date = mov.getDate();
            dto.clientName = mov.getClientName();
            dto.accountType = mov.getAccountType();
            return dto;
        }
    }

    @GetMapping
    public Mono<ApiResponse<List<MovementRestResponse>>> getAllMovements() {
        return Mono.fromCallable(() -> {
            List<MovementResponse> movements = movementStub.getAllMovements(EmptyMovementRequest.newBuilder().build()).getMovementsList();
            return movements.stream().map(this::enrichWithClientName).map(MovementRestResponse::from).collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    public static class MovementCreateDTO {
        public String accountId;
        public String movementType;
        public double value;
    }

    public static class MovementUpdateDTO {
        public String movementType;
        public double value;
    }

    private void assertNotAdmin(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        Claims claims = jwtUtil.getClaimsFromToken(authHeader.substring(7));
        if ("admin".equals(claims.get("role", String.class))) {
            throw new ForbiddenOperationException("Administrators cannot register deposits or withdrawals");
        }
    }

    @PostMapping
    public Mono<ApiResponse<MovementRestResponse>> createMovement(@RequestBody MovementCreateDTO dto, ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            assertNotAdmin(request);
            AccountResponse account = accountStub.getAccountById(GetAccountByIdRequest.newBuilder().setAccountId(dto.accountId).build());

            MovementRequest movementRequest = MovementRequest.newBuilder()
                    .setAccountNumber(account.getAccountNumber() != null ? account.getAccountNumber() : "")
                    .setMovementType(dto.movementType != null ? dto.movementType : "")
                    .setValue(dto.value)
                    .build();
            return MovementRestResponse.from(enrichWithClientName(movementStub.createMovement(movementRequest)));
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @GetMapping("/{movementId}")
    public Mono<ApiResponse<MovementRestResponse>> getMovementById(@PathVariable Long movementId) {
        return Mono.fromCallable(() -> MovementRestResponse.from(
                enrichWithClientName(movementStub.getMovementById(GetMovementByIdRequest.newBuilder().setMovementId(movementId).build()))
        )).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @PutMapping("/{movementId}")
    public Mono<ApiResponse<MovementRestResponse>> updateMovement(@PathVariable Long movementId, @RequestBody MovementUpdateDTO dto) {
        return Mono.fromCallable(() -> {
            UpdateMovementRequest request = UpdateMovementRequest.newBuilder()
                    .setMovementId(movementId)
                    .setMovementType(dto.movementType != null ? dto.movementType : "")
                    .setValue(dto.value)
                    .build();
            return MovementRestResponse.from(enrichWithClientName(movementStub.updateMovement(request)));
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @DeleteMapping("/{movementId}")
    public Mono<ApiResponse<Boolean>> deleteMovement(@PathVariable Long movementId) {
        return Mono.fromCallable(() -> movementStub.deleteMovement(
                DeleteMovementRequest.newBuilder().setMovementId(movementId).build()).getSuccess()
        ).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @GetMapping("/account/{accountNumber}")
    public Mono<ApiResponse<List<MovementRestResponse>>> getMovementsByAccount(@PathVariable String accountNumber) {
        return Mono.fromCallable(() -> {
            List<MovementResponse> movements = movementStub.getMovementsByAccount(GetMovementsByAccountRequest.newBuilder().setAccountNumber(accountNumber).build()).getMovementsList();
            return movements.stream().map(this::enrichWithClientName).map(MovementRestResponse::from).collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @GetMapping("/client/{clientId}")
    public Mono<ApiResponse<List<MovementRestResponse>>> getMovementsByClient(@PathVariable Long clientId) {
        return Mono.fromCallable(() -> {
            List<AccountResponse> accounts = accountStub.getAccountsByClient(GetAccountsByClientRequest.newBuilder().setClientId(clientId).build()).getAccountsList();
            
            List<MovementResponse> allMovements = new java.util.ArrayList<>();
            for (AccountResponse acc : accounts) {
                List<MovementResponse> movs = movementStub.getMovementsByAccount(GetMovementsByAccountRequest.newBuilder().setAccountNumber(acc.getAccountNumber()).build()).getMovementsList();
                allMovements.addAll(movs);
            }
            return allMovements.stream().map(this::enrichWithClientName).map(MovementRestResponse::from).collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }
}
