package com.laft.movement.application.port.out;

import com.laft.movement.domain.MovementDomain;

public interface MovementEventPort {
    void publishMovementCreated(MovementDomain movement);
}
