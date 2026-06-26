package com.laft.movement.infrastructure.adapter.out.kafka;

import com.laft.movement.application.port.out.MovementEventPort;
import com.laft.movement.domain.MovementDomain;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MovementKafkaAdapter implements MovementEventPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "movements-topic";

    public MovementKafkaAdapter(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishMovementCreated(MovementDomain movement) {
        String payload = String.format("{\"movementId\":\"%s\",\"accountNumber\":\"%s\",\"value\":%s,\"balance\":%s}",
                movement.getMovementId(), movement.getAccountNumber(), movement.getValue(), movement.getBalance());
        kafkaTemplate.send(TOPIC, movement.getAccountNumber(), payload);
    }
}
