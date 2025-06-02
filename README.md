Key Achievements:

- REST API Endpoints: All primary endpoints (POST /persons, PUT /persons/{id}/location, GET /persons/nearby, GET /persons) are functional.
- Core Architecture: The solution features solid domain models, UseCase pattern (Query interface with execute method), service layer, and a repository pattern.
  It leverages PostgreSQL with PostGIS extension for persistence and efficient spatial data management and geospatial querying
- GET /persons/nearby Optimization: This endpoint is optimized for massive datasets (10M+ records)
  This was accomplished by:
  - Leveraging a GiST index on spatial data for ultra-fast lookups.
  - Implementing precise PostGIS geography type calculations (ST_DWithin, ST_Distance) for accurate spherical distances.
  - Optimizing database resource allocation, including shared buffers, work memory, and parallel processing workers, within Docker container limits.
  - Utilizing LIMIT and OFFSET directly in SQL for efficient pagination, ensuring only necessary records are transferred.
- API Documentation: The application integrates OpenAPI/Swagger for comprehensive API documentation.
- Testing: Integration Tests are included, utilizing appropriate frameworks to ensure robust functionality.
- Robustness: Features comprehensive Input Validation and Error Handling (basic handling is present).
- Deployment & Monitoring: Includes Docker Compose for easy environment management and deployment (Spring Boot, PostgreSQL/PostGIS, pgAdmin, Prometheus, Grafana),
  along with tuned Docker resource limits for both the application and database for enhanced performance and stability.
- Implemented Liquibase for robust database schema version control, enabling automated, reliable, and repeatable database deployments across all environments.
- Implemented a benchmarking and monitoring stack using Spring Actuator, Micrometer, Prometheus, and Grafana. This provides real-time insights into API performance and resource consumption

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

Grafana:

- Dashboards for Grafana are in the app folder 'person-finder/monitoring/grafana/provisioning/dashboards'
- To display the app's metrics:
- open http://localhost:3000/login in the browser (username 'admin', password 'admin')
- import the dashboards prepared:
![image](https://github.com/user-attachments/assets/dc544a91-0d67-4d6e-8fed-fa4e759bb909)

- The Spring-Boot Dashboard' HTTP Statistics is useful for analyzing the api response latency:
![image](https://github.com/user-attachments/assets/87d02a50-e97c-47e9-82b1-da3581328f5e)

- CPU/Memory JVM usage can be monitored using the JVM Micrometer Dashboard:
![image](https://github.com/user-attachments/assets/97bfd564-1d68-45bb-8120-928d202bc8b8)


Database management:

- Open PgAdmin UI in your browser: http://localhost:5050/browser/
- Credentials: PGADMIN_DEFAULT_EMAIL=admin@example.com , PGADMIN_DEFAULT_PASSWORD=admin
- Register a new db server (right mouse click on 'Servers' -> 'Register'):
- Name: (Your choice, e.g., "PersonsFinderDB")
- Host name/address: db
- Port: 5432
- Maintenance Database: leave it as it is
- Username: emerge
- Password: emerge

Database indexes:

![image](https://github.com/user-attachments/assets/340cab68-9fb9-4579-a455-caf6edaf0546)

idx_person_location_location_gist:

- This is a spatial GIST index on the location column, vital for fast geographic searches like finding nearby points.
- A GIST index is better than a standard B-tree index for location because it's designed for spatial data. 
  Unlike B-tree (which works best for ordered, single-dimensional data), GIST efficiently handles multi-dimensional geographic data, 
  making distance, bounding box, and overlap queries more efficient.

person_locations_person_id_key: 

- A unique B-tree index on person_id, ensuring each person has one unique location entry and speeding up lookups by person ID.

person_locations_pkey: 
- The primary key B-tree index on id in person_locations, guaranteeing unique records and enabling fast lookups

persons_pkey: 
- The primary key B-tree index on id in persons

Database query for GET /persons/nearby endpoint (the query is in PersonLocationRepository.findWithinRadius()):

- The query efficiently finds nearby persons by combining two powerful filters:
  - Fast Initial Filter (pl.location && ST_Expand(...)): This uses the GIST spatial index to quickly narrow down potential matches 
    to a rough rectangular area around the search point. It converts the given radius into approximate degrees for speed.
  - Accurate Final Filter (ST_DWithin(...)): From the already filtered results, this step precisely calculates distances on the Earth's curved surface 
  - (using geography type) to confirm which points are exactly within the specified radius.
This two-step process ensures both speed (via the index-assisted bounding box) and accuracy (via precise spherical distance calculations).

Data seeding:

- Open PgAdmin ui http://localhost:5050/browser/ -> Tools -> Query tool
- Run the query from src/main/kotlin/com/persons/finder/config/dataseeder.sql


Application Performance with 10 000 000 records in both users, user_locations tables:

![image](https://github.com/user-attachments/assets/77ca33ea-c99d-4939-b009-6a088aa59daf)

- find nearby users within 5000 km radius: http://localhost:8080/api/v1/persons/nearby?lat=-36.8485&lon=174.7645&radiusKm=5000&page=120&size=100
![image](https://github.com/user-attachments/assets/303e4738-9898-48cb-8f87-80af5908f3e6)

- Overall Performance of all 4 api endpoints:
![image](https://github.com/user-attachments/assets/beab2e45-afda-4adb-aff8-55cf0b929278)
