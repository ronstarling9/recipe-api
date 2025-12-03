# Security Review Report: Recipe API

**Date:** December 3, 2025
**Reviewer:** Security Assessment
**Application:** Recipe REST API (Spring Boot 3.2.0)
**Severity Legend:** CRITICAL | HIGH | MEDIUM | LOW | INFO

---

## Executive Summary

This security review identified **7 findings** across the Recipe API codebase. The most critical issues involve missing authentication/authorization, exposed H2 console in production configuration, and outdated dependencies with known CVEs.

| Severity | Count |
|----------|-------|
| CRITICAL | 2     |
| HIGH     | 2     |
| MEDIUM   | 2     |
| LOW      | 1     |

---

## Findings

### 1. CRITICAL: No Authentication or Authorization

**Location:** All controllers (`AuthorController.java`, `RecipeController.java`, `IngredientController.java`)

**Description:** The API has no authentication or authorization mechanism. All endpoints are publicly accessible, allowing any user to create, read, update, or delete all resources.

**Impact:**
- Unauthorized data access
- Data tampering and deletion
- No audit trail of user actions
- Complete compromise of data integrity

**Recommendation:**
Add Spring Security with appropriate authentication:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Implement a security configuration with authentication and role-based access control.

---

### 2. CRITICAL: H2 Console Enabled (Remote Code Execution Risk)

**Location:** `src/main/resources/application.properties:8`

```properties
spring.h2.console.enabled=true
```

