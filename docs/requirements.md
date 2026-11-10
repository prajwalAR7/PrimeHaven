# Requirements Specification
## Enterprise Real Estate Listings & Property Inspection Scheduling Platform
**Platform Name:** PrimeHaven Real Estate Network  
**Target Delivery:** Academic College Review & Demo

---

## 1. Problem Statement
In traditional real estate management systems, property listings, buyer inquiries, and inspection schedules are often tightly coupled within monolithic architectures or managed across disconnected spreadsheets and manual channels. This creates operational bottlenecks:
- Changes in listing data or high-traffic inquiries can degrade the entire application.
- Scheduling on-site property inspections requires tedious cross-verification of property availability and buyer inquiry details.
- Lack of centralized routing and service discovery makes scaling individual modules difficult.

The **PrimeHaven Real Estate Network** platform solves this by adopting a **cloud-native microservices architecture** using Spring Boot, Spring Cloud, and Java 21. By isolating identity, property catalogs, buyer inquiries, and inspection scheduling into dedicated microservices fronted by an API Gateway and Eureka Service Registry, the system provides high cohesion, loose coupling, autonomous scalability, and a resilient workflow.

---

## 2. System Actors

| Actor | Description | Primary Interactions |
| :--- | :--- | :--- |
| **Buyer / Client** | Prospective real estate customer looking to view listings and inspect properties. | - Registers and logs in (JWT authentication)<br>- Browses and filters active property listings<br>- Submits property inquiries<br>- Requests/schedules property inspection visits |
| **Agent / Admin** | Real estate broker or system administrator managing property inventory and interactions. | - Authenticates via JWT<br>- Creates, updates, and deletes property listings<br>- Reviews buyer inquiries<br>- Approves, manages, and updates property inspection visit schedules |
| **System Services (Internal)** | Automated inter-service communication between microservices. | - `inquiry-service` calls `property-service` to validate listing existence and status.<br>- `inquiry-service` calls `visit-service` to create an inspection visit linked to the verified property and inquiry. |

---

## 3. Functional Requirements (FR)

### FR-1: Authentication & User Management (`auth-service`)
- **FR-1.1 User Registration:** Support user registration with username, email, password, and assigned role (`ROLE_BUYER`, `ROLE_AGENT`, `ROLE_ADMIN`).
- **FR-1.2 User Login & Token Generation:** Authenticate credentials and issue a cryptographically signed JSON Web Token (JWT) containing username, role, and expiration timestamp.
- **FR-1.3 Token Verification:** Expose validation capability or share token signature verification secret across the gateway/services for stateless request authorization.

### FR-2: Property Listings Management (`property-service`)
- **FR-2.1 Create Listing:** Allow authorized users (Agents/Admins) to create new property listings with title, description, address, city, price, property type (`APARTMENT`, `VILLA`, `COMMERCIAL`), status (`AVAILABLE`, `PENDING`, `SOLD`), and bedrooms/bathrooms.
- **FR-2.2 Retrieve Listings:** Allow public/authenticated browsing of all listings with filtering by city, status, and price range.
- **FR-2.3 Listing Details:** Fetch single property listing details by its unique identifier (`id`).
- **FR-2.4 Update & Delete Listings:** Allow updating property details/status and removing listings.
- **FR-2.5 Availability Check API:** Expose an endpoint for other internal microservices to verify property existence and availability.

### FR-3: Property Visit & Inspection Scheduling (`visit-service`)
- **FR-3.1 Schedule Visit:** Allow buyers to schedule an in-person property inspection visit with `propertyId`, `buyerUsername` (or ID), preferred date, time slot, and inspection notes.
- **FR-3.2 Retrieve Visits:** Retrieve scheduled visits filtered by buyer username or by property ID.
- **FR-3.3 Update Visit Status:** Allow agents/buyers to modify visit status (`SCHEDULED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`).
- **FR-3.4 Visit Details:** Retrieve specific visit details by visit ID.

### FR-4: Buyer Inquiries Management (`inquiry-service`)
- **FR-4.1 Submit Inquiry:** Allow buyers to submit inquiries for a specific property with buyer contact info, inquiry type (`PRICE_INQUIRY`, `VISIT_REQUEST`, `GENERAL`), and a message.
- **FR-4.2 Inter-Service Inquiry-to-Visit Workflow (`Inquiry -> Property -> Visit`):**
  - When an inquiry includes a visit request, `inquiry-service` calls `property-service` to verify that the property exists and is `AVAILABLE`.
  - Upon successful property verification, `inquiry-service` calls `visit-service` to schedule the inspection visit.
  - The inquiry record stores references to both the `propertyId` and generated `visitId`.
