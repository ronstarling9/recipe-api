# Security Review Report: Recipe API

**Date:** 2025-12-03
**Reviewer:** Security Analyst
**Application:** Spring Boot 3.2.0 REST API

---

## Executive Summary

This security review identified **7 critical/high severity** and **5 medium/low severity** security issues in the Recipe API application. The most severe issues involve the lack of authentication/authorization and an exposed H2 database console that could enable remote code execution.

---

## Critical Findings

### 1. No Authentication or Authorization (CRITICAL)

**Location:** All controllers
**Risk:** CRITICAL
**CVSS Score:** 9.8

The application has no authentication or authorization mechanism. All REST endpoints are publicly accessible, allowing anyone to:
- Create, read, update, and delete authors
- Create, read, update, and delete recipes
- Create, read, update, and delete ingredients

**Evidence:**
- No `spring-boot-starter-security` dependency in `pom.xml`
- No security configuration class
- No `@PreAuthorize`, `@Secured`, or `@RolesAllowed` annotations

**Recommendation:**
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Implement proper authentication (JWT, OAuth2, or session-based) and role-based access control.

---

### 2. H2 Console Enabled in Configuration (CRITICAL)

**Location:** `src/main/resources/application.properties:8`
**Risk:** CRITICAL
**CVSS Score:** 9.8

```properties
spring.h2.console.enabled=true
```

The H2 database web console is enabled, which:
- Exposes the database at `/h2-console` by default
- Can lead to Remote Code Execution (RCE) via JDBC URL manipulation
- Related to CVE-2021-42392 and CVE-2022-23221

**Recommendation:**
```properties
# Disable H2 console in production
spring.h2.console.enabled=false

# If needed for development only, use profile-specific configs
# application-dev.properties: spring.h2.console.enabled=true
# application-prod.properties: spring.h2.console.enabled=false
```

---

### 3. Outdated Spring Boot Version with Known CVEs (HIGH)

**Location:** `pom.xml:11`
**Risk:** HIGH

Spring Boot 3.2.0 is affected by multiple CVEs:

