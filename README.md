# ⚡ SkillBarter — Time-Banking & Skill Exchange Platform

> **UE23CS352B Object Oriented Analysis & Design — Distinction-Level Project**  
> Trade time credits instead of money. 1 hour teaching = 1 credit.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Features](#features)
4. [Design Patterns & SOLID Principles](#design-patterns--solid-principles)
5. [Architecture](#architecture)
6. [Setup & Run](#setup--run)
7. [Default Credentials](#default-credentials)
8. [UML Diagrams](#uml-diagrams)
9. [Package Structure](#package-structure)
10. [Report Checklist](#report-checklist)

---

## Project Overview

SkillBarter is a full-stack Spring Boot MVC application implementing a time-banking
platform where users exchange skills using "time credits" as currency.

### Core Concept
- 1 hour of teaching = 1 time credit earned
- Credits are held in **escrow** during sessions (not released until both parties confirm)
- Disputes are resolved by a **Tribunal Verifier** role
- Skills can be **Verified** via certificate upload + admin review → badge awarded
- **Gamification**: streaks, leaderboards, achievement badges

---

## Tech Stack

| Layer        | Technology                               |
|-------------|------------------------------------------|
| Language     | Java 17                                  |
| Framework    | Spring Boot 3.2.3 (MVC enforced)         |
| Frontend     | Thymeleaf + custom CSS (no framework)    |
| Database     | MySQL 8+ / JPA + Hibernate               |
| Security     | Spring Security 6 (BCrypt, Sessions)     |
| PDF          | iText 7                                  |
| Build        | Maven                                    |
| Async Events | Spring ApplicationEventPublisher         |

---

## Features

### 4 Major Features
| # | Feature | Details |
|---|---------|---------|
| M1 | User Auth & Profile Management | Registration, login, profile edit, avatar upload, skill verification badges |
| M2 | Skill Request & Smart Matching | 3 matching strategies (Rating, Affordable, Verified-Only) via Strategy Pattern |
| M3 | Time Credit Transaction / Escrow | PENDING→ESCROWED→RELEASED/REFUNDED/DISPUTED state machine |
| M4 | Review, Rating & Dispute Resolution | Post-session reviews + Tribunal with ROLE_VERIFIER adjudication |

### 4 Minor Features
| # | Feature | Details |
|---|---------|---------|
| m1 | Search Skills by Category | Full-text search + category filter |
| m2 | In-App Notifications | Observer Pattern; real-time badge; auto-read on view |
| m3 | Admin Dashboard & User Bans | Ban/suspend/reinstate, promote to Verifier |
| m4 | PDF Receipt Generation | Auto-generated session receipt via iText 7 |

---

## Design Patterns & SOLID Principles

### Design Patterns (4 distinct)

| Pattern | Classification | Location | Purpose |
|---------|---------------|----------|---------|
| **Strategy** | Behavioral | `matching/` package | 3 interchangeable matching algorithms; context = `MatchingService` |
| **Observer** | Behavioral | `pattern/DomainEvents.java` + `NotificationService` | Domain events decouple service layer from notification logic |
| **Builder** | Creational | `pattern/NotificationBuilder.java` | Step-by-step construction of `Notification` objects with validation |
| **Decorator** | Structural | `pattern/UserProfileDecorator.java` | Wraps `User` entity to add computed display properties without modification |

**Bonus — State Pattern:**  
`Transaction.transitionTo()` + `TransactionStatus` enum enforces legal state transitions; 
`SessionService` drives the Session state machine.

### MVC Pattern
- **Model**: JPA Entities + Service layer business logic
- **View**: Thymeleaf HTML templates in `resources/templates/`
- **Controller**: All `@Controller` classes in `controller/` package

### SOLID Principles (4 explicit)

| Principle | Where |
|-----------|-------|
| **SRP** — Single Responsibility | Each Service class manages one domain (e.g., `TransactionService` only moves credits; `NotificationService` only handles notifications) |
| **OCP** — Open/Closed | `MatchingStrategy` interface: new strategies added by implementing interface, zero modification to existing code |
| **LSP** — Liskov Substitution | All `MatchingStrategy` implementations substitutable in `MatchingService`; `UserDetails` extended by `CustomUserDetailsService` |
| **DIP** — Dependency Inversion | `SecurityConfig` depends on `UserDetailsService` abstraction; `MatchingService` depends on `Map<String, MatchingStrategy>` abstraction |

---

## Architecture

```
com.skillbarter/
├── SkillBarterApplication.java        ← Entry point
├── config/
│   └── SecurityConfig.java            ← Spring Security config
├── controller/                        ← MVC Controllers (HTTP layer)
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── SessionController.java
│   ├── SkillController.java
│   ├── DisputeController.java
│   ├── ProfileController.java
│   ├── NotificationController.java
│   └── AdminController.java
├── service/                           ← Business logic layer
│   ├── UserService.java
│   ├── SessionService.java
│   ├── TransactionService.java        ← Escrow engine
│   ├── SkillService.java
│   ├── ReviewService.java
│   ├── DisputeService.java            ← Tribunal logic
│   ├── NotificationService.java       ← Observer listener
│   └── PdfReceiptService.java
├── entity/                            ← JPA entities (Model)
│   ├── User.java
│   ├── Skill.java
│   ├── Session.java
│   ├── Transaction.java
│   ├── Review.java
│   ├── Dispute.java
│   ├── Badge.java
│   └── Notification.java
├── repository/                        ← Spring Data JPA interfaces
├── dto/                               ← Form/request DTOs
├── enums/                             ← All enumerations
├── exception/                         ← Custom exceptions + handler
├── matching/                          ← Strategy Pattern implementation
│   ├── MatchingStrategy.java          ← Interface
│   ├── RatingBasedMatchingStrategy.java
│   ├── AffordabilityMatchingStrategy.java
│   ├── VerifiedOnlyMatchingStrategy.java
│   └── MatchingService.java           ← Context class
├── pattern/                           ← Design pattern classes
│   ├── DomainEvents.java              ← Observer events
│   ├── NotificationBuilder.java       ← Builder pattern
│   └── UserProfileDecorator.java      ← Decorator pattern
└── security/
    ├── CustomUserDetailsService.java
    └── SecurityUtils.java

resources/
├── templates/                         ← Thymeleaf Views (MVC View layer)
│   ├── layout.html                    ← Shared navbar/footer fragments
│   ├── auth/login.html
│   ├── auth/register.html
│   ├── dashboard/home.html
│   ├── sessions/list.html
│   ├── sessions/detail.html
│   ├── skills/browse.html
│   └── admin/dashboard.html
└── static/
    ├── css/main.css
    └── js/app.js

docs/
├── diagrams.puml    ← All 10 PlantUML diagrams
└── schema.sql       ← Full DDL + seed data
```

---

## Setup & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### Steps

```bash
# 1. Clone
git clone https://github.com/your-org/skillbarter.git
cd skillbarter

# 2. Configure database
#    Edit src/main/resources/application.properties:
#    spring.datasource.password=YOUR_MYSQL_PASSWORD

# 3. Initialize schema (optional — Hibernate auto-creates)
mysql -u root -p < docs/schema.sql

# 4. Build & run
mvn spring-boot:run

# 5. Open browser
open http://localhost:8080
```

---

## Default Credentials

| Role     | Email                          | Password     |
|----------|-------------------------------|-------------|
| Admin    | admin@skillbarter.app          | Admin@1234  |
| Verifier | verifier@skillbarter.app       | Verify@1234 |
| User     | alice@example.com              | Test@1234   |

> ⚠️ Change all passwords before any deployment.

---

## UML Diagrams

All diagrams are in `docs/diagrams.puml`. Render with:
- [PlantUML online server](https://www.plantuml.com/plantuml/uml/)
- IntelliJ IDEA PlantUML plugin
- VS Code PlantUML extension

| Diagram | Type | Description |
|---------|------|-------------|
| UseCaseDiagram | Use Case | All actors and system goals |
| ClassDiagram | Class | Full entity + relationship map |
| StateDiagram_Transaction | State | Escrow state machine (4 states) |
| StateDiagram_Session | State | Session lifecycle (6 states) |
| StateDiagram_User | State | Account status machine (4 states) |
| StateDiagram_Dispute | State | Tribunal state machine (5 states) |
| ActivityDiagram_Login | Activity | Login flow with security checks |
| ActivityDiagram_BookSession | Activity | Full session booking workflow |
| ActivityDiagram_CompleteTransaction | Activity | Escrow release + gamification |
| ActivityDiagram_ResolveDispute | Activity | Dispute tribunal workflow |

---

## Report Checklist

- [x] Cover page: Project name, team, USN, semester, guide name
- [x] Abstract (1 page)
- [x] Table of contents
- [x] Chapter 1: Introduction & Problem Statement
- [x] Chapter 2: OOAD Analysis (Use Case + Activity Diagrams)
- [x] Chapter 3: Design (Class + State Diagrams, Design Patterns)
- [x] Chapter 4: Architecture (Spring MVC layers, package diagram)
- [x] Chapter 5: Implementation (code excerpts, screenshots)
- [x] Chapter 6: Testing (unit test cases, coverage)
- [x] Chapter 7: Conclusion & Future Work
- [x] References (Spring docs, OOAD textbook, iText docs)
- [x] Appendix: Full class listing, SQL schema

### GitHub Repository Checklist
- [x] README.md (this file)
- [x] `docs/diagrams.puml`
- [x] `docs/schema.sql`
- [x] `src/` — full Maven project
- [x] `screenshots/` — login, dashboard, session flow, admin, disputes

### Presentation Tips
1. **Run the app live** — show registration → add skill → book session → confirm → PDF receipt
2. **Show escrow** — highlight credit balance changing at each step
3. **Demonstrate patterns** — open `MatchingService.java` and swap strategies in the browser
4. **Show State Machine** — walk through `Transaction.transitionTo()` on the whiteboard
5. **Admin demo** — ban a user, verify a skill, assign a verifier, resolve a dispute
