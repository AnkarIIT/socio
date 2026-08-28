# TeaGram

A vernacular-first, lightweight social media app for India — built for Gen Z, Gen Alpha, and Millennials. Think “Instagram for Bharat”, optimized for low-end devices, slow networks, and Indian languages.

**Current status:** Local prototype (offline / demo data). Backend, auth, and real networking are not yet wired.

## Stack

- **Android:** Kotlin + Jetpack Compose
- **Local storage:** Room
- **Images:** Coil
- **AI:** Gemini AI via Firebase/Google AI Studio
- **Build system:** Gradle (AGP + KSP)

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android SDK with at least API 36 configured

## Run locally

1. Open the project in Android Studio.
2. Sync Gradle and allow any suggested fixes.
3. Create a `.env` file in the project root and set `GEMINI_API_KEY`.
4. Remove the `signingConfig = signingConfigs.getByName("debugConfig")` line from `app/build.gradle.kts` if present.
5. Run on an emulator or physical device.

## Project layout

- `app/src/main/java/com/example/ui/screens` — screens
- `app/src/main/java/com/example/ui/components` — reusable UI
- `app/src/main/java/com/example/ui/viewmodel` — ViewModels
- `app/src/main/java/com/example/data` — Room DB, entities, repository
- `app/src/main/java/com/example/util` — small helpers like `TimeAgo`

## Roadmap

- Real backend + auth
- Follow graph + block/mute
- Comments, likes, saves
- Reels
- Stories
- Chat
- Notifications
- 11+ language support
- Offline-first mutations

## License

This project is provided as-is for learning and experimentation.
