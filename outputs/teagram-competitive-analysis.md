# TeaGram — Instagram Competitive Analysis & Gap Assessment

> **Date**: 2025-08-28
> **App**: TeaGram (`com.teagram.app`)
> **Current State**: Native Android/Kotlin/Jetpack Compose, local-only prototype with Room, Coil, Firebase App Check, Gemini AI
> **Goal**: Compete with Instagram as a vernacular-first, India-focused social app

---

## 1. Feature Completeness Matrix

### ✅ Implemented (Core Shell)

| Feature | Status | Quality | Notes |
|---------|--------|---------|-------|
| **Feed / Home** | ✅ Done | Medium | Follow-based feed, pagination, seed data |
| **Stories** | ✅ Done | Medium | Auto-advance 5s, tap zones, seen tracking |
| **Post Creation** | ✅ Done | Low | Image picker, filters (UI only), caption, location |
| **Like / Save** | ✅ Done | Medium | Local state toggle, count update |
| **Comments** | ✅ Done | Medium | Bottom sheet, add comment, like comment |
| **Share Sheet** | ✅ Done | Low | UI only, all actions are no-ops |
| **Explore** | ✅ Done | Low | Search bar + category chips + grid, shallow logic |
| **Reels** | ✅ Done | Low | Vertical pager, static images, like only |
| **Profile** | ✅ Done | Medium | Grid, stats, bio, edit profile, follow/unfollow |
| **Follow / Unfollow** | ✅ Done | Medium | Local Room toggle |
| **Direct Messages** | ✅ Done | Low | Local Room, static threads, no real-time |
| **Chat Detail** | ✅ Done | Low | Send/receive local messages only |
| **Search** | ✅ Done | Low | Filter by username only, no real search |
| **Post Detail** | ✅ Done | Medium | Full post view, like/comment/share actions |

### ⚠️ Partially Implemented (Broken or Incomplete)

| Feature | Status | Issue |
|---------|--------|-------|
| **Reels comment/share** | 🐛 Broken | Opens random feed post via `allPosts.firstOrNull()` |
| **Reels like persistence** | 🐛 Broken | Only in-memory StateFlow; lost on recomposition |
| **Comment timestamps** | 🐛 Broken | Hardcoded "1h" for every comment |
| **Chat active status** | 🐛 Broken | Always shows "Active now" |
| **CreatePost filters** | 🐛 Broken | Filter chips selectable but no image processing |
| **ShareSheet actions** | 🐛 Broken | Copy link / Add to story / Share to... only dismiss |
| **Followers count** | 🐛 Wrong | `baseFollowers + (isFollowing ? 1 : 0)` — not real aggregation |
| **Profile follow state** | ⚠️ Laggy | Derived from Flow; toggle doesn't invalidate locally |
| **Comment pagination** | ⚠️ Missing | Loads all comments at once |
| **Carousel posts** | ❌ Missing | `PostEntity` has single `imageResName` only |
| **Video playback** | ❌ Missing | Reels use static images; no ExoPlayer/Media3 |
| **Real networking** | ❌ Missing | Retrofit/Moshi/OkHttp installed but unused |
| **Authentication** | ❌ Missing | Hardcoded `sarah.creatives`; no login/signup |
| **Image upload** | ❌ Missing | CreatePost stores local URI/drawable string only |
| **Real chat** | ❌ Missing | DMs are local Room rows; no server, no real-time |
| **Offline sync** | ❌ Missing | Room is device-only; no conflict resolution |
| **Push notifications** | ❌ Missing | Notification bell is a Toast |
| **i18n / Hindi** | ❌ Missing | Hardcoded English strings; no locale support |
| **Onboarding** | ❌ Missing | Cold start is blank feed |
| **Settings / Privacy** | ❌ Missing | No account settings, privacy, or moderation |
| **Report / Block** | ❌ Missing | No user safety features |
| **Hashtags / Trending** | ❌ Missing | No hashtag creation or trend calculation |
| **Moderation** | ❌ Missing | No content moderation or admin tools |
| **Backend API** | ❌ Missing | Entire app is offline/demo |
| **Cloud storage** | ❌ Missing | No R2/S3 presigned URLs or media CDN |

---

## 2. Critical Bugs (Must Fix Before Launch)

