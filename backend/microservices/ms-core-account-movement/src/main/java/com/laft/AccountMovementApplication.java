package com.laft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.laft.account", "com.laft.movement", "com.laft.commons.grpc"})
public class AccountMovementApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountMovementApplication.class, args);
    }
}
