# Recipe API

A RESTful API for managing recipes, authors, and ingredients. Built with Spring Boot 4.0 and Java 25.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.0**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **Maven**

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.9+ (or use the included Maven wrapper)

### Run the Application

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

### Run Tests

```bash
./mvnw test
```

## API Endpoints

### Authors

| Method | Endpoint       | Description         |
|--------|----------------|---------------------|
| GET    | /authors       | List all authors    |
| GET    | /authors/{id}  | Get author by ID    |
| POST   | /authors       | Create an author    |
| PUT    | /authors/{id}  | Update an author    |
| DELETE | /authors/{id}  | Delete an author    |

### Recipes

| Method | Endpoint       | Description         |
|--------|----------------|---------------------|
| GET    | /recipes       | List all recipes    |
| GET    | /recipes/{id}  | Get recipe by ID    |
| POST   | /recipes       | Create a recipe     |
| PUT    | /recipes/{id}  | Update a recipe     |
| DELETE | /recipes/{id}  | Delete a recipe     |

### Ingredients

| Method | Endpoint                              | Description              |
|--------|---------------------------------------|--------------------------|
| GET    | /recipes/{recipeId}/ingredients       | List recipe ingredients  |
| GET    | /recipes/{recipeId}/ingredients/{id}  | Get ingredient by ID     |
| POST   | /recipes/{recipeId}/ingredients       | Add ingredient to recipe |
| PUT    | /recipes/{recipeId}/ingredients/{id}  | Update an ingredient     |
| DELETE | /recipes/{recipeId}/ingredients/{id}  | Delete an ingredient     |

## Data Model

```
Author (1) ──< Recipe (1) ──< Ingredient (many)
```

- **Author**: name
- **Recipe**: title, description, instructions, author
- **Ingredient**: name, quantity, unit, recipe

## Sample Data

The application loads 20 sample recipes on startup from `src/main/resources/data/recipes.json`.

## Project Structure

```
src/main/java/com/rgs/recipeapi/
├── controller/          # REST controllers
├── entity/              # JPA entities
├── repository/          # Spring Data repositories
├── dto/                 # Data transfer objects
├── DataLoader.java      # Seed data loader
└── RecipeApiApplication.java
```

## License

MIT
