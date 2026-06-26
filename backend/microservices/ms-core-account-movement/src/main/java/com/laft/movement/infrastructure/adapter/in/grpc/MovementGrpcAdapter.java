package com.laft.movement.infrastructure.adapter.in.grpc;

import com.laft.account.application.port.in.AccountUseCase;
import com.laft.account.domain.AccountDomain;
import com.laft.commons.exception.ResourceNotFoundException;
import com.laft.commons.grpc.movement.*;
import com.laft.movement.application.port.in.MovementUseCase;
import com.laft.movement.domain.MovementDomain;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class MovementGrpcAdapter extends MovementServiceGrpcGrpc.MovementServiceGrpcImplBase {

    private final MovementUseCase movementUseCase;
    private final AccountUseCase accountUseCase;

    public MovementGrpcAdapter(MovementUseCase movementUseCase, AccountUseCase accountUseCase) {
        this.movementUseCase = movementUseCase;
        this.accountUseCase = accountUseCase;
    }

    @Override
    public void createMovement(MovementRequest request, StreamObserver<MovementResponse> responseObserver) {
        MovementDomain domain = new MovementDomain();
        domain.setAccountNumber(request.getAccountNumber());
        domain.setMovementType(request.getMovementType());
        domain.setValue(BigDecimal.valueOf(request.getValue()));

        MovementDomain saved = movementUseCase.registerMovement(domain);
        responseObserver.onNext(toResponse(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void getMovementById(GetMovementByIdRequest request, StreamObserver<MovementResponse> responseObserver) {
        MovementDomain movement = movementUseCase.getMovementById(request.getMovementId());
        responseObserver.onNext(toResponse(movement));
        responseObserver.onCompleted();
    }

    @Override
    public void updateMovement(UpdateMovementRequest request, StreamObserver<MovementResponse> responseObserver) {
        MovementDomain updated = movementUseCase.updateMovement(
                request.getMovementId(),
                request.getMovementType(),
                BigDecimal.valueOf(request.getValue())
        );
        responseObserver.onNext(toResponse(updated));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteMovement(DeleteMovementRequest request, StreamObserver<DeleteMovementResponse> responseObserver) {
        movementUseCase.deleteMovement(request.getMovementId());
        responseObserver.onNext(DeleteMovementResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getMovementsByAccount(GetMovementsByAccountRequest request, StreamObserver<MovementListResponse> responseObserver) {
        List<MovementResponse> responses = movementUseCase.getMovementsByAccount(request.getAccountNumber()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        responseObserver.onNext(MovementListResponse.newBuilder().addAllMovements(responses).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllMovements(EmptyMovementRequest request, StreamObserver<MovementListResponse> responseObserver) {
        List<MovementResponse> responses = movementUseCase.getAllMovements().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        responseObserver.onNext(MovementListResponse.newBuilder().addAllMovements(responses).build());
        responseObserver.onCompleted();
    }

    @Override
    public void generateReport(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        LocalDate start = LocalDate.parse(request.getStartDate());
        LocalDate end = LocalDate.parse(request.getEndDate());
        List<ReportItem> items = movementUseCase.getAllMovements().stream()
                .filter(m -> isWithinRange(m.getDate(), start, end))
                .filter(m -> matchesClient(m, request.getClientId()))
                .map(this::toReportItem)
                .collect(Collectors.toList());
        responseObserver.onNext(ReportResponse.newBuilder().addAllItems(items).build());
        responseObserver.onCompleted();
    }

    private boolean isWithinRange(LocalDateTime date, LocalDate start, LocalDate end) {
        if (date == null) {
            return false;
        }
        LocalDate movementDate = date.toLocalDate();
        return !movementDate.isBefore(start) && !movementDate.isAfter(end);
    }

    private boolean matchesClient(MovementDomain movement, long clientId) {
        if (clientId <= 0) {
            return true;
        }
        try {
            AccountDomain account = accountUseCase.getAccount(movement.getAccountNumber());
            return account.getClientId().equals(clientId);
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }

    private ReportItem toReportItem(MovementDomain movement) {
        AccountDomain account = accountUseCase.getAccount(movement.getAccountNumber());
        return ReportItem.newBuilder()
                .setDate(movement.getDate() != null ? movement.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "")
                .setClientName("Client " + account.getClientId())
                .setAccountNumber(movement.getAccountNumber())
                .setAccountType(account.getAccountType())
                .setInitialBalance(account.getInitialBalance() != null ? account.getInitialBalance().doubleValue() : 0.0)
                .setStatus(account.getStatus() != null && account.getStatus())
                .setMovementValue(movement.getValue() != null ? movement.getValue().doubleValue() : 0.0)
                .setAvailableBalance(movement.getBalance() != null ? movement.getBalance().doubleValue() : 0.0)
                .build();
    }

    private MovementResponse toResponse(MovementDomain saved) {
        return MovementResponse.newBuilder()
                .setMovementId(saved.getMovementId() != null ? saved.getMovementId() : 0L)
                .setDate(saved.getDate() != null ? saved.getDate().toString() : "")
                .setMovementType(saved.getMovementType() != null ? saved.getMovementType() : "")
                .setValue(saved.getValue() != null ? saved.getValue().doubleValue() : 0.0)
                .setBalance(saved.getBalance() != null ? saved.getBalance().doubleValue() : 0.0)
                .setAccountNumber(saved.getAccountNumber() != null ? saved.getAccountNumber() : "")
                .build();
    }
}
