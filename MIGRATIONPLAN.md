# Migration Plan: Java 7 to Java 21 LTS + Spring Batch Refactoring

## Stakeholder Decisions (Confirmed)

| Decision | Answer |
|----------|--------|
| Java Version | **Java 21** (Latest LTS) |
| Spring Framework Approach | **Spring Batch** |
| Application Type | **CLI** (Keep CLI interface) |
| CLI Breaking Changes | **Allowed** (dataflow must work as-is) |
| Deployment | **Docker Image** |

## Executive Summary

This document outlines the migration strategy for upgrading the invoice processing application from Java 7 to Java 21 LTS and refactoring it to use Spring Batch. The application will be packaged and delivered as a Docker image.

## Current State Analysis

### Technology Stack
- **Java Version**: 1.7 (Released 2011, EOL 2015)
- **Build Tool**: Maven 3
- **Testing Framework**: JUnit 4.12
- **Dependencies**: Minimal (only JUnit)
- **Architecture**: Standalone application with Parser → Processor → Writer pipeline

### Project Structure
```
src/main/java/it/slager/exercises/invoicing/
├── model/
│   └── ReceiptItem.java
├── parser/
│   ├── ReceiptLineParser.java
│   ├── ReceiptLineParserState.java
│   └── ReceiptParseException.java
├── processor/
│   └── ReceiptItemTaxEvaluator.java
└── printer/
    ├── InvoicePrinter.java
    └── PrinterState.java
```

### Key Features
- Sales tax calculation with special rules for imported goods
- Receipt parsing from text format
- Invoice printing with totals and tax summaries
- Clean separation of concerns (Parser, Processor, Writer)
- Comprehensive test coverage

## Migration Strategy

### Phase 1: Java Version Upgrade (Java 7 → Java 21)

#### Step 1.1: Update Build Configuration
**Priority**: HIGH | **Risk**: LOW | **Estimated Time**: 30 minutes

**Actions**:
1. Update `pom.xml`:
   - Set `maven.compiler.source` and `maven.compiler.target` to `21`
   - Update Maven plugin versions to Java 21 compatible versions:
     - maven-compiler-plugin → 3.11.0+
     - maven-surefire-plugin → 3.2.5+
     - maven-failsafe-plugin → 3.2.5+
2. Update `.travis.yml` to use Java 21
3. Update `Jenkinsfile` to use Java 21

**Verification**:
- `mvn clean compile` succeeds
- `mvn test` passes all tests
- `mvn verify` runs integration tests successfully

#### Step 1.2: Update Dependencies
**Priority**: HIGH | **Risk**: LOW | **Estimated Time**: 20 minutes

**Actions**:
1. Upgrade JUnit 4.12 → JUnit 5.10.x (Jupiter)
   - Update test annotations (@Test, @Before → @BeforeEach, etc.)
   - Update assertions (Assert.assertEquals → Assertions.assertEquals)
   - Update parameterized tests syntax
2. Add SLF4J + Logback for logging (prepare for Spring)

**Verification**:
- All tests pass with new JUnit version
- No deprecation warnings during compilation

#### Step 1.3: Code Modernization
**Priority**: MEDIUM | **Risk**: LOW | **Estimated Time**: 1 hour

**Optional Improvements** (can be deferred):
- Use try-with-resources for AutoCloseable resources
- Replace StringBuffer/StringTokenizer with modern alternatives where appropriate
- Add `@Override` annotations where missing
- Consider using var for local variables (Java 10+)
- Consider using Records for immutable data classes (Java 14+)

**Verification**:
- Code compiles without warnings
- All tests pass
- Code style remains consistent

### Phase 2: Spring Batch Integration

#### Step 2.1: Spring Boot + Spring Batch Setup
**Priority**: HIGH | **Risk**: MEDIUM | **Estimated Time**: 2 hours

**Approach**: Implement using **Spring Batch** to leverage batch processing capabilities.

**Benefits of Spring Batch**:
- Built-in support for chunk-oriented processing (Reader → Processor → Writer)
- Job monitoring and restart capabilities
- Transaction management
- Scalability options for future growth
- Industry standard for batch processing