| # | Bug | Location | Impact |
|---|-----|----------|--------|
| 1 | **Reels comment/share open random post** | `MainActivity.kt:297-306` | Users commenting on reels see wrong post |
| 2 | **Comment timestamps hardcoded** | `CommentsBottomSheet.kt` | Every comment shows "1h" |
| 3 | **Chat "Active now" always true** | `ChatDetailScreen.kt` | Fake presence indicator |
| 4 | **Reels like not persisted** | `TeaGramViewModel.toggleReelLike` | Like disappears on recomposition |
| 5 | **CreatePost filters no-op** | `CreatePostScreen.kt` | Selected filter never applied |
| 6 | **ShareSheet actions no-op** | `ShareSheet.kt` | Copy link / Add story do nothing |
| 7 | **Room destructive migration** | `AppDatabase.kt` | Schema change = data wipe |
| 8 | **No error handling** | Repository / ViewModel | DB failures silent; no user feedback |
| 9 | **No loading states** | All mutation buttons | No spinners or disabled states |
| 10 | **Hardcoded user** | ViewModel + Repository | No multi-user support |

---

## 3. Architecture Flaws

| # | Flaw | Impact |
|---|------|--------|
| 1 | **God ViewModel (467 lines)** | Single file handles all state; untestable |
| 2 | **No DI** | Repository instantiated in ViewModel; no mocking |
| 3 | **Hardcoded magic strings** | `"sarah.creatives"`, `"img_feed_portrait"` everywhere |
| 4 | **Static chat threads** | `getChatThreads()` returns hardcoded list |
| 5 | **Wrong follower math** | Not real aggregation |
| 6 | **No pagination for comments** | `getCommentsForPost` returns all |
| 7 | **No carousel support** | `PostEntity` single image only |
| 8 | **No video playback** | Static images for reels |
| 9 | **No offline-first sync** | Room local only |
| 10 | **No input validation** | Empty captions auto-filled |
| 11 | **No string resources** | No i18n possible |
| 12 | **Release minify disabled** | Larger APK, exposed code |
| 13 | **Proguard unused** | Dead rules |
| 14 | **`compileSdk` unusual config** | Non-standard `release(36) { minorApiLevel = 1 }` |
| 15 | **No dark/light switching** | Only frosted dark theme |
| 16 | **No onboarding/splash** | Blank cold start |
| 17 | **No accessibility labels** | Missing `contentDescription` |
| 18 | **Unused dependencies** | Retrofit/Moshi/OkHttp dead weight |
| 19 | **No architecture tests** | No unit tests for ViewModel/Repository |
| 20 | **No screenshot tests baseline** | Roborazzi installed but no baseline |

---

## 4. Instagram Feature Gap Analysis

### 4.1 Core Social Features

| Instagram Feature | TeaGram Status | Gap |
|-------------------|----------------|-----|
| Feed (follow-based) | ✅ Present | Missing algorithmic ranking |
| Stories (24h) | ✅ Present | Missing story highlights, archive |
| Reels (video) | ⚠️ Present | Static images only; no video |
| Post (photo/carousel) | ⚠️ Present | Single image only; no carousel |
| Explore / Search | ⚠️ Present | Shallow; no real search/trending |
| Like / Save / Comment | ✅ Present | Missing comment replies |
| Share / DMs | ⚠️ Present | Local only; no real sharing |
| Follow / Block / Mute | ⚠️ Partial | Follow only; no block/mute UI |
| Profile / Bio / Link | ✅ Present | Missing highlights, badges |
| Notifications | ❌ Missing | No push, no in-app |
| Live | ❌ Missing | Not planned |
| Rooms / Audio | ❌ Missing | Not planned |

### 4.2 Creation Tools

| Instagram Feature | TeaGram Status | Gap |
|-------------------|----------------|-----|
| Camera + Filters | ⚠️ Present | Filter UI only; no real processing |
| Boomerang | ❌ Missing | Not implemented |
| Layout (collage) | ❌ Missing | Not implemented |
| Text / Sticker / Draw | ❌ Missing | Not implemented |
| Reels editing | ❌ Missing | No trimming, audio, effects |
| Post scheduling | ❌ Missing | Not applicable offline |
| Drafts | ❌ Missing | Not implemented |

### 4.3 Safety & Privacy

| Instagram Feature | TeaGram Status | Gap |
|-------------------|----------------|-----|
| Report / Block | ❌ Missing | No safety tools |
| Mute accounts | ❌ Missing | No mute UI |
| Restricted accounts | ❌ Missing | Not implemented |
| Hidden words | ❌ Missing | Not implemented |
| Two-factor auth | ❌ Missing | No auth at all |
| Login activity | ❌ Missing | No auth |
| Download your data | ❌ Missing | Not applicable offline |

### 4.4 Monetization (Future)

| Instagram Feature | TeaGram Status | Gap |
|-------------------|----------------|-----|
| Shopping / Tags | ❌ Missing | Not applicable yet |
| Badges / Live badges | ❌ Missing | Not planned |
| Ads | ❌ Missing | Not applicable |
| Creator monetization | ❌ Missing | Not planned |

