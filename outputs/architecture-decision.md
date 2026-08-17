# Architecture Decision Record — "Instagram for Bharat"

**Date:** 2026-08-16
**Status:** Committed for MVP v0.1
**Companion:** `product-spec.md` (product spec)
**Basis:** `india-social-app-arch.md` (Feynman research report)

> Format: every decision is **Decision → Rationale → Reversal trigger**. No unresolved options. Where this spec deliberately diverges from the research report (which describes Instagram-scale microservices), the divergence is called out with its trigger.

---

## 1. Principles (what we optimize for)

1. **Solo-buildable.** One developer must be able to ship v0.1. Complexity is deferred, not hidden.
2. **India-first performance.** 2GB-RAM / Android 9 devices, 2G–4G networks, <50MB APK, low-data mode, cold start <2s.
3. **Monolith that can split.** Clean module boundaries map 1:1 to future services. The split is mechanical, not a rewrite.
4. **Postgres is the source of truth.** Redis/socket are accelerators, never authoritative.
5. **Everything fails; design for degradation.** Offline-first UX, retryable jobs, idempotent writes, graceful degradation of optional deps.

---

## 2. Committed Technology Stack

| Layer | Decision | Rationale | Reversal trigger |
|---|---|---|---|
| Language/runtime | Node.js 22 + TypeScript (strict) | Installed; solo-friendly; one language across mobile/API | None — permanent |
| Monorepo | npm workspaces (no Turborepo initially) | Zero extra infra; enough for 2 apps + 2 packages | Builds >2min or >5 packages |
| Mobile | **Expo SDK 56** (RN 0.85, React 19.2, New Architecture-only) | Managed workflow = solo speed; SDK 57 too fresh | SDK 57 stabilizes → upgrade |
| Mobile routing/state | Expo Router · Zustand (client state) · TanStack Query (server state) | File-based routing; minimal boilerplate | — |
| Mobile DB/cache | `expo-sqlite` + custom query layer (feeds, outbox) | Offline-first requirement | — |
| API framework | **NestJS 11** (Express platform) | Module boundaries = future microservices; built-in WS/BullMQ/validation/DI | Cold start >500ms in prod → switch to Fastify adapter (same NestJS) |
| Validation | class-validator + class-transformer, DTOs everywhere | Boundary-trust model | — |
| ORM | **Prisma 7** + `@prisma/adapter-pg` (pg driver) | v7 requires driver adapter; type-safe; migrations built-in | Query perf issues → add raw SQL views / read replicas |
| Database | PostgreSQL 16 (Neon managed) | Relational core (graph, content, transactions); Neon = serverless + Mumbai region + scale-to-zero | — |
| Cache | Redis 7 (Upstash) | Sessions, hot feed cache, rate limiting, pub/sub | — |
| Queue | **BullMQ** (on Redis) | Media, fanout, notification jobs; retries + backoff + DLQ built-in | >10 job types or per-job throughput pressure → Kafka |
| Real-time | socket.io + `@socket.io/redis-adapter` | Presence, typing, chat, live toasts | Chat scales → dedicated service (G2) |
| Object storage | **Cloudflare R2** (S3-compatible) | Zero egress fees + Cloudflare CDN in one vendor; presigned uploads | — |
| CDN | Cloudflare | Global + India PoPs; signed URLs | Regional latency issues → India-specific CDN (G2) |
| Media processing | Sharp (images) + FFmpeg (video) in BullMQ workers | Solo-runnable; containerized | Transcoding volume → dedicated media service |
| Auth | Phone OTP primary (Msg91, Twilio fallback) + email/password + Google OAuth; JWT access (15m) + rotating refresh (30d) | India-first phone identity; Msg91 has India SMS + WhatsApp OTP | — |
| Search | PG `tsvector` full-text (search module) | Zero infra; good enough for v0.1 | G2 → OpenSearch |
| Push | Expo Notifications (FCM + APNs) | Wraps both platforms; no native push code | — |
| i18n | i18next + locale pipeline; v0.1 English + Hindi | Largest vernacular coverage first | — |
| Feature flags | Env + `feature_flags` table (DB-backed, cached) | Kill-switch for risky toggles | — |
| Logging | pino structured logs, `requestId` correlation | Fast, JSON, correlates with Sentry | — |
| Errors/APM | **Sentry** (mobile + API) | Crash-free sessions, breadcrumbs, release tracking | — |
| Tracing | OpenTelemetry (HTTP + Prisma) → Grafana/Tempo later | Standard, vendor-neutral | — |
| Deployment | Docker Compose (dev) → **Fly.io Mumbai** + Neon + Upstash (MVP) | Solo ops; Fly = apps near users | G2 → AWS `ap-south-1` (ECS) |
| CI/CD | GitHub Actions (lint → typecheck → test → build → migrate → deploy) | Free, standard | — |
| Secrets | GitHub repo secrets + `.env` vault pattern; no secrets in code | Security baseline | — |

