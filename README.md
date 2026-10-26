# Geekspad

Geekspad is a Java Android app for developer profiles, skill-based discovery, meetups, peer feedback, and private messaging. This repair keeps the original app's activities, fragments, adapters, and Firebase architecture, modernizes its build, and fills in features that the original README described but did not implement.

**Start with the local Firebase emulators.** They run real Firebase Authentication and Realtime Database APIs against fictional accounts. This is a working local backend, not an in-memory UI mock. The default APK cannot connect to the historical hosted project or university server.

## Quick start

Prerequisites:

- JDK 21; Android Studio or Android SDK with platform 35, build tools, and an emulator/device running **Android 6.0 / API 23 or newer**.
- Node.js 22 and npm. The pinned Firebase CLI uses JDK 21; the Android source remains Java 17 compatible.
- `ANDROID_HOME` pointing at your SDK, or an untracked `local.properties` with `sdk.dir=...`.

Terminal 1, from the repository:

```sh
cd backend
npm ci
npm start
```

Wait for `All emulators ready`. Terminal 2:

```sh
cd backend
npm run seed
cd ..
./gradlew assembleDebug
./gradlew installDebug
```

Launch **Geekspad** on the Android emulator. The login form is prefilled with `alex@example.test` / `reviewer123`. Other fictional accounts: `sam@example.test` and `taylor@example.test`, same password. Sign out through the toolbar before switching accounts.

- Android emulator backend address: `10.0.2.2` (default).
- Firebase Auth: `127.0.0.1:9099`; Database: `127.0.0.1:9000`.
- Local inspection UI: <http://127.0.0.1:4000>.
- APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Android Studio: open this repository, use JDK 21 for Gradle, and run `app`.

For a USB device, keep services bound to loopback and forward their ports:

```sh
adb reverse tcp:9099 tcp:9099
adb reverse tcp:9000 tcp:9000
./gradlew installDebug -PemulatorHost=127.0.0.1
```

The seed command resets the three fictional profiles and welcome event, but does not erase other local data. Emulator data is temporary by default. For a clean slate, stop and restart `npm start`, then seed again. Do not run the destructive test suite against a reviewer session whose data you want to keep.

## Reviewer walkthrough

1. **List:** view Sam and Taylor, open a profile, or filter by country/state, skill, and minimum self-declared proficiency. Clear every filter to restore everyone. This is the recruiter discovery workflow; no separate recruiter account is required.
2. **Profile:** edit your own profile and skills. UID ownership prevents writing someone else's profile. Skills survive the activity transition; removing a skill now persists.
3. **Events:** create a future meetup, edit it, or cancel it. Sign in as Sam to RSVP, then cancel your RSVP. Only the organizer can edit/cancel an event; only the attendee can alter their RSVP.
4. **Peer skill ratings:** open another person's profile, select a listed skill, give 1–5 stars, and describe the interaction. Each person has one editable rating per subject/skill. Averages and feedback are shown separately from self-declared proficiency; self-rating is rejected.
5. **Chat:** choose another person and send a message. Success appears only after the database accepts the write. Sign in as the recipient to read/reply. Rooms are private to their two members; messages cannot be forged, edited, or deleted through the client rules. The screen shows the most recent 200 messages.
6. **Map:** without a configured API key, the screen explains the requirement instead of showing a broken map. The people list and location filters remain usable. With your own Maps key, stored profile coordinates become clickable markers; cloud-mode address geocoding runs off the UI thread. Local mode never calls a geocoding service or invents coordinates.

## Original README claims and implementation evidence

The original README is preserved at [docs/README-original.md](docs/README-original.md). The old installation claim (Android 4.3/4.4) no longer applies after dependency modernization.

| Original claim | What is implemented now | Evidence boundary |
| --- | --- | --- |
| Tech geeks connect through profiles | Firebase email/password signup/login, profile editing, skills, people list, location filters, optional phone dialer | Real local Auth/Database; UID ownership rules and tests |
| Organize meetups and social events | Shared create/edit/cancel workflow, date validation, RSVP/cancel RSVP, attendance count | Emulator workflow/rules tests; Android UI smoke |
| Rate another person's technical skills | One 1–5 rating per reviewer/subject/skill, interaction context, averages, update support; no self-rating | Emulator positive/negative tests; Android UI smoke |
| Recruiters find people for a position | Filter profiles by location, skill and minimum self-declared proficiency; inspect peer feedback | Unit tests for filter combinations and reset; existing UI retained |
| Messaging | Deterministic two-person UID rooms, realtime last-200 history, send acknowledgment, lifecycle listener cleanup | Emulator privacy/tampering tests and Android UI smoke |
| Push notifications | FCM registration/refresh/removal, Android notification permission/channel, server-side message trigger and stale-token cleanup | Compiled client, supplied deployable function, and tested fan-out/token cleanup. **Actual hosted FCM delivery has not been verified** and is disabled locally |
| Firebase backend and authentication | Explicit project configuration, local emulators, rules, fictional seed script | Tests use real local APIs; hosted deployment remains your configuration step |
| Google Maps location plotting | Profile coordinates, markers, clickable profile detail, lifecycle cleanup, optional geocoding | Compiles; missing-key state tested locally. **Live map tiles/geocoding require your key and have not been verified here** |
| SDSU Bismarck trial integration | No surviving source implementation was found; no requests go to that historical server | Replaced operationally by the documented local Firebase backend; no claim that Bismarck still works |
| Design patterns | Chat presenter/interactor; observer-style Firebase listeners; SDK singleton access; builder-based Firebase/notification setup; static `ChatFragment.newInstance` creation helper | Concrete source examples, rather than implying every historical pattern label is a dedicated custom framework |

