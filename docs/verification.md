# Local verification record

Reverified 2026-10-25 (America/Los_Angeles) from the permanent saved clone before commit/publication. Dependencies were freshly installed from the final lockfiles; builds used the checked-in Gradle wrapper.

## Environment

- macOS ARM64; JDK 21.0.13; Gradle 8.9; Android Gradle Plugin 8.7.3.
- Android SDK 35; Android 15 / API 35 Google APIs ARM64 virtual device; window/transition/animator scales set to 0, matching CI.
- Node.js 22.23.4; Firebase CLI 15.34.1; Firebase web SDK 12.19.0; rules-unit-testing 5.0.2; Admin SDK 14.5.0; Functions SDK 7.4.0.
- Firebase project `demo-geekspad`; Auth and Realtime Database emulators bound to loopback.
- All accounts, events, ratings, messages, and notification tokens in tests were fictional/local.

## Checks executed

| Check | Result | What it establishes |
| --- | --- | --- |
| `./gradlew testDebugUnitTest` | 9 passed | Discovery thresholds/combinations/reset, missing skills, room identity, meetup validation |
| `cd backend && npm test` | 6 groups passed | Real local Auth signup/sign-in/wrong-password; profile ownership; event create/edit/RSVP/cancel; peer ratings; two-way chat persistence/privacy/anti-forgery/immutability; private tokens |
| `cd backend/functions && npm test` | 2 passed | Recipient-only FCM routing, batches of 500, permanent-invalid-token cleanup, no private text in notifications, malformed/self-message rejection; injected clients, no real push |
| `./gradlew connectedDebugAndroidTest` | 5 passed on API 35 | Real installed UI login, loaded profiles, meetup creation, peer rating, message send, database persistence; profile mapping/parcel; edit validation/birthday/skill preservation; save gating; retained adapter row updates |
| `./gradlew lintDebug` | Passed: 0 errors, 226 warnings | Static Android check; legacy UI/accessibility/deprecation warnings remain |
| `./gradlew assembleDebug` | Passed; APK installed and launched | Build and device installation with the local backend configuration |
| Manual installed-app inspection | Passed | People list loaded; missing-Maps-key explanation displayed without a map/API request |
| `git diff --check` | Passed | No whitespace errors |

The final Gradle commands were:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew connectedDebugAndroidTest
```

The final APK is `app/build/outputs/apk/debug/app-debug.apk`. SHA-256:

```text
386e0cd3bf4d023cc31e50def4f9b84a4d8f1a6c85849bfb679e6c84cb4d3301
```

Screenshots from the final installed APK: [people list](screenshots/people-emulator.png) and [Maps configuration state](screenshots/maps-configuration-state.png).

Generated test reports are under `app/build/test-results/testDebugUnitTest/`, `app/build/outputs/androidTest-results/connected/debug/`, and `app/build/reports/`. These are ignored build output; rerun the documented commands to reproduce them. The first hosted run failed during SDK setup before any project checks: the action requested the removed Android `tools` package. The workflow now explicitly installs `platform-tools`, pins command-line tools 20.0, and applies the emulator action's documented Ubuntu KVM setup. A successful hosted rerun remains to be verified.

## Regressions found through device execution and review

- A fresh device rerun caught a navigation click reaching a neighboring tab during startup. Bottom navigation now has fixed item widths and visible labels, initializes selection through its public API, and the regression explicitly checks the selected tab. Toolbar filtering also tolerates menu creation timing.
- Firebase profile skill values failed to deserialize through an untyped setter; typed map decoding and parcel round-trip now pass.
- Repeated Firebase database lookup after emulator configuration created inconsistent connections; one configured database instance is shared throughout the app.
- A legacy support-library chat layout compiled but crashed when opened; the AndroidX layout now passes the full chat flow.
- Invalid profile input previously dereferenced a null model; required first/last names now remain on the form with field errors.
- Birthday and skill editor results previously shared a request code; choosing a birthday no longer closes the unsaved profile editor.
- Saving skills before asynchronous loading could erase the existing set; save is enabled only after successful loading and remains disabled on missing/error results.
- Updating a skill incorrectly notified the adapter that the row was removed; it now emits a change and retains the row.
- Editing a profile with unchanged location could erase existing coordinates when local geocoding was disabled; those coordinates are retained.

## Dependency audit and reproducibility

Both `backend/` and `backend/functions/` passed fresh `npm ci` installs from their checked-in lockfiles using Node 22.23.4. Every resolved package URL points to the public npm registry. No temporary SDK paths, machine configuration, production credentials, or generated build output are committed.

Fresh `npm audit --json` results:

- Deployable notification function (`backend/functions`): **0 vulnerabilities**. Both routing/cleanup tests pass; the actual `index.js` trigger loads with the updated SDKs. This does not establish hosted delivery.
- Local emulator/test tooling (`backend`): **5 moderate findings, 0 high, 0 critical**. The five affected packages are `firebase-tools`, `@google-cloud/pubsub`, `@opentelemetry/core`, `gaxios`, and `uuid`; these reflect two underlying advisory chains in the CLI dependency tree, not five separate application vulnerabilities.
- The remaining upstream advisories concern [OpenTelemetry baggage allocation](https://github.com/advisories/GHSA-8988-4f7v-96qf) and [UUID buffer bounds](https://github.com/advisories/GHSA-w5hq-g745-h8pq). They are not dependencies of the deployed notification function. The CLI is kept local and the app's default endpoints bind to loopback. No claim of a zero-finding tooling tree is made.
- The same-major `@grpc/grpc-js` override at 1.14.6 removes an inherited vulnerable Firestore test dependency. The Auth/Realtime Database suite passes with it. Forced major dependency downgrades proposed by npm audit were not applied.

The original README archive matches the original Git version byte-for-byte. Its path-specific `.gitattributes` entry preserves the historical whitespace; all other staged content passes the standard whitespace check. CI is configured for JDK 21/Node 22 and the same commands, but a remote GitHub Actions run has not yet been verified.

## Evidence boundaries

- Hosted Firebase deployment and actual FCM delivery were not performed. The compiled client and supplied server function need the reviewer's own project; the delivery test uses injected clients.
- Live Google Maps tiles and address geocoding were not verified. A configured restricted Maps key and Google services access are required. Local mode skips geocoding.
- No request was sent to the historical university backend or original Firebase project.
- The default APK targets local emulators, not a standalone offline database. Keep the backend running as described in the README.
- Tests cover the stated flows and regressions, not every device/API level, lifecycle interruption, or production deployment condition. Store publication, production moderation, and scale/load qualification are outside this repair.
