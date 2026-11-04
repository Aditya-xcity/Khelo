# Khelo Android UI and navigation audit

Khelo is a native Android prototype for discovering local sports games, joining or leaving a game, and hosting a match. The manifest launches a single Kotlin activity. Its screens are programmatically constructed Android Views, with no Compose, XML screen layouts, fragments, or navigation graph. The app uses AGP 9.1.1, targets Android API 36, compiles against 36.1, and supports API 24 and above.

[MainActivity](app/src/main/java/com/example/khelo/MainActivity.kt) owns the window, current route, back stack, and tab selection. [KheloNav](app/src/main/java/com/example/khelo/khelonav.kt) defines what screens can ask the activity to do. [Screens](app/src/main/java/com/example/khelo/Screens.kt) constructs the seven screens, and [UiKit](app/src/main/java/com/example/khelo/UiKit.kt) contains shared controls and palette. [MockRepo](app/src/main/java/com/example/khelo/Mockrepo.kt) provides synchronous local sample data and mutable game membership. There are no ViewModels, network calls, authentication, or durable storage. There are no asynchronous loading or network error states; discovery provides an empty state for unmatched filters.

| Screen | Existing flow and data | Issues found and changes |
| --- | --- | --- |
| Home | Nearby sample games, sport shortcuts to Games, notifications and profile shortcuts, create prompt | Search accepted text without filtering. Added live game search, a clearer introduction, a matching-results empty state, and more room for sport labels. |
| Games | Sport filters, game cards, join/leave, and Create shortcut | Date and search controls did not filter results. All three filters now work together; filters and scroll position are restored on return. |
| Game details | Game metadata, participation count, and join/leave action | The fixed action competed with the app tabs. The back arrow always went to an unfiltered Games screen, and the player dots could run offscreen. Details now hides the tabs, returns to the previous screen, uses a progress bar, and updates the count when membership changes. |
| Create | Sport, title, date/time, location, player count, skill, and notes; creates a local game | Switching screens discarded the draft, and small screens squeezed the date and time fields. Added draft restoration, stacked inputs, larger stepper controls, and IME-safe scrolling. |
| Game created | Confirmation, game summary, invite/share toast actions, and details link | Added Back, removed excessive top spacing, and hid the app tabs. Back returns to the screen that opened Create after a successful submission. |
| Profile | Sample profile, game counts, sports, nearby players, and existing action feedback | Removed bottom spacer and moved the screen into the shared safe viewport. Shared cards and action styling apply here. |
| Notifications | Static sample notification feed reached from Home | Back now uses the same stack as Android Back, tabs stay hidden, and the feed uses the shared viewport. |

The old activity placed the screen and tab bar in the same full-window `FrameLayout`. Android 15 enforces edge-to-edge for this target, so the bar was pinned to the physical bottom and never reserved the system navigation inset. Several screens tried to compensate by adding fixed blank views to the end of their scroll content. These blanks could not move the app tabs or the pinned game action out of Android's system bar.

The new window reserves the union of the system bars, display cutout, and IME insets once, through `WindowInsetsCompat`. The screen and tab bar are laid out as siblings in a vertical container, so screen content is measured above the app tabs. The keyboard hides the app tabs while it is open. Wider windows center the content with a maximum width; landscape uses a shorter tab layout with icons beside labels. The manifest enables keyboard resize and keeps the IME closed on launch. The Material 3 light theme replaces the unrelated purple action bar while retaining Khelo's green identity. The app uses one light palette consistently.

The repository and model layer were kept intact. Game data is still process-local, so game membership and newly created games are lost on process death. The UI restores routes where the referenced data still exists. Profile editing, invites, share, location and other pre-existing simulated actions still use the local prototype behavior. Real services and an explicit dark palette are separate follow-up work.

## Verification

- Installed the debug APK over the existing Khelo app on the connected Xiaomi (Android 15, model identifier 23076RN4BI). In portrait three-button mode, the original app's labels began below the system navigation boundary at y=2330. In the updated app, all four tab hit areas end at y=2319, above the system bar.
- The debug build, JVM unit tests, and lint passed. Lint reported warnings and no errors.
- All three instrumentation tests passed on the connected phone. Regression coverage checks safe tab bounds, navigation from Home to Notifications and back, returning from game details to the active date filter, and draft preservation when switching tabs and recreating the activity. The device blocked instrumentation-initiated background launches, so the test activities were brought forward using ADB.
- The phone blocked scripted input and device configuration changes. A full keyboard-open form walkthrough, gesture navigation, landscape, alternate font scales, and other display sizes were not verified on the final build. There was no configured emulator.