| CVE | Severity | Description | Fixed In |
|-----|----------|-------------|----------|
| [CVE-2024-38807](https://spring.io/security/cve-2024-38807/) | Medium | Signature forgery in spring-boot-loader | 3.2.9 |
| [CVE-2025-22235](https://spring.io/blog/2025/04/24/spring-boot-CVE-2025-22235/) | Medium | EndpointRequest.to() matcher issue | 3.2.14 |
| [CVE-2024-22233](https://spring.io/security/cve-2024-22233/) | High | DoS vulnerability in Spring Framework | 3.2.2 |

**Recommendation:**
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.14</version>  <!-- Upgrade from 3.2.0 -->
    <relativePath/>
</parent>
```

---

## High Severity Findings

### 4. No Input Validation on Entities (HIGH)

**Location:** All entity classes and controllers
**Risk:** HIGH

No input validation is performed on incoming data:

**Author.java** - No validation on name field
```java
private String name;  // Can be null, empty, or extremely long
```

**Recipe.java** - No validation on any fields
```java
private String title;        // No @NotBlank
private String description;  // No @Size limit validation
private String instructions; // No validation
```

**Ingredient.java** - No validation, allows negative quantities
```java
// INTENTIONAL BUG: No @Min or @Positive validation
private Float quantity;  // Can be negative, null, or NaN
```

**Controllers** - No `@Valid` annotation on `@RequestBody`
```java
// Current (vulnerable)
public ResponseEntity<Author> createAuthor(@RequestBody Author author)

// Recommended
public ResponseEntity<Author> createAuthor(@Valid @RequestBody Author author)
```

**Recommendation:**

Add validation dependency and annotations:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

```java
// Example for Ingredient.java
import jakarta.validation.constraints.*;

@NotBlank
private String name;

@NotNull
@Positive
private Float quantity;

@NotBlank
private String unit;
```

---

### 5. Insecure Object Direct Reference (IDOR) (HIGH)

**Location:** All controllers
**Risk:** HIGH

All endpoints use sequential IDs without ownership validation:

```java
// Anyone can access/modify any resource by guessing IDs
@GetMapping("/{id}")
public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id)

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteRecipe(@PathVariable Long id)
```

**Recommendation:**
- Implement authorization checks to verify the user owns the resource
- Consider using UUIDs instead of sequential integers
- Add ownership validation in service layer

---

## Medium Severity Findings

### 6. Weak Database Credentials (MEDIUM)

**Location:** `src/main/resources/application.properties:4-5`
**Risk:** MEDIUM

```properties
spring.datasource.username=sa
spring.datasource.password=
```

Empty password and default username for database access.

**Recommendation:**
- Use environment variables for credentials
- Use Spring's `${DB_PASSWORD}` placeholder syntax
- For production, use a proper database with strong credentials

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

---

### 7. Information Disclosure via Stack Traces (MEDIUM)

**Location:** `AuthorController.java:50-58`
**Risk:** MEDIUM

Unhandled exceptions (like `ConstraintViolationException`) will expose stack traces to clients. The known bug of deleting an author with recipes will leak internal details.

**Recommendation:**
Add global exception handler:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Cannot delete: resource has dependencies"));
    }
}
```

---

### 8. Missing Security Headers (MEDIUM)

**Risk:** MEDIUM

The application does not configure security headers:
- No Content-Security-Policy
- No X-Content-Type-Options
- No X-Frame-Options
- No X-XSS-Protection

**Recommendation:**
With Spring Security, headers are added by default. Alternatively, add a filter:

```java
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        chain.doFilter(request, response);
    }
}
```

---

## Low Severity Findings

### 9. Console Logging in Production Code (LOW)

**Location:** `DataLoader.java:70`
**Risk:** LOW

```java
System.out.println("Loaded " + data.getRecipes().size() + " recipes from JSON.");
```

Using `System.out.println` instead of a proper logging framework.

**Recommendation:**
Use SLF4J logging:
```java
private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
// ...
log.info("Loaded {} recipes from JSON", data.getRecipes().size());
```

---

### 10. Missing Rate Limiting (LOW)

**Risk:** LOW

No rate limiting on API endpoints, making the application vulnerable to:
- Brute force attacks
- DoS through resource exhaustion
- API abuse

**Recommendation:**
Consider using Spring Cloud Gateway or a rate limiting library like Bucket4j.

---

### 11. No HTTPS Enforcement (LOW)

**Risk:** LOW (development context)

The application does not enforce HTTPS. While acceptable for local development with H2, production deployments should enforce TLS.

**Recommendation:**
For production:
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD}
```

---

## Positive Security Findings

1. **SQL Injection Protection:** Using Spring Data JPA repositories with parameterized queries. No native SQL queries found.

2. **No Hardcoded API Keys/Secrets:** No API keys, tokens, or external service credentials found in the codebase.

3. **Modern Framework:** Using Java 17 and Spring Boot 3.x with current security patches (once upgraded).

---

## Dependency Analysis

### Current Dependencies

| Dependency | Version | Status |
|------------|---------|--------|
| spring-boot-starter-parent | 3.2.0 | **VULNERABLE** - Upgrade to 3.2.14 |
| spring-boot-starter-web | (managed) | Update with parent |
| spring-boot-starter-data-jpa | (managed) | Update with parent |
| h2 | (managed) | Monitor for CVEs |
| spring-boot-starter-test | (managed) | Update with parent |

### Recommended Upgrades

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.14</version>
</parent>
```

This version addresses all known CVEs while maintaining API compatibility with 3.2.0.

---

## Remediation Priority

| Priority | Finding | Effort |
|----------|---------|--------|
| P0 | Add authentication/authorization | High |
| P0 | Disable H2 console | Low |
| P1 | Upgrade Spring Boot to 3.2.14 | Low |
| P1 | Add input validation | Medium |
| P2 | Fix IDOR vulnerabilities | Medium |
| P2 | Add global exception handler | Low |
| P3 | Add security headers | Low |
| P3 | Implement proper logging | Low |
| P4 | Add rate limiting | Medium |
| P4 | Configure HTTPS | Low |

---

## References

- [Spring Security Advisories](https://spring.io/security/)
- [CVE-2024-38807](https://spring.io/security/cve-2024-38807/)
- [CVE-2025-22235](https://spring.io/blog/2025/04/24/spring-boot-CVE-2025-22235/)
- [H2 Database Security Vulnerabilities](https://security.snyk.io/package/maven/com.h2database%3Ah2)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Boot Security Best Practices](https://spring.io/guides/topicals/spring-security-architecture/)