**Description:** The H2 database console is enabled, exposing it at `/h2-console`. This is a known attack vector for remote code execution (RCE) vulnerabilities, including [CVE-2021-42392](https://jfrog.com/blog/the-jndi-strikes-back-unauthenticated-rce-in-h2-database-console/) which has similarities to Log4Shell.

**Impact:**
- Remote code execution via JNDI injection
- Full server compromise
- Data exfiltration

**Recommendation:**
1. Disable H2 console in production:
```properties
spring.h2.console.enabled=false
```

2. If needed for development only, use Spring profiles:
```properties
# application.properties (production)
spring.h2.console.enabled=false

# application-dev.properties (development only)
spring.h2.console.enabled=true
```

---

### 3. HIGH: Outdated Spring Boot Version with Known CVEs

**Location:** `pom.xml:11`

```xml
<version>3.2.0</version>
```

**Description:** Spring Boot 3.2.0 is affected by multiple CVEs:

| CVE | Severity | Description |
|-----|----------|-------------|
| [CVE-2024-38807](https://spring.io/security/cve-2024-38807/) | Medium | Signature forgery in spring-boot-loader |
| [CVE-2025-22235](https://spring.io/blog/2025/04/24/spring-boot-CVE-2025-22235/) | Medium | Actuator endpoint matcher bypass |
| [CVE-2024-22233](https://spring.io/security/cve-2024-22233/) | Medium | DoS vulnerability in Spring Framework |

**Recommendation:**
Upgrade to Spring Boot 3.2.14 or later:

```xml
<version>3.2.14</version>
```

Alternatively, upgrade to the latest 3.4.x or 3.5.x version for full long-term support.

---

### 4. HIGH: No Input Validation

**Location:** All controllers - `@RequestBody` parameters lack validation

**Examples:**
- `AuthorController.java:34` - `createAuthor(@RequestBody Author author)`
- `RecipeController.java:34` - `createRecipe(@RequestBody Recipe recipe)`
- `IngredientController.java:34` - `createIngredient(..., @RequestBody Ingredient ingredient)`

**Description:**
- No `@Valid` annotation on request bodies
- No Bean Validation constraints on entity fields
- Negative quantities accepted for ingredients (documented intentional bug)
- No length limits enforced on string fields

**Impact:**
- Stored XSS if data is rendered in a frontend without sanitization
- Database storage exhaustion with unlimited-length strings
- Business logic bypass (negative quantities)

**Recommendation:**
1. Add validation dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

2. Add validation constraints to entities:
```java
// Ingredient.java
@Positive(message = "Quantity must be positive")
@NotNull
private Float quantity;

@NotBlank
@Size(max = 255)
private String name;
```

3. Add `@Valid` to controller parameters:
```java
public ResponseEntity<Author> createAuthor(@Valid @RequestBody Author author)
```

---

### 5. MEDIUM: Insecure Database Configuration

**Location:** `src/main/resources/application.properties:4-5`

```properties
spring.datasource.username=sa
spring.datasource.password=
```

**Description:**
- Default H2 database username `sa` with empty password
- Credentials hardcoded in properties file (though empty password is H2 default)
- For production databases, this pattern would expose credentials in source control

**Impact:**
- If migrated to a production database, credentials may be exposed
- No defense-in-depth for database access

**Recommendation:**
1. Use environment variables for credentials:
```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

2. Add `.env` to `.gitignore` and use externalized configuration for production

---

### 6. MEDIUM: Missing CORS Configuration

**Location:** Not configured (missing)

**Description:** No CORS (Cross-Origin Resource Sharing) configuration exists. Depending on deployment:
- If too permissive: Enables CSRF attacks from malicious sites
- If too restrictive: May break legitimate frontend integration

**Impact:**
- Potential cross-site request forgery
- API abuse from unauthorized origins

**Recommendation:**
Add explicit CORS configuration:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("https://your-frontend.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

---

### 7. LOW: Exception Handling Exposes Stack Traces

**Location:** All controllers (no global exception handler)

**Description:** When errors occur (e.g., constraint violations, null pointers), Spring Boot's default error handling may expose stack traces and internal implementation details to clients.

**Impact:**
- Information disclosure about internal structure
- Aids attackers in identifying vulnerabilities

**Recommendation:**
Add a global exception handler:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // Log full exception internally
        log.error("Unhandled exception", ex);

        // Return sanitized response
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An unexpected error occurred"));
    }
}
```

---

## Dependency Analysis

### Current Dependencies

| Dependency | Version (via BOM) | Status |
|------------|-------------------|--------|
| Spring Boot | 3.2.0 | Outdated - upgrade to 3.2.14+ |
| Spring Framework | 6.1.1 | Outdated - via Boot BOM |
| H2 Database | 2.2.224 (approx) | Monitor for CVEs |
| Jackson | 2.15.x | Check for updates |

### Recommended Upgrades

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.14</version>  <!-- Minimum recommended -->
</parent>
```

For maximum security, consider upgrading to 3.4.x which has longer support.

---

## Security Posture Summary

### What's Good
- Uses Spring Data JPA (parameterized queries, protects against SQL injection)
- No custom native SQL queries that could introduce injection vulnerabilities
- No command execution (Runtime.exec, ProcessBuilder)
- No hardcoded API keys or secrets (besides empty H2 password)
- Uses constructor injection (preferred over field injection)

### What Needs Improvement
1. **Authentication/Authorization** - Add Spring Security
2. **Input Validation** - Add Bean Validation constraints
3. **Dependency Updates** - Upgrade Spring Boot to patched version
4. **H2 Console** - Disable or restrict to dev environment only
5. **CORS** - Add explicit configuration
6. **Error Handling** - Add global exception handler with sanitized responses
7. **Externalized Configuration** - Move credentials to environment variables

---

## Remediation Priority

| Priority | Finding | Effort |
|----------|---------|--------|
| 1 | Add authentication/authorization | High |
| 2 | Upgrade Spring Boot to 3.2.14+ | Low |
| 3 | Disable H2 console | Low |
| 4 | Add input validation | Medium |
| 5 | Add CORS configuration | Low |
| 6 | Add global exception handler | Low |
| 7 | Externalize credentials | Low |

---

## References

- [Spring Security Advisories](https://spring.io/security/)
- [CVE-2024-38807: Spring Boot Loader Vulnerability](https://spring.io/security/cve-2024-38807/)
- [CVE-2025-22235: Endpoint Matcher Bypass](https://spring.io/blog/2025/04/24/spring-boot-CVE-2025-22235/)
- [H2 Database JNDI RCE (JFrog)](https://jfrog.com/blog/the-jndi-strikes-back-unauthenticated-rce-in-h2-database-console/)
- [Snyk: Spring Boot Vulnerabilities](https://security.snyk.io/package/maven/org.springframework.boot%3Aspring-boot)
- [Snyk: H2 Database Vulnerabilities](https://security.snyk.io/package/maven/com.h2database%3Ah2)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
