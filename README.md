# 🍽️ Gourmet-Go Distributed Order System

A production-like distributed system implementing the **Saga Orchestration Pattern** with compensation logic.

## Architecture

```
┌─────────────┐     REST      ┌──────────────────────┐
│   Frontend  │ ──────────── ▶│  Order-Orchestrator   │
│  (nginx:80) │               │   (Saga Coordinator)  │
└─────────────┘               └──────────┬────────────┘
                                         │ gRPC
                    ┌────────────────────┼────────────────────┐
                    ▼                    ▼                     ▼
           ┌──────────────┐   ┌──────────────────┐  ┌──────────────────┐
           │ Order-Service│   │ Kitchen-Service  │  │Accounting-Service│
           │  (gRPC:9091) │   │  (gRPC:9092)     │  │  (gRPC:9093)     │
           └──────┬───────┘   └────────┬─────────┘  └────────┬─────────┘
                  │                    │                       │
           ┌──────▼───────┐   ┌────────▼─────────┐  ┌────────▼─────────┐
           │   order-db   │   │   kitchen-db     │  │  accounting-db   │
           │ (PostgreSQL) │   │  (PostgreSQL)    │  │  (PostgreSQL)    │
           └──────────────┘   └──────────────────┘  └──────────────────┘
```

## Saga Flow

### ✅ Happy Path (amount ≤ 1000€)
```
UI → POST /api/orders
  → Create Order (APPROVAL_PENDING)
  → Create Kitchen Ticket
  → Authorize Payment
  → Update Order (APPROVED) ✅
```

### ❌ Compensation Path (amount > 1000€)
```
UI → POST /api/orders
  → Create Order (APPROVAL_PENDING)
  → Create Kitchen Ticket ✓
  → Authorize Payment → FAILS
  → [COMPENSATION] Cancel Kitchen Ticket
  → [COMPENSATION] Update Order (REJECTED) ❌
```

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Git

### 1. Clone and configure
```bash
git clone https://github.com/YOUR_USERNAME/gourmet-go.git
cd gourmet-go
```

### 2. Set your Docker Hub username
```bash
export DOCKER_USERNAME=your_dockerhub_username
```

### 3. Start everything
```bash
docker-compose up -d
```

### 4. Open the UI
```
http://localhost:3000
```

### 5. Test Happy Path
- Order ID: `ORDER-001`, Amount: `45.00` → Should be **APPROVED**

### 6. Test Compensation Path
- Order ID: `ORDER-002`, Amount: `1500.00` → Should be **REJECTED**

## CI/CD Setup (GitHub Actions)

Add these secrets to your GitHub repository:
- `DOCKER_USERNAME` — your Docker Hub username
- `DOCKER_PASSWORD` — your Docker Hub password/token

Pipeline runs on every push to `main`:
1. Build all services (Maven)
2. Package JARs
3. Build Docker images
4. Push to Docker Hub

## Service Ports

| Service | HTTP | gRPC |
|---|---|---|
| Frontend | 3000 | — |
| Order-Orchestrator | 8080 | — |
| Order-Service | 8081 | 9091 |
| Kitchen-Service | 8082 | 9092 |
| Accounting-Service | 8083 | 9093 |
| order-db | 5432 | — |
| kitchen-db | 5433 | — |
| accounting-db | 5434 | — |

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.2, gRPC (grpc-spring-boot-starter)
- **Database**: PostgreSQL 15 (one per service)
- **Frontend**: HTML/JS + Nginx
- **Containerization**: Docker + Docker Compose
- **CI/CD**: GitHub Actions
