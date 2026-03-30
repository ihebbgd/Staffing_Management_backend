# Production Operations Guide

Operational notes for deploying the Staffing Management backend. Pairs with `.env.example`.

## Running in production

Activate the `prod` profile so development conveniences are switched off:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar Staffing_Management_backend-*.jar
```

The `prod` profile (`application-prod.yaml`) layers on top of `application.yaml` and:

- Disables the OpenAPI schema (`/v3/api-docs/**`) and Swagger UI (`/swagger-ui/**`). When disabled,
  `SecurityConfig` also stops treating those paths as anonymously accessible — the full endpoint map is
  never published to unauthenticated callers in production.
- Keeps application logging at `INFO` (no request/entity payloads in logs). The base config already
  defaults to `INFO`; raise a package to `DEBUG` locally only when needed.

## Required environment variables

All secrets are read from the environment (loaded from a local `.env` in development). See `.env.example`.

| Variable | Required | Notes |
|----------|----------|-------|
| `MONGODB_URI` | yes | MongoDB connection string. Must point at a **replica set** (see below). |
| `JWT_SECRET` | yes | Base64-encoded HMAC key, ≥32 bytes decoded. Generate with `openssl rand -base64 48`. |
| `APP_ADMIN_PASSWORD` | yes | Initial ADMIN password, ≥12 characters (enforced at startup). |
| `APP_CORS_ALLOWED_ORIGINS` | yes (prod) | Comma-separated exact origins for the Angular app. |
| `APP_ADMIN_USERNAME` / `APP_ADMIN_EMAIL` | no | Override the seeded admin identity. |
| `APP_SEED_SAMPLE_DATA` | no | Demo data loader. **Ignored under the `prod` profile** regardless of value. |

### Credential hygiene

- Rotate the MongoDB database-user password and the `JWT_SECRET` to strong, generated values before
  going live. Never commit a real `.env` (it is git-ignored; only `.env.example` is tracked).
- Rotating `JWT_SECRET` invalidates all previously issued access/refresh tokens (users re-authenticate).

## MongoDB replica set (required)

Employee create/delete run inside a MongoDB transaction (`@Transactional`, see `config/MongoConfig`),
so multi-collection writes commit or roll back atomically. MongoDB transactions require the server to be
a **replica set**:

- **MongoDB Atlas** clusters are always replica sets — no action needed.
- A local **standalone `mongod`** does not support transactions. For local development that exercises the
  employee create/delete paths, run mongod as a single-node replica set (`mongod --replSet rs0` then
  `rs.initiate()`).

## API notes for the frontend

- **Authentication:** all `/api/**` endpoints require a Bearer access token except `/api/auth/**` and the
  anonymous `/actuator/health` probe. Access tokens live 15 min; refresh tokens 7 days.
- **Authorization:** write operations and cross-employee reads require `ADMIN`/`MANAGER`. Employees read
  their own data through `/api/me/**`.
- **Pagination:** all collection endpoints return a Spring `Page` object
  (`{content, totalElements, totalPages, number, size}`) and accept `?page=&size=&sort=`. Default page
  size 20, max 100. This includes `/api/employee-skills` and `/api/notifications/recipient/{id}`.
- **Errors:** a consistent envelope `{timestamp, status, error, message, path}` for every failure. Internal
  details and stack traces are never returned to clients.

## Known follow-ups (not blocking, scale-triggered)

- Reporting/dashboard/bench aggregation is computed in-memory. Fine at current data volumes; migrate the
  hot paths to MongoDB aggregation pipelines (`$match`/`$group`/`$lookup`) or pre-aggregated summary
  documents once collections grow large enough to measure a benefit.
- Consider a TTL index or scheduled cleanup for old, read notifications.
- API versioning (`/api/v1/...`) is intentionally omitted for this single-frontend internal API; introduce
  it only if the API is exposed to independent third-party consumers.
