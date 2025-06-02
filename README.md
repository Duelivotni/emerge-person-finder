Application start:

- git clone: https://github.com/Duelivotni/emerge-person-finder.git 
- Once cloned, CD into 'persons-finder' root folder
- Run the command: docker-compose up --build -d
- This will pull images and run 5 containers: 
  - person-finder main api
  - Postgres database (with PostGIS extension)
  - PgAdmin4 for accessing/managing the database in the browser
  - Prometheus for metrics collection
  - Grafana for metrics visualization

Persons Finder main Api:
- Open the url in your browser: http://localhost:8080/swagger-ui/index.html#/

Database:

- Open PgAdmin UI in your browser: http://localhost:5050/browser/
- Credentials: PGADMIN_DEFAULT_EMAIL=admin@example.com , PGADMIN_DEFAULT_PASSWORD=admin
- Register a new db server (right mouse click on 'Servers' -> 'Register'):
- Name: (Your choice, e.g., "PersonsFinderDB")
- Host name/address: db
- Port: 5432
- Maintenance Database: leave it as it is
- Username: emerge
- Password: emerge

Metrics:



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


