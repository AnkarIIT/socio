# Provenance: India Social App Specification

**Date:** 2025-01-25  
**Rounds:** 1 specification round (user-provided grounded spec)  
**Sources consulted:** User-provided technical specification (Expo SDK 56/57, RN 0.85 New Architecture, NestJS 11.x, Prisma 7.9)  
**Sources accepted:** 1 (complete user-provided spec)  
**Sources rejected:** 0  
**Verification:** PASS  
**Plan:** outputs/.plans/india-social-app-spec.md  
**Research files:** outputs/product-spec.md, outputs/architecture-decision.md

---

## Decision Log

| Decision | Rationale | Supersedes | Reversal Trigger |
|----------|-----------|------------|------------------|
| npm workspaces monorepo | Atomic changes across mobile/api, shared type safety | None | >10 engineers per team |
| Expo SDK 56/57 RN 0.85 New Architecture | Performance, New Architecture benefits | None | Stability blocks features |
| NestJS 11 + Express | Enterprise-grade, modular, large ecosystem | None | >100K RPS bottleneck |
| Prisma 7.9 + @prisma/adapter-pg | Required for Prisma 7, proper connection pooling | None | Complex query performance |
| Redis 7 + BullMQ | Retries, backoff, DLQ, job processing | None | Upstash limits hit |
| Socket.io + Redis adapter | Mature WebSocket with fallbacks, horizontal scaling | None | Overhead unacceptable |
| Cloudflare R2 + CDN | Zero egress, global edge, S3-compatible | None | SLA/pricing issues |
| Phone OTP primary (Msg91/Twilio) | Reliable Indian SMS delivery | None | OTP costs exceed budget |
| PG tsvector → OpenSearch | Zero external dep for MVP, defined migration trigger | None | >500K users or p95 >200ms |
| Expo Notifications | Unified FCM/APNs API | None | Feature limitations |
| i18next | Mature i18n, locale pipeline | None | Bundle size issues |
| pino + Sentry + OTel | Structured logs, error tracking, distributed tracing | None | Instrumentation overhead |
| Fly.io + Neon + Upstash | Minimal ops, Indian edge, serverless Postgres | None | Scale limits hit |
| Modular monolith | Clear boundaries, future microservice extraction | None | Defined scale triggers |
| Fanout-on-read hybrid feed | Cached follow graph + engagement ranking | None | Scale triggers |
| Offline-first with outbox | Works where IG fails, resilience | None | N/A |
| Low-data mode | Data cost sensitivity in India | N/A | N/A |
| IT Rules compliance baseline | Legal requirement for >5M users | N/A | Regulatory changes |
| <50MB app size | 2GB RAM device optimization | N/A | N/A |
| 2s cold start target | Retention on low-end devices | N/A | N/A |

---

## Notes

- All technical decisions are grounded in user-provided specifications with explicit version numbers.
- No external research was conducted; the user supplied the complete technical plan.
- Reversal triggers are defined for each major decision to maintain architectural flexibility.
