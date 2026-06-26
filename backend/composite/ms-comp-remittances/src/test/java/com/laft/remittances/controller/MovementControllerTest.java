package com.laft.remittances.controller;

import com.laft.commons.grpc.account.*;
import com.laft.commons.grpc.movement.*;
import com.laft.composite.infrastructure.security.JwtUtil;
import com.laft.composite.infrastructure.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import io.grpc.Status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MovementController.class,
        properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration")
@Import(GlobalExceptionHandler.class)
class MovementControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovementController movementController;

    @MockBean
    private JwtUtil jwtUtil;

    private MovementServiceGrpcGrpc.MovementServiceGrpcBlockingStub movementStub;
    private AccountServiceGrpcGrpc.AccountServiceGrpcBlockingStub accountStub;

    @BeforeEach
    void setUp() {
        movementStub = org.mockito.Mockito.mock(MovementServiceGrpcGrpc.MovementServiceGrpcBlockingStub.class);
        accountStub = org.mockito.Mockito.mock(AccountServiceGrpcGrpc.AccountServiceGrpcBlockingStub.class);
        ReflectionTestUtils.setField(movementController, "movementStub", movementStub);
        ReflectionTestUtils.setField(movementController, "accountStub", accountStub);
        ReflectionTestUtils.setField(movementController, "clientStub",
                org.mockito.Mockito.mock(com.laft.commons.grpc.client.ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub.class));
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
    }

    @Test
    void createMovementShouldReturnCreatedMovement() {
        io.jsonwebtoken.Claims userClaims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        when(userClaims.get("role", String.class)).thenReturn("user");
        when(jwtUtil.getClaimsFromToken("user-token")).thenReturn(userClaims);

        when(accountStub.getAccountById(any())).thenReturn(AccountResponse.newBuilder()
                .setAccountNumber("478758")
                .setAccountId("11111111-1111-1111-1111-111111111111")
                .build());
        when(movementStub.createMovement(any())).thenReturn(MovementResponse.newBuilder()
                .setMovementId(1L)
                .setAccountNumber("478758")
                .setMovementType("DEPOSITO")
                .setValue(10.0)
                .setBalance(2010.0)
                .setDate("2026-06-26")
                .build());

        webTestClient.post()
                .uri("/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer user-token")
                .bodyValue("""
                        {"accountId":"11111111-1111-1111-1111-111111111111","movementType":"DEPOSITO","value":10.0}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo("OK-000")
                .jsonPath("$.type").isEqualTo("success")
                .jsonPath("$.data.movementId").isEqualTo(1)
                .jsonPath("$.data.movementType").isEqualTo("DEPOSITO");
    }

    @Test
    void createMovementShouldRejectAdminUsers() {
        io.jsonwebtoken.Claims claims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        when(claims.get("role", String.class)).thenReturn("admin");
        when(jwtUtil.getClaimsFromToken("admin-token")).thenReturn(claims);

        webTestClient.post()
                .uri("/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer admin-token")
                .bodyValue("""
                        {"accountId":"11111111-1111-1111-1111-111111111111","movementType":"DEPOSITO","value":10.0}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ERR-003")
                .jsonPath("$.type").isEqualTo("error");
    }

    @Test
    void createMovementShouldReturnCatalogErrorWhenInsufficientBalance() {
        io.jsonwebtoken.Claims userClaims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        when(userClaims.get("role", String.class)).thenReturn("user");
        when(jwtUtil.getClaimsFromToken("user-token")).thenReturn(userClaims);

        when(accountStub.getAccountById(any())).thenReturn(AccountResponse.newBuilder()
                .setAccountNumber("478758")
                .setAccountId("11111111-1111-1111-1111-111111111111")
                .build());
        when(movementStub.createMovement(any())).thenThrow(
                Status.FAILED_PRECONDITION.withDescription("Saldo no disponible").asRuntimeException());

        webTestClient.post()
                .uri("/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer user-token")
                .bodyValue("""
                        {"accountId":"11111111-1111-1111-1111-111111111111","movementType":"Retiro","value":-99999.0}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ERR-001")
                .jsonPath("$.type").isEqualTo("error")
                .jsonPath("$.message").isEqualTo("Saldo no disponible");
    }
}