**Actions**:
1. Add Spring Boot and Spring Batch dependencies:
   - spring-boot-starter-batch
   - spring-boot-starter-test
   - h2 (for batch job metadata storage)
2. Create Spring Boot main application class
3. Add `application.properties` / `application.yml` configuration

**Verification**:
- Spring Boot application starts successfully
- Batch job metadata is stored correctly

#### Step 2.2: Implement Spring Batch Job
**Priority**: HIGH | **Risk**: MEDIUM | **Estimated Time**: 3 hours

**Actions**:
1. Create batch job configuration:
   - **ItemReader**: `FlatFileItemReader` to read receipt lines from input file
   - **ItemProcessor**: Wrap `ReceiptLineParser` and `ReceiptItemTaxEvaluator` 
   - **ItemWriter**: Wrap `InvoicePrinter` for output
2. Configure job steps and chunk size
3. Externalize configuration:
   - Move hardcoded values (tax rates, keywords) to `application.properties`
   - Use `@Value` or `@ConfigurationProperties` for injection
4. Add job parameters for input/output file paths

**Batch Job Flow**:
```
┌─────────────────────────────────────────────────────────────┐
│                    Invoice Processing Job                    │
├─────────────────────────────────────────────────────────────┤
│  Step: processInvoices                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  ItemReader  │→│ItemProcessor │→│  ItemWriter  │       │
│  │  (File Read) │  │ (Parse+Tax)  │  │   (Print)    │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

**Verification**:
- Batch job executes successfully
- Same output as original application
- Configuration properties inject properly

#### Step 2.3: Update Testing Strategy
**Priority**: HIGH | **Risk**: LOW | **Estimated Time**: 1.5 hours

**Actions**:
1. Convert tests to use Spring Test context:
   - Add `@SpringBootTest` or `@ExtendWith(SpringExtension.class)`
   - Use `@MockBean` where appropriate
2. Keep unit tests as plain JUnit tests (no Spring context needed)
3. Update integration test to use Spring Boot test slices

**Verification**:
- All unit tests pass
- Integration tests pass with Spring context
- Test execution time remains reasonable

#### Step 2.4: CLI Interface
**Priority**: HIGH | **Risk**: LOW | **Estimated Time**: 1 hour

**Actions**:
1. Implement `CommandLineRunner` or use Spring Boot's command-line argument support
2. Accept input file path as command-line argument
3. Support output redirection or file output
4. Maintain dataflow compatibility (input format → output format)

**CLI Usage**:
```bash
# Run with Docker
docker run -v /path/to/data:/data invoice-processor /data/input.txt

