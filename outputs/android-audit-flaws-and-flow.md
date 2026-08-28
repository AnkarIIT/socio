# Bharat Social — Project Audit: Flaws + App Flow

> **Context**: The current repo at `C:\codes\social` is a **native Android/Kotlin/Jetpack Compose** app generated from Google AI Studio, not the earlier Expo/NestJS/Prisma plan. It is a **local-only Instagram-style prototype** with Room, Coil, Firebase App Check, and Gemini AI. No backend, no real auth, no real networking.

---

## 1. Complete User Flow

### 1.1 Launch → Feed (Home)
- `MainActivity.onCreate` → `InstagramApp()` composable
- `InstagramViewModel.init` seeds Room if empty, then exposes StateFlows
- Bottom nav `HOME` tab selected by default
- `FeedScreen` shows:
  - `StoriesRow` (tappable stories)
  - `PostCard` list (paged via `_feedLimit`)
  - `SuggestedCreatorsCard` after ≥2 posts
  - "All caught up" footer

### 1.2 Story Flow
- Tap story → `viewModel.openStory(story)` → `StoryViewerDialog`
- Auto-advances after 5s via `progress.animateTo(1f)`
- Tap zones: left 30% prev, right 70% next
- Reply input + heart button; reply closes story with Toast

### 1.3 Post Interaction Flow
- **Like**: double-tap image or heart button → `toggleLike(postId)` → Room update + local StateFlow bump
- **Save**: bookmark button → `toggleSave(postId)` → Room update
- **Comment**: comment button → `openComments(postId)` → `CommentsBottomSheet`
  - Input field → `addComment(text)` → Room insert + increment commentsCount
  - Like comment → `toggleCommentLike(comment, ...)` → Room update
- **Share**: share button → `openShareModal(post)` → `ShareSheet`
  - Friends row, "Add to story", "Copy link", "Share to..." — all dismiss-only
- **Author**: tap avatar/name → `openUserProfile(username)`
  - If self → `selectTab(PROFILE)` with `viewedProfileUsername = null`
  - If other → `viewedProfileUsername = username` → `ProfileScreen`

### 1.4 Profile Flow
- Top bar with back (if viewed profile) or lock icon (self)
- Stats row: posts count, followers, following
- Bio + website + edit profile dialog (self only)
- Highlights row (hardcoded)
- Tab row: Grid / Reels / Saved / Tagged
- 3-column post grid from `currentProfilePosts`
- Follow/Message buttons for other users

### 1.5 Create Post Flow
- Bottom nav center button → `selectTab(CREATE)` → `CreatePostScreen`
- Pick preset image or gallery via `GetContent`
- Filter chips (NORMAL, CLARENDON, VINTAGE, MONO, WARM, COOL) — **no actual filter applied**
- Caption input + hashtag quick-adds
- Location input + suggestions
- Share → `createPost(...)` → Room insert → navigate HOME

### 1.6 Explore Flow
- Search bar + category chips
- Grid of `ExploreTile` (real posts + placeholder tiles)
- Tap tile → `openPostDetail(post)` → `PostDetailScreen`
- PostDetail shows full `PostCard` inside scrollable column

### 1.7 Reels Flow
- Fullscreen `VerticalPager` of `ReelItem`
- Static image + gradient overlay + action bar (like/comment/share/more/vinyl disc)
- Like toggles local `_reels` StateFlow only
- Comment/share pass through to **feed posts** (`allPosts.firstOrNull()`), **not the reel**

### 1.8 DM Flow
- Top-bar mail icon → `openDirectMessages()` → `DirectMessagesScreen`
- Notes tray + search + thread list
- Tap thread → `openConversation(id)` → `ChatDetailScreen`
- Send message → `sendMessage(text)` → Room insert

---

## 2. Flaws List

### 🔴 Critical / App-Breaking

| # | Flaw | Location | Impact |
|---|------|----------|--------|
| 1 | **Reels comment/share use wrong post** | `MainActivity.kt` Reels tab callbacks | Commenting/sharing a reel opens a random feed post instead |
| 2 | **All data is hardcoded demo data** | `Repository.kt` `seedInitialDataIfNeeded` | App has no real content persistence or backend |
| 3 | **No real networking** | Retrofit/Moshi/OkHttp deps unused | Dependencies are dead weight; no API integration |
| 4 | **No authentication** | Entire app | Anyone is hardcoded as `sarah.creatives`; no login/signup |
| 5 | **No real image upload** | `CreatePostScreen`, `Repository.createPost` | Post creation only stores local drawable/URI string in Room |
| 6 | **No real chat** | `ChatDetailScreen`, `DirectMessagesScreen` | DMs are local Room rows; no real-time, no server |
| 7 | **Comment timestamps hardcoded** | `CommentsBottomSheet.kt` `CommentItem` | Every comment shows "1h" regardless of real time |
| 8 | **Chat "Active now" hardcoded** | `ChatDetailScreen.kt` top bar | Every partner shows "Active now" always |
| 9 | **CreatePost filters do nothing** | `CreatePostScreen.kt` `selectedFilter` | Filter enum exists but never applied to image |
| 10 | **ShareSheet actions are no-ops** | `ShareSheet.kt` | Copy link / Add to story / Share to... only dismiss sheet |
| 11 | **Room `fallbackToDestructiveMigration`** | `AppDatabase.kt` | Any schema change wipes all user data silently |
| 12 | **Release build has minify disabled** | `app/build.gradle.kts` | No code shrinking/obfuscation; larger APK, exposed code |
| 13 | **`isMinifyEnabled = false` + proguard file unused** | `app/build.gradle.kts` | Proguard rules are dead code |
| 14 | **No error handling anywhere** | Repository / ViewModel | DB failures are silent; UI never shows error states |
| 15 | **No loading states for mutations** | ViewModel / screens | Like/comment/follow/save have no spinners or disabled states |

