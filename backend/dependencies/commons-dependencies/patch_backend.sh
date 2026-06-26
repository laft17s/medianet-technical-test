#!/bin/bash
set -e

# MovementUseCase.java
sed -i '' 's/List<MovementDomain> getMovementsByAccount(String accountNumber);/List<MovementDomain> getMovementsByAccount(String accountNumber);\n    List<MovementDomain> getAllMovements();/g' ../microservices/ms-core-account-movement/src/main/java/com/laft/movement/application/port/in/MovementUseCase.java

# MovementRepositoryPort.java
sed -i '' 's/List<MovementDomain> findByAccountNumberOrderByDateDesc(String accountNumber);/List<MovementDomain> findByAccountNumberOrderByDateDesc(String accountNumber);\n    List<MovementDomain> findAll();/g' ../microservices/ms-core-account-movement/src/main/java/com/laft/movement/application/port/out/MovementRepositoryPort.java

# MovementService.java
sed -i '' 's/public List<MovementDomain> getMovementsByAccount(String accountNumber) {/public List<MovementDomain> getAllMovements() {\n        return movementRepositoryPort.findAll();\n    }\n\n    @Override\n    public List<MovementDomain> getMovementsByAccount(String accountNumber) {/g' ../microservices/ms-core-account-movement/src/main/java/com/laft/movement/application/service/MovementService.java

# AccountUseCase.java
sed -i '' 's/List<AccountDomain> getAccountsByClient(Long clientId);/List<AccountDomain> getAccountsByClient(Long clientId);\n    List<AccountDomain> getAllAccounts();/g' ../microservices/ms-core-account-movement/src/main/java/com/laft/account/application/port/in/AccountUseCase.java

# AccountRepositoryPort.java
sed -i '' 's/List<AccountDomain> findByClientId(Long clientId);/List<AccountDomain> findByClientId(Long clientId);\n    List<AccountDomain> findAll();/g' ../microservices/ms-core-account-movement/src/main/java/com/laft/account/application/port/out/AccountRepositoryPort.java

# AccountService.java
sed -i '' 's/public List<AccountDomain> getAccountsByClient(Long clientId) {/public List<AccountDomain> getAllAccounts() {\n        return accountRepositoryPort.findAll();\n    }\n\n    @Override\n    public List<AccountDomain> getAccountsByClient(Long clientId) {/g' ../microservices/ms-core-account-movement/src/main/java/com/laft/account/application/service/AccountService.java

