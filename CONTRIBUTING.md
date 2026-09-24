# Contributing

> Short guide for contributing to this repository.

---

## Branch model

This repository uses three persistent branches:

- `develop` — main development branch.
- `qa` — QA and validation branch.
- `main` — stable branch.

**Direct commits to `develop`, `qa`, or `main` are not allowed.**

---

## Workflow

### 1. Develop

```bash
git switch develop
git pull origin develop
git switch -c feat/<short-description>
```

Make changes, then:

```bash
git add .
git commit -m "feat(patient): <short description>"
git push -u origin feat/<short-description>
```

Open a Pull Request against `develop`. Wait for CI and 2 approvals, then merge.

### 2. Promote to `qa`

```bash
git switch qa
git pull origin qa
git switch -c promote/<short-description>
git cherry-pick -x <sha-of-the-commit-in-develop>
git push -u origin promote/<short-description>
```

Open a Pull Request against `qa`.

> **Note:** The `qa/` prefix is not usable as a branch name when a branch named `qa` exists.
> Use `promote/` instead.

### 3. Promote to `main`

```bash
git switch main
git pull origin main
git switch -c release/<version>
git cherry-pick -x <sha-of-the-commit-in-qa>
git push -u origin release/<version>
```

Open a Pull Request against `main`. Requires approval from `@ariel5253`.

> **The `-x` flag in `cherry-pick` is mandatory.** It preserves the traceability of the original commit.

---

## Commit convention

Conventional Commits:

```text
<type>(<scope>): <short description>
```

Types: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `perf`.

For this repository, use the scope `patient`:

```text
feat(patient): add GET /api/patients/me endpoint
fix(patient): correct birth_date validation
chore(patient): bump Spring Boot version
```

---

## Architecture

This service follows **Hexagonal Architecture** (Ports and Adapters):

```text
infrastructure  →  application  →  domain
```

The `domain` layer must NOT depend on Spring, JPA, or any infrastructure technology.

External technologies are accessed through application ports implemented by infrastructure adapters.

---

## Testing

- Unit tests for domain logic (`@Test`, no Spring context).
- Integration tests for controllers and persistence (`@SpringBootTest` + Testcontainers).
- Architecture tests to verify the hexagonal boundaries (ArchUnit).

Run locally:

```bash
mvn clean verify
```