### 🟡 Major Design / Architecture Flaws

| # | Flaw | Location | Impact |
|---|------|----------|--------|
| 16 | **God ViewModel (467 lines)** | `InstagramViewModel.kt` | Single file handles all state; impossible to test/maintain |
| 17 | **No DI / Repository instantiated in ViewModel** | `InstagramViewModel.init` | Tight coupling; no mocking for tests |
| 18 | **Hardcoded magic strings** | Entire app | `"sarah.creatives"`, `"img_feed_portrait"` repeated everywhere |
| 19 | **Chat threads are static, not derived** | `Repository.getChatThreads()` | Threads don't reflect actual message data or unread counts |
| 20 | **Followers count is wrong for other users** | `InstagramViewModel.userProfile` combine | Uses `baseFollowersCount + (isFollowing ? 1 : 0)` — not real follower math |
| 21 | **PostDetail uses nested scroll** | `PostDetailScreen.kt` | `Column` with `verticalScroll` wrapping `PostCard` (which has internal tap gestures) — can cause scroll jitter |
| 22 | **BackHandler cascade is fragile** | `MainActivity.kt` | Multiple overlapping `BackHandler` conditions; edge cases when closing DMs then profile |
| 23 | **Explore grid mixes real + placeholder tiles** | `ExploreScreen.kt` | 8 hardcoded placeholders pollute real data; search/filter logic is shallow |
| 24 | **No pagination for comments** | `CommentsBottomSheet.kt` | Loads all comments at once; Room `getCommentsForPost` returns unfiltered Flow |
| 25 | **Reels like doesn't persist** | `InstagramViewModel.toggleReelLike` | Only mutates in-memory `_reels` StateFlow; lost on recomposition from other triggers |
| 26 | **No carousel / multi-media support** | `PostEntity` | Instagram is carousel-first; model only has single `imageResName` |
| 27 | **No video playback** | `ReelsScreen`, `PostCard` | Reels use static images; no `ExoPlayer` or `androidx.media3` |
| 28 | **No offline-first sync strategy** | Entire data layer | Room is local only; no conflict resolution, no sync queue |
| 29 | **Profile follow button state can lag** | `ProfileScreen` / `PostCard` | Follow state derived from `followingUsernames` Flow; toggleFollow doesn't invalidate locally |
| 30 | **No input validation** | `CreatePostScreen`, `EditProfileDialog` | Empty captions auto-filled; no username format checks |

### 🟢 Minor / Polish Flaws

| # | Flaw | Location | Impact |
|---|------|----------|--------|
| 31 | **Hardcoded strings, no string resources** | All screens | No i18n; cannot add Hindi/vernacular languages |
| 32 | **Missing contentDescription on many IconButtons** | Multiple composables | Accessibility gap |
| 33 | **`remember` misuse for stable lists** | `ProfileScreen.highlights` | Minor; stable list doesn't need `remember` |
| 34 | **Unused imports everywhere** | Multiple files | Lint warnings, slightly larger compile |
| 35 | **`isCrunchPngs = false`** | `build.gradle.kts` | Release APK larger than necessary |
| 36 | **`compileSdk` unusual config** | `build.gradle.kts` | `compileSdk { version = release(36) { minorApiLevel = 1 } }` is non-standard |
| 37 | **No dark/light theme switching** | `Theme.kt` | Both defined but app only uses frosted dark |
| 38 | **No onboarding / splash** | Entire app | Cold start is blank feed |
| 39 | **No settings / privacy / account screens** | Entire app | Missing Instagram-expected flows |
| 40 | **Notification bell is a Toast** | `MainActivity` | Top-bar notifications do nothing real |

---

## 3. What the App Actually Does Right Now

