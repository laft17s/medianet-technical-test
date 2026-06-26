package com.laft.remittances.controller;

import com.laft.commons.grpc.client.*;
import com.laft.composite.infrastructure.security.JwtUtil;
import com.laft.composite.infrastructure.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ClientController.class,
        properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration")
@Import(GlobalExceptionHandler.class)
class ClientControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ClientController clientController;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private JwtUtil jwtUtil;

    private ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub clientStub;

    @BeforeEach
    void setUp() {
        clientStub = org.mockito.Mockito.mock(ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub.class);
        ReflectionTestUtils.setField(clientController, "clientStub", clientStub);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
    }

    @Test
    void getClientShouldReturnClientData() {
        io.jsonwebtoken.Claims claims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        when(claims.getSubject()).thenReturn("test-user");
        when(jwtUtil.getClaimsFromToken("test-token")).thenReturn(claims);

        when(clientStub.getClient(any())).thenReturn(ClientResponse.newBuilder()
                .setClientId(1L)
                .setName("Jose Lema")
                .setIdentification("1710034065")
                .setGender("Masculino")
                .setAge(30)
                .setAddress("Otavalo")
                .setPhone("098254785")
                .setStatus(true)
                .build());

        webTestClient.get()
                .uri("/clients/1")
                .header("Authorization", "Bearer test-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo("OK-000")
                .jsonPath("$.type").isEqualTo("success")
                .jsonPath("$.data.clientId").isEqualTo(1)
                .jsonPath("$.data.name").isEqualTo("Jose Lema");
    }
}
