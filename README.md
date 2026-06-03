# Distributed Real-Time Fraud Detection and Velocity Engine
Understood. Here is the revised, strictly professional GitHub README.

**ZBank Distributed Real-Time Fraud Detection & Velocity Engine**
A high-throughput, low-latency transaction processing and fraud mitigation engine designed to evaluate payment requests in real time. Built with Java 21 and Spring Boot 3.x, this system leverages Virtual Threads (Project Loom) for massive concurrency and Redis Pipelining to execute sliding-window velocity checks within single-digit milliseconds, protecting financial infrastructure against rapid-fire card exploitation and velocity attacks.

**Key Features**
**Sliding-Window Velocity Engine:** Utilizes Redis Sorted Sets (ZSET) to evaluate card activity within a rolling 60-second window, instantaneously blocking anomalous transaction bursts.

**Virtual Thread Concurrency Model:** Out-of-the-box configuration for Java 21 Virtual Threads, removing the standard OS thread-per-request limitation and allowing the engine to handle thousands of concurrent API evaluations without thread pool exhaustion.

**Optimized Network I/O via Pipelining:** Batches multiple Redis operations (ZADD, ZREMRANGEBYSCORE, ZCARD, EXPIRE) into a single network round-trip, minimizing network latency at critical decision points.

**Self-Cleaning Architecture:** Automatic Time-To-Live (TTL) enforcement on transactional keys inside Redis to avoid data leaks and safely manage in-memory RAM usage.

**Domain Immutability:** Enforces strict data integrity using Java 21 record representations for incoming transaction payloads, eliminating side-effects across concurrent processing routines.

# Technology Stack
**Technology	Purpose**
**Java 21	**Utilizing modern language features like records, pattern matching, and lightweight Virtual Threads.
**Spring Boot 3.x	**Enterprise framework for configuration, dependency injection, and RESTful API presentation.
**Spring Data Redis**	High-performance, in-memory key-value abstraction layer utilized for the sliding window algorithm.
**Spring Data JPA**	Object-relational mapping for maintaining static transaction rules and audit tables.
**PostgreSQL**	Relational data persistence tier optimized for complex, relational ledger indexing and rule lookup.
**JUnit 5**	Test execution framework driving the system's strict integration and automated regression verification.

# Core Directory Structure
The system implements a structured, clean-architecture framework organized by business domains and infrastructural responsibilities:

fraud-velocity-engine/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── zbank/
    │   │           └── fraud/
    │   │               ├── FraudVelocityEngineApplication.java  # Application Entry Point
    │   │               ├── config/
    │   │               │   └── RedisConfig.java                 # Redis Template Engine Configurations
    │   │               ├── controller/
    │   │               │   └── FraudCheckController.java        # REST API Processing Layer
    │   │               ├── model/
    │   │               │   └── TransactionRequest.java          # Immutable Transaction Domain Records
    │   │               ├── service/
    │   │               │   └── VelocityFraudDetector.java       # Core Sliding Window Logic
    │   │               ├── entity/
    │   │               │   └── StaticFraudRule.java             # Database Persistence Models
    │   │               └── repository/
    │   │                   └── FraudRuleRepository.java         # Relational Storage Query Abstractions
    │   └── resources/
    │       └── application.yml                                  # Virtual Thread and Connection Pools Configuration
    └── test/
        └── java/
            └── com/
                └── zbank/
                    └── fraud/
                        └── service/
                            └── VelocityFraudDetectorTest.java   # Core Engine Integration Validation Suite
# Configuration & Deployment

**Virtual Threads Activation**
The engine optimizes the thread execution model implicitly by running incoming web requests on lightweight fibers rather than platform threads:

YAML File:
spring:
  threads:
    virtual:
      enabled: true
      
# Local Development Setup

1- Clone the repository and navigate to the project directory.

2- Initialize a local Redis cache instance running on port 6379:

docker run -p 6379:6379 -d redis

3- Compile the build artifacts using Maven:

mvn clean install

4- Execute the system validation suite to confirm environment functionality:

mvn test
