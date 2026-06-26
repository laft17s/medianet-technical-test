package com.laft.composite.infrastructure.adapter.in.web;

import com.laft.commons.grpc.account.AccountServiceGrpcGrpc;
import com.laft.commons.grpc.client.AuthRequest;
import com.laft.commons.grpc.client.ClientRequest;
import com.laft.commons.grpc.client.ClientResponse;
import com.laft.commons.grpc.client.ClientServiceGrpcGrpc;
import com.laft.commons.grpc.movement.MovementServiceGrpcGrpc;
import com.laft.commons.response.ApiResponse;
import com.laft.commons.response.ResponseCode;
import com.laft.composite.infrastructure.security.JwtUtil;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GrpcClient("client-service")
    private ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub clientServiceBlockingStub;

    @GrpcClient("account-service")
    private AccountServiceGrpcGrpc.AccountServiceGrpcBlockingStub accountServiceBlockingStub;

    @GrpcClient("movement-service")
    private MovementServiceGrpcGrpc.MovementServiceGrpcBlockingStub movementServiceBlockingStub;

    private final JwtUtil jwtUtil;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.admin.identification}")
    private String adminIdentification;

    public AuthController(JwtUtil jwtUtil, KafkaTemplate<String, String> kafkaTemplate) {
        this.jwtUtil = jwtUtil;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/login")
    public Mono<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        String identification = credentials.get("identification");
        String password = credentials.get("password");

        try {
            AuthRequest authRequest = AuthRequest.newBuilder()
                    .setIdentification(identification)
                    .setPassword(password)
                    .build();

            ClientResponse clientResponse = clientServiceBlockingStub.validateCredentials(authRequest);

            boolean isAdmin = adminIdentification.equals(clientResponse.getIdentification());
            String role = isAdmin ? "admin" : "user";
            String token = jwtUtil.generateToken(
                    clientResponse.getName(),
                    clientResponse.getIdentification(),
                    clientResponse.getClientId(),
                    role);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("clientId", clientResponse.getClientId());
            data.put("name", clientResponse.getName());
            data.put("identification", clientResponse.getIdentification());
            data.put("role", role);

            return Mono.just(ApiResponse.success(data, "Inicio de sesión exitoso"));

        } catch (StatusRuntimeException e) {
            return Mono.just(ApiResponse.<Map<String, Object>>error(
                    ResponseCode.ERR_006,
                    ResponseCode.ERR_006.getDefaultMessage()));
        } catch (Exception e) {
            return Mono.just(ApiResponse.<Map<String, Object>>error(
                    ResponseCode.ERR_999,
                    ResponseCode.ERR_999.getDefaultMessage()));
        }
    }

    @PostMapping("/register")
    public Mono<ApiResponse<Map<String, Object>>> register(@RequestBody Map<String, String> data) {
        try {
            ClientRequest clientRequest = ClientRequest.newBuilder()
                    .setName(data.get("name"))
                    .setGender(data.get("gender"))
                    .setAge(Integer.parseInt(data.get("age")))
                    .setIdentification(data.get("identification"))
                    .setAddress(data.get("address"))
                    .setPhone(data.get("phone"))
                    .setPassword(data.get("password"))
                    .setStatus(true)
                    .build();

            ClientResponse clientResponse = clientServiceBlockingStub.createClient(clientRequest);
            Long clientId = clientResponse.getClientId();

            String accountType = data.getOrDefault("accountType", "Ahorros");

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("clientId", clientId);
            eventData.put("accountType", accountType);
            eventData.put("initialBalance", 5.0);

            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String payload = mapper.writeValueAsString(eventData);
                kafkaTemplate.send("client-registered-topic", clientId.toString(), payload);
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean isAdmin = adminIdentification.equals(clientResponse.getIdentification());
            String token = jwtUtil.generateToken(
                    clientResponse.getName(),
                    clientResponse.getIdentification(),
                    clientId,
                    isAdmin ? "admin" : "user");

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("clientId", clientId);
            responseData.put("name", clientResponse.getName());
            responseData.put("identification", clientResponse.getIdentification());
            responseData.put("role", isAdmin ? "admin" : "user");

            return Mono.just(ApiResponse.success(responseData, "Registro exitoso"));

        } catch (Exception e) {
            e.printStackTrace();
            return Mono.just(ApiResponse.<Map<String, Object>>error(
                    ResponseCode.ERR_999,
                    "Error interno durante el registro"));
        }
    }
}
