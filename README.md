# 🍽️ Gourmet-Go — Distributed Order Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=for-the-badge&logo=springboot)
![gRPC](https://img.shields.io/badge/gRPC-Protobuf-blue?style=for-the-badge&logo=google)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql)
![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-black?style=for-the-badge&logo=githubactions)

[![CI/CD Pipeline](https://github.com/louaymdimagh/gourmet-go/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/louaymdimagh/gourmet-go/actions/workflows/ci-cd.yml)
[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-louaymdimagh-2496ED?logo=docker)](https://hub.docker.com/u/louaymdimagh)

> **A production-grade distributed system** implementing the **Saga Orchestration Pattern** with full compensation logic, gRPC inter-service communication, and automated CI/CD.

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#️-architecture)
- [Saga Pattern](#-saga-pattern)
- [Services](#-services)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [API Reference](#-api-reference)
- [CI/CD Pipeline](#️-cicd-pipeline)
- [Docker Hub Images](#-docker-hub-images)
- [Service Ports](#-service-ports)

---

## 🎯 Overview

Gourmet-Go simulates a **restaurant order management system** built with a **microservices architecture**. Each service owns its data, communicates via **gRPC**, and is coordinated by a central **Saga Orchestrator** to ensure data consistency across distributed transactions.

**Key Design Principles:**
- ✅ **Database per Service** — no shared state between microservices
- ✅ **Saga Orchestration** — centralized coordination with compensation logic
- ✅ **gRPC Communication** — high-performance, strongly-typed inter-service calls
- ✅ **Containerized** — fully Dockerized, one command to run everything
- ✅ **Automated CI/CD** — build, test, and push to Docker Hub on every push

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER BROWSER                             │
└────────────────────────────┬────────────────────────────────────┘
                             │  HTTP/REST
                             ▼  (port 3000)
                   ┌─────────────────┐
                   │    Frontend     │
                   │  HTML/JS/Nginx  │
                   └────────┬────────┘
                            │  HTTP/REST (port 8080)
                            ▼
                   ┌─────────────────────────┐
                   │    Order-Orchestrator   │
                   │    (Saga Coordinator)   │
                   └───┬──────────┬──────────┘
                       │ gRPC     │ gRPC      │ gRPC
              (9091)   ▼ (9092)   ▼  (9093)   ▼
           ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐
           │ Order-Service│ │Kitchen-Service│ │Accounting-Service│
           └──────┬───────┘ └──────┬───────┘ └────────┬─────────┘
                  │                │                   │
           ┌──────▼──────┐  ┌──────▼──────┐   ┌───────▼──────┐
           │  order-db   │  │ kitchen-db  │   │ accounting-db│
           │ (PostgreSQL)│  │ (PostgreSQL)│   │ (PostgreSQL) │
           └─────────────┘  └─────────────┘   └──────────────┘

                   ┌─────────────────────────┐
                   │  Zipkin (port 9411)     │  ← Distributed Tracing
                   └─────────────────────────┘
```

---

## 🔄 Saga Pattern

The **Saga Orchestration Pattern** manages distributed transactions across multiple services without a global database transaction. The `Order-Orchestrator` acts as the single coordinator.

### ✅ Happy Path — Amount ≤ 1000€

```
Client → POST /api/orders
  │
  ├─▶ [1] Order-Service    : Create Order → Status: APPROVAL_PENDING
  ├─▶ [2] Kitchen-Service  : Create Kitchen Ticket ✓
  ├─▶ [3] Accounting-Service: Authorize Payment ✓
  └─▶ [4] Order-Service    : Update Order → Status: APPROVED ✅
```

### ❌ Compensation Path — Amount > 1000€

```
Client → POST /api/orders
  │
  ├─▶ [1] Order-Service     : Create Order → Status: APPROVAL_PENDING
  ├─▶ [2] Kitchen-Service   : Create Kitchen Ticket ✓
  ├─▶ [3] Accounting-Service: Authorize Payment → REJECTED ❌ (amount > 1000€)
  │         ↓ Compensation triggered
  ├─▶ [4] Kitchen-Service   : Cancel Kitchen Ticket (rollback)
  └─▶ [5] Order-Service     : Update Order → Status: REJECTED ❌
```

> The compensation steps execute in **reverse order**, ensuring the system returns to a consistent state even when partial failures occur.

---

## 🧩 Services

### 🎼 Order-Orchestrator
The central coordinator. Implements the Saga workflow and drives all other services. Exposes a REST API for the frontend and calls downstream services via gRPC.

### 📦 Order-Service
Manages order lifecycle. Persists orders in PostgreSQL. States: `APPROVAL_PENDING` → `APPROVED` | `REJECTED`.

### 👨‍🍳 Kitchen-Service
Manages kitchen tickets. Creates a ticket when an order is received; cancels it if payment fails.

### 💳 Accounting-Service
Handles payment authorization. Business rule: **orders > 1000€ are automatically rejected**.

### 🖥️ Frontend
A lightweight HTML/JS single-page app served by Nginx. Provides:
- Order submission form
- Real-time order status table (APPROVED / REJECTED / PENDING)

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Inter-service communication | gRPC + Protocol Buffers |
| Database | PostgreSQL 15 (one per service) |
| Frontend | HTML5 / JavaScript / Nginx |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Image Registry | Docker Hub |
| Observability | Zipkin (Distributed Tracing) |

---

## 🚀 Quick Start

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- Git

### 1. Clone the repository
```bash
git clone https://github.com/louaymdimagh/gourmet-go.git
cd gourmet-go
```

### 2. Configure environment
```bash
# Windows PowerShell
$env:DOCKER_USERNAME="louaymdimagh"

# Linux / macOS
export DOCKER_USERNAME=louaymdimagh
```

### 3. Start all services
```bash
docker-compose up -d
```

### 4. Open the UI
```
http://localhost:3000
```

### 5. Test the system

**Happy Path (APPROVED):**
```
Order ID : ORDER-001
Amount   : 45.00
→ Result : APPROVED ✅
```

**Compensation Path (REJECTED):**
```
Order ID : ORDER-002
Amount   : 1500.00
→ Result : REJECTED ❌
```

### 6. View Distributed Traces
```
http://localhost:9411  (Zipkin)
```

### 7. Stop all services
```bash
docker-compose down
```

---

## 📡 API Reference

### POST `/api/orders` — Place an Order

**Request:**
```json
{
  "orderId": "ORDER-001",
  "amount": 45.00
}
```

**Response (Approved):**
```json
{
  "orderId": "ORDER-001",
  "status": "APPROVED"
}
```

**Response (Rejected):**
```json
{
  "orderId": "ORDER-002",
  "status": "REJECTED"
}
```

### GET `/api/orders` — List all Orders

**Response:**
```json
[
  { "orderId": "ORDER-001", "amount": 45.00, "status": "APPROVED" },
  { "orderId": "ORDER-002", "amount": 1500.00, "status": "REJECTED" }
]
```

---

## ⚙️ CI/CD Pipeline

The GitHub Actions pipeline runs automatically on every push to `main`.

```
Push to main
     │
     ├──▶ Build Order Service (Maven)      ─┐
     ├──▶ Build Kitchen Service (Maven)     ├── Parallel Jobs
     ├──▶ Build Accounting Service (Maven)  ┤
     └──▶ Build Orchestrator (Maven)       ─┘
                    │
                    ▼ (all pass)
          ┌──────────────────────┐
          │  Build & Push Docker │
          │  Images to Docker Hub│
          └──────────────────────┘
               │
    ├──▶ louaymdimagh/gourmet-go-order-service:latest
    ├──▶ louaymdimagh/gourmet-go-kitchen-service:latest
    ├──▶ louaymdimagh/gourmet-go-accounting-service:latest
    ├──▶ louaymdimagh/gourmet-go-order-orchestrator:latest
    └──▶ louaymdimagh/gourmet-go-frontend:latest
```

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `DOCKER_USERNAME` | Docker Hub username (`louaymdimagh`) |
| `DOCKER_PASSWORD` | Docker Hub Access Token |

---

## 🐳 Docker Hub Images

All images are publicly available on Docker Hub:

| Image | Link |
|-------|------|
| `louaymdimagh/gourmet-go-order-service` | [↗ Docker Hub](https://hub.docker.com/r/louaymdimagh/gourmet-go-order-service) |
| `louaymdimagh/gourmet-go-kitchen-service` | [↗ Docker Hub](https://hub.docker.com/r/louaymdimagh/gourmet-go-kitchen-service) |
| `louaymdimagh/gourmet-go-accounting-service` | [↗ Docker Hub](https://hub.docker.com/r/louaymdimagh/gourmet-go-accounting-service) |
| `louaymdimagh/gourmet-go-order-orchestrator` | [↗ Docker Hub](https://hub.docker.com/r/louaymdimagh/gourmet-go-order-orchestrator) |
| `louaymdimagh/gourmet-go-frontend` | [↗ Docker Hub](https://hub.docker.com/r/louaymdimagh/gourmet-go-frontend) |

---

## 🔌 Service Ports

| Service | HTTP Port | gRPC Port |
|---------|-----------|-----------|
| Frontend (Nginx) | `3000` | — |
| Order-Orchestrator | `8080` | — |
| Order-Service | `8081` | `9091` |
| Kitchen-Service | `8082` | `9092` |
| Accounting-Service | `8083` | `9093` |
| order-db (PostgreSQL) | `5432` | — |
| kitchen-db (PostgreSQL) | `5433` | — |
| accounting-db (PostgreSQL) | `5434` | — |
| Zipkin | `9411` | — |

---

## 📁 Project Structure

```
gourmet-go/
├── .github/
│   └── workflows/
│       └── ci-cd.yml          # GitHub Actions CI/CD pipeline
├── proto/                     # Shared .proto definitions (gRPC contracts)
├── order-orchestrator/        # Saga coordinator — REST + gRPC client
│   ├── src/
│   └── Dockerfile
├── order-service/             # Order management — gRPC server
│   ├── src/
│   └── Dockerfile
├── kitchen-service/           # Kitchen ticket management — gRPC server
│   ├── src/
│   └── Dockerfile
├── accounting-service/        # Payment authorization — gRPC server
│   ├── src/
│   └── Dockerfile
├── frontend/                  # HTML/JS UI + Nginx
│   ├── index.html
│   └── Dockerfile
├── docker-compose.yml         # Full stack orchestration
└── README.md
```

---

<div align="center">

Made with ❤️ by **Louay Mdimagh**

</div>
