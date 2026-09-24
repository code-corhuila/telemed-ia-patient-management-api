# telemed-ia-patient-management-api

> patient-management bounded context: service API (Spring Boot)

Part of the **TeleMed IA** distributed system — team `telemed-ia`, Grupo 2.
Governance and documentation live in [`telemed-ia-docs`](https://github.com/code-corhuila/telemed-ia-docs).

## Branching

Three permanent branches. **None of them accepts a direct commit** — you enter through a child
branch and leave through a Pull Request.

```
develop  <--PR--  feat/... fix/... chore/...
qa       <--PR--  qa/...
main     <--PR--  release/...  hotfix/...
```

Promotion happens **by re-application** (`git cherry-pick -x`), never by merging one permanent
branch into another.

`main` requires **1 approval from `ariel5253`**. On `develop` and `qa` the team sets its own review
rule.

Full policy: `00-governance/branching-policy.md` in `telemed-ia-docs`.

## Purpose

The Patient Management **service API** for TeleMed IA.

Exposes REST endpoints for patients to view and update their own profile. It is the runtime
component that connects to its own dedicated PostgreSQL database, whose schema is provisioned
by the sibling repository [`telemed-ia-patient-management-db`](https://github.com/code-corhuila/telemed-ia-patient-management-db).
The service enforces the domain rules of the Patient Management bounded context.

## Stack

| Component | Version |
|-----------|---------|
| Java | 17 |
| Spring Boot | 3.3.x |
| Spring Data JPA | (bundled) |
| PostgreSQL Driver | 42.7.x |
| Maven | 3.9+ |
| Docker / Docker Compose | latest |

## Endpoints

See the OpenAPI contract:

```
telemed-ia-docs/07-api/contracts/openapi/patient-service.yaml
```

Endpoints implemented:

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/patients/me` | Get the authenticated patient's profile |
| PUT | `/api/patients/me` | Update the authenticated patient's profile |
| POST | `/api/patients/internal/register` | Internal endpoint triggered by the `PatientRegistered` event |

## Local development

### Prerequisites

- Docker Desktop or Docker Engine
- Java 17
- Maven 3.9+

### Start the full stack

```bash
docker compose up -d
```

This starts:

- PostgreSQL 16 (port 5432)
- Flyway migrations (applies the schema)
- The API (port 8080)

### Verify

```bash
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

### Stop

```bash
docker compose down
```

## Related documentation

- `telemed-ia-docs/07-api/contracts/openapi/patient-service.yaml`
- `telemed-ia-docs/09-microservices/services/03-patient-service/`
- `telemed-ia-docs/06-data/models.md`

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md).