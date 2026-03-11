# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Java Knowledge Review Demo** project containing educational code examples for Java backend development. It includes 47 knowledge point files covering Java fundamentals, design patterns, frameworks, databases, and system architecture.

## Build Commands

```bash
# Compile project
mvn compile

# Run tests
mvn test

# Package
mvn package

# Clean build
mvn clean compile
```

Note: This project requires Java 8. Set JAVA_HOME appropriately:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
```

## Code Architecture

The codebase is organized by knowledge domain under `src/main/java/com/example/demo/`:

```
├── javabase/           # Java fundamentals
│   ├── designpatterns/ # 10 design patterns (Singleton, Factory, Strategy, etc.)
│   ├── concurrency/    # Thread safety, Thread pools
│   ├── collections/    # Collection framework
│   ├── jvm/           # JVM memory model, Class loading
│   └── java8/         # Java 8+ features (Lambda, Stream, Optional)
│
├── framework/          # Framework knowledge
│   ├── spring/        # IoC/DI, AOP, Transactions, Security
│   ├── springboot/    # Auto-configuration
│   ├── springcloud/   # Service discovery, Load balancer, Circuit breaker, Gateway, Feign
│   └── mybatis/       # MyBatis core concepts
│
├── database/           # Database knowledge
│   ├── mysql/         # Indexes, MVCC, Transactions, Sharding, Connection pool
│   ├── redis/         # Data structures, High availability
│   ├── distributed/   # Distributed lock, Distributed transaction
│   └── mq/            # RocketMQ, Kafka, Message reliability
│
├── csbasics/           # Computer science fundamentals
│   ├── algorithm/     # Sorting algorithms
│   ├── datastructure/ # HashMap source, Advanced data structures
│   └── network/       # TCP/HTTP, Netty/NIO
│
└── architecture/       # System design
    ├── SystemDesign   # Cache problems, Distributed ID, Seckill system
    ├── DistributedTracing  # Tracing concepts (SkyWalking, Zipkin)
    └── DockerKubernetes    # Container & orchestration
```

## Code Style Notes

- All code comments are in Chinese
- Each file contains ASCII diagrams for visual explanation
- Most files have a `main()` method for demonstration
- Example code in comments uses `/* ... */` blocks (not executable)
- Comment blocks at file top explain core concepts with tables

## Dependencies

- **Java 8** (source/target 1.8)
- **Spring 5.3.x** + **Spring Boot 2.7.x** (for framework examples)
- **Lombok** (optional, for code simplification)
- **JUnit 5** (for testing)
- **SLF4J + Logback** (for logging)

## Key Patterns

1. **Knowledge files** are self-contained educational modules
2. **Commented examples** show usage patterns without actual implementation
3. **Static inner classes** demonstrate concepts within single files
4. **ASCII art diagrams** visualize architecture and data structures
