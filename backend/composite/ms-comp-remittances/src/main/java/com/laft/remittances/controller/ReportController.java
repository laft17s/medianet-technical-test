package com.laft.remittances.controller;

import com.laft.commons.grpc.account.AccountResponse;
import com.laft.commons.grpc.account.AccountServiceGrpcGrpc;
import com.laft.commons.grpc.account.GetAccountRequest;
import com.laft.commons.grpc.client.ClientResponse;
import com.laft.commons.grpc.client.ClientServiceGrpcGrpc;
import com.laft.commons.grpc.client.GetClientRequest;
import com.laft.commons.grpc.movement.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping(value = "/reports", produces = "application/json")
public class ReportController {

    @GrpcClient("movement-service")
    private MovementServiceGrpcGrpc.MovementServiceGrpcBlockingStub movementStub;

    @GrpcClient("account-service")
    private AccountServiceGrpcGrpc.AccountServiceGrpcBlockingStub accountStub;

    @GrpcClient("client-service")
    private ClientServiceGrpcGrpc.ClientServiceGrpcBlockingStub clientStub;

    public static class ReportItemDTO {
        public String date;
        public String clientName;
        public String accountNumber;
        public String accountType;
        public double initialBalance;
        public boolean status;
        public double movementValue;
        public double availableBalance;

        public static ReportItemDTO from(ReportItem item) {
            ReportItemDTO dto = new ReportItemDTO();
            dto.date = item.getDate();
            dto.clientName = item.getClientName();
            dto.accountNumber = item.getAccountNumber();
            dto.accountType = item.getAccountType();
            dto.initialBalance = item.getInitialBalance();
            dto.status = item.getStatus();
            dto.movementValue = item.getMovementValue();
            dto.availableBalance = item.getAvailableBalance();
            return dto;
        }
    }

    @GetMapping
    public Mono<List<ReportItemDTO>> getReport(
            @RequestParam("fecha") String dateRange,
            @RequestParam(value = "cliente", required = false) Long clientId) {
        String[] dates = dateRange.split(",");
        String startDate = dates[0];
        String endDate = dates.length > 1 ? dates[1] : dates[0];
        
        return Mono.fromCallable(() -> {
            ReportRequest.Builder requestBuilder = ReportRequest.newBuilder()
                    .setStartDate(startDate)
                    .setEndDate(endDate);
            if (clientId != null) {
                requestBuilder.setClientId(clientId);
            }
            
            List<ReportItem> items = movementStub.generateReport(requestBuilder.build()).getItemsList();
            return items.stream().map(item -> {
                ReportItemDTO dto = ReportItemDTO.from(item);
                try {
                    AccountResponse account = accountStub.getAccount(
                            GetAccountRequest.newBuilder().setAccountNumber(item.getAccountNumber()).build());
                    ClientResponse client = clientStub.getClient(
                            GetClientRequest.newBuilder().setClientId(account.getClientId()).build());
                    dto.clientName = client.getName();
                } catch (Exception ignored) {
                    // keep default clientName from movement service
                }
                return dto;
            }).toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
