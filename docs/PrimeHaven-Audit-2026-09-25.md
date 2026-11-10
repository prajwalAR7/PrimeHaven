# PrimeHaven Current-State Audit

This is a source and environment audit conducted on 2026-09-25. No application code was modified.

## Evidence boundaries

Java 21.0.10 is installed. `mvn -version` fails because Maven is not available on PATH; no `mvnw`/`mvnw.cmd` and no application JARs were found. Therefore `mvn clean test`, service startup, HTTP health checks, Eureka registration, and gateway routing could **not** be executed. No expected application port (8080–8084, 8761) was listening at audit time. Existing `target/surefire-reports` show historic zero-failure test reports (2 tests each for auth/property/visit/inquiry, 1 each gateway/Eureka), but they are not current-build evidence.

## Applications and structure

| Module | Folder | Boot / Java / main class | Port | Current database | Key implementation |
|---|---|---|---:|---|---|
| Eureka server | `infrastructure/eureka-server` | 3.5.15 / 21 / `EurekaServerApplication` | 8761 | none | Eureka server, actuator, one context test |
| API gateway | `infrastructure/api-gateway` | 3.5.15 / 21 / `ApiGatewayApplication` | 8080 | none | WebFlux gateway, Eureka client, load balancer, custom JWT global filter, actuator, one context test |
| Auth | `services/auth-service` | 3.5.15 / 21 / `AuthServiceApplication` | 8081 | PostgreSQL `primehaven_auth_db` | JPA User/repository, BCrypt, JWT, security config, controller/service/DTOs/exceptions, 2 tests |
| Property | `services/property-service` | 3.5.15 / 21 / `PropertyServiceApplication` | 8082 | PostgreSQL `primehaven_property_db` | JPA Property/repository, CRUD/availability controller, DTOs/exceptions, 2 tests |
| Visit | `services/visit-service` | 3.5.15 / 21 / `VisitServiceApplication` | 8083 | PostgreSQL `primehaven_visit_db` | JPA Visit/repository, scheduling/status/cancel controller, DTOs/exceptions, 2 tests |
| Inquiry | `services/inquiry-service` | 3.5.15 / 21 / `InquiryServiceApplication` | 8084 | H2 memory `inquirydb` | JPA Inquiry/repository, Feign property+visit clients, DTOs/exceptions, 2 tests |

All module POMs have the Boot parent 3.5.15, Java 21, Spring Cloud 2025.0.3; Maven version is not declared. `README.md` and architecture docs instead claim Boot 3.3.4, Cloud 2023.0.3, and H2 for all services: these documents are stale.

Dependencies: auth has web/JPA/H2/PostgreSQL/security/validation/Eureka/JJWT/actuator/Lombok/test; property and visit have web/JPA/H2/PostgreSQL/Eureka/validation/actuator/Lombok/test; inquiry additionally has OpenFeign; gateway has WebFlux gateway/Eureka/load-balancer/security/OAuth2-resource-server/JJWT/actuator/test; Eureka has Netflix Eureka server/actuator/test.

## Build and runtime status

| Service | Command attempted | Build | Tests | Result |
|---|---|---|---|---|
| all six | `mvn -version` | NOT RUN | NOT RUN | `mvn` is not recognized; no wrapper exists |

No per-module clean test can be truthfully marked PASS. Historic reports are supplementary only. Startup is **NOT VERIFIED** for all six modules: Maven/JAR absence prevents launch and no ports were listening. Actual Eureka dashboard/registration state is therefore unknown/not demonstrated.

## Discovery and gateway

`@EnableEurekaServer` is present. Each other module has `@EnableDiscoveryClient` and points to `http://localhost:8761/eureka/`. Configured names, which Eureka conventionally displays uppercase, align with `lb://` route IDs: `auth-service`→`AUTH-SERVICE`, `property-service`→`PROPERTY-SERVICE`, `visit-service`→`VISIT-SERVICE`, `inquiry-service`→`INQUIRY-SERVICE`. The inquiry test properties contain an incorrect `eureka.instance.instance-id=inquiry-service:8082` despite `server.port=8084`; its test file also points to PostgreSQL, not a test H2 database.

| Gateway path predicate | Method(s) from controller | Target | Auth at gateway | Status |
|---|---|---|---|---|
| `/api/auth/**` | GET, POST | `lb://AUTH-SERVICE` | no | IMPLEMENTED in configuration |
| `/api/properties/**` | GET, POST, PUT, DELETE | `lb://PROPERTY-SERVICE` | GET except availability is public; mutating methods require JWT | IMPLEMENTED in configuration |
| `/api/visits/**` | GET, POST, PATCH | `lb://VISIT-SERVICE` | yes | IMPLEMENTED in configuration |
| `/api/inquiries/**` | GET, POST, PATCH | `lb://INQUIRY-SERVICE` | yes | IMPLEMENTED in configuration |

No discovery locator, CORS configuration, route filters, or gateway `SecurityWebFilterChain` was found. The global JWT filter validates HS256 with a hardcoded shared secret, removes caller-supplied `X-User-*`, and forwards token-derived username/role headers. It does **not** enforce roles; downstream property/visit/inquiry applications have no security configuration and do not consume the forwarded headers.

## API inventory

