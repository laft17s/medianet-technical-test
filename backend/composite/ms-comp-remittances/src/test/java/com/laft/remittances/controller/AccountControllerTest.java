package com.laft.remittances.controller;

import com.laft.commons.grpc.account.*;
import com.laft.commons.grpc.client.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AccountController.class,
        properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration")
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AccountController accountController;

    @MockBean
    private JwtUtil jwtUtil;

    private AccountServiceGrpcGrpc.AccountServiceGrpcBlockingStub accountStub;
    private ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub clientStub;
    private MovementServiceGrpcGrpc.MovementServiceGrpcBlockingStub movementStub;

    @BeforeEach
    void setUp() {
        accountStub = org.mockito.Mockito.mock(AccountServiceGrpcGrpc.AccountServiceGrpcBlockingStub.class);
        clientStub = org.mockito.Mockito.mock(ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub.class);
        movementStub = org.mockito.Mockito.mock(MovementServiceGrpcGrpc.MovementServiceGrpcBlockingStub.class);
        ReflectionTestUtils.setField(accountController, "accountStub", accountStub);
        ReflectionTestUtils.setField(accountController, "clientStub", clientStub);
        ReflectionTestUtils.setField(accountController, "movementStub", movementStub);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
    }

    @Test
    void createAccountShouldUseClientIdFromTokenForUserRole() {
        io.jsonwebtoken.Claims claims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        when(claims.get("role", String.class)).thenReturn("user");
        when(claims.get("clientId", Long.class)).thenReturn(4L);
        when(jwtUtil.getClaimsFromToken("user-token")).thenReturn(claims);

        when(accountStub.createAccount(any())).thenReturn(AccountResponse.newBuilder()
                .setAccountNumber("REI0001")
                .setAccountId("aaaa-bbbb-cccc-dddd")
                .setAccountType("Ahorros")
                .setInitialBalance(0.0)
                .setStatus(true)
                .setClientId(4L)
                .build());
        when(clientStub.getClient(any())).thenReturn(ClientResponse.newBuilder()
                .setClientId(4L)
                .setName("Rei Ayanami")
                .build());
        when(movementStub.getMovementsByAccount(any())).thenReturn(MovementListResponse.newBuilder()
                .addMovements(MovementResponse.newBuilder()
                        .setBalance(1000.0)
                        .build())
                .build());
        when(movementStub.createMovement(any())).thenReturn(MovementResponse.newBuilder().setBalance(1000.0).build());

        webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer user-token")
                .bodyValue("""
                        {"accountNumber":"REI0001","accountType":"Ahorros","initialBalance":1000,"status":true}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo("OK-000")
                .jsonPath("$.type").isEqualTo("success")
                .jsonPath("$.data.accountNumber").isEqualTo("RXXXXX1")
                .jsonPath("$.data.clientId").isEqualTo(4)
                .jsonPath("$.data.clientName").isEqualTo("Rei Ayanami")
                .jsonPath("$.data.initialBalance").isEqualTo(1000.0);
    }

    @Test
    void createAccountShouldReturnCatalogErrorWhenClientIdMissing() {
        io.jsonwebtoken.Claims claims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        when(claims.get("role", String.class)).thenReturn("user");
        when(claims.get("clientId", Long.class)).thenReturn(null);
        when(jwtUtil.getClaimsFromToken("user-token")).thenReturn(claims);

        webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer user-token")
                .bodyValue("""
                        {"accountNumber":"REI0002","accountType":"Ahorros","initialBalance":0,"status":true}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ERR-004")
                .jsonPath("$.type").isEqualTo("error")
                .jsonPath("$.message").isEqualTo("Valid clientId is required to create an account");
    }
}
