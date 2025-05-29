Key Achievements:

- REST API Endpoints: All primary endpoints (POST /persons, PUT /persons/{id}/location, GET /persons/nearby, GET /persons) are functional.
- Core Architecture: The solution features solid domain models, service layers, and a repository pattern. It leverages PostgreSQL with PostGIS for persistence and follows a Use Case pattern (Query interface with execute method).
- GET /persons/nearby Optimization: This endpoint is optimized for large datasets, utilizing a GiST index on spatial data for fast lookups. Distance calculation and sorting are handled effectively.
- The application integrates OpenAPI/Swagger for API documentation.
- Clean Code: The codebase employs DTOs and maintains a clear separation of concerns.
- Integration Tests added (frameworks are included).
- Comprehensive Input Validation and Error Handling (basic handling is present).

TODO:
- Seed the system with 1 million, 10 million, and 100 million records
- includes Docker Compose for easy environment management (Spring Boot, PostgreSQL/PostGIS, pgAdmin)
