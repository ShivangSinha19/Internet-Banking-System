# Internet Banking System

## Project Overview

This repository contains a Core Java internet banking application with an in-memory mode and an optional JDBC/MySQL persistence mode. Spring Boot and React are not part of this phase.

## Features

- Customer registration and login with email/password validation
- Demo admin login: `admin@bank.com` / `admin123`
- Multiple savings accounts per user
- Deposits, withdrawals, and atomic transfers using `BigDecimal`
- Frozen account protection
- Transaction history
- Beneficiary add, list, and remove operations
- Admin user/account/transaction views, search, freeze/unfreeze, and statistics

## Tech Stack

Java, OOP, collections, checked exceptions, `BigDecimal`, and `LocalDateTime`.

## Architecture

The default console application remains backward-compatible and uses in-memory services:

```text
Main -> Service -> ArrayList
```

The JDBC-ready services use:

```text
Main -> Service -> DAO -> JDBC -> MySQL
```

SQL is contained in `com.banking.dao.jdbc`. `DatabaseConnection` reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from environment variables or JVM system properties.

## Project Structure

The existing `com.banking` package structure is preserved under `src/main/java`. Custom exceptions are in `exception`, state is in `model`, and business logic is in `service`. Tests are under `src/test/java`.

## Core Java Version

Compile and run from the project directory with Maven:

```powershell
mvn test
mvn package -DskipTests
java -cp target/classes com.banking.Main
```

The project targets Java 25 bytecode and should be compiled with JDK 25.

## JDBC + MySQL

The schema is in [database/schema.sql](database/schema.sql). It defines `users`, `accounts`, `transactions`, and `beneficiaries` with primary keys, foreign keys, uniqueness constraints, `DECIMAL(15,2)` amounts, and timestamps.

Required configuration is shown in [.env.example](.env.example):

```text
DB_URL=jdbc:mysql://localhost:3306/internet_banking
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

Set these values in the process environment or pass them as JVM properties. Do not commit `.env` or real credentials. Execute the schema with a MySQL client, for example:

```text
mysql -u your_username -p < database/schema.sql
```

Implemented JDBC contracts and classes:

- `UserDAO` / `JdbcUserDAO`
- `AccountDAO` / `JdbcAccountDAO`
- `TransactionDAO` / `JdbcTransactionDAO`
- `BeneficiaryDAO` / `JdbcBeneficiaryDAO`
- `JdbcBankingService` and transaction-safe `JdbcTransferService`

JDBC user credentials are stored as PBKDF2-derived values by the JDBC service path. BCrypt is intentionally not included in this phase.

Live MySQL integration has not been verified in this environment. `mvn clean test` runs offline tests only and does not claim database connectivity.

## Testing

The project includes 11 JUnit 5 tests covering registration, duplicate registration, login success/failure, account creation, deposit, withdrawal, insufficient balance, transfer, same-account transfer rejection, and frozen-account rejection. Run them with `mvn clean test`. Live database tests should be added separately once a configured MySQL instance is available.

## Future Improvements

- Add the Spring Boot API with DTOs, validation, BCrypt password hashing, and role-based authorization.
- Add the requested ER diagram image after choosing a diagram generation tool.