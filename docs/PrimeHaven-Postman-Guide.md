# PrimeHaven Postman Testing Guide — Source Audit

Audit date: 2026-09-25. This guide contains only mappings found in the current controllers and gateway configuration. It is a request specification, not evidence that the applications are running.

## Required environment

Set these collection variables after startup:

```text
gatewayUrl = http://localhost:8080
authUrl = http://localhost:8081
propertyUrl = http://localhost:8082
visitUrl = http://localhost:8083
inquiryUrl = http://localhost:8084
jwt = token returned by registration or login
propertyId = id returned by Create property
visitId = id returned by Schedule visit
inquiryId = id returned by Create inquiry
```

Direct service calls are not protected by Spring Security in property, visit, or inquiry service: those POMs have no security starter/configuration. At the gateway, every route except `/api/auth/**`, `/actuator/**`, `/api/gateway/health`, and GET `/api/properties...` requires `Authorization: Bearer {{jwt}}`.

## Authentication flow

### API TEST 1 — Register agent

METHOD: `POST`  
DIRECT ENDPOINT: `{{authUrl}}/api/auth/register`  
GATEWAY ENDPOINT: `{{gatewayUrl}}/api/auth/register`  
HEADERS: `Content-Type: application/json`  
BODY:

```json
{"username":"agent_sarah","email":"sarah@primehaven.test","password":"Password123!","role":"ROLE_AGENT"}
```

Expected: `201 Created`; actual response fields are `token`, `tokenType`, `username`, `email`, `role`, `expiresInMs`. Validation requires username 3–50 characters, valid email, and password at least 6 characters. Duplicate username/email returns 400.

### API TEST 2 — Register buyer

METHOD: `POST`  
DIRECT ENDPOINT: `{{authUrl}}/api/auth/register`  
GATEWAY ENDPOINT: `{{gatewayUrl}}/api/auth/register`  
HEADERS: `Content-Type: application/json`  
BODY:

```json
{"username":"buyer_david","email":"buyer.david@primehaven.test","password":"Password123!","role":"ROLE_BUYER"}
```

Expected: `201 Created`. Save its `token` to `jwt` for gateway-protected requests.

### API TEST 3 — Login

METHOD: `POST`  
DIRECT ENDPOINT: `{{authUrl}}/api/auth/login`  
GATEWAY ENDPOINT: `{{gatewayUrl}}/api/auth/login`  
HEADERS: `Content-Type: application/json`  
BODY:

```json
{"username":"buyer_david","password":"Password123!"}
```

Expected: `200 OK` and the same `AuthResponse` fields. Invalid credentials return 401. JWT is HS256 with subject=username, claim `role`, issue time, expiration; configured lifetime is 86,400,000 ms (24 hours).

### API TEST 4 — Validate token

METHOD: `GET`  
DIRECT ENDPOINT: `{{authUrl}}/api/auth/validate`  
GATEWAY ENDPOINT: `{{gatewayUrl}}/api/auth/validate`  
HEADERS: `Authorization: Bearer {{jwt}}`  
BODY: none  
Expected: `200 OK` with `valid`, `username`, `role`, `message`; missing/invalid token gives 401.

## Property service (`property-service`, direct port 8082)

### API TEST 5 — Create property

METHOD: `POST`  
DIRECT ENDPOINT: `{{propertyUrl}}/api/properties`  
GATEWAY ENDPOINT: `{{gatewayUrl}}/api/properties`  
HEADERS: `Content-Type: application/json`; gateway also requires `Authorization: Bearer {{jwt}}`  
BODY:

```json
{"title":"Palm Grove Villa","description":"Four-bedroom villa with a pool","address":"42 Palm Grove Road","city":"Bengaluru","price":12500000.00,"propertyType":"VILLA","status":"AVAILABLE","bedrooms":4,"bathrooms":3,"agentUsername":"agent_sarah"}
```

Expected: `201 Created`, response fields `id`, `title`, `description`, `address`, `city`, `price`, `propertyType`, `status`, `bedrooms`, `bathrooms`, `agentUsername`, `createdAt`, `updatedAt`. Required: title, address, city, positive price, propertyType, agentUsername. Effect: inserts `properties` row.

### API TEST 6 — List properties

METHOD: `GET`  
DIRECT ENDPOINT: `{{propertyUrl}}/api/properties?city=Bengaluru&status=AVAILABLE&maxPrice=13000000`  
GATEWAY ENDPOINT: same path at `{{gatewayUrl}}`  
HEADERS: none (gateway makes GET property browsing public)  
Expected: `200 OK`, array of PropertyResponse. All three query parameters are optional. No database effect.

### API TEST 7 — Get property

METHOD: `GET`  
DIRECT ENDPOINT: `{{propertyUrl}}/api/properties/{{propertyId}}`  
GATEWAY ENDPOINT: same path at `{{gatewayUrl}}`  
HEADERS: none  
Expected: `200 OK` PropertyResponse; nonexistent id returns 404. No database effect.

### API TEST 8 — Update property

METHOD: `PUT`  
DIRECT ENDPOINT: `{{propertyUrl}}/api/properties/{{propertyId}}`  
GATEWAY ENDPOINT: same path at `{{gatewayUrl}}`  
HEADERS: `Content-Type: application/json`; gateway requires bearer JWT  
BODY: the same valid `PropertyRequest` body from API TEST 5.  
Expected: `200 OK`; nonexistent id 404; validation error 400. Effect: updates row.

### API TEST 9 — Availability (internal API)

