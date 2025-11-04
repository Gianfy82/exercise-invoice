# Migration Plan: Java 7 to Java 21 LTS + Spring Framework Refactoring

## Executive Summary

This document outlines the migration strategy for upgrading the invoice processing application from Java 7 to Java 21 LTS and refactoring it to use the Spring Framework.

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

### Phase 2: Spring Framework Integration

#### Step 2.1: Spring Boot Setup
**Priority**: HIGH | **Risk**: MEDIUM | **Estimated Time**: 2 hours

**Decision Point**: Choose between:
1. **Spring Boot with Spring Batch** (Recommended for batch processing)
2. **Spring Boot with custom pipeline** (Simpler, closer to current design)

**Recommendation**: Start with **Spring Boot + Custom Pipeline**, then evaluate Spring Batch if needed.

**Rationale**:
- Current application is lightweight and well-structured
- Spring Batch adds complexity that may not be necessary for this use case
- Easier to maintain existing test coverage
- Can migrate to Spring Batch later if requirements change

**Actions**:
1. Add Spring Boot starter dependencies:
   - spring-boot-starter (core)
   - spring-boot-starter-test (testing)
2. Create Spring Boot main application class
3. Add `application.properties` / `application.yml` configuration

**Verification**:
- Spring Boot application starts successfully
- Dependency injection works

#### Step 2.2: Convert Components to Spring Beans
**Priority**: HIGH | **Risk**: MEDIUM | **Estimated Time**: 2 hours

**Actions**:
1. Add `@Component` annotations:
   - `ReceiptLineParser` → `@Component`
   - `ReceiptItemTaxEvaluator` → `@Component`
   - `InvoicePrinter` → `@Component`
2. Externalize configuration:
   - Move hardcoded values (tax rates, keywords) to `application.properties`
   - Use `@Value` or `@ConfigurationProperties` for injection
3. Add constructor-based dependency injection
4. Create service layer if needed:
   - `InvoiceProcessingService` to orchestrate the pipeline

**Verification**:
- Components auto-wire correctly
- Configuration properties inject properly
- Integration tests pass

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

#### Step 2.4: Add Spring Boot Features (Optional)
**Priority**: LOW | **Risk**: LOW | **Estimated Time**: 2-3 hours

**Optional Enhancements**:
1. Add REST API endpoint:
   - POST `/api/invoices/process` to process receipts
   - GET `/api/invoices/{id}` to retrieve processed invoices
2. Add CommandLineRunner for existing CLI functionality
3. Add Actuator for health checks and metrics
4. Add validation with `spring-boot-starter-validation`
5. Add OpenAPI/Swagger documentation

**Verification**:
- API endpoints respond correctly
- CLI runner works as before
- Documentation is accessible

### Phase 3: Spring Batch Integration (Optional - Future Enhancement)

**When to Consider**:
- Processing large volumes of receipts from files
- Need for job scheduling and monitoring
- Requirement for chunk processing and retry logic
- Need for job persistence and restart capability

**High-Level Approach** (if needed):
1. Add `spring-boot-starter-batch` dependency
2. Create batch job configuration:
   - **ItemReader**: Read receipt lines from file
   - **ItemProcessor**: Parse and evaluate taxes
   - **ItemWriter**: Print to output
3. Add job launcher and scheduling
4. Add job repository configuration

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
- Remove Spring dependencies from `pom.xml`
- Delete Spring configuration files
- Remove Spring annotations from code
- Restore constructor parameters

## Timeline Estimation

| Phase | Duration | Can Start After |
|-------|----------|-----------------|
| Phase 1.1: Build Config | 30 min | Immediate |
| Phase 1.2: Dependencies | 20 min | Phase 1.1 |
| Phase 1.3: Code Modernization | 1 hour | Phase 1.2 |
| Phase 2.1: Spring Boot Setup | 2 hours | Phase 1.3 |
| Phase 2.2: Spring Beans | 2 hours | Phase 2.1 |
| Phase 2.3: Testing | 1.5 hours | Phase 2.2 |
| Phase 2.4: Optional Features | 2-3 hours | Phase 2.3 |
| **Total (Core)** | **~9 hours** | |
| **Total (with Optional)** | **~12 hours** | |

## Questions for Stakeholders

1. **Java Version**: Should we target Java 21 (latest LTS) or Java 17 (more conservative)?
   - **Recommendation**: Java 21 for long-term support and modern features

2. **Spring Framework Approach**: Spring Boot with custom pipeline or Spring Batch?
   - **Recommendation**: Start with custom pipeline, evaluate Spring Batch later

3. **Application Type**: Should this remain a CLI application or add REST API?
   - **Recommendation**: Keep CLI primary, REST API as optional enhancement

4. **Backward Compatibility**: Should the application maintain the same CLI interface?
   - **Recommendation**: Yes, maintain existing interface

5. **Deployment Target**: Where will this application be deployed?
   - **Impact**: Affects containerization and packaging decisions

6. **Breaking Changes**: Are breaking changes acceptable during migration?
   - **Recommendation**: Minimize breaking changes, especially in public API

## Success Criteria

### Phase 1 Success
- ✅ Application builds with Java 21
- ✅ All existing tests pass
- ✅ No new bugs introduced
- ✅ CI/CD pipeline updated and passing

### Phase 2 Success
- ✅ Spring Boot application runs successfully
- ✅ All components managed by Spring
- ✅ Configuration externalized
- ✅ All tests pass with Spring context
- ✅ Same functionality as before migration
- ✅ Documentation updated

## Post-Migration Tasks

1. Update README.md with new requirements and setup instructions
2. Update CI/CD pipelines (Travis CI, Jenkins)
3. Document new configuration options
4. Create migration guide for other developers
5. Update SonarQube configuration if needed
6. Consider adding:
   - Docker support
   - Kubernetes deployment manifests
   - Performance benchmarks

## Conclusion

This migration plan provides a structured approach to modernizing the invoice processing application. The phased approach allows for incremental changes with validation at each step, minimizing risk while maximizing benefits of modern Java features and Spring Framework capabilities.

**Recommended Next Steps**:
1. Review and approve this migration plan
2. Address any questions or concerns
3. Execute Phase 1 (Java 21 upgrade)
4. Evaluate results before proceeding to Phase 2
5. Make decision on Spring Batch vs. custom pipeline based on requirements
