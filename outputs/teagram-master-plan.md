# TeaGram — Master Build Plan

> **Vision**: Build a vernacular-first, India-optimized Instagram competitor that a solo developer can actually ship.
> **Current State**: Local-only Android prototype with UI shell, Room DB, hardcoded data, 10 critical bugs.
> **Target State**: Production-ready Android app with backend, auth, real media, real-time chat, and India-first features.

---

## Phase 0: Foundation & Research (Week 1-2)
**Goal**: Lock tech decisions, set up backend skeleton, fix critical bugs.

### Tasks
1. **Research & Tech Decisions**
   - Backend: NestJS vs Firebase vs Supabase
   - Android DI: Hilt vs manual
   - Video: ExoPlayer vs Media3
   - Image filters: GPU rendering approach
   - Real-time: Socket.io vs Firebase
   - Auth: Firebase Auth vs custom OTP
   - Storage: S3-compatible (Cloudflare R2) vs Firebase Storage

2. **Backend Skeleton**
   - NestJS 11 + Express + Prisma 7 + Postgres 16 + Redis 7
   - Docker Compose for local dev
   - CI/CD: GitHub Actions
   - Deploy: Fly.io ( Mumbai region )

3. **Fix Critical Bugs**
   - Reels comment/share bug
   - Comment timestamps
   - Chat active status
   - Reels like persistence
   - CreatePost filters
   - ShareSheet actions
   - Room migration
   - Error handling + loading states

4. **Branding**
   - Custom launcher icon (tea cup / leaf)
   - Splash screen
   - Warm tea-toned color palette
   - Remove all Instagram branding

---

## Phase 1: Backend Core (Week 3-4)
**Goal**: Working backend with auth, posts, comments, follows.

### API Modules
1. **Auth Module**
   - Phone OTP via Firebase Auth or Msg91
   - JWT access (15m) + rotating refresh (30d)
   - Zod validation pipes
   - Error envelope: `{ error: { code, message, details, requestId } }`

2. **User Module**
   - Profile CRUD
   - Avatar upload
   - Follow/unfollow
   - Block/mute
   - Follower/following lists

3. **Post Module**
   - Create post with media
   - Feed (follow-based + algorithmic)
   - Pagination
   - Like/save/comment
   - Carousel support

4. **Media Module**
   - Presigned URL generation
   - Image upload flow
   - Thumbnail generation
   - CDN integration

5. **Search Module**
   - User search
   - Hashtag search
   - PG tsvector → OpenSearch migration path

---

## Phase 2: Real-Time & Notifications (Week 5-6)
**Goal**: Chat, notifications, presence.

1. **Chat Module**
   - Socket.io gateway
   - Conversation list
   - Real-time messaging
   - Online/presence status
   - Typing indicators

2. **Notification Module**
   - Push notifications (FCM)
   - In-app notification inbox
   - Notification preferences

3. **Story Module**
   - Story CRUD
   - Seen tracking
   - Story rings

---

## Phase 3: Android App Integration (Week 7-10)
**Goal**: Connect app to real backend.

1. **Network Layer**
   - Retrofit + Moshi client
   - SecureStore token persistence
   - Auto-refresh logic
   - Error handling

2. **Auth Flow**
   - Phone number input
   - OTP verification
   - Profile setup
   - Logout

3. **Real Features**
   - Real image upload with presigned URLs
   - Real filters (bitmap processing)
   - Real video reels (ExoPlayer)
   - Carousel posts
   - Real search
   - Real chat

4. **Offline Support**
   - SQLite outbox for failed mutations
   - Cache strategy
   - Sync on reconnect

---

## Phase 4: India-First Features (Week 11-12)
**Goal**: Differentiate from Instagram.

1. **i18n**
   - Hindi + 11 languages
   - i18next or native Android strings
   - RTL support where needed

2. **Low-Data Mode**
   - Image quality toggle
   - Data saver mode
   - Compressed thumbnails

3. **Performance**
   - <50MB APK target
   - <2s cold start
   - 2GB RAM friendly
   - Baseline profiles for low-end devices

4. **Vernacular Features**
   - Hindi-first content discovery
   - Regional creator highlights
   - Localized trends

---

## Phase 5: Safety, Privacy & Polish (Week 13-14)
**Goal**: Production-ready quality.

1. **Safety**
   - Report/block/mute UI
   - Comment filtering
   - Hidden words

2. **Privacy**
   - Account privacy toggle
   - Download your data
   - Clear data

3. **Settings**
   - Account settings
   - Notification preferences
   - Privacy settings
   - Help / Support

4. **Onboarding**
   - Splash → Phone auth → Profile setup → Feed

5. **Testing**
   - Unit tests (ViewModel, Repository)
   - Screenshot tests (Roborazzi)
   - Integration tests

---