See `PrimeHaven-Postman-Guide.md` for full direct/gateway URLs, actual DTO fields, request examples, validation, success/error outcomes, prerequisites, and database effects. Controller mappings are: auth health/register/login/validate; property health/create/list/get/update/delete/availability; visit health/schedule/list/get/status/cancel; inquiry health/create/list/get/status; gateway health. Standard errors are 400 validation/bad request, 401 auth login/validation or gateway token rejection, 404 entity lookup; inquiry wraps Feign/property/visit failures as 400.

## JWT audit

Implemented: BCrypt password encoding; `users` JPA entity/repository; public register/login/validate paths; HS256 JJWT token generation/validation; claims `sub` and `role`, `iat`, `exp`; 24-hour configured expiration; stateless auth-service `SecurityFilterChain` permitting `/api/auth/**`, H2 console, actuator. Auth-service itself does not install a bearer-token authentication filter: only its three API paths are public and there are no other protected controller routes.

Security weaknesses for the review: hardcoded shared JWT secret in source/config; no role authorization despite roles being issued; gateway security starters include OAuth2 resource server but the implementation uses the custom filter and has no security chain; direct core-service URLs bypass gateway JWT enforcement; registration accepts arbitrary role strings (normalizes but does not allow-list); and error responses reveal JWT parser messages. Do not claim RBAC is working.

## Database audit

Auth, property, visit runtime configs use local PostgreSQL credentials `postgres/postgres`, `ddl-auto=update`; their H2 dependency is unused at runtime. Inquiry runtime uses volatile H2 memory with `ddl-auto=update` and console enabled. Entities/tables are `users`, `properties`, `visits`, `inquiries`; no JPA relationships exist—cross-service references are scalar IDs. There is no seed/data SQL. PostgreSQL/H2 runtime schema creation, connectivity, and persistence were not verified. To make demo data, register users then create a property before any inquiry; visit can be created independently, and a visit-request inquiry additionally needs property and visit services discoverable.

## Inter-service calls

`inquiry-service` → `property-service`: Feign client name `property-service`, GET `/api/properties/{id}/availability`, verifies availability.  
`inquiry-service` → `visit-service`: Feign client name `visit-service`, POST `/api/visits`, schedules a requested inspection.  
No RestTemplate/WebClient/hardcoded service URL was found. The workflow is non-transactional across services: a visit could persist if inquiry persistence then fails.

## Functional requirements

| Requirement | Status | Evidence / missing work |
|---|---|---|
| Property listing/retrieval/update/delete | IMPLEMENTED | PropertyController CRUD + repository; runtime unverified |
| Visit scheduling/retrieval/status/cancel | IMPLEMENTED | VisitController mappings; runtime unverified |
| Buyer inquiries | IMPLEMENTED | InquiryController/repository; runtime unverified |
| JWT authentication | PARTIALLY IMPLEMENTED | issuance and gateway validation exist; no effective role enforcement/direct-service protection |
| Eureka discovery / gateway / load balancing | PARTIALLY IMPLEMENTED | annotations/routes/lb URIs exist; no live proof |
| Inter-service communication | PARTIALLY IMPLEMENTED | two Feign clients exist; no end-to-end proof |
| Validation / exception handling | PARTIALLY IMPLEMENTED | DTO constraints and controller advice exist; statuses/enums not allow-listed |
| Basic testing | PARTIALLY IMPLEMENTED | source tests and historic reports exist; fresh test run blocked |

## Review and shortest safe demo path

Before demo, install Apache Maven 3.9+ or add Maven Wrapper; provision the three configured PostgreSQL databases and credentials (or deliberately change configuration in a later authorized fix). Then run `mvn clean test` independently in all six module folders, start Eureka, auth/property/visit/inquiry, then gateway, and prove `GET /actuator/health` for each plus Eureka dashboard. Register agent/buyer through gateway, create AVAILABLE property as agent token, show public GET property list, login buyer, submit visit-request inquiry, then GET visits. Show Eureka instances and the inquiry response with `visitDetails` as inter-service evidence.

For the review areas: Problem/requirements are documented in `docs/requirements.md`; microservice identification is demonstrable only after Eureka shows six instances; JWT proof is register/login then a protected gateway request without token (401) and with token; gateway proof is the exact `lb://` routes and gateway requests; linked DTI concepts evidenced by database-per-service configuration, discovery, gateway, Feign orchestration, but qualify them as source-implemented until live tested.

## Final status

```text
Total Services: 6
Buildable Services: 0 verified (build tooling absent)
Runnable Services: 0 verified (not started; no ports listening)
Eureka: PARTIAL
API Gateway: PARTIAL
JWT: PARTIAL
Property APIs: PARTIAL
Visit APIs: PARTIAL
Inquiry APIs: PARTIAL
Inter-Service Communication: PARTIAL
Database: PARTIAL
Postman Testing: PARTIAL
```

Remaining work before a credible review: restore Maven/wrapper; fresh-build all six; start and health-test all modules; make DB configuration consistent with the intended demo; correct inquiry test configuration/instance ID; prove Eureka registrations and gateway routes; implement/enforce role authorization and service-side protection; externalize the JWT secret; add seed/demo data or use the documented requests; and run an end-to-end inquiry→property→visit test.
