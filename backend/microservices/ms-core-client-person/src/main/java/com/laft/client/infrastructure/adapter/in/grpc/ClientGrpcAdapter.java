package com.laft.client.infrastructure.adapter.in.grpc;

import com.laft.client.application.port.in.ClientUseCase;
import com.laft.client.domain.ClientDomain;
import com.laft.commons.grpc.client.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class ClientGrpcAdapter extends ClientServiceGrpcGrpc.ClientServiceGrpcImplBase {

    private final ClientUseCase clientUseCase;

    public ClientGrpcAdapter(ClientUseCase clientUseCase) {
        this.clientUseCase = clientUseCase;
    }

    @Override
    public void createClient(ClientRequest request, StreamObserver<ClientResponse> responseObserver) {
        ClientDomain domain = toDomain(request);
        ClientDomain saved = clientUseCase.createClient(domain);
        responseObserver.onNext(toResponse(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void getClient(GetClientRequest request, StreamObserver<ClientResponse> responseObserver) {
        ClientDomain found = clientUseCase.getClient(request.getClientId());
        responseObserver.onNext(toResponse(found));
        responseObserver.onCompleted();
    }

    @Override
    public void updateClient(ClientRequest request, StreamObserver<ClientResponse> responseObserver) {
        ClientDomain domain = toDomain(request);
        ClientDomain updated = clientUseCase.updateClient(domain);
        responseObserver.onNext(toResponse(updated));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteClient(DeleteClientRequest request, StreamObserver<DeleteClientResponse> responseObserver) {
        clientUseCase.deleteClient(request.getClientId());
        responseObserver.onNext(DeleteClientResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllClients(EmptyRequest request, StreamObserver<ClientListResponse> responseObserver) {
        List<ClientDomain> clients = clientUseCase.getAllClients();
        ClientListResponse response = ClientListResponse.newBuilder()
                .addAllClients(clients.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void patchClientStatus(PatchClientStatusRequest request, StreamObserver<ClientResponse> responseObserver) {
        ClientDomain updated = clientUseCase.updateClientStatus(request.getClientId(), request.getStatus());
        responseObserver.onNext(toResponse(updated));
        responseObserver.onCompleted();
    }

    @Override
    public void validateCredentials(AuthRequest request, StreamObserver<ClientResponse> responseObserver) {
        try {
            ClientDomain client = clientUseCase.validateCredentials(request.getIdentification(), request.getPassword());
            responseObserver.onNext(toResponse(client));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private ClientDomain toDomain(ClientRequest request) {
        ClientDomain domain = new ClientDomain();
        domain.setClientId(request.getClientId());
        domain.setName(request.getName());
        domain.setGender(request.getGender());
        domain.setAge(request.getAge());
        domain.setIdentification(request.getIdentification());
        domain.setAddress(request.getAddress());
        domain.setPhone(request.getPhone());
        domain.setPassword(request.getPassword());
        domain.setStatus(request.getStatus());
        return domain;
    }

    private ClientResponse toResponse(ClientDomain domain) {
        return ClientResponse.newBuilder()
                .setClientId(domain.getClientId() != null ? domain.getClientId() : 0L)
                .setName(domain.getName() != null ? domain.getName() : "")
                .setGender(domain.getGender() != null ? domain.getGender() : "")
                .setAge(domain.getAge() != null ? domain.getAge() : 0)
                .setIdentification(domain.getIdentification() != null ? domain.getIdentification() : "")
                .setAddress(domain.getAddress() != null ? domain.getAddress() : "")
                .setPhone(domain.getPhone() != null ? domain.getPhone() : "")
                .setStatus(domain.getStatus() != null ? domain.getStatus() : false)
                .build();
    }
}
