# Provenance: Product + Architecture Spec Set

**Date:** 2026-08-16
**Artifacts:**
- `outputs/product-spec.md`
- `outputs/architecture-decision.md`
- This sidecar

**Status:** Committed for MVP v0.1 (single-developer build)

---

## Decision Log

| Decision | Choice | Alternatives considered | Rationale | Supersedes |
|---|---|---|---|---|
| Build pattern | Modular monolith (NestJS modules) | Microservices (research-recommended) | Solo build speed; mechanical split later | Research §1.2 "microservices at scale" for the MVP phase |
| Mobile framework | Expo SDK 56 (RN 0.85, New Arch) | Native Kotlin/Swift, Flutter | Solo speed, one language; New Arch-only current | Research §7.2 "native preferred for low-end" — mitigated by perf budget |
| API framework | NestJS 11 (Express) | Fastify, Hono, Go | Module boundaries = future services; WS/BullMQ integration | — |
| Data | PostgreSQL 16 + Prisma 7 (adapter-pg) | MongoDB, Cassandra | Relational core; solo-friendly; PG full-text search covers v0.1 | Research §1.3 wide-column Cassandra (deferred to scale) |
| Queue | Redis (Upstash) + BullMQ | Kafka | Zero infra, retries+DLQ built in | Research §1.4 Kafka (G2) |
| Real-time | socket.io + redis adapter | Native WS, CPaaS | Standard, solo-friendly | — |
| Storage/CDN | Cloudflare R2 + CDN | AWS S3+CloudFront | Zero egress, one vendor | — |
| Feed | Fanout-on-read hybrid | Fanout-on-write (Instagram-style) | Correct at MVP scale; simplest correct thing | Research §1.2 feed fanout at scale (G2) |
| Auth | Phone OTP (Msg91) + email + Google | Email-only | India phone-first identity | — |
| Search | PG tsvector | Elasticsearch/OpenSearch | Zero infra v0.1 | Research §1.3 (G2) |
| Hosting | Fly.io Mumbai + Neon + Upstash | AWS ECS | Solo ops; near-India region | Research §7.3 Kubernetes (G2) |
| Media weight | Lightweight (text/image + ≤60s clips) | Video-first Reels-style | Cohort undecided; cheapest correct path | Open question → G1 gate |
| Instagram strategy | Underserved-segment wedge + superposition loops | Head-on feature parity | Structurally winnable | — |

---

## Notes & Caveats

- **Scope discipline:** v0.1 deliberately omits live streaming, AR, gifting, ads, payments, e2ee, ML personalization, and full microservices. Each has a documented gate/trigger. This is the largest conscious divergence from the research report and is a *timing* decision, not a disagreement.
- **Version pins** grounded 2026-08: Expo SDK 56 (RN 0.85, React 19.2), NestJS 11.x, Prisma 7.9, PostgreSQL 16, Node 22 (installed locally), Redis 7.
- **Paid tiers assumed:** Msg91, Neon, Upstash, Cloudflare, Fly.io, Sentry.
- **Solo constraint:** milestone estimates assume one developer; parallel workstreams would compress M2–M4, not M1 or M5.
- **Open items needing owner action:** cohort decision (G1 gate), IT Rules traceability legal posture, vernacular moderation program, creator-payout tax structure. None block M1.
- **Next artifact (M1):** repository scaffold per §3 layout — `git init`, npm workspaces, `packages/contracts`, NestJS API, Expo app, `docker-compose.yml`, CI.

---

## M1 Build Log (2026-08-16)

- **Implemented deviations (supersede earlier choices):**
  - *Request validation:* implementation uses **Zod schemas** (`packages/contracts`) via a `ZodValidationPipe` — supersedes the ADR's `class-validator`/`class-transformer` choice. Keeps contracts/types as the single source of truth for request validation. (`class-validator`/`class-transformer` no longer installed.)
  - *Model naming:* schema model is `OtpChallenge` (Prisma camelCases to `otpChallenge`), not `OTPChallenge`.
  - *Health checks:* plain handlers returning `{status}` under `/healthz` + `/readyz` (no terminus dependency).
- **Scaffold facts:** NestJS 11 CLI (`--strict`), `create-expo-app` `default@sdk-56` (Expo `~56.0.19`, RN `0.85.3`, React `19.2.3`), Prisma 7.9.1 + `@prisma/adapter-pg`, generated client committed to `apps/api/src/generated/prisma` (CJS, tsconfig `include: ["src"]` → clean `dist/main.js`).
- **Migrations applied locally:** `20260816180057_init`, `20260816180808_rename_otp_model`, `20260816181538_otp_challenge_standalone` (OTP challenge decoupled from `User` FK — pre-registration OTP must not require a user row).
- **Smoke-tested end-to-end (local PG16 + Redis7):** `/healthz`, `/readyz` (DB+Redis up, public); OTP request → dev-provider code in logs; verify issues access+refresh tokens and auto-creates user; `/v1/users/me`; garbage token → 401 envelope; refresh rotation; replayed refresh token → 401 "Session expired"; logout revokes session; bad payload → `VALIDATION` envelope with field details.
- **Pending M2 wiring:** Msg91 provider (env-gated), Redis rate-limiter policy tuning, `@nestjs/terminus`-free readiness (current), mobile→API contracts consumption (root `dev:mobile` targets `@bharat/mobile`).