**Stack divergence note vs research report:** the research recommends Go for chat and microservices at scale. We are a solo-built MVP; Node/NestJS covers all workloads to mid-scale. Go is the documented chat-service migration path at G2, not a v0.1 choice.

---

## 3. Repository Layout

```
social/
├─ apps/
│  ├─ mobile/                  # Expo SDK 56 app
│  │  ├─ app/                  # Expo Router routes
│  │  ├─ src/
│  │  │  ├─ api/               # typed API client (contracts)
│  │  │  ├─ components/
│  │  │  ├─ features/          # feature folders (feed, auth, chat…)
│  │  │  ├─ state/             # Zustand stores
│  │  │  ├─ db/                # sqlite cache + outbox
│  │  │  ├─ i18n/              # locales (en, hi)
│  │  │  └─ ws/                # socket.io client
│  │  └─ eas.json
│  └─ api/                     # NestJS 11
│     └─ src/
│        ├─ modules/           # one folder per module (see §4)
│        ├─ common/            # interceptors, filters, guards, decorators
│        ├─ prisma/            # schema.prisma, seed
│        └─ main.ts
├─ packages/
│  ├─ contracts/               # shared Zod schemas + TS types (API contract)
│  └─ shared/                  # utils, error codes enum, date utils
├─ docker-compose.yml          # postgres + redis for dev
├─ infra/                      # fly.toml, Dockerfiles, deploy scripts
└─ .github/workflows/          # ci.yml, deploy.yml
```

**Contract-first:** `packages/contracts` defines every API request/response as Zod schemas. API uses them for validation; mobile uses them for types. A single source of truth, no drift.

---

## 4. Modular Monolith

### 4.1 Modules and their future-service mapping

| NestJS module | Owns (tables) | Exposed to other modules via | Future service |
|---|---|---|---|
| `auth` | Session, OTPChallenge | `IAuthService` (issue/verify tokens) | auth-svc |
| `user` | User, Profile | `IUserService` | user-svc |
| `content` | Post, MediaAsset, Story, Hashtag | `IContentService` | content-svc |
| `media` | (R2/transcode orchestration) | `IMediaService` (presign, transcode) | media-svc |
| `social` | Follow, Block, Mute, Room, RoomMember | `ISocialService` (graph queries) | social-graph-svc |
| `feed` | (read model, cached) | `IFeedService` | feed-svc |
| `chat` | ChatRoom, Message | `IChatService` | chat-svc |
| `notification` | Notification, NotificationPreference | `INotificationService` | notif-svc |
| `moderation` | Report, Appeal, GrievanceTicket | `IModerationService` | moderation-svc |
| `search` | (PG tsvector queries) | `ISearchService` | search-svc |
| `admin` | (back-office APIs, guarded) | — | admin-console |

