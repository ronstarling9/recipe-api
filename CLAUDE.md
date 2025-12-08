# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build the project
./mvnw compile

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthorControllerTest

# Run a single test method
./mvnw test -Dtest=AuthorControllerTest#shouldCreateAuthor

# Run the application (starts on port 8080)
./mvnw spring-boot:run
```

## Architecture

Spring Boot 4.0.0 REST API with H2 in-memory database, running on Java 25. Package: `com.rgs.recipeapi`

### Entity Relationships

```
Author (1) ──< Recipe (1) ──< Ingredient (many)
```

- **Author**: Has many Recipes (cascade delete enabled - deleting author deletes associated recipes)
- **Recipe**: Belongs to Author, has many Ingredients
- **Ingredient**: Belongs to Recipe (nested under `/recipes/{recipeId}/ingredients`)

### REST Endpoints

| Resource    | Path                                     |
|-------------|------------------------------------------|
| Authors     | `/authors`                               |
| Recipes     | `/recipes`                               |
| Ingredients | `/recipes/{recipeId}/ingredients`        |

### Data Seeding

`DataLoader` runs on startup and loads 20 recipes from `src/main/resources/data/recipes.json`.

### Test Setup Pattern

Controller tests must clean data in correct order (FK constraints):
```java
@BeforeEach
void setUp() {
    ingredientRepository.deleteAll();
    recipeRepository.deleteAll();
    authorRepository.deleteAll();
}
```

### Spring Boot 4.0 Import Locations

Spring Boot 4.0 modularized test dependencies. Key imports:
- Jackson: `tools.jackson.databind.ObjectMapper` (not `com.fasterxml.jackson`)
- MockMvc: `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
- JPA Tests: `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`

## Known Intentional Bugs (Demo Purposes)

1. **No quantity validation on Ingredient**: Negative quantities are accepted (see `IngredientControllerTest#shouldAllowNegativeQuantityOnCreate`)

## Workflow

Keep every change simple and minimal. Avoid big rewrites.

