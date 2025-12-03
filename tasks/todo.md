# Java 17 to Java 25 + Spring Boot 4.0.0 Upgrade Plan

## Analysis

**Current State:**
- Java 17
- Spring Boot 3.2.0
- Maven wrapper

**Key Considerations:**
- Spring Boot 3.2.0 officially supports Java 17-21
- For Java 25 support, Spring Boot needs to be upgraded to 3.4.x or newer
- Spring Boot 4.0.0 provides the latest features with Java 25 support
- Dependencies (H2, Spring Data JPA) will get compatible versions via Spring Boot parent

## Checklist

- [x] Update Spring Boot version to 4.0.0
- [x] Update Java version property from 17 to 25
- [x] Update Maven wrapper to support Java 25
- [x] Update Jackson imports (com.fasterxml.jackson → tools.jackson)
- [x] Add spring-boot-starter-webmvc-test dependency
- [x] Add spring-boot-starter-data-jpa-test dependency
- [x] Update AutoConfigureMockMvc imports (4 files)
- [x] Update DataJpaTest imports (3 files)
- [x] Run `./mvnw compile` to verify compilation
- [x] Run `./mvnw test` to verify all tests pass
- [x] Build executable JAR
- [x] Document any issues encountered

## Review

### Changes Made

1. **pom.xml:11** - Updated Spring Boot from `3.2.0` to `4.0.0`
2. **pom.xml:22** - Updated Java version from `17` to `25`
3. **pom.xml:44-53** - Added `spring-boot-starter-webmvc-test` and `spring-boot-starter-data-jpa-test` dependencies
4. **.mvn/wrapper/maven-wrapper.properties** - Updated Maven to `3.9.9`

**Jackson 3.0 Package Migration (4 files):**
5. **DataLoader.java:10** - Changed `com.fasterxml.jackson.databind.ObjectMapper` → `tools.jackson.databind.ObjectMapper`
6. **AuthorControllerTest.java:7** - Same Jackson import change
7. **RecipeControllerTest.java:8** - Same Jackson import change
8. **IngredientControllerTest.java:9** - Same Jackson import change

**Modularized Test Imports:**
9. **AuthorControllerTest.java:11** - Changed `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
10. **RecipeControllerTest.java:12** - Same AutoConfigureMockMvc import change
11. **IngredientControllerTest.java:13** - Same AutoConfigureMockMvc import change
12. **OrphanedRecipesBugTest.java:11** - Same AutoConfigureMockMvc import change
13. **AuthorRepositoryTest.java:6** - Changed `org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`
14. **RecipeRepositoryTest.java:7** - Same DataJpaTest import change
15. **IngredientRepositoryTest.java:8** - Same DataJpaTest import change

### Verification

- Compilation: SUCCESS
- Tests: 21 run, 0 failures, 0 errors
- Executable JAR: 53 MB at `target/recipe-api-0.0.1-SNAPSHOT.jar`

### Issues Encountered and Resolved

1. **Jackson 3.0 Package Rename**: Spring Boot 4.0.0 uses Jackson 3.0 which changed its base package from `com.fasterxml.jackson` to `tools.jackson`. Required updating imports in 4 files.

2. **Modularized Test Dependencies**: Spring Boot 4.0.0 reorganized test auto-configuration into separate modules:
   - `AutoConfigureMockMvc` moved to `org.springframework.boot.webmvc.test.autoconfigure`
   - `DataJpaTest` moved to `org.springframework.boot.data.jpa.test.autoconfigure`
   - Required adding `spring-boot-starter-webmvc-test` and `spring-boot-starter-data-jpa-test` dependencies