**Rules:**
- Cross-module access only through exported interface + explicit provider token (no reaching into another module's repo/table).
- Each module owns its tables; foreign keys are the only cross-table coupling.
- Business logic lives in module services (not controllers, not in DB).
- The `feed` module reads via `social`/`content` interfaces + a cached read model.

### 4.2 Split triggers (extract when metrics demand)
- **G2 (>500k MAU):** extract `feed` + `media` into separate deployables first (they touch the most infra); then `chat` when socket nodes exceed 3.
- **Trigger signals:** feed p95 >120ms sustained; BullMQ queue depth >50k; deployment becomes a pain (>30min).

---

## 5. Data Model (Prisma 7)

Key models (full schema in `apps/api/src/prisma/schema.prisma` during M1):

```
User            id, phone, email?, name, username (unique), avatarUrl, coverUrl,
                locale, isVerified, isPrivate, bannedAt?, createdAt
Profile         id, userId 1:1, bio, website, links[], highlights[], languageTags[]
Session         id, userId, refreshTokenHash, device, ip, expiresAt, revokedAt
OTPChallenge    id, phone, codeHash, attempts, expiresAt, verifiedAt
Post            id, authorId, text?, type (TEXT|IMAGE|CLIP|MIXED), status
                (DRAFT|PENDING|PUBLISHED|REJECTED|TAKEDOWN), langTag,
                score, likeCount, commentCount, shareCount, idempotencyKey,
                publishedAt, createdAt
MediaAsset      id, postId?/storyId?, userId, kind (IMAGE|VIDEO),
                variants (JSON: {avif, webp, jpeg, mp4, hls}), sizeBytes,
                width, height, durationMs?, status
Story           id, userId, mediaAssets[], expiresAt, seenBy[]
Comment         id, postId, authorId, parentId?, text, status
Like            id, postId, userId, reaction (EMOJI)      @@unique(postId,userId)
Follow          id, followerId, followeeId, createdAt     @@unique(followerId,followeeId)
Block           id, blockerId, blockedId                  @@unique(blockerId,blockedId)
Mute            id, muterId, muteeId, until?
Hashtag         id, tag (unique, lower), langTag?, postCount
Room            id, name, description, ownerId, langTag?, isPrivate, coverUrl
RoomMember      id, roomId, userId, role                   @@unique(roomId,userId)
ChatRoom        id, kind (DIRECT|GROUP), members[], lastMessageId?, createdAt
Message         id, roomId, senderId, type (TEXT|IMAGE|STICKER), text?, mediaAssetId?,
                status (SENT|DELIVERED|READ), createdAt
Notification    id, userId, actorId?, type, entityRef (JSON), isRead, createdAt
Report          id, reporterId, targetType, targetId, reason, status (OPEN|REVIEWED|TAKEDOWN|DISMISSED)
Appeal          id, reportId, userId, reason, status
GrievanceTicket id, externalRef (law-enforcement ref), status, slaDeadline, resolution
CreatorStats    id, userId, period, postCount, viewCount, followerGained, earningsINR?
FeatureFlag     id, key, enabled, rolloutPct
```

**Key indexes:**
- Feed: `Post(publishedAt DESC)` + partial index on `status=PUBLISHED`; keyset `(publishedAt, id)`.
- Like/unlike: `@@unique(postId, userId)` → 409-conflict-safe.
- Messages: `Message(roomId, createdAt)` for pagination.
- Notifications: `Notification(userId, isRead, createdAt DESC)`.
- Search: GIN `tsvector` on `Post(text)`, `User(name)` with language config.

**Constraints & integrity:**
- Usernames unique + case-insensitive (store lowercase, display original).
- Idempotency keys (`unique`) on Post.create, Like.create, Message.send, Follow.create → safe retries.
- All monetary fields stored as integer paise (`Int`), never float. Creator payout tables UPI-ready (bankAccount, upiId, taxRef) in phase 2 schema.

---

## 6. API Surface

### 6.1 REST resources (versioned `/v1`)
| Resource | Endpoints |
|---|---|
| Auth | `POST /auth/otp/request` · `POST /auth/otp/verify` · `POST /auth/refresh` · `POST /auth/logout` · `POST /auth/oauth/{provider}` |
| Users | `GET/PATCH /users/me` · `GET /users/:username` · `GET /users/:id/posts` · `POST /users/me/avatar` |
| Social | `POST/DELETE /users/:id/follow` · `GET /users/:id/followers` · `GET /users/:id/following` · `POST/DELETE /blocks/:id` · `POST /contacts` (hashed) |
| Posts | `POST /posts` (idempotency-key) · `GET /posts/:id` · `DELETE /posts/:id` · `POST /posts/:id/like` · `POST /posts/:id/reactions` · `GET /posts/:id/comments` · `POST /posts/:id/comments` · `POST /posts/:id/report` |
| Stories | `POST /stories` · `GET /stories/feed` · `POST /stories/:id/seen` |
| Rooms | `POST /rooms` · `GET /rooms/:id` · `POST /rooms/:id/join` · `GET /rooms/:id/posts` |
| Feed | `GET /feed?cursor=` (home) · `GET /explore?lang=&cursor=` |
| Search | `GET /search?q=&type=` |
| Chat | `GET /chats` · `GET /chats/:id/messages?cursor=` · `POST /chats/:id/messages` (idem-key) · `POST /chats` |
| Notifications | `GET /notifications?cursor=` · `PATCH /notifications/read` · `PATCH /notifications/prefs` |
| Moderation | `POST /reports` · `POST /appeals` · `GET /grievances` (admin) |
| Media | `POST /media/presign` (returns upload URL + `objectKey`) · `POST /media/complete` |
| Flags | `GET /flags/:key` |

### 6.2 WebSocket events (socket.io, `/ws`, JWT handshake)
- `message:new` · `message:typing` · `message:read`
- `notification:toast`
- `presence:online/:userId`
- `post:live` (toasts for followed creators, digest-gated)
- Client acks with `ack` callbacks; message send is REST (idempotency-safe), WS only for delivery events.

### 6.3 Error envelope (see §8)

---

## 7. Key Subsystems

### 7.1 Feed (fanout-on-read)
- **Home:** `feed` module reads follows from cached graph (Redis set per user, refreshed from social module), queries posts via content module with keyset cursor, computes `score = 1/(1+ageHours)^0.5 × log10(1+engagement)` in SQL, paginates.
- **Cache:** top N feed items per user cached in Redis (TTL 60s); cache miss = recompute.
- **Explore:** `status=PUBLISHED AND langTag IN user.languages` ordered by score, keyset.
- **Stories feed:** follows' stories `expiresAt > now`, oldest first, bundled per author.

### 7.2 Media pipeline
```
Client → POST /media/presign → R2 presigned PUT (client streams direct)
      → POST /media/complete → BullMQ job [validate → scan → transcode]
         · Sharp: WebP+AVIF+progressive JPEG (multi-res)
         · FFmpeg: H.264 MP4 (≤60s); HLS if >30s
      → variants in R2 → CDN cache warm → post.status=PUBLISHED → fanout notify
```
- Uploads chunked with resume; size caps (image 10MB, clip 100MB).
- Thumbnails generated (feed + story ring use small variants).
- Signed CDN URLs with TTL; `Cache-Control: public, immutable` on variants keyed by hash.

### 7.3 Chat
- Rooms persisted in PG; message create via REST (idempotency key) → BullMQ lightweight delivery → WS fanout via Redis pub/sub → ack/read receipts → PG read receipts.
- Presence via Redis key TTL heartbeat.
- Optimistic UI client-side; red-! retry; offline outbox.
- e2ee deferred (post-MVP) — documented privacy risk.

### 7.4 Notifications
- Event bus in-module → `notification` module writes PG rows + enqueues push (FCM/APNs via Expo) + WS toast.
- **Digest mode:** if user hasn't opened app in >4h, batch pushes ("5 new likes from your friends") instead of per-event.
- Preference matrix per user (event type → push/in-app/off).

### 7.5 Moderation
- AI pre-screen on publish (image: NSFW/violence classifier — off-the-shelf model, e.g. `nsfwjs`/CLIP-based; text: hate/abuse keyword + model scan).
- Confidence thresholds → auto-reject to review queue vs publish + post-scan.
- Report → moderation queue → resolve (dismiss/takedown) within **72h SLA** (IT Rules).
- Appeal → re-review; GrievanceTicket for law-enforcement refs.
- Note: Hindi/English models only in v0.1; regional languages = community reviewers (G1+).

---

## 8. Error Handling (full system)

### 8.1 Error taxonomy
| Class | HTTP | Meaning | Retryable? | User UX |
|---|---|---|---|---|
| Validation | 400 | Malformed input | No | Inline field errors (localized) |
| Auth | 401 | Missing/invalid token | No | Silent refresh → re-login if refresh dead |
| Forbidden | 403 | No permission / blocked | No | "You can't do that" |
| Not found | 404 | Resource missing | No | "Post removed or never existed" |
| Conflict | 409 | Unique violation / state | No | "Already liked" → flip UI state, no error |
| Rate limit | 429 | Too many requests | Yes (backoff) | "Slow down" + cooldown indicator |
| Server | 5xx | Unhandled / dep failure | Yes (idempotent) | Generic + requestId + auto-retry |
| Network | — | Offline / timeout (client) | Yes (outbox) | Offline banner, queued ops |
| Dep degraded | 503 | Redis/CDN optionality failed | Yes | Cache miss path, retry later |

### 8.2 Backend
- **Global exception filter** → always returns `{ error: { code, message, details?, requestId } }`; unknown errors mapped to `INTERNAL` with requestId (Sentry groups by `code + stack`).
- **Typed error classes** per taxonomy above; modules throw domain errors, filter maps to HTTP.
- **Validation pipe** (global) → 400 with field map; Zod contracts double-enforce at boundary.
- **Idempotency:** every mutating endpoint accepts `Idempotency-Key` header; unique constraint + catch 409 → return original result. Kills double-tap/retry duplicates.
- **Retry policy:** transient failures (DB connect, Redis, 503) retried in BullMQ with exponential backoff + jitter; jobs have `attempts`, dead-letter queue + alert.
- **DB:** Prisma interactive transactions for multi-table writes; unique-violation handling (catch P2002 → conflict mapping); deadlock retry loop (catch P2034 → retry ≤3).
- **Graceful degradation:** Redis down → skip cache + rate-limit into local lru, never crash; CDN down → serve from R2 origin; media worker down → posts sit in `PENDING` with admin alert, clients show "processing" state.
- **Request context:** `requestId` (request-scoped via `AsyncLocalStorage`) in all logs + Sentry + error envelope; supports support triage.

### 8.3 Mobile
- **Offline outbox:** mutations (like/comment/post/message) → SQLite outbox → optimistic UI → sync worker on reconnect with idempotency keys → reconcile (server result wins).
- **TanStack Query:** `retry: 2, backoff`, `refetchOnReconnect`, mutation `onError` → typed toasts.
- **Global ErrorBoundary** (render fallback + Sentry report + reload CTA).
- **Sentry:** `Sentry.captureException` with breadcrumbs (navigation, api calls, ws events); `release = build number`.
- **Network awareness:** `@react-native-community/netinfo` → offline banner + disable non-idempotent actions.
- **Skeletons everywhere** (no blank screens); images with blurhash placeholders.

### 8.4 Ops & reliability targets
- SLOs: availability 99.9% (monthly), API error rate <1%, feed p95 <150ms, cold start p50 <2s, crash-free sessions >99.5%.
- Alerting: Sentry issue alerts → email/telegram; error-budget burn alerts on failure-rate thresholds.
- **Incident runbook:** template (impact → triage → mitigation → root cause → post-mortem 48h) committed to repo `docs/runbooks/`.
- **Post-mortem template:** severity, timeline, root cause (5-whys), action items with owners.
- Deploys: CI → staging (migrate + smoke) → prod (migrate-safe pattern: expand-migrate-contract for breaking changes).

---

## 9. Security

- Auth: Argon2id for passwords, bcrypt hashes n/a; OTP codes hashed, 5-attempt lockout + SMS-bomb rate limiting per phone/IP.
- Tokens: JWT access 15m (stateless), refresh 30d rotating (revoke-on-use, hash stored); device fingerprinting; logout-all.
- Transport: TLS everywhere; HSTS; no secrets in client.
- Input: Zod/class-validator at boundary; parameterized queries via Prisma; filename/path sanitization on media.
- Storage: R2 presigned URLs scoped to object + TTL; signed CDN URLs; private-by-default for DMs/stories.
- Admin: separate role, IP allowlist, MFA, full audit log.
- Headers: helmet, CORS allowlist, rate limiting (per-route, per-IP, per-user via Redis).
- **IT Rules 2021 compliance:** grievance officer contact published, report→resolution ≤72h SLAs, transparency report job (monthly stub in v0.1), resident grievance officer infra ready pre-SSMI (>5M). Traceability (first-originator identification) → legal posture decision documented in risks; design keeps originator metadata recoverable without breaking e2ee-for-chat decision.

---

## 10. Observability

| Concern | Tool |
|---|---|
| Logs | pino (JSON, `requestId`, `module`, `correlationId`) → Fly.io log drains |
| Errors/crashes | Sentry (mobile+API), release tracking |
| Metrics | Prometheus (API: latency/error/queue depth/DB pool) via Grafana Cloud |
| Tracing | OpenTelemetry (HTTP spans, Prisma spans, BullMQ spans) → Grafana Tempo |
| Health | `/healthz` (liveness), `/readyz` (DB+Redis+queue checks) |
| Dashboards | One Grafana dashboard: latency histograms, error rate, queue depth, crash-free, cold start |

---

## 11. Environments, CI/CD, Deployment

### 11.1 Environments
| Env | Purpose | Stack |
|---|---|---|
| dev | Local iteration | docker-compose (PG 16 + Redis 7) + local Expo + NestJS watch |
| staging | Pre-prod mirror | Fly.io staging app + Neon branch DB + Upstash staging |
| prod | Live | Fly.io Mumbai + Neon prod + Upstash prod + Cloudflare + Sentry |

### 11.2 CI/CD (GitHub Actions)
`ci.yml`: install → prisma generate → lint → typecheck → test (jest) → build (both apps) → **migrate (staging)** → smoke. Deploy on main: staging deploy → E2E smoke → prod deploy (Fly.io blue/green via `fly deploy` releases) → Sentry release + sourcemaps.

### 11.3 Dev container
- `docker-compose.yml`: `postgres:16-alpine`, `redis:7-alpine`, optional `minio` (R2 emulation) + local worker.
- One-command boot: `npm run dev` (concurrently API + worker + mobile).

---

## 12. Milestones (solo, ~10 weeks)

| Milestone | Scope | Exit criteria | Est. |
|---|---|---|---|
| **M1 · Foundation** | Monorepo, NestJS+Prisma+PG boot, Expo app boots, contracts package, auth (OTP+refresh+oauth), Sentry wiring, CI | Sign up + stay logged in on device; CI green | wk 1–2 |
| **M2 · Content+Feed** | User/profile, media presign+R2+Sharp/FFmpeg pipeline, posts, comments, likes, stories, feed+explore (keyset), search (tsvector) | Post a photo+clip → visible in own feed; search works | wk 3–5 |
| **M3 · Social** | Follow, block, mute, contacts import + suggestions, rooms, hashtags, trends, profile highlights | Follow 5 → their posts in feed; join room | wk 6–7 |
| **M4 · Chat+Notifications** | socket gateway, chat (1:1+group), push via Expo, notification center + digests | Two devices chat with receipts; push wakes device | wk 8–9 |
| **M5 · Launch** | Moderation pipeline + reports/appeals, i18n EN+HI, low-data mode, offline outbox hardening, perf pass (<50MB, cold start), Play Console beta + TestFlight, IT Rules surface (grievance contact, transparency stub) | Store beta live; SLOs measurable; moderation SLA scripted | wk 10 |

**Post-MVP backlog (G1+):** For-You vertical feed (cohort gate), 9 more languages, creator analytics console, UPI payouts (Razorpay), e2ee chat, live streaming, ads.

---

## 13. Risks & Deferred Decisions

| Risk | Impact | Mitigation / decision |
|---|---|---|
| Cohort undecided | Wrong content weight | Lightweight-media MVP (G1 gate before video-first) |
| SMS OTP costs & abuse | Burn + spam | Msg91 bulk + WhatsApp OTP fallback; strict rate limits; CAPTCHA on request |
| Regional-language moderation | Compliance gap | Hindi/EN models v0.1; community reviewer program (G1) |
| Traceability (IT Rules) | Legal | Legal posture doc pre-5M users; originator metadata design ready |
| Creator payout tax (TDS/GST) | Compliance | Schema UPI-ready; legal review in phase 2 |
| Prisma 7 driver-adapter complexity | Dev friction | Pin adapter-pg; skill references available in env |
| e2ee deferred | Privacy PR risk | Explicit privacy policy + roadmap item; chat not marketed as "secure" |
| Fly.io cost drift | Burn | Env scale-to-zero on staging; Neon autosuspend; review monthly |
| Solo bus-factor | Project risk | Docs-first (this spec), contracts-first, CI-green discipline |

---

## 14. Decision Log (ADR index)

| # | Decision | Status |
|---|---|---|
| 001 | Modular monolith over microservices for MVP | Accepted (split triggers in §4.2) |
| 002 | NestJS 11 + Express over Go/Node-without-framework | Accepted (Go path for chat at G2) |
| 003 | Expo SDK 56 + New Architecture only | Accepted |
| 004 | Prisma 7 + adapter-pg + PostgreSQL 16 (Neon) | Accepted |
| 005 | Redis (Upstash) + BullMQ for queue | Accepted (Kafka at G2) |
| 006 | socket.io + redis adapter for real-time | Accepted |
| 007 | R2 + Cloudflare CDN, presigned uploads | Accepted |
| 008 | Fanout-on-read hybrid feed | Accepted (fanout-on-write at G2 if needed) |
| 009 | Phone OTP primary auth (Msg91) | Accepted |
| 010 | PG tsvector search (OpenSearch at G2) | Accepted |
| 011 | Sentry + pino + OTel observability | Accepted |
| 012 | Fly.io Mumbai + Neon + Upstash MVP hosting | Accepted (AWS ap-south-1 at G2) |
| 013 | Error envelope + idempotency + outbox pattern | Accepted |
| 014 | e2ee chat deferred to phase 2 | Accepted (documented privacy risk) |
| 015 | Lightweight-media MVP (≤60s clips, no AR/live) | Accepted (G1 cohort gate) |
