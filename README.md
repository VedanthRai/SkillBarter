# SkillBarter — Time-Banking & Skill Exchange Platform

> **UE23CS352B Object Oriented Analysis & Design — Distinction-Level Project**
> Trade time credits instead of money. 1 hour teaching = 1 credit earned.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Features](#features)
4. [Design Patterns & SOLID Principles](#design-patterns--solid-principles)
5. [Architecture](#architecture)
6. [Setup & Run](#setup--run)
7. [Default Credentials](#default-credentials)
8. [UML Diagrams](#uml-diagrams)
9. [Package Structure](#package-structure)
10. [API Endpoints](#api-endpoints)

---

## Project Overview

SkillBarter is a full-stack Spring Boot MVC web application implementing a time-banking
platform where users exchange skills using "time credits" as currency — no money involved.

### Core Concept
- 1 hour of teaching = 1 time credit earned
- Credits are held in **escrow** during sessions (released only after both parties confirm)
- Disputes are resolved by a **Tribunal Verifier** role
- Skills can be **Verified** via certificate upload + admin review → badge awarded
- **Gamification**: streaks, leaderboards, achievement badges, referral system

### What Makes It Competitive
- AI-powered skill recommendations (collaborative + content-based filtering)
- Real-time platform statistics dashboard
- Advanced multi-criteria search with autocomplete
- Social activity feed with trending skills
- Profile completeness scoring system
- Teacher availability scheduling
- Skill swap proposals (barter without credits)
- Learning path creation and tracking
- Credit gifting between users
- PDF receipt generation for sessions

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 (compiled with JDK 25) |
| Framework | Spring Boot 3.2.3 (MVC enforced) |
| Frontend | Thymeleaf + custom CSS (dark theme, no framework) |
| Database | MySQL 8+ / JPA + Hibernate |
| Security | Spring Security 6 (BCrypt, Sessions) |
| PDF | iText 7 |
| AI | Google Gemini 2.0 Flash API |
| Build | Maven |
| Async Events | Spring ApplicationEventPublisher |
| Charts | Chart.js (admin analytics) |

---

## Features

### 4 Major Features (OOAD Requirement)

| # | Feature | Details |
|---|---------|---------|
| M1 | User Auth & Profile Management | Registration, login, profile edit, avatar upload, skill verification badges, profile completeness score |
| M2 | Skill Request & Smart Matching | 3 matching strategies (Rating, Affordable, Verified-Only) via Strategy Pattern, advanced search with autocomplete |
| M3 | Time Credit Transaction / Escrow | PENDING→ESCROWED→RELEASED/REFUNDED/DISPUTED state machine, wallet, credit gifting |
| M4 | Review, Rating & Dispute Resolution | Post-session reviews + Tribunal with ROLE_VERIFIER adjudication, dispute tracking |

### 4 Minor Features (OOAD Requirement)

| # | Feature | Details |
|---|---------|---------|
| m1 | Search Skills by Category | Full-text search on name + description, category filter, autocomplete suggestions |
| m2 | In-App Notifications | Observer Pattern; real-time badge count; categorized notification center |
| m3 | Admin Dashboard & User Management | Ban/suspend/reinstate users, promote to Verifier, analytics with charts |
| m4 | PDF Receipt Generation | Auto-generated session receipt via iText 7 |

### Bonus Features (Urban Pro Competitive)

| Feature | Description |
|---------|-------------|
| AI Recommendations | Collaborative + content-based skill and teacher recommendations |
| Activity Feed | Social timeline of platform activity, trending skills |
| Real-Time Stats | Live platform statistics (active users, sessions, credits) |
| Analytics Dashboard | 6 Chart.js charts for admin insights |
| Skill Swaps | Propose direct skill-for-skill barter without credits |
| Learning Paths | Create structured multi-step learning journeys |
| Teacher Availability | Schedule and manage teaching time slots |
| Referral System | Unique referral codes with credit bonuses |
| Credit Gifting | Send credits to other users |
| Session Notes | Attach notes and resources to sessions |
| Skill Requests Board | Post wanted skills for teachers to respond |
| Wishlist | Save skills to learn later with alerts |
| Endorsements | Peer endorsements for skills |
| Password Reset | Email-based password reset flow |
| AI Chat Assistant | Gemini-powered skill learning assistant |
| Report Export | CSV/PDF export of analytics data |

---

## Design Patterns & SOLID Principles

### Design Patterns (4 required)

| Pattern | Classification | Location | Purpose |
|---------|---------------|----------|---------|
| **Strategy** | Behavioral | `matching/` package | 3 interchangeable matching algorithms; context = `MatchingService` |
| **Observer** | Behavioral | `pattern/DomainEvents.java` + `NotificationService` | Domain events decouple service layer from notification logic |
| **Builder** | Creational | `pattern/NotificationBuilder.java` | Step-by-step construction of `Notification` objects with validation |
| **Decorator** | Structural | `pattern/UserProfileDecorator.java` | Wraps `User` entity to add computed display properties without modification |

### SOLID Principles (4 required)

| Principle | Where Applied |
|-----------|--------------|
| **SRP** — Single Responsibility | Each Service class manages one domain (e.g., `TransactionService` only moves credits; `NotificationService` only handles notifications) |
| **OCP** — Open/Closed | `MatchingStrategy` interface: new strategies added by implementing interface, zero modification to existing code |
| **LSP** — Liskov Substitution | All `MatchingStrategy` implementations substitutable in `MatchingService`; `UserDetails` extended by `CustomUserDetailsService` |
| **DIP** — Dependency Inversion | `SecurityConfig` depends on `UserDetailsService` abstraction; `MatchingService` depends on `Map<String, MatchingStrategy>` abstraction |

---

## Architecture

```
com.skillbarter/
├── SkillBarterApplication.java
├── config/
│   ├── DataInitializer.java          ← Demo data seeding
│   ├── GlobalModelAdvice.java
│   ├── OpenApiConfig.java
│   ├── SecurityConfig.java
│   └── WebMvcConfig.java
├── controller/                        ← MVC Controllers (HTTP layer)
│   ├── ActivityFeedController.java
│   ├── AdminAnalyticsController.java
│   ├── AdminController.java
│   ├── AdvancedSearchController.java
│   ├── AiChatController.java
│   ├── AnalyticsController.java
│   ├── AuthController.java
│   ├── CreditGiftController.java
│   ├── DashboardController.java
│   ├── DisputeController.java
│   ├── EndorsementController.java
│   ├── EnhancedNotificationController.java
│   ├── LearningPathController.java
│   ├── NotificationApiController.java
│   ├── NotificationController.java
│   ├── PasswordResetController.java
│   ├── ProfileCompletenessController.java
│   ├── ProfileController.java
│   ├── RealTimeStatsController.java
│   ├── RecommendationController.java
│   ├── ReferralController.java
│   ├── ReportExportController.java
│   ├── SessionController.java
│   ├── SessionNotesController.java
│   ├── SkillController.java
│   ├── SkillRequestController.java
│   ├── SkillSwapController.java
│   ├── TeacherAvailabilityController.java
│   ├── WalletController.java
│   └── WishlistController.java
├── service/                           ← Business logic layer
│   ├── ActivityFeedService.java
│   ├── AdvancedSearchService.java
│   ├── ChartDataService.java
│   ├── CreditExpiryService.java
│   ├── CreditGiftService.java
│   ├── DisputeService.java
│   ├── EmailService.java
│   ├── LearningPathService.java
│   ├── MeetingLinkService.java
│   ├── NotificationService.java       ← Observer listener
│   ├── PasswordResetService.java
│   ├── PdfReceiptService.java
│   ├── ProfileCompletenessService.java
│   ├── RealTimeStatsService.java
│   ├── RecommendationService.java
│   ├── ReferralService.java
│   ├── ReportExportService.java
│   ├── ResponseTimeService.java
│   ├── ReviewService.java
│   ├── SessionNotesService.java
│   ├── SessionReminderService.java
│   ├── SessionService.java
│   ├── SkillRequestService.java
│   ├── SkillService.java
│   ├── SkillSwapService.java
│   ├── TeacherAvailabilityService.java
│   ├── TransactionService.java        ← Escrow engine
│   ├── UserService.java
│   └── WalletService.java
├── entity/                            ← JPA entities (Model)
│   ├── Badge.java
│   ├── CreditGift.java
│   ├── CreditTransaction.java
│   ├── Dispute.java
│   ├── LearningPath.java
│   ├── LearningPathStep.java
│   ├── Notification.java
│   ├── ReferralCode.java
│   ├── Review.java
│   ├── Session.java
│   ├── SessionMessage.java
│   ├── SessionNotes.java
│   ├── Skill.java
│   ├── SkillEndorsement.java
│   ├── SkillRequest.java
│   ├── SkillSwap.java
│   ├── TeacherAvailability.java
│   ├── Transaction.java
│   ├── User.java
│   └── WishlistItem.java
├── repository/                        ← Spring Data JPA interfaces (19 repos)
├── dto/                               ← Form/request DTOs
├── enums/                             ← All enumerations
├── exception/                         ← Custom exceptions + global handler
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
├── templates/                         ← Thymeleaf Views
│   ├── layout.html
│   ├── activity/feed.html
│   ├── admin/analytics.html
│   ├── admin/dashboard.html
│   ├── admin/realtime-stats.html
│   ├── auth/login.html
│   ├── auth/register.html
│   ├── auth/password-reset.html
│   ├── availability/list.html
│   ├── dashboard/home.html
│   ├── gifts/send.html
│   ├── learning-paths/list.html
│   ├── notifications/center.html
│   ├── profile/view.html
│   ├── recommendations/index.html
│   ├── referrals/dashboard.html
│   ├── sessions/list.html
│   ├── skill-requests/board.html
│   ├── skill-swaps/list.html
│   └── skills/browse.html
└── static/
    ├── css/main.css
    ├── css/dark-mode.css
    └── js/app.js

docs/
├── diagrams.puml    ← All 10 PlantUML UML diagrams
└── schema.sql       ← Full DDL schema
```

---

## Setup & Run

### Prerequisites
- Java 17+ (tested with JDK 25.0.2)
- Maven 3.8+
- MySQL 8.0+

### Steps

```bash
# 1. Clone
git clone https://github.com/VedanthRai/SkillBarter.git
cd SkillBarter/skillbarter

# 2. Create MySQL database
mysql -u root -p -e "CREATE DATABASE skillbarter_db;"

# 3. Configure credentials
#    Edit src/main/resources/application.properties:
#    spring.datasource.password=YOUR_MYSQL_PASSWORD

# 4. Run (Hibernate auto-creates all tables)
mvn spring-boot:run

# Or with specific Java version:
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.2"; mvn spring-boot:run

# 5. Open browser
open http://localhost:8081
```

> Demo data (users, skills, sessions, notifications, referral codes) is seeded automatically on first startup.

---

## Default Credentials

| Role | Email | Password | Description |
|------|-------|----------|-------------|
| Admin | admin@skillbarter.app | Admin@1234 | Full platform access |
| Verifier | verifier@skillbarter.app | Verify@1234 | Dispute resolution |
| User | alice@example.com | Test@1234 | Python/Java teacher |
| User | bob@example.com | Test@1234 | Music teacher |
| User | charlie@example.com | Test@1234 | Language teacher |

> Change all passwords before any deployment.

---

## UML Diagrams

All diagrams are in `docs/diagrams.puml`. Render with:
- [PlantUML online](https://www.plantuml.com/plantuml/uml/)
- IntelliJ IDEA PlantUML plugin
- VS Code PlantUML extension

| Diagram | Type | Description |
|---------|------|-------------|
| UseCaseDiagram | Use Case | All actors and system goals |
| ClassDiagram | Class | Full entity + relationship map |
| StateDiagram_Transaction | State | Escrow state machine |
| StateDiagram_Session | State | Session lifecycle |
| StateDiagram_User | State | Account status machine |
| StateDiagram_Dispute | State | Tribunal state machine |
| ActivityDiagram_Login | Activity | Login flow with security checks |
| ActivityDiagram_BookSession | Activity | Full session booking workflow |
| ActivityDiagram_CompleteTransaction | Activity | Escrow release + gamification |
| ActivityDiagram_ResolveDispute | Activity | Dispute tribunal workflow |

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Redirect to dashboard or login |
| GET/POST | `/auth/login` | Login page |
| GET/POST | `/auth/register` | Registration page |
| GET | `/dashboard` | Main dashboard |
| GET | `/skills/browse` | Browse all skills |
| GET | `/skills/search?q=python` | Search skills |
| GET | `/sessions` | My sessions |
| GET | `/wallet` | Credit wallet |
| GET | `/notifications` | Notification center |
| GET | `/referrals` | Referral dashboard |
| GET | `/recommendations` | AI recommendations |
| GET | `/activity` | Social activity feed |
| GET | `/skill-swaps` | Skill swap proposals |
| GET | `/learning-paths` | Learning paths |
| GET | `/availability` | Teacher availability |
| GET | `/admin/dashboard` | Admin panel (ROLE_ADMIN) |
| GET | `/admin/analytics` | Analytics charts (ROLE_ADMIN) |
| GET | `/admin/realtime-stats` | Live stats (ROLE_ADMIN) |

---

## Project Stats

| Metric | Count |
|--------|-------|
| Java source files | 143 |
| Thymeleaf templates | 40+ |
| JPA Repositories | 19 |
| REST/MVC Controllers | 29 |
| Service classes | 28 |
| Entity classes | 19 |
| Design Patterns | 4 (Strategy, Observer, Builder, Decorator) |
| SOLID Principles | 4 (SRP, OCP, LSP, DIP) |
| UML Diagrams | 10 |
| Total features | 75+ |

---

## Team Roles (UE23CS352B)

| Member | Role | Features |
|--------|------|---------|
| Member 1 | Auth & Profile | Registration, login, profile, leaderboard (Decorator Pattern) |
| Member 2 | Skill Management | Skill CRUD, smart matching, wishlist (Strategy Pattern) |
| Member 3 | Session & Escrow | Session booking, credit transactions, PDF receipts (Builder Pattern) |
| Member 4 | Disputes & Admin | Dispute resolution, admin dashboard, notifications (Observer Pattern) |

---

## Build Status

```
[INFO] BUILD SUCCESS
[INFO] Compiling 143 source files
[INFO] Total time: ~7s
```

All 143 Java files compile and the application starts successfully on port 8081.
