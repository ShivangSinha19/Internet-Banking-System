# Internet Banking System

## Project Overview

This repository contains an Internet Banking System with a Spring Boot REST backend, a React/Vite frontend, and the original Core Java/JDBC implementation preserved for reference and compatibility.

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

Java 25, Spring Boot 4.1.1, Spring Web, Spring Data JPA, Hibernate, Spring Security HTTP Basic, BCrypt, MySQL, Maven, React, Vite, TypeScript, Axios, and React Router.

## Architecture

The default console application remains backward-compatible and uses in-memory services:

```text
Main -> Service -> ArrayList
```

The JDBC-ready services use:

```text
Main -> Service -> DAO -> JDBC -> MySQL
```

The current REST application uses:

```text
React/Vite -> Axios -> Spring Boot REST Controllers -> Services -> Spring Data JPA -> Hibernate -> MySQL
```

SQL is contained in `com.banking.dao.jdbc`. `DatabaseConnection` reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from environment variables or JVM system properties.

## Project Structure

The existing `com.banking` package structure is preserved under `src/main/java`. The Spring Boot API is organized into `controller`, `service`, `repository`, `entity`, `dto`, `security`, `config`, and `exception`. The older `model`, `dao`, and JDBC services remain under their original packages. The frontend lives separately under `frontend/src`.

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

## Spring Boot Backend

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the process environment. Spring Boot does not automatically load `.env` files. The application uses `spring.jpa.hibernate.ddl-auto=validate` and never creates or drops the existing schema.

Start the backend from the repository root:

```powershell
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

Authentication uses HTTP Basic. Registration and login accept JSON. After login, clients send the email and password through the Basic Authorization header for protected requests. The React client keeps this authentication in memory and never stores the password in local storage.

## React Frontend

Configure the API URL in `frontend/.env` using [frontend/.env.example](frontend/.env.example), then run:

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs at `http://localhost:5173`. Build it with `npm run build`.

Customer routes cover the dashboard, accounts, deposits, withdrawals, transfers, transaction history, beneficiaries, and profile. Admin routes cover users, accounts, transactions, and account freeze/unfreeze operations.

## REST API Endpoints

Authentication: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`

Accounts: `POST /api/accounts`, `GET /api/accounts/my`, `GET /api/accounts/{accountNumber}`, `POST /api/accounts/{accountNumber}/deposit`, `POST /api/accounts/{accountNumber}/withdraw`

Transfers and history: `POST /api/transfers`, `GET /api/transactions/my/{accountNumber}`

Beneficiaries: `POST /api/beneficiaries`, `GET /api/beneficiaries/my`, `DELETE /api/beneficiaries/{beneficiaryId}`

Admin: `GET /api/admin/users`, `GET /api/admin/accounts`, `GET /api/admin/transactions`, `PATCH /api/admin/accounts/{accountNumber}/freeze`, `PATCH /api/admin/accounts/{accountNumber}/unfreeze`

An importable request set is available at [postman/internet-banking.postman_collection.json](postman/internet-banking.postman_collection.json).

## Testing

The project includes 22 tests covering the original Core Java behavior, REST service behavior, transfer ownership rules, and Spring web wiring. Run the backend tests with `mvn clean test`. Build the frontend with `cd frontend; npm run build`.

## Future Improvements

- Add integration tests against a dedicated test MySQL database.
- Add a production deployment profile and secret manager integration.