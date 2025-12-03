# Security Assessment Report: Recipe API

**Assessment Date:** December 3, 2025
**Application:** Recipe API (Spring Boot 3.2.0)
**Assessor:** Security Review (10 years SaaS security experience)
**Scope:** Java source code, dependencies, configuration

---

## Executive Summary

This security assessment identified **4 Critical**, **4 High**, and **5 Medium** severity vulnerabilities in the Recipe API application. The most significant findings are the complete absence of authentication/authorization and multiple vulnerable dependencies requiring immediate upgrade.

| Severity | Count | Key Issues |
|----------|-------|------------|
| Critical | 4 | No auth, H2 console exposed, vulnerable dependencies |
| High | 4 | No input validation, mass assignment, no CORS/CSRF |
| Medium | 5 | Weak credentials, unsafe DDL mode, no rate limiting |

---

## Critical Vulnerabilities

### CRIT-01: No Authentication or Authorization

**Location:** All controllers (`AuthorController.java`, `RecipeController.java`, `IngredientController.java`)

**Description:**
The application has no authentication mechanism. All REST endpoints are publicly accessible without credentials. There is no Spring Security dependency, no `@PreAuthorize`, `@Secured`, or `@RolesAllowed` annotations.

**Attack Vector:**
Any attacker can:
- Read all recipes, authors, and ingredients (data exfiltration)
- Create, modify, or delete any data (data tampering)
- Perform denial of service through mass deletions

**Risk:** Data breach, complete data loss, regulatory non-compliance (GDPR, SOC2)

**Remediation:**
Add Spring Security with proper authentication:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

```java
// SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/recipes/**").permitAll()
                .requestMatchers("/authors/**", "/recipes/**").authenticated()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable()); // Only for stateless APIs
        return http.build();
    }
}
```

---

### CRIT-02: H2 Database Console Exposed

**Location:** `src/main/resources/application.properties:8`

```properties
spring.h2.console.enabled=true
```

**Description:**
The H2 database console is enabled and accessible at `/h2-console` without authentication. Combined with default credentials (`sa` / empty password), this provides unauthenticated database access.

**Attack Vector:**
Attacker can:
1. Access `/h2-console` in browser
2. Connect with default credentials
3. Execute arbitrary SQL including `DROP TABLE`, data exfiltration, or insert malicious data