- **FR-4.3 View Inquiries:** Retrieve inquiries by buyer username or by property ID.
- **FR-4.4 Update Inquiry Status:** Update inquiry status (`NEW`, `IN_REVIEW`, `RESOLVED`).

### FR-5: Service Discovery & Registration (`eureka-server`)
- **FR-5.1 Dynamic Registration:** All functional microservices (`auth-service`, `property-service`, `visit-service`, `inquiry-service`) and the API Gateway register as Eureka clients upon startup.
- **FR-5.2 Health Heartbeats:** Provide periodic heartbeats and display active service instances on the Eureka Web Dashboard (`http://localhost:8761`).

### FR-6: API Gateway & Centralized Routing (`api-gateway`)
- **FR-6.1 Single Entry Point:** Act as the unified reverse proxy running on port `8080`.
- **FR-6.2 Dynamic Route Mapping:** Route client requests using Eureka service IDs and client-side load balancing (`lb://AUTH-SERVICE`, `lb://PROPERTY-SERVICE`, `lb://VISIT-SERVICE`, `lb://INQUIRY-SERVICE`).
- **FR-6.3 Security Enforcement:** Validate incoming JWT `Authorization: Bearer <token>` headers before forwarding requests to downstream services (or relaying token headers).

### FR-7: Automated Verification & Testing
- **FR-7.1 Unit & Controller Tests:** Implement basic Spring Boot slice tests (`@WebMvcTest` or `@SpringBootTest`) verifying health checks, authentication, CRUD operations, and mock inter-service calls.

---

## 4. Non-Functional Requirements (NFR)

| Category | Requirement | Specification |
| :--- | :--- | :--- |
| **Portability & Runtime** | Java & Spring Compatibility | Java 21 LTS runtime, Spring Boot 3.3.4, and Spring Cloud 2023.0.3 (Leyton). |
| **Data Isolation** | Database per Service Pattern | Each microservice maintains its own independent in-memory H2 database (`jdbc:h2:mem:<service>db`), preventing cross-service database coupling. |
| **Security** | Stateless Authentication | Cryptographic JWT (HMAC-SHA256) validation; passwords hashed using BCrypt. |
| **Observability** | Eureka Dashboard & H2 Web Console | Real-time health monitoring on Eureka Dashboard (`:8761`) and visual database inspection via H2 Web Console for each service. |
| **Maintainability** | Clean Code & Maven Modularization | Standard Maven project structure with clean controller-service-repository layered patterns. |
| **Performance** | Low Latency Local Execution | Fast cold-start times and immediate in-memory persistence suited for live college demo execution. |

---

## 5. Project Scope

### In-Scope (Deliverables for College Review)
1. **6 Independent Spring Boot Maven Applications:**
   - `eureka-server` (Port 8761)
   - `api-gateway` (Port 8080)
   - `auth-service` (Port 8081)
   - `property-service` (Port 8082)
   - `visit-service` (Port 8083)
   - `inquiry-service` (Port 8084)
2. **Service Discovery:** Netflix Eureka Server & Eureka Clients.
3. **Gateway Routing & Load Balancing:** Spring Cloud Gateway with reactive routing filters.
4. **Security:** Spring Security 6 with JWT token generation and validation.
5. **Business Domains:** Property listings catalog, inspection visit scheduler, buyer inquiry tracker.
6. **Synchronous Inter-Service Communication:** OpenFeign / RestClient orchestrating `Inquiry -> Property -> Visit`.
7. **In-Memory Storage:** Dedicated H2 databases with JPA/Hibernate entity mappings.
8. **Automated Tests:** Basic tests validating core endpoints and application contexts.
9. **Documentation:** Complete architectural specs, setup instructions, and demo guide.

### Out-of-Scope (Deferred for Post-Review Production Enhancements)
- Third-party payment gateways, bank financing calculators, or escrow contracts.
- External cloud identity providers (e.g., Keycloak, Auth0, Google OAuth2).
- Distributed event streaming message brokers (e.g., Apache Kafka / RabbitMQ) — synchronous REST is intentionally used to satisfy college review requirements cleanly.
- External persistent RDBMS servers (PostgreSQL / MySQL installation requirements are avoided to guarantee zero-friction local execution on any examiner's machine).
