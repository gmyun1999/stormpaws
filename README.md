# 🌩️ StormPaws

StormPaws is a **cloud-based backend server for a turn-based strategy game**.
The game client is developed in Unity and communicates with this server in real time via REST APIs.
The backend is designed with a **Clean Architecture** approach to ensure domain independence and scalability.

---

## 🔗 Deployment

- **API Server**: [https://stormpaws.duckdns.org](https://stormpaws.duckdns.org)
- **Game Client Download**: [https://github.com/NamJongha/MSE_StormPaws](https://github.com/NamJongha/MSE_StormPaws)

---

## ⚙️ Tech Stack

- **Java 17**
- **Spring Boot 3.4.4**
- **Gradle**
- **PostgreSQL 13+**
- **Docker**
- **Clean Architecture layered structure**

---

## 📁 Project Structure

StormPaws is organized using a **Clean Architecture** to ensure maintainability and testability. It is composed of the following four layers:

### 📁 1. `domain` – Domain Layer (Business Rules)

- Defines core logic and data structures
- Instead of using pure Value Objects or Entities, `*Model` classes are used as **domain objects** to match project scale and reduce overhead
  Example: `UserModel`, `DeckModel`, `CardModel`, `BattleModel`
- Given the small scale of the project, we also used the `domain model` as a DTO to increase reusability.

> ✅ Not separating domain models and DTOs is a strategic decision based on team size and development efficiency for small/mid-scale projects.

### 📁 2. `service` – Application Service Layer

- Coordinates multiple domain objects to implement business flows
- Ensures clear transactional responsibilities
  Example: `BattleService`, `DeckService`

### 📁 3. `interface` – Interface Layer (Input/Output Adapters)

- Handles HTTP requests from clients
- Example: `UserController`, `DeckController`, `BattleController`

### 📁 4. `infra` – Infrastructure Layer (External Systems)

- Implements external systems, DB access, and security

  - `infra/jpa`: Spring Data JPA repository implementations
  - `infra/OpenMeteo`: External weather API integration
  - `infra/RestTemplate`: Wrapper for external API calls
  - `infra/security`: Google OAuth and JWT-based security

> 🔁 All dependencies point **toward the domain**.
> 📉 External technologies and frameworks do not directly affect domain logic.

---

## 🔐 External Dependencies

StormPaws adopts a secure architecture using Google OAuth and JWT. It also uses a RestTemplate-based wrapper for external API communication.

- `infra/security/GoogleOAuthProvider` – Handles Google OAuth authentication
- `infra/security/JwtTokenProvider` – Generates and validates JWT tokens
- `infra/OpenMeteo` – Integrates with external weather API (used in battle logic)
- `infra/RestTemplate` – Common setup for HTTP API calls
- `infra/jpa` – Implements data repositories with Spring Data JPA

---

## 📊 API Testing

You can test REST API calls using the example files located in the `util/rest_client` directory (`*.http`).

> These can be executed directly in IDEs such as IntelliJ or VSCode.