1. **UI shell** is a competent Instagram clone: bottom nav, top bar, stories row, post cards, reels pager, profile grid, create post, DMs list, chat detail.
2. **Local CRUD works** for posts, comments, follows, stories, messages via Room.
3. **StateFlow architecture** is mostly correct: `collectAsStateWithLifecycle`, `flatMapLatest`, `combine`.
4. **Pagination** exists for feed via `_feedLimit` + `getFeedPostsForUserPaged`.
5. **Seed data** makes the prototype immediately explorable.
6. **Compose UI** uses modern patterns: `ModalBottomSheet`, `VerticalPager`, `AnimatedVisibility`, `DropdownMenu`.

---

## 4. Recommended Next Steps (Priority Order)

### Immediate (to make it a real app)
1. Decide: **continue Android native** or **switch back to Expo + NestJS** as originally planned. The current repo is a dead-end AI Studio prototype for production.
2. If continuing Android: introduce **real backend API** (NestJS/Firebase), replace Room seed data with network sources.
3. Add **auth** (Firebase Auth or phone OTP) and remove hardcoded user.
4. Fix **reels comment/share** to use reel id, not `allPosts.firstOrNull()`.
5. Fix **comment timestamps** to use real time-ago logic.
6. Fix **destructive migration** — write proper Room migrations.

### Short-term
7. Extract ViewModel into feature modules (`feed`, `profile`, `create`, `chat`, `reels`).
8. Add **Result/Either** wrappers to Repository for error handling.
9. Add **loading/disabled states** to all mutation buttons.
10. Implement **real filters** on CreatePost (e.g., `RenderScript` or `OpenGL` shaders).
11. Add **ExoPlayer / Media3** for real reel video playback.
12. Add **carousel support** to PostEntity (`mediaAssets` array).

### Medium-term
13. Add **offline sync queue** if staying local-first.
14. Add **string resources** + Hindi locale files.
15. Add **onboarding**, **settings**, **privacy**, **report/block**.
16. Enable **minify** + proper Proguard rules for release.
17. Add **real search** via backend or local FTS.

---

## 5. Flow Diagram (Text)

```
MainActivity
 └─ InstagramApp (Scaffold + BackHandler cascade)
     ├─ selectedTab == HOME → FeedScreen
     │    ├─ StoriesRow → StoryViewerDialog (fullscreen overlay)
     │    ├─ PostCard[] → PostDetailScreen (overlay)
     │    │    └─ comments → CommentsBottomSheet (overlay)
     │    │    └─ share → ShareSheet (overlay)
     │    ├─ SuggestedCreatorsCard → ProfileScreen (overlay)
     │    └─ empty state → SuggestedCreatorsCard
     ├─ selectedTab == EXPLORE → ExploreScreen
     │    └─ ExploreTile → PostDetailScreen
     ├─ selectedTab == CREATE → CreatePostScreen
     │    └─ Share → repository.createPost → HOME tab
     ├─ selectedTab == REELS → ReelsScreen
     │    └─ ReelPage[] → CommentsBottomSheet (BROKEN: dummy post)
     │                   → ShareSheet (BROKEN: dummy post)
     └─ selectedTab == PROFILE → ProfileScreen
          ├─ Edit profile dialog
          ├─ Grid/Reels/Saved/Tagged tabs
          └─ Post click → PostDetailScreen

Overlays (shown via early return / if-check in InstagramApp):
 1. StoryViewerDialog
 2. CommentsBottomSheet
 3. ShareSheet
 4. ChatDetailScreen
 5. DirectMessagesScreen
 6. PostDetailScreen
 7. ProfileScreen (viewed user)
```

---

## 6. Files Audited

| Category | Files |
|----------|-------|
| Entry | `MainActivity.kt` |
| ViewModel | `ui/viewmodel/InstagramViewModel.kt` |
| Screens | `ui/screens/{Feed,PostDetail,Explore,CreatePost,Reels,ChatDetail,DirectMessages,Profile}Screen.kt` |
| Components | `ui/components/{PostCard,CommentsBottomSheet,StoriesRow,StoryViewerDialog,InstagramTopBar,InstagramBottomNav,ShareSheet,ImageHelper}.kt` |
| Theme | `ui/theme/{Color,Type,Theme}.kt` |
| Data models | `data/model/{Post,Comment,Story,User,UserProfile,Follow,DirectMessage}Entity.kt`, `ReelItem`, `ChatThread` |
| DAOs | `data/db/{Post,Comment,Story,User,Follow,Message}Dao.kt`, `AppDatabase.kt` |
| Repository | `data/repository/InstagramRepository.kt` |
| Build | `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties` |

---

## 7. Bottom Line

The app is a **visually polished local prototype**. It looks like Instagram but has **no backend, no auth, no real networking, and no persistence beyond a single device**. The two biggest issues are:

1. **Reels comment/share are wired to the wrong object** — this is an actual runtime bug.
2. **The entire architecture is offline/demo** — if you want a real social app, this needs a backend or a clear local-first sync plan.

If the goal is a solo-buildable India-first social app, the current codebase should be treated as a **UI reference**, not as production foundation.
