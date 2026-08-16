# Qulloobul Moe'mieneen — Android Port

A native **Android Studio (Kotlin + Jetpack Compose, Material 3)** port of the
`INSY7315_Prototype_v1.0` Blazor Server / MudBlazor prototype.

Scope of this pass, as agreed: **Login + Home + Daily Tasks**, in-memory data
only (no Room/database yet), Jetpack Compose UI.

---

## How to open

1. Unzip the project.
2. Open the folder in Android Studio (Koala/Ladybug or newer).
3. Let Android Studio sync Gradle — it will regenerate the Gradle wrapper
   jar/scripts automatically on first sync if they're missing (or run
   `gradle wrapper` once if you have Gradle installed locally).
4. Run on an emulator or device (minSdk 26 / Android 8.0+).

---

## Mobile App YouTube Video Demonstration Link

https://youtu.be/QPmFQNbdpw8

---

## What's done

| Area | Razor source | Kotlin/Compose equivalent | Notes |
|---|---|---|---|
| Role model | `MainLayout.razor` (string role) | `model/Role.kt` | Enum with the same 4 roles and `canSeeMeetings` / `canSeeUserManagement` / `canSeeEvents` gating logic |
| Task models | `Tasks.razor` private `TaskItem`, `Priority`; `Home.razor` private `DailyTask` | `model/Task.kt` | Kept as two separate models, same as the original |
| Session + demo data | `NotificationService.cs` pattern, seeded lists in `Home.razor` / `Tasks.razor` | `data/AppViewModel.kt` | Single in-memory `ViewModel`; resets on process death, same as the prototype having no real backend |
| Theme / palette | `MainLayout.razor`'s `MudTheme` (light/dark) | `ui/theme/Color.kt`, `ui/theme/Theme.kt` | Colors copied 1:1 from the MudBlazor palette |
| Login screen | `MainLayout.razor` login card | `screens/LoginScreen.kt` | Name field, role dropdown, role hint text, dark-mode toggle, same "prototype sign-in" disclaimer |
| Home dashboard | `Home.razor` | `screens/HomeScreen.kt` | Header banner + completed chip, task checklist, "Completed Today" summary panel |
| Daily Tasks timeline | `Tasks.razor` | `screens/TasksScreen.kt` | Week day-strip, day navigation, all-day bar, hourly timeline, priority legend, "New Task" button |
| Completion dialog | `CompletedByDialog.razor` | `components/CompletedByDialog.kt` | Dropdown of staff, Confirm/Cancel |
| New task dialog | `CreateTaskDialog.razor` | `components/CreateTaskDialog.kt` | Title, date picker, time picker, all-day checkbox, category + priority dropdowns |
| Navigation shell | `MainLayout.razor` + `NavMenu.razor` | `nav/AppNavHost.kt` | Modal nav drawer, role-gated menu items, top bar with notification/dark-mode icons |
| App icon / logo | `wwwroot/images/logos/...png` | `res/drawable/logo.png` | Copied in, used on login screen and in the drawer header |

Not ported (by design, this pass): the "now" red time-indicator line on the
timeline, and the day-strip's live current-time positioning math — these are
minor visual details that can be added later if wanted.

---

## What still needs to be done

### 1. Remaining screens (currently placeholder "Coming soon" stubs)
Each of these has a full Razor implementation to port — none of the logic
below exists in Kotlin yet, only a nav-drawer entry + stub screen:

- ~~**Resource Requests** (`Requests.razor`, 622 lines)~~ — **Done.** `screens/RequestsScreen.kt` (submission form + filterable list) and `components/DenyRequestDialog.kt` (the deny-with-reason dialog). Approve/Deny only render for Admin, matching `IsAdmin`.
- ~~**Meetings** (`Meetings.razor`, 566 lines)~~ — not yet ported (still next in line).
- **User Management** (`UserManagement.razor`, 643 lines) — Secondary Staff / Admin only. Not yet ported.
- ~~**Event Planning** (`Events.razor`, 717 lines)~~ — **Done.** `model/Event.kt` (`EventModel`, ported as an immutable data class rather than the original's mutable Clone()/CopyFrom() class, which fits Compose state better) and `screens/EventsScreen.kt` — roster + create/edit/detail panel, stacked in one scrollable column instead of the original's two-column md=5/7 grid since this is a phone layout. Saving a new event or adding staff to an existing one fires the same "you've been assigned" notifications as `NotifyAssignedStaff` did, reusing the notification pipeline built for Resource Requests.
- ~~**Notifications** (`Notifications.razor`, 359 lines)~~ — **Done.** `model/Notification.kt` + the notification list/actions on `AppViewModel` are the Kotlin equivalent of `NotificationService.cs` (in-memory, on the shared ViewModel rather than a separate scoped service). `screens/NotificationsScreen.kt` has filter chips, mark-as-read/mark-all/clear-all, and relative timestamps. The top bar bell now shows an unread-count badge and routes to Requests/Events on tap, same as the original's `OpenNotification`.

### 2. Smaller gaps within the ported screens
- The red "now" line on the Daily Tasks timeline (current-time indicator) isn't implemented.
- The day-strip's live "today" highlighting works, but there's no auto-scroll to the current hour on load.
- `NewTaskResult`/`CreateTaskDialog` doesn't validate against past dates/times the way a production app might want.

### 3. Data & persistence
- Everything is in-memory (`ViewModel` state) and resets on process death or app restart — matches the original prototype's behavior, but if this becomes a real product it will need:
  - A real backend or local persistence (Room was declined for this pass, per your choice — happy to add it later)
  - Real authentication instead of the "prototype sign-in — role only, no password" flow

### 4. Polish / production readiness
- Adaptive/launcher icon set (currently using the raw logo PNG as `android:icon`, not a proper adaptive icon with foreground/background layers)
- App icon and splash screen styling
- String resources are only partially externalized to `strings.xml` — most UI text is still inline in Compose code, which is fine for a prototype but should move to resources for localization
- No automated tests yet (unit tests for `AppViewModel` logic, UI tests for the Compose screens)
- Accessibility pass (content descriptions are present but not audited)
- Tablet/large-screen layout (nav drawer vs. permanent rail) not addressed

### 5. Gradle wrapper
- `gradle/wrapper/gradle-wrapper.properties` is included (points at Gradle 8.7), but the wrapper `.jar` and `gradlew`/`gradlew.bat` scripts were not generated in this sandbox (no network access to Gradle's distribution site). Android Studio will regenerate these automatically on first sync, or run `gradle wrapper` locally once if needed.

---

## Suggested next steps

1. Open the project and confirm it builds/runs for Login → Home → Daily Tasks → Resource Requests → Notifications → **Event Planning** (new this pass).
2. Pick the next screen to port — **Meetings** or **User Management** are all that's left of the five originally-stubbed pages.
3. Decide if/when to add Room for persistence — the `AppViewModel` is structured so this is a fairly contained change (swap the in-memory lists for a Room-backed repository without touching the screens much).