# Or with JAR
java -jar invoice-processor.jar /path/to/input.txt
```

**Verification**:
- CLI accepts input file argument
- Dataflow produces same results as original application
- Works seamlessly in Docker container

### Phase 3: Docker Containerization

#### Step 3.1: Create Dockerfile
**Priority**: HIGH | **Risk**: LOW | **Estimated Time**: 1 hour

**Actions**:
1. Create multi-stage `Dockerfile`:
   - Build stage: Use Maven image to build and test the application
   - Runtime stage: Use slim JRE 21 image for final container
2. Configure entry point for CLI execution
3. Optimize image size

**Dockerfile Structure**:
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Verification**:
- Docker image builds successfully
- Tests pass during image build
- Image size is optimized (target: < 300MB, based on Alpine JRE base ~200MB + app ~50MB)

#### Step 3.2: Docker Compose and CI/CD Integration
**Priority**: MEDIUM | **Risk**: LOW | **Estimated Time**: 1 hour

**Actions**:
1. Create `docker-compose.yml` for local development/testing
2. Update CI/CD pipelines to build and push Docker images
3. Add image tagging strategy (version, latest, commit hash)

**Verification**:
- `docker-compose up` works correctly
- CI/CD pipeline builds and pushes images
- Images are properly tagged

## Risk Assessment

### High Risk Areas
1. **Plugin Compatibility**: Maven plugins may have breaking changes
   - **Mitigation**: Test incrementally, update one plugin at a time
   
2. **JUnit Migration**: Test framework changes require code updates
   - **Mitigation**: Keep backup of tests, update systematically

3. **Spring Integration**: Dependency injection may affect existing flow
   - **Mitigation**: Maintain backward compatibility, add Spring gradually

### Low Risk Areas
1. **Business Logic**: Core calculation logic remains unchanged
2. **Data Model**: Simple POJOs don't require changes
3. **Algorithm**: Tax calculation algorithm stays the same

## Testing Strategy

### Test Coverage Maintenance
1. Ensure all existing tests pass after each phase
2. Add new tests for Spring-specific functionality
3. Maintain or improve code coverage metrics

### Testing Levels
1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test Spring context and component interaction
3. **End-to-End Tests**: Test complete pipeline with sample data

## Rollback Strategy

### Phase 1 Rollback
- Revert `pom.xml` changes
- Restore original plugin versions
- Rebuild with Java 7

### Phase 2 Rollback
- Remove Spring Batch dependencies from `pom.xml`
- Delete Spring configuration files
- Remove Spring annotations from code
- Restore original implementation

### Phase 3 Rollback
- Delete Dockerfile and docker-compose.yml
- Remove Docker-related CI/CD configurations
- Revert to JAR-based deployment

## Timeline Estimation

| Phase | Duration | Can Start After |
|-------|----------|-----------------|
| Phase 1.1: Build Config | 30 min | Immediate |
| Phase 1.2: Dependencies | 20 min | Phase 1.1 |
| Phase 1.3: Code Modernization | 1 hour | Phase 1.2 |
| Phase 2.1: Spring Batch Setup | 2 hours | Phase 1.3 |
| Phase 2.2: Batch Job Implementation | 3 hours | Phase 2.1 |
| Phase 2.3: Testing | 1.5 hours | Phase 2.2 |
| Phase 2.4: CLI Interface | 1 hour | Phase 2.3 |
| Phase 3.1: Dockerfile | 1 hour | Phase 2.4 |
| Phase 3.2: Docker CI/CD | 1 hour | Phase 3.1 |
| **Total** | **~12 hours** | |

## ~~Questions for Stakeholders~~ Decisions Made

| Question | Decision |
|----------|----------|
| Java Version | ✅ **Java 21** |
| Spring Framework Approach | ✅ **Spring Batch** |
| Application Type | ✅ **CLI** |
| Backward Compatibility | ✅ **CLI breaking changes OK, dataflow must work as-is** |
| Deployment Target | ✅ **Docker Image** |

## Success Criteria

### Phase 1 Success
- ✅ Application builds with Java 21
- ✅ All existing tests pass
- ✅ No new bugs introduced
- ✅ CI/CD pipeline updated and passing

### Phase 2 Success
- ✅ Spring Batch job runs successfully
- ✅ All components integrated with Spring Batch
- ✅ Configuration externalized
- ✅ All tests pass with Spring context
- ✅ Dataflow produces same results as original
- ✅ CLI interface works correctly

### Phase 3 Success
- ✅ Docker image builds successfully
- ✅ Container runs and processes files correctly
- ✅ CI/CD pipeline builds and pushes images
- ✅ Image size is optimized

## Post-Migration Tasks

1. Update README.md with new requirements and setup instructions
2. Update CI/CD pipelines (Travis CI, Jenkins) for Docker builds
3. Document new configuration options
4. Create Docker Hub or container registry setup
5. Add container health checks
6. Document Docker usage and deployment instructions
7. Consider adding:
   - Kubernetes deployment manifests
   - Helm charts
   - Performance benchmarks

## Conclusion

This migration plan provides a structured approach to modernizing the invoice processing application. Based on stakeholder decisions, the application will:
- Upgrade to **Java 21** (latest LTS)
- Use **Spring Batch** for robust batch processing
- Maintain **CLI** interface
- Be delivered as a **Docker image**

The phased approach allows for incremental changes with validation at each step, minimizing risk while maximizing benefits of modern Java features and Spring Batch capabilities.

**Next Steps**:
1. ~~Review and approve this migration plan~~ ✅ Approved
2. Execute Phase 1 (Java 21 upgrade)
3. Execute Phase 2 (Spring Batch integration)
4. Execute Phase 3 (Docker containerization)
5. Verify dataflow produces same results