### 4.5 Infrastructure

| Instagram Feature | TeaGram Status | Gap |
|-------------------|----------------|-----|
| Cloud backend | ❌ Missing | Local Room only |
| CDN / Media hosting | ❌ Missing | No S3/R2/CloudFront |
| Real-time sync | ❌ Missing | No WebSocket/Socket.io |
| Push notifications | ❌ Missing | No FCM/Expo notifications |
| Analytics / Insights | ❌ Missing | No creator stats |
| Cross-device sync | ❌ Missing | Device-only |
| Backup / Restore | ❌ Missing | No cloud backup |

---

## 5. What TeaGram Does Better / Differently (Opportunity)

| Differentiator | Description |
|----------------|-------------|
| **Vernacular-first** | Hindi + 11 languages by design |
| **India-first performance** | <50MB APK, 2GB RAM friendly, <2s cold start, low-data mode |
| **Offline-first** | Local-first with outbox; works on bad networks |
| **Privacy-first** | No tracking, minimal permissions |
| **Creator-friendly** | No algorithmic shadowing; transparent reach |
| **Lightweight** | No bloat; fast on low-end Androids |
| **Solo-buildable** | Modular monolith backend; easy to maintain |

---

## 6. Roadmap to Compete With Instagram

### Phase 1: Fix the Broken (Week 1-2)
1. Fix reels comment/share to use reel id
2. Fix comment timestamps with real `timeAgo` util
3. Fix chat "Active now" to real presence
4. Fix reels like persistence
5. Fix CreatePost filters (apply actual bitmap filters)
6. Fix ShareSheet actions (copy link, etc.)
7. Fix Room destructive migration
8. Add error handling + loading states
9. Add input validation

### Phase 2: Backend Integration (Week 3-6)
1. Set up NestJS + Prisma + Postgres + Redis
2. Add phone OTP auth (Msg91/Firebase)
3. Replace Room seed data with API calls
4. Add real image upload (S3/R2 presigned URLs)
5. Add real-time chat (Socket.io)
6. Add push notifications (FCM)
7. Add JWT + refresh tokens
8. Add idempotency middleware

### Phase 3: Core Feature Parity (Week 7-10)
1. Video playback for Reels (ExoPlayer/Media3)
2. Carousel posts (multi-image)
3. Real search (user + hashtag)
4. Hashtag creation + trending
5. Block / Mute / Report UI
6. Notifications (push + in-app)
7. Settings / Privacy screens
8. Onboarding + splash

### Phase 4: India-First Differentiation (Week 11-14)
1. i18n (Hindi + 11 languages)
2. Low-data mode (image quality toggle)
3. Offline outbox (expo-sqlite style with Room)
4. Vernacular content discovery
5. Creator stats / insights
6. Lite mode for <2GB RAM devices

### Phase 5: Polish & Scale (Week 15-18)
1. Extract ViewModel into feature modules
2. Add DI (Hilt/Koin)
3. Add unit tests + screenshot tests
4. Enable minify + Proguard
5. Custom launcher icon + splash
6. Performance audit (<2s cold start, <50MB APK)
7. CI/CD with Firebase App Distribution

---

## 7. Bottom Line

**TeaGram currently has:**
- ✅ Competent Instagram-like UI shell
- ✅ Local CRUD for posts, comments, follows, stories, DMs
- ✅ Basic feed, stories, reels, explore, profile, chat

**TeaGram currently lacks:**
- ❌ Backend, auth, networking, cloud storage
- ❌ Real video, real filters, real search
- ❌ Safety, privacy, moderation
- ❌ Notifications, onboarding, settings
- ❌ i18n, offline sync, cross-device

**To compete with Instagram, TeaGram needs:**
1. **Backend** (NestJS + Postgres + Redis)
2. **Auth** (Phone OTP + JWT)
3. **Media** (Real uploads, video, filters)
4. **Real-time** (Chat, notifications, presence)
5. **Safety** (Report, block, mute)
6. **India features** (Vernacular, low-data, offline-first)

**Estimated time to MVP competitive:** 3-4 months with 1 senior Android + 1 backend dev.
**Estimated time for solo dev:** 6-8 months part-time.

---

## 8. Immediate Next Actions

1. **Fix all 10 critical bugs** listed above
2. **Decide backend strategy**: NestJS (original plan) or Firebase?
3. **Add custom launcher icon** (tea cup / leaf)
4. **Add splash screen** with TeaGram branding
5. **Rebrand colors** to warm tea-toned palette (currently frosted blue/purple)
6. **Plan architecture cleanup**: Extract ViewModel, add DI, add tests