## Phase 6: Launch Prep (Week 15-16)
**Goal**: Ready for Play Store.

1. **Build Optimization**
   - Minify + Proguard
   - App bundle (AAB)
   - Play Feature Delivery
   - Size optimization

2. **CI/CD**
   - GitHub Actions for APK/AAB
   - Firebase App Distribution
   - Internal testing track

3. **Monitoring**
   - Crashlytics
   - Performance monitoring
   - Analytics (privacy-friendly)

4. **Compliance**
   - IT Rules 2021
   - Grievance officer
   - Privacy policy
   - Terms of service

---

## Architecture Decisions (ADR)

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Backend | NestJS 11 | Type-safe, modular, solo-buildable |
| Database | Postgres 16 + Prisma 7 | Strong typing, migrations, ecosystem |
| Cache/Queue | Redis 7 + BullMQ | Feed fanout, notifications, rate limiting |
| Hosting | Fly.io | India edge (Mumbai), simple deploys |
| Mobile | Android native (Kotlin/Compose) | Performance, low-end device control |
| DI | Hilt | Standard Android DI, testable |
| Video | Media3 (ExoPlayer) | Official Android, modern API |
| Filters | RenderScript / GPU | Performance on low-end devices |
| Real-time | Socket.io + Redis adapter | Familiar, scales with BullMQ |
| Auth | Firebase Auth (phone OTP) | Fastest path, handles OTP globally |
| Storage | Cloudflare R2 | S3-compatible, cheap, India reachable |
| Search | PG tsvector → OpenSearch | MVP: zero deps; scale: migrate |

---

## File Structure (Target)

```
tea-gram/
├── apps/
│   ├── android/                 # Android app (current)
│   │   └── app/src/main/java/com/teagram/
│   │       ├── data/
│   │       │   ├── db/          # Room DAOs
│   │       │   ├── model/       # Entities
│   │       │   ├── remote/      # Retrofit APIs
│   │       │   └── repository/  # Repository implementations
│   │       ├── di/              # Hilt modules
│   │       ├── domain/          # Use cases
│   │       ├── ui/
│   │       │   ├── components/  # Reusable composables
│   │       │   ├── screens/     # Feature screens
│   │       │   ├── theme/       # Colors, typography
│   │       │   └── viewmodel/   # Feature ViewModels
│   │       └── util/            # Extensions, helpers
│   └── api/                     # NestJS backend
│       └── src/
│           ├── auth/
│           ├── users/
│           ├── posts/
│           ├── media/
│           ├── comments/
│           ├── chat/
│           ├── notifications/
│           ├── stories/
│           ├── search/
│           └── common/
├── packages/
│   ├── contracts/               # Shared types (Zod schemas)
│   └── shared/                  # Shared utilities
├── docker-compose.yml
├── .env.example
└── outputs/                     # Documentation & plans
```

---

## Current Bugs to Fix (Priority Order)

| Priority | Bug | Effort | Fix |
|----------|-----|--------|-----|
| P0 | Reels comment/share wrong post | 10min | Pass reel to openComments |
| P0 | Comment timestamps hardcoded | 30min | Add TimeAgo util |
| P0 | Chat active status hardcoded | 10min | Remove hardcoded text |
| P0 | Reels like not persisted | 20min | Persist to Room/Repository |
| P1 | CreatePost filters no-op | 2hr | Apply bitmap filters |
| P1 | ShareSheet actions no-op | 1hr | Implement copy/share |
| P1 | Room destructive migration | 30min | Write migrations |
| P1 | No error handling | 2hr | Result wrappers |
| P1 | No loading states | 1hr | Add spinners |
| P2 | Hardcoded user | 3hr | Add auth flow |
| P2 | No input validation | 1hr | Add checks |

---

## Research Questions

1. **Backend**: NestJS vs Firebase vs Supabase for solo dev?
2. **Android DI**: Hilt vs manual Service Locator?
3. **Video**: Media3 vs ExoPlayer vs custom?
4. **Filters**: RenderScript deprecated — what's 2025 approach?
5. **Real-time**: Socket.io vs Firebase Realtime DB?
6. **Auth**: Firebase Auth vs custom phone OTP with Msg91?
7. **Storage**: Cloudflare R2 vs Firebase Storage vs S3?
8. **Search**: PG tsvector vs Elasticsearch vs Algolia?
9. **i18n**: Android string resources vs i18next vs Lingver?
10. **Offline**: Room outbox vs SQLiteDelight vs Realm?

---

## Next Steps

1. **Research** — Answer the 10 questions above via web search + paper sources
2. **Plan** — Create detailed task breakdown with time estimates
3. **Build** — Start with Phase 0: fix bugs + branding + backend skeleton
4. **Deploy** — Get a working build on device after each milestone

---

*This plan is a living document. Update after each phase.*
