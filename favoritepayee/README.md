# Favorite Payee Management Service

A production-ready Spring Boot backend for managing favorite bank payees. It provides secure REST APIs to create, manage, and list favorite bank accounts, ensuring all business rules (like maximum limits and unique IBANs) are enforced.

## 🚀 Technology Stack
* **Java:** 21
* **Framework:** Spring Boot 3.2.x
* **Security:** Spring Security with Stateless JWT
* **Database:** PostgreSQL & Spring Data JPA
* **Validation:** Spring Validation (Hibernate Validator)
* **Utilities:** Lombok, MapStruct (for DTO mappings)
* **Documentation:** Springdoc OpenAPI (Swagger UI)
* **Testing:** JUnit 5 & Mockito
* **Build Tool:** Maven

---

## 📂 Project Structure

```text
com.hackathon.favoritepayee
├── config/
│   ├── DatabaseSeeder.java      # Automatically populates the 'bank_code_mapping' table on startup
│   └── OpenApiConfig.java       # Configuration for Swagger/OpenAPI UI
├── controller/
│   ├── AuthController.java            # Endpoints for User Login and Registration
│   └── FavoriteAccountController.java # Endpoints for CRUD operations on favorite accounts
├── dto/
│   ├── AuthRequest/Response.java           # Payloads for authentication
│   ├── FavoriteAccountRequest/Response.java # Payloads for managing favorite accounts
│   ├── PageResponse.java                   # Standard wrapper for paginated lists
│   └── ErrorResponse.java                  # Standard wrapper for API errors
├── entity/
│   ├── Customer.java            # Represents a user
│   ├── FavoriteAccount.java     # Represents a user's saved favorite account
│   └── BankCodeMapping.java     # Lookup table for mapping bank codes to bank names
├── exception/
│   ├── BusinessException.java         # Thrown for limit reached, duplicate IBAN, etc.
│   ├── ResourceNotFoundException.java # Thrown when a record doesn't exist
│   └── GlobalExceptionHandler.java    # Intercepts exceptions and formats HTTP ErrorResponses
├── mapper/
│   └── FavoriteAccountMapper.java  # MapStruct interface bridging Entities and DTOs
├── repository/
│   ├── CustomerRepository.java            # JPA repository for Customers
│   ├── FavoriteAccountRepository.java     # JPA repository for Accounts (supports pagination)
│   └── BankCodeMappingRepository.java     # JPA repository for Bank mappings
├── security/
│   ├── SecurityConfig.java           # Spring Security rules (exposing /auth and /actuator)
│   ├── JwtTokenProvider.java         # Generates and validates JWT tokens
│   ├── JwtAuthenticationFilter.java  # Intercepts requests to enforce JWT validation
│   └── CustomUserDetailsService.java # Loads the user from the database
├── service/
│   ├── AuthService.java            # Logic for registering users and verifying logins
│   └── FavoriteAccountService.java # Core business logic, IBAN checks, limit enforcement
└── util/
    └── IbanParser.java             # Component to extract bank codes from IBAN strings
```

---

## 🔄 API Flow & Business Logic

### 1. Authentication Flow
1. **Registration:** You send a `POST /api/v1/auth/register`. The `AuthService` checks if the username is unique, hashes the password using `BCrypt`, and saves the new `Customer`.
2. **Login:** You send a `POST /api/v1/auth/login`. Spring Security verifies the credentials. If valid, `JwtTokenProvider` generates a JWT.
3. **Subsequent Requests:** You pass the JWT in the `Authorization: Bearer <token>` header. The `JwtAuthenticationFilter` intercepts the request, validates the token signature, extracts the user, and securely allows access to the requested endpoint.

### 2. Favorite Accounts Flow
1. **Creation:** You send a `POST /api/v1/favorites` containing the `accountName` and `iban`.
   * **Validation Phase:** The `FavoriteAccountController` ensures inputs aren't blank and match the RegEx rules (alphanumeric).
   * **Limit Phase:** `FavoriteAccountService` ensures the user hasn't hit the 20 account maximum.
   * **Uniqueness Phase:** Ensures the user doesn't already have this exact IBAN saved.
   * **Bank Extraction:** `IbanParser` extracts the substring (index 4 to 8) from the IBAN to get the Bank Code. The code is looked up in `bank_code_mapping`. If invalid, it throws a `BusinessException`.
   * **Save Phase:** Saved to the DB, mapped back to a Response DTO, and returned.
2. **Retrieval:** `GET /api/v1/favorites?page=0&size=5` uses JPA pagination to quickly retrieve chunks of favorite accounts without loading the entire database into memory.

---

## 🌐 API Endpoints

### Authentication
* **`POST /api/v1/auth/register`**
  * **Payload:** `{"username":"user", "password":"password123"}`
  * **Returns:** `201 Created`
* **`POST /api/v1/auth/login`**
  * **Payload:** `{"username":"user", "password":"password123"}`
  * **Returns:** `200 OK` with `{"token": "eyJhb..."}`

### Favorite Accounts (Requires Bearer Token)
* **`POST /api/v1/favorites`** (Create)
  * **Payload:** `{"accountName": "Rent", "iban": "DE89DEUT100200300400"}`
  * **Returns:** Details including dynamically extracted `bankName` (e.g., "Deutsche Bank").
* **`GET /api/v1/favorites?page=0&size=5`** (List)
  * **Returns:** Paginated response containing `content`, `totalPages`, `totalElements`, etc.
* **`PUT /api/v1/favorites/{id}`** (Update)
  * **Payload:** New `accountName` or `iban`. Recalculates the bank code dynamically.
* **`DELETE /api/v1/favorites/{id}`** (Delete)
  * **Returns:** `204 No Content`.

### Actuator (Health Checks)
* **`GET /actuator/health`** (Publicly accessible)
  * **Returns:** Detailed application health status, perfect for Docker Compose / Kubernetes.

---

## 🛠️ Setup & Running

### Using Docker Compose (Recommended)
This spins up both the PostgreSQL database and the Spring Boot application perfectly configured.
```bash
docker-compose up --build
```
The application will start on `http://localhost:8080`.

### Running Locally (Without Docker)
1. Start a local PostgreSQL instance.
2. Create a database named `favoritepayee`.
3. Build and run the app:
```bash
mvn clean install
mvn spring-boot:run
```

### Swagger Documentation
Explore and test the APIs interactively by visiting:
**http://localhost:8080/swagger-ui.html**