METHOD: `GET`  
DIRECT ENDPOINT: `{{propertyUrl}}/api/properties/{{propertyId}}/availability`  
GATEWAY ENDPOINT: same path at `{{gatewayUrl}}`  
HEADERS: gateway requires bearer JWT  
Expected: `200 OK` with fields `propertyId`, `available`, `title`, `status`, `price`; nonexistent id 404. This is called by inquiry-service via Feign.

### API TEST 10 — Delete property

METHOD: `DELETE`  
DIRECT ENDPOINT: `{{propertyUrl}}/api/properties/{{propertyId}}`  
GATEWAY ENDPOINT: same path at `{{gatewayUrl}}`  
HEADERS: gateway requires bearer JWT  
Expected: `200 OK` with `{"message":"Property deleted successfully"}`; missing id 404. Effect: deletes row.

## Visit service (`visit-service`, direct port 8083)

### API TEST 11 — Schedule visit

METHOD: `POST`  
DIRECT ENDPOINT: `{{visitUrl}}/api/visits`  
GATEWAY ENDPOINT: `{{gatewayUrl}}/api/visits`  
HEADERS: `Content-Type: application/json`; gateway requires bearer JWT  
BODY:

```json
{"propertyId":{{propertyId}},"buyerUsername":"buyer_david","visitDate":"2026-10-05","timeSlot":"10:00 AM - 11:00 AM","notes":"Please show the garden."}
```

Expected: `201 Created`; VisitResponse fields `id`, `propertyId`, `buyerUsername`, `visitDate`, `timeSlot`, `status`, `notes`, `createdAt`; `status` is set to `SCHEDULED`. Required: propertyId, buyerUsername, visitDate, timeSlot. Note: despite an unused `FutureOrPresent` import, the date is not annotated for future validation. Effect: inserts `visits` row.

### API TEST 12 — Query visits

METHOD: `GET`  
DIRECT ENDPOINT: `{{visitUrl}}/api/visits?buyerUsername=buyer_david&propertyId={{propertyId}}`  
GATEWAY ENDPOINT: same path at `{{gatewayUrl}}`  
HEADERS: gateway requires bearer JWT  
Expected: `200 OK`, array. Both query parameters are optional.

### API TEST 13 — Get visit / update status / cancel

`GET {{visitUrl}}/api/visits/{{visitId}}` (gateway requires JWT): `200` VisitResponse or 404.  
`PATCH {{visitUrl}}/api/visits/{{visitId}}/status` (gateway requires JWT), JSON `{"status":"CONFIRMED"}`: `200`; status only has `@NotBlank`, so no enum whitelist is enforced.  
`POST {{visitUrl}}/api/visits/{{visitId}}/cancel` (gateway requires JWT): `200`; effect sets status to `CANCELLED`.

## Inquiry service (`inquiry-service`, direct port 8084)

### API TEST 14 — Create inquiry without visit

METHOD: `POST`  
DIRECT ENDPOINT: `{{inquiryUrl}}/api/inquiries`  
GATEWAY ENDPOINT: `{{gatewayUrl}}/api/inquiries`  
HEADERS: `Content-Type: application/json`; gateway requires bearer JWT  
BODY:

```json
{"propertyId":{{propertyId}},"buyerUsername":"buyer_david","inquiryType":"GENERAL","message":"Could you share the maintenance details?","requestVisit":false}
```

Expected: `201 Created`; first validates availability by Feign GET to property service. Response fields include `id`, `propertyId`, `propertyTitle`, `buyerUsername`, `inquiryType`, `message`, `status`, `visitId`, `visitDetails`, `createdAt`. Effect: inserts `inquiries` row. Preconditions: Eureka, property service and propertyId must be available.

### API TEST 15 — Create inquiry that schedules a visit

METHOD: `POST`  
DIRECT ENDPOINT: `{{inquiryUrl}}/api/inquiries`  
GATEWAY ENDPOINT: `{{gatewayUrl}}/api/inquiries`  
HEADERS: `Content-Type: application/json`; gateway requires bearer JWT  
BODY:

```json
{"propertyId":{{propertyId}},"buyerUsername":"buyer_david","inquiryType":"VISIT_REQUEST","message":"I want to inspect this villa.","requestVisit":true,"visitDate":"2026-10-05","timeSlot":"10:00 AM - 11:00 AM","visitNotes":"Please cover parking details."}
```

Expected: `201 Created`; it calls property availability then POSTs to visit service. `visitDate` and nonblank `timeSlot` are additionally required when `requestVisit` is true. Failure to resolve either Feign service or an unavailable property is converted to `400 Bad Request`. Effect: creates a visit first, then an inquiry containing its `visitId`; there is no distributed transaction/rollback.

### API TEST 16 — Query/update inquiry

`GET {{inquiryUrl}}/api/inquiries?buyerUsername=buyer_david&propertyId={{propertyId}}` (gateway requires JWT): `200` array; both filters optional.  
`GET {{inquiryUrl}}/api/inquiries/{{inquiryId}}` (gateway requires JWT): `200` or 404.  
`PATCH {{inquiryUrl}}/api/inquiries/{{inquiryId}}/status` (gateway requires JWT), JSON `{"status":"RESOLVED"}`: `200`; only nonblank validation is implemented.  

## Health endpoints

The custom health mappings are `GET /api/auth/health`, `/api/properties/health`, `/api/visits/health`, `/api/inquiries/health`, and `/api/gateway/health`; each returns `{ "service": "...", "status": "UP" }` from source. Actuator `GET /actuator/health` is exposed in all six modules, but was not live-tested in this audit.
