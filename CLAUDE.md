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

Spring Boot 3.2.0 REST API with H2 in-memory database. Package: `com.rgs.recipeapi`

### Entity Relationships

```
Author (1) ──< Recipe (1) ──< Ingredient (many)
```

- **Author**: Has many Recipes (no cascade - deleting author with recipes causes constraint violation)
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

## Known Intentional Bugs (Demo Purposes)

1. **No cascade delete on Author**: Deleting an author with recipes throws unhandled `ConstraintViolationException` (see `OrphanedRecipesBugTest`)
2. **No quantity validation on Ingredient**: Negative quantities are accepted (see `IngredientControllerTest#shouldAllowNegativeQuantityOnCreate`)

## Workflow

1. First, think through the problem. Read the codebase and write a plan in tasks/todo.md.
2. The plan should be a checklist of todo items.
3. Check in with me before starting work-I'll verify the plan.
4. Then, complete the todos one by one, marking them off as you go.
5. At every step, give me a high-level explanation of what you changed.
6. Keep every change simple and minimal. Avoid big rewrites.
7. At the end, add a review section in todo.md summarizing the changes.
