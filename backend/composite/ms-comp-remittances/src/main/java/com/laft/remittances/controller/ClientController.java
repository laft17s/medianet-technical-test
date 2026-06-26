package com.laft.remittances.controller;

import com.laft.commons.grpc.client.*;
import com.laft.commons.response.ApiResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping(value = "/clients", produces = "application/json")
public class ClientController {

    @GrpcClient("client-service")
    private ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub clientStub;

    private final org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    public ClientController(org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public Mono<ApiResponse<java.util.Map<String, Object>>> createClient(@RequestBody java.util.Map<String, Object> payloadData) {
        return Mono.fromCallable(() -> {
            
            String name = (String) payloadData.getOrDefault("name", "");
            String gender = (String) payloadData.getOrDefault("gender", "");
            int age = Integer.parseInt(String.valueOf(payloadData.getOrDefault("age", "0")));
            String identification = (String) payloadData.getOrDefault("identification", "");
            String address = (String) payloadData.getOrDefault("address", "");
            String phone = (String) payloadData.getOrDefault("phone", "");
            String password = (String) payloadData.getOrDefault("password", "");
            boolean status = Boolean.parseBoolean(String.valueOf(payloadData.getOrDefault("status", "true")));
            
            ClientRequest request = ClientRequest.newBuilder()
                    .setName(name)
                    .setGender(gender)
                    .setAge(age)
                    .setIdentification(identification)
                    .setAddress(address)
                    .setPhone(phone)
                    .setPassword(password)
                    .setStatus(status)
                    .build();

            ClientResponse response = clientStub.createClient(request);
            
            // Publish to Kafka for async account creation
            try {
                String accountType = (String) payloadData.getOrDefault("accountType", "Ahorros");
                
                java.util.Map<String, Object> eventData = new java.util.HashMap<>();
                eventData.put("clientId", response.getClientId());
                eventData.put("accountType", accountType);
                eventData.put("initialBalance", 5.0);
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String payload = mapper.writeValueAsString(eventData);
                kafkaTemplate.send("client-registered-topic", String.valueOf(response.getClientId()), payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return clientResponseToMap(response);
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    private java.util.Map<String, Object> clientResponseToMap(ClientResponse response) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("clientId", response.getClientId());
        map.put("name", response.getName());
        map.put("gender", response.getGender());
        map.put("age", response.getAge());
        map.put("identification", response.getIdentification());
        map.put("address", response.getAddress());
        map.put("phone", response.getPhone());
        map.put("status", response.getStatus());
        return map;
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<java.util.Map<String, Object>>> getClient(@PathVariable("id") Long clientId) {
        return Mono.fromCallable(() -> clientResponseToMap(clientStub.getClient(GetClientRequest.newBuilder().setClientId(clientId).build())))
                .subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @GetMapping
    public Mono<ApiResponse<List<java.util.Map<String, Object>>>> getAllClients() {
        return Mono.fromCallable(() -> clientStub.getAllClients(EmptyRequest.newBuilder().build()).getClientsList()
                .stream().map(this::clientResponseToMap).collect(java.util.stream.Collectors.toList()))
                .subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @PutMapping
    public Mono<ApiResponse<java.util.Map<String, Object>>> updateClient(@RequestBody java.util.Map<String, Object> payloadData) {
        return Mono.fromCallable(() -> {
            ClientRequest request = ClientRequest.newBuilder()
                    .setClientId(Long.parseLong(String.valueOf(payloadData.get("clientId"))))
                    .setName((String) payloadData.getOrDefault("name", ""))
                    .setGender((String) payloadData.getOrDefault("gender", ""))
                    .setAge(Integer.parseInt(String.valueOf(payloadData.getOrDefault("age", "0"))))
                    .setIdentification((String) payloadData.getOrDefault("identification", ""))
                    .setAddress((String) payloadData.getOrDefault("address", ""))
                    .setPhone((String) payloadData.getOrDefault("phone", ""))
                    .setPassword((String) payloadData.getOrDefault("password", ""))
                    .setStatus(Boolean.parseBoolean(String.valueOf(payloadData.getOrDefault("status", "true"))))
                    .build();
            return clientResponseToMap(clientStub.updateClient(request));
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @DeleteMapping("/{id}")
    public Mono<ApiResponse<Boolean>> deleteClient(@PathVariable("id") Long clientId) {
        return Mono.fromCallable(() -> clientStub.deleteClient(DeleteClientRequest.newBuilder().setClientId(clientId).build()).getSuccess())
                .subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }

    @PatchMapping("/{id}/status")
    public Mono<ApiResponse<java.util.Map<String, Object>>> updateClientStatus(@PathVariable("id") Long clientId, @RequestBody java.util.Map<String, Object> payloadData) {
        return Mono.fromCallable(() -> {
            boolean status = Boolean.parseBoolean(String.valueOf(payloadData.get("status")));
            PatchClientStatusRequest request = PatchClientStatusRequest.newBuilder()
                    .setClientId(clientId)
                    .setStatus(status)
                    .build();
            return clientResponseToMap(clientStub.patchClientStatus(request));
        }).subscribeOn(Schedulers.boundedElastic()).map(ApiResponse::success);
    }
}