## Tests

```sh
# Java model/filter regressions and Android static analysis
./gradlew testDebugUnitTest lintDebug assembleDebug

# Starts fresh local Auth/Database emulators, runs workflow/security tests, then stops
cd backend
npm test

# Notification routing and stale-token cleanup, using injected clients (no real push)
cd functions
npm test
```

Do not run `npm test` while `npm start` is using the same ports. Tests reset local data and exercise both allowed and rejected operations, including unauthorized profile edits, event hijacking, fabricated ratings, cross-room reads, forged chat messages, message modification/deletion, and private token access.

For Android UI tests, start `npm start`, run `npm run seed`, start an Android virtual device, then run from the repository root. Disable device animations to match CI and avoid transient touch-target movement:

```sh
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
./gradlew connectedDebugAndroidTest
```

The UI suite signs in, creates a meetup, rates a peer, sends a message, and verifies database persistence. Regression tests also verify Firebase profile/skill deserialization and parceling, invalid profile input, birthday return behavior, skill preservation on save, protection against saving before profile loading, and adapter update notifications. The suite asserts emulator mode and must not target a hosted project. The CI workflow is configured to build the APK and run these checks on an Android emulator; a remote CI run has not been performed. See [the local verification record](docs/verification.md).

## Configure a hosted Firebase project and Maps

No historical keys or project configuration are bundled. Create your **own** Firebase project and Android app with package `com.example.sreer.geekspad`. Enable email/password Authentication and Realtime Database. Use the actual database URL from your console (including region if applicable).

Keep configuration in your untracked user Gradle properties, or supply `-P` options:

```properties
useEmulators=false
firebaseProject=YOUR_PROJECT_ID
firebaseAppId=YOUR_ANDROID_APP_ID
firebaseApiKey=YOUR_FIREBASE_WEB_API_KEY
firebaseUrl=https://YOUR_DATABASE_URL
firebaseSenderId=YOUR_MESSAGING_SENDER_ID
mapsApiKey=YOUR_RESTRICTED_ANDROID_MAPS_KEY
```

Firebase client configuration is not a replacement for access control. Deploy the supplied database rules to your project. Existing email-keyed databases require deliberate migration; this version uses UIDs and a new `rooms` schema. There is no automatic write/migration to the original project.

```sh
cd backend
npx firebase login
npx firebase deploy --project YOUR_PROJECT_ID --only database
cd functions
npm ci
cd ..
npx firebase deploy --project YOUR_PROJECT_ID --only functions
```

The function runs on accepted `/rooms/{room}/messages/{message}` creation and sends a generic private-message notification to the recipient's device tokens. Cloud Functions deployment may require a billing-enabled project. Use the database's region for the deployed function; configure `onValueCreated` with an explicit `region` in `functions/index.js` if using a non-default regional database. Verify delivery on two test devices after deployment; emulator tests cannot prove FCM delivery.

For Maps, enable **Maps SDK for Android** and configure a key restricted to this package and signing certificate SHA-1. `./gradlew signingReport` shows the debug fingerprint. Billing/API access and Google Play services must be available. Add the key through `mapsApiKey`, rebuild, and inspect seeded profile markers. Do not commit signing keys, service-account keys, or production configuration.

Useful primary setup references: [Firebase Android setup](https://firebase.google.com/docs/android/setup), [Firebase Local Emulator Suite](https://firebase.google.com/docs/emulator-suite), [Realtime Database rules](https://firebase.google.com/docs/database/security), [FCM Android client](https://firebase.google.com/docs/cloud-messaging/android/client), [Maps Android API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key).

## Source map and operational changes

- `app/src/main/.../ui/`: original activity/fragment/adapters; new event and peer-rating screens.
- `db/Backend.java`: one configured database instance shared across screens and account changes.
- `db/FireBaseHelper.java`: safe profile decoding and centralized discovery queries.
- `chat/`: preserved presenter/interactor structure with deterministic rooms and listener teardown.
- `GeekspadApplication.java`: explicit cloud/local Firebase configuration.
- `notifications/`: real FCM handler, registration, and notification presentation.
- `backend/`: local setup, rules, seed data, integration tests, optional hosted notification function.

Additional repairs include profile form validation, distinct birthday/skill activity results, load-gated skill saving, retained skill-row notifications, preserving existing coordinates when an unchanged address cannot be geocoded, empty-data callbacks, null-safe skills, minimum-threshold filtering, clearing filters, session sign-out, acknowledged writes, a profile-loading guard before sending, main-thread geocoder removal, Maps lifecycle handling, Android notification permissions, AndroidX migration, JCenter removal, a checked-in modern Gradle wrapper, and removal of tracked build output/machine settings/historical service configuration.

This remains a small portfolio app: discovery loads authenticated profiles client-side; chat has a bounded recent-history window; there is no moderation console, search index, email-verification gate, pagination UI, or production abuse-control service. These are not implied by the demo.

## License

The original repository only stated “Free Software” without a named license. No new license grant is invented here; obtain the owner's permission before reuse that requires one.