**Historical Context:** [CVE-2021-42392](https://jfrog.com/blog/the-jndi-strikes-back-unauthenticated-rce-in-h2-database-console/) demonstrated RCE via H2 Console JNDI injection.

**Risk:** Complete database compromise, RCE potential

**Remediation:**

```properties
# application.properties - DISABLE for production
spring.h2.console.enabled=false

# If needed for development, restrict access:
spring.h2.console.settings.web-allow-others=false
spring.h2.console.path=/h2-console-dev-only
```

For production, use a proper database (PostgreSQL, MySQL) with strong credentials via environment variables.

---

### CRIT-03: CVE-2024-12798 - Logback Expression Language Injection

**Affected Component:** `ch.qos.logback:logback-classic:1.4.11` / `logback-core:1.4.11`

**CVE:** [CVE-2024-12798](https://vulert.com/vuln-db/CVE-2024-12798) (CVSS 5.9)

**Description:**
JaninoEventEvaluator in logback-core allows arbitrary code execution if an attacker can:
- Compromise a logback configuration file, OR
- Set an environment variable pointing to a malicious configuration

**Remediation:**
Upgrade Spring Boot to 3.2.12+ which includes logback 1.4.14:

```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.12</version>  <!-- Was 3.2.0 -->
</parent>
```

Or explicitly override:

```xml
<properties>
    <logback.version>1.4.14</logback.version>
</properties>
```

---

### CRIT-04: CVE-2024-38807 - Spring Boot Signature Forgery

**Affected Component:** `spring-boot:3.2.0`

**CVE:** [CVE-2024-38807](https://spring.io/security/cve-2024-38807/)

**Description:**
Applications using `spring-boot-loader` with custom signature verification of nested JARs may be vulnerable to signature forgery. Content appearing signed by one entity may actually be signed by another.

**Affected Versions:** Spring Boot 3.2.0 - 3.2.8

**Remediation:**
Upgrade to Spring Boot 3.2.9 or later:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.12</version>
</parent>
```

---

## High Severity Vulnerabilities

### HIGH-01: No Input Validation

**Location:** All controllers, all entities

**Description:**
Request bodies are bound directly to entities without validation. No `@Valid`, `@Validated`, `@NotNull`, `@NotBlank`, `@Size`, `@Min`, or `@Max` annotations exist.

**Examples of exploitable inputs:**
- Author with 10MB name (DoS)
- Recipe with null title
- Ingredient with negative quantity (documented intentional bug)
- SQL-like strings in text fields

**Current Code (`AuthorController.java:34`):**
```java
@PostMapping
public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
    Author saved = authorRepository.save(author);  // No validation!
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

**Remediation:**

```java
// Author.java
@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be 1-255 characters")
    private String name;
}

// AuthorController.java
@PostMapping
public ResponseEntity<Author> createAuthor(@Valid @RequestBody Author author) {
    // Validation errors now throw MethodArgumentNotValidException
    Author saved = authorRepository.save(author);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

Add validation dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

### HIGH-02: Mass Assignment Vulnerability

**Location:** All controllers

**Description:**
Entities are directly bound from HTTP request bodies. Attackers can set any field, including internal fields like `id` or relationship fields.

**Attack Example:**
```json
POST /authors
{
    "id": 999,
    "name": "Attacker",
    "internalField": "malicious"
}
```

**Remediation:**
Use DTOs (Data Transfer Objects) to control which fields are bindable:

```java
// AuthorCreateDTO.java
public record AuthorCreateDTO(
    @NotBlank @Size(max = 255) String name
) {}

// AuthorController.java
@PostMapping
public ResponseEntity<Author> createAuthor(@Valid @RequestBody AuthorCreateDTO dto) {
    Author author = new Author();
    author.setName(dto.name());
    Author saved = authorRepository.save(author);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

---

### HIGH-03: No CORS Configuration

**Location:** Entire application (no CORS config found)

**Description:**
No CORS (Cross-Origin Resource Sharing) restrictions exist. Any website can make requests to this API from client-side JavaScript.

**Attack Vector:**
Malicious website can:
1. Execute JavaScript that calls this API
2. Perform CSRF-like attacks
3. Exfiltrate data if user has authenticated session

**Remediation:**

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("https://your-trusted-domain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

---

### HIGH-04: No CSRF Protection

**Location:** N/A (Spring Security not present)

**Description:**
Without Spring Security, there is no CSRF protection. State-changing operations (POST, PUT, DELETE) can be triggered by malicious websites.

**Note:** For stateless REST APIs using token-based auth, CSRF tokens may be unnecessary if cookies are not used for authentication.

**Remediation:**
Add Spring Security (see CRIT-01). For stateless APIs with bearer tokens:

```java
http.csrf(csrf -> csrf.disable())  // Only if truly stateless
```

For traditional session-based APIs, keep CSRF enabled (default).

---

## Medium Severity Vulnerabilities

### MED-01: Default/Weak Database Credentials

**Location:** `src/main/resources/application.properties:4-5`

```properties
spring.datasource.username=sa
spring.datasource.password=
```

**Description:**
Default H2 credentials with empty password. If H2 console is enabled or database is accessible, trivial to compromise.

**Remediation:**
Use environment variables for credentials:

```properties
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:}
```

For production, use secrets management (AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets).

---

### MED-02: DDL Auto Create-Drop Mode

**Location:** `src/main/resources/application.properties:7`

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

**Description:**
Schema is dropped and recreated on each application restart. This causes complete data loss and is dangerous if accidentally deployed to production.

**Remediation:**

```properties
# Development
spring.jpa.hibernate.ddl-auto=update

# Production
spring.jpa.hibernate.ddl-auto=validate
# Or use Flyway/Liquibase for migrations
```

---

### MED-03: No Rate Limiting

**Location:** All endpoints

**Description:**
No rate limiting or throttling exists. Attackers can:
- Perform brute force attacks (if auth existed)
- Execute DoS via rapid requests
- Scrape all data quickly

**Remediation:**
Add Spring Boot Actuator with Circuit Breaker, or use a rate limiting library:

```java
@Bean
public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
    // Implement rate limiting (e.g., bucket4j, resilience4j)
}
```

Or use API Gateway (Kong, AWS API Gateway) for rate limiting.

---

### MED-04: Unhandled Exception Information Disclosure

**Location:** `AuthorController.java:50-58`

**Description:**
Deleting an author with associated recipes throws `DataIntegrityViolationException`. Without global exception handling, stack traces may leak to clients.

**Remediation:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("Cannot delete: resource has dependencies"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Internal server error"));
    }
}
```

Also configure:
```properties
server.error.include-stacktrace=never
server.error.include-message=never
```

---

### MED-05: CVE-2024-12801 - Logback SSRF

**Affected Component:** `ch.qos.logback:logback-core:1.4.11`

**CVE:** [CVE-2024-12801](https://vulert.com/vuln-db/CVE-2024-12801) (CVSS 2.4)

**Description:**
Server-Side Request Forgery in SaxEventRecorder. Attackers who can manipulate DOCTYPE in XML configuration can forge internal requests.

**Remediation:**
Same as CRIT-03: upgrade to logback 1.4.14+ via Spring Boot 3.2.12+.

---

## Dependency Analysis Summary

| Component | Current Version | Recommended Version | CVEs |
|-----------|-----------------|---------------------|------|
| spring-boot | 3.2.0 | 3.2.12+ | CVE-2024-38807 |
| logback-core | 1.4.11 | 1.4.14+ | CVE-2024-12798, CVE-2024-12801 |
| h2database | 2.2.224 | 2.2.224 (OK) | None known |
| jackson-databind | 2.15.3 | 2.15.3 (OK) | None known |
| hibernate-core | 6.3.1.Final | 6.3.1.Final (OK) | None known |
| tomcat-embed-core | 10.1.16 | 10.1.16 (OK) | None known |

**Recommended pom.xml change:**
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.12</version>  <!-- Upgrade from 3.2.0 -->
</parent>
```

---

## Positive Security Observations

1. **No SQL Injection Risk** - Uses Spring Data JPA with parameterized queries (no raw SQL, no `@Query` with string concatenation)
2. **No Command Injection** - No `Runtime.exec()` or `ProcessBuilder` usage
3. **No Hardcoded Secrets** - No API keys, tokens, or passwords in source code
4. **Safe Logging** - No sensitive data concatenation in log statements
5. **Modern Stack** - Java 17, Spring Boot 3.x with Jakarta EE namespace

---

## Prioritized Remediation Plan

### Immediate (0-7 days)
1. Upgrade Spring Boot to 3.2.12+ (fixes CRIT-03, CRIT-04, MED-05)
2. Disable H2 console in production (CRIT-02)
3. Add Spring Security with authentication (CRIT-01)

### Short-term (7-30 days)
4. Add input validation with `@Valid` (HIGH-01)
5. Implement DTOs for request bodies (HIGH-02)
6. Configure CORS restrictions (HIGH-03)
7. Add global exception handler (MED-04)

### Medium-term (30-90 days)
8. Externalize database credentials (MED-01)
9. Change DDL mode to `validate` (MED-02)
10. Implement rate limiting (MED-03)
11. Add security headers (X-Content-Type-Options, X-Frame-Options, etc.)
12. Implement API audit logging

---

## Sources

- [Spring Security Advisories](https://spring.io/security/)
- [CVE-2024-38807: Spring Boot Signature Forgery](https://spring.io/security/cve-2024-38807/)
- [CVE-2024-12798: Logback EL Injection](https://vulert.com/vuln-db/CVE-2024-12798)
- [CVE-2024-12801: Logback SSRF](https://vulert.com/vuln-db/CVE-2024-12801)
- [H2 Database Security (Snyk)](https://security.snyk.io/package/maven/com.h2database%3Ah2/2.2.224)
- [Logback Security (Snyk)](https://security.snyk.io/package/maven/ch.qos.logback%3Alogback-core)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
