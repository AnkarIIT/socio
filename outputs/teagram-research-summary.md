# TeaGram — Research Summary & Finalized Decisions

> Based on 10 web research queries conducted on 2025-08-28.

---

## 1. Backend: NestJS ✅ (Already Planned)

**Decision**: Stick with NestJS 11 + Prisma 7 + Postgres 16 + Redis 7.

**Rationale**:
- Type-safe, modular, solo-buildable
- Prisma gives excellent TypeScript types
- BullMQ for async jobs (fanout, transcoding, notifications)
- Fly.io deployment with Mumbai region for India latency
- Docker Compose for local dev

**Alternatives considered**:
- Firebase: Good for auth/storage but less control over backend logic
- Supabase: Excellent but PostgREST limits complex business logic
- Custom Node/Express: More boilerplate, less structure

**Winner**: NestJS — best balance of structure, solo productivity, and scalability.

---

## 2. Android DI: Hilt ✅

**Decision**: Use Hilt for dependency injection.

**Rationale**:
- Google-recommended standard
- Makes testing ViewModels easy
- Integrates with ViewModel, WorkManager, etc.
- Reduces boilerplate vs manual DI

**Source**: [Android Developers - Dependency injection with Hilt](https://developer.android.com/training/dependency-injection/hilt-android)

---

## 3. Video: Media3 (ExoPlayer) ✅

**Decision**: Use Media3 ExoPlayer with `DefaultPreloadManager` for reels.

**Rationale**:
- Official Android media library
- `DefaultPreloadManager` enables zero inter-presentation delay
- Single player instance for memory efficiency
- Supports HLS/DASH for adaptive streaming
- `VerticalPager` + `settledPage` state management

**Implementation**:
- Single `ExoPlayer` instance shared across reels
- Preload adjacent 2-3 reels
- Lifecycle-aware playback
- LRU caching for thumbnails

**Sources**:
- [ComposeReels](https://github.com/clown6613/ComposeReels)
- [Building a Video Feed in Jetpack Compose](https://proandroiddev.com/building-a-video-feed-in-jetpack-compose-a-media3-tutorial-5d8f6955600f)
- [Media3 Preloading Blog](https://android-developers.googleblog.com/2025/09/introducing-preloading-with-media3.html)

---

## 4. Image Filters: AGSL RuntimeShader ✅

**Decision**: Use AGSL `RuntimeShader` for filters on Android 13+, fallback to bitmap color matrix for older devices.

**Rationale**:
- RenderScript is deprecated (removed in future Android releases)
- AGSL runs on GPU via fragment shaders
- Zero-dependency, fast
- Compatible with Jetpack Compose

**Implementation**:
- Define filter shaders in `assets/shaders/`
- Use `RuntimeShader` with `Brush` in Compose
- Fallback: `ColorMatrix` + `ColorFilter` for API < 33
- Libraries: [mirage](https://github.com/AndroidPoet/mirage) for prebuilt shaders

**Source**: [Migrate from RenderScript](https://developer.android.com/guide/topics/renderscript/migrate)

---

## 5. Real-time Chat: Socket.io ✅

**Decision**: Use Socket.io with Redis adapter on backend, Socket.io client on Android.

**Rationale**:
- Familiar ecosystem (works with NestJS)
- Redis adapter enables horizontal scaling
- Rooms/namespaces for conversations
- Automatic reconnection
- Easier than Firebase Realtime DB for custom logic

**Implementation**:
- NestJS `@nestjs/platform-socket.io`
- Redis adapter for multi-instance
- JWT auth on socket handshake
- Android client: `io.socket:socket.io-client`

---

## 6. Auth: Firebase Auth (Phone OTP) ✅

**Decision**: Firebase Auth for phone OTP, fallback to Msg91 for India-specific edge cases.

**Rationale**:
- Fastest path to working OTP
- Handles SMS retries, auto-retrieval (Google Play Services)
- Works in India with Firebase's SMS routing
- Msg91 as backup for non-Play Services devices

**Flow**:
1. User enters phone number
2. Firebase sends OTP via SMS
3. User enters OTP
4. Firebase returns ID token
5. Backend creates/updates user, returns JWT

---

## 7. Storage: Cloudflare R2 ✅

**Decision**: Cloudflare R2 for media storage, Cloudflare CDN for delivery.

**Rationale**:
- S3-compatible API
- **Zero egress fees** (huge for media-heavy app)
- 10 GB free, then $0.015/GB/month
- Much cheaper than Firebase Storage or S3 at scale
- India reachable via Cloudflare edge

**Flow**:
1. App requests presigned URL from backend
2. Backend signs R2 URL with secret key
3. App uploads directly to R2
4. App confirms upload to backend
5. Backend creates post with R2 URL

**Cost comparison**:
- R2: 5TB + 50TB egress = ~$75/month
- S3: Same = ~$4,625/month
- Firebase: Blaze plan required, unpredictable

**Source**: [Storage Comparison 2026](https://agentdeals.dev/storage-comparison-2026)

---

## 8. Search: PostgreSQL tsvector → Meilisearch ✅

**Decision**: Start with PostgreSQL tsvector, migrate to Meilisearch at 500K users.

**Rationale**:
- Zero external dependencies for MVP
- Good enough for <100K users
- Meilisearch: typo-tolerant, fast, easy to run
- Elasticsearch: overkill for social app search

**Implementation**:
- `tsvector` column on `Post.caption`, `User.displayName`, `User.username`
- GIN index for fast search
- Trigger to update tsvector on insert/update
- Migration path: export to Meilisearch when needed

**Source**: [Search Implementation Guide](https://viprasol.com/blog/search-implementation/)

---

## 9. i18n: Android String Resources ✅

**Decision**: Use Android string resources with per-app language preference.

**Rationale**:
- Native Android approach
- Supports 11+ languages out of the box
- Per-app language preference (Android 13+)
- No external library overhead
- Easy for translators

**Implementation**:
- `res/values/strings.xml` (English)
- `res/values-hi/strings.xml` (Hindi)
- `res/values-xx/` for other languages
- In-app language switcher in Settings
- Persist choice in DataStore

**Source**: [Android Localization](https://developer.android.com/guide/topics/resources/localization)

---

## 10. Offline: Room + Outbox Pattern ✅

**Decision**: Room for local cache + explicit outbox table for failed mutations.

**Rationale**:
- Already using Room
- Outbox pattern: failed mutations go to `outbox` table
- Retry on connectivity restore
- Conflict resolution: last-write-wins with server timestamp

**Implementation**:
- `Outbox` entity with `operation`, `entityType`, `entityId`, `payload`, `retryCount`
- Worker retries failed mutations
- UI shows optimistic updates, rolls back on failure

---

## Finalized Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Backend** | NestJS 11 + Express | Latest |
| **Database** | PostgreSQL 16 + Prisma 7 | Latest |
| **Cache/Queue** | Redis 7 + BullMQ | Latest |
| **Hosting** | Fly.io (Mumbai) | Latest |
| **Mobile** | Android Kotlin + Jetpack Compose | API 24-36 |
| **DI** | Hilt | Latest |
| **Networking** | Retrofit + Moshi | 2.12.0 |
| **Video** | Media3 ExoPlayer | 1.8+ |
| **Filters** | AGSL RuntimeShader + ColorMatrix fallback | - |
| **Real-time** | Socket.io + Redis adapter | 4.x |
| **Auth** | Firebase Auth (phone OTP) | 23.x |
| **Storage** | Cloudflare R2 + CDN | - |
| **Search** | PG tsvector → Meilisearch | - |
| **i18n** | Android string resources | - |
| **Offline** | Room + Outbox pattern | - |
| **Monitoring** | Firebase Crashlytics + Performance | - |

---

## Implementation Order (Updated)

### Week 1: Fix Bugs + Branding
- [ ] Fix reels comment/share bug
- [ ] Fix comment timestamps
- [ ] Fix chat active status
- [ ] Fix reels like persistence
- [ ] Fix CreatePost filters (bitmap color matrix for now)
- [ ] Fix ShareSheet copy link
- [ ] Fix Room migration
- [ ] Add error handling + loading states
- [ ] Custom launcher icon
- [ ] Splash screen
- [ ] Warm tea color palette

### Week 2: Backend Skeleton
- [ ] NestJS project setup
- [ ] Prisma schema (User, Post, Comment, Follow, Story, Message)
- [ ] Docker Compose
- [ ] Auth module (phone OTP)
- [ ] User module (CRUD)
- [ ] Post module (CRUD + feed)
- [ ] Media module (presigned URLs)
- [ ] GitHub Actions CI/CD
- [ ] Deploy to Fly.io

### Week 3: Android-Backend Integration
- [ ] Retrofit API client
- [ ] Token management (SecureStore)
- [ ] Auth flow (phone → OTP → home)
- [ ] Real image upload
- [ ] Feed from API
- [ ] Profile from API
- [ ] Comments from API
- [ ] Likes from API

### Week 4: Real-time + Notifications
- [ ] Socket.io chat
- [ ] FCM push notifications
- [ ] Presence/typing indicators
- [ ] Notification inbox

### Week 5: Media Polish
- [ ] Media3 reels player
- [ ] Real filters (AGSL)
- [ ] Carousel posts
- [ ] Video transcoding (FFmpeg on backend)

### Week 6: Search + Discovery
- [ ] User search
- [ ] Hashtag search
- [ ] Trending hashtags
- [ ] Explore page from API

### Week 7: Safety + Settings
- [ ] Report/block/mute UI
- [ ] Settings screens
- [ ] Privacy controls
- [ ] Onboarding flow

### Week 8: India-First Features
- [ ] Hindi + 11 languages
- [ ] Low-data mode
- [ ] Performance optimization
- [ ] Offline outbox

### Week 9-10: Polish + Launch
- [ ] Hilt DI migration
- [ ] Unit tests
- [ ] Screenshot tests
- [ ] Minify + Proguard
- [ ] Play Store listing
- [ ] Internal testing track

---

## Next Immediate Actions

1. **Start Phase 0 — Fix Bugs + Branding** (this session)
2. **Set up backend skeleton** (next session)
3. **Connect Android to backend** (following sessions)
