# Bharat — "Instagram for Bharat"

Vernacular-first, lightweight social network for India. Posts, stories, rooms, and chat — built for 2GB-RAM phones and slow networks.

## Repo layout

```
apps/
  api/       NestJS 11 API (modular monolith)
  mobile/    Expo SDK 56 React Native app
packages/
  contracts/ Zod API contracts (single source of truth)
  shared/    Shared utils + error codes
```

## Docs

- `outputs/product-spec.md` — product spec (what we build and why)
- `outputs/architecture-decision.md` — architecture decision record (how we build it)
- `outputs/spec-set.provenance.md` — decision log

## Prerequisites

- Node.js >= 20.19
- Docker (Postgres 16 + Redis 7 for local dev)

## Quick start

```bash
npm install
npm run db:up            # start Postgres + Redis
npm run db:generate      # prisma generate
cp apps/api/.env.example apps/api/.env
npm run dev:api          # API on :3000
npm run dev:mobile       # Expo app
```

## Scripts

| Script | Purpose |
|---|---|
| `dev:api` | NestJS watch mode |
| `dev:mobile` | Expo dev server |
| `db:migrate` | Run prisma migrations |
| `build` / `lint` / `typecheck` / `test` | Across all workspaces |

## Standards

- Contract-first: every API request/response defined in `packages/contracts` (Zod), consumed by API for validation and mobile for types.
- ADR format in `outputs/architecture-decision.md` — decisions have rationale and reversal triggers.
