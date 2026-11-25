# 📘 Smart Drill Android Developer Guide

Welcome to the **Smart Drill** Android project guide! This document provides comprehensive instructions on setting up, running, testing, and continuing development on the Android application.

---

## 1. Project Overview

### What the App Does & Who Uses It
**Smart Drill** ("Stay Smart, Stay Safe") is a campus emergency drill and disaster response mobile application built for students, faculty, and campus safety officers at **DNSC (Davao del Norte State College)**. It provides real-time disaster warnings (earthquake, fire, flood, storm), interactive evacuation routing, offline peer-to-peer mesh connectivity, emergency hotlines, and a secure reporting mechanism for students to notify administrators of campus hazards.

### Implementation Status
* **Implemented:**
  * Jetpack Compose UI with dynamic color theme switching (Dark Slate, Midnight Navy, Emerald Defense, Crimson Command).
  * Persistent bottom navigation bar (`Map`, `Home`, `SOS`).
  * Firebase Authentication (Email/Password for admins, and background Anonymous Authentication for instant student SOS reporting).
  * Firestore integration for real-time alerts (`alerts/{alertId}`) and live evacuation locations (`evacuationLocations/{locationId}` rendered as markers and polygons).
  * SOS reporting workflow with actual GPS coordinate capture, anti-spam 5-minute cooldown (`lastReportAt`), and admin restriction enforcement (admins cannot submit SOS reports).
  * High-priority local push notifications with custom alarm sounds and vibration patterns (`EmergencyNotifier.kt`).
  * Offline P2P mesh networking via Google Play Services Nearby Connections (`NearbyMeshManager.kt`).
  * osmdroid OpenStreetMap integration with campus mapping and routing graph engine (`CampusGraph.kt`, `RouteFinder.java`).
* **Partially Implemented:**
  * Forgot Password flow (Standard Firebase password reset email flow configured via Auth).
* **Not Implemented / Pending:**
  * **MaxFlow route calculation algorithm:** The MaxFlow algorithm is currently not implemented in routing; routing utilizes standard graph finding (`CampusGraph` / `RouteFinder.java`). Do not invent a MaxFlow formula or describe current routes as MaxFlow results.

---

## 2. Project Structure

Important source folders and files located in `app/src/main/java/com/example/smartdrill/`:

* **`MainActivity.kt`**: Central application entry point. Manages Compose navigation (`HOME`, `MAP`, `SOS`), theme state, Firebase anonymous authentication initialization on startup, and real-time Firestore snapshot listeners for active alerts (`alerts`).
* **`EmergencyNotifier.kt`**: Core notification utility. Manages high-importance notification channels (`smart_drill_emergency_channel`), alarm sound/vibration execution, persistent sender ID generation (`Sender-#XXXX`), device fingerprinting (`getDeviceInfo()`), and location helper.
* **`NearbyMeshManager.kt`**: Handles offline peer-to-peer advertising and discovery using Google Play Services Nearby Connections API for off-grid SOS packet relay.
* **`ui/` Package**:
  * **`AppShell.kt`**: Defines top-level UI components including `ScreenGradient`, `HomeHeader` (with theme selector palette), `AppBottomBar`, and `DisasterAlertContent`.
  * **`HomeScreen.kt`**: Citizen dashboard displaying active disaster status, emergency instructions, and status cards.
  * **`MapScreen.kt`**: OpenStreetMap (`osmdroid`) canvas rendering user location, hazard zones, evacuation points (markers), evacuation areas (polygons), and interactive details cards.
  * **`EmergencyActionScreen.kt`**: SOS reporting screen featuring disaster tiles, official Panabo City hotlines, emergency instructions, and GPS location/cooldown validation.
  * **`LoginScreen.kt` & `SignUpScreen.kt`**: Authentication screens with show/hide password toggles and admin role checks (`users/{uid}.role == "admin"`).
* **`routing/` Package**:
  * **`CampusGraph.kt`**: Defines campus center coordinates (`LatLon(7.313721, 125.670175)`) and reference anchors for the DNSC campus map.
  * **`RouteFinder.java` & `Graph.java`**: Java routing graph engine.

---

## 3. Setup on a New Computer

### Requirements
* **Android Studio:** Ladybug or newer recommended.
* **JDK:** Java Development Kit 17 (configured via Gradle toolchain).
* **Android SDK:** Compile SDK 37, Min SDK 24, Target SDK 37 (configured in `app/build.gradle.kts`).
* **Gradle:** Gradle wrapper (`gradlew` / `gradlew.bat`) included in the project root.

### Step-by-Step Setup
1. **Clone or Open Project:** Open Android Studio and select *Open*, then choose the project root folder (`SmartDrill`).
2. **Gradle Sync:** Android Studio will automatically prompt to sync Gradle. If not, click **File > Sync Project with Gradle Files**.
3. **Configure Device/Emulator:**
   * Open the **Device Manager** in Android Studio.
   * Start an Android Virtual Device (AVD) running API 34+ (recommended: Medium Phone API 34/37), or connect a physical Android device with USB Debugging enabled.
4. **Run the App:**
   * Click the green **Run** (`▶`) button in the toolbar, or execute `./gradlew app:assembleDebug` in the terminal from the project root.

---

## 4. Firebase Setup

### Project Identifier
* **Firebase Project ID:** `smardrill-2b` (configured in `google-services.json`).

### Console Verification & App Registration
1. Log in to the [Firebase Console](https://console.firebase.google.com/).
2. Select project **Smardrill**.
3. Verify the Android app is registered with package name `com.example.smartdrill`.
4. **`google-services.json` File Location:** Must be placed in the project root (`google-services.json`) and the app module directory (`app/google-services.json`). *(Note: Never commit secret production keys or private service-account JSON files to public repositories).*

### Authentication Providers
In **Firebase Console > Authentication > Sign-in method**, ensure the following providers are enabled:
* **Email/Password:** Used by administrators (`albit.al@dnsc.edu.ph`) to sign in through the Admin Portal.
* **Anonymous:** Used in the background for instant student SOS reporting without requiring login.

### Checking Project ID in App
The project ID `smardrill-2b` and storage bucket are loaded automatically at runtime by the Google Services plugin using `google-services.json`.

---

## 5. Sign-in, Accounts, and Permissions

### Sign-in Flow & Account Types
* **Regular Students / Citizens:** No login or sign-up required. When sending an SOS report, the app authenticates anonymously in the background (`FirebaseAuth.signInAnonymously()`).
* **Administrators:** Must sign in via the Admin Login mode using approved credentials (`albit.al@dnsc.edu.ph`) registered in Firestore with `role: "admin"`. Administrators cannot submit SOS reports.

### Android Location Permissions
* **Permissions Requested:** `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` (declared in `AndroidManifest.xml` and requested at runtime in `MapScreen.kt` and `EmergencyActionScreen.kt`).
* **When Requested:** When viewing the map or when tapping an SOS disaster tile.
* **If Denied:** The app shows a Toast warning (`"Location permission required to send emergency reports with accurate coordinates"`) and prevents report submission to ensure no fake coordinates are submitted.
* **Notification Permission:** `POST_NOTIFICATIONS` requested on Android 13+ (API 33+) in `MainActivity.kt`.

---

## 6. Firestore Contract Shared with the Admin Website

The Android app and the admin web portal share the same Firebase project (`smardrill-2b`) and collection schemas:

* **`users/{uid}`**:
  * Fields: `name`, `email`, `username`, `role` (`"user"` or `"admin"`), `createdAt`, `lastReportAt`.
  * *Note on Cooldown:* The app checks `lastReportAt` to enforce a 5-minute anti-spam cooldown between report submissions. It updates this timestamp in an atomic Firestore `WriteBatch` alongside report creation.
* **`reports/{reportId}`**:
  * Fields: `userId`, `type` (`"flood"` | `"earthquake"` | `"storm"` | `"fire"`), `lat`, `lng`, `createdAt`, `status` (`"pending"` | `"approved"` | `"rejected"`), `reviewedBy`, `reviewedAt`.
  * *Contract:* Reports are created with `status: "pending"`. SOS coordinates come from the device's real GPS location (never the campus center).
* **`alerts/{alertId}`**:
  * Fields: `type`, `lat`, `lng`, `radiusMeters`, `message`, `steps` (array of strings), `createdAt`, `approvedBy`, `active` (boolean), `sourceReportId`.
  * *Contract:* The app listens to active alerts (`active == true`) in real time via a Firestore snapshot listener.
* **`evacuationLocations/{locationId}`**:
  * Fields: `kind` (`"point"` | `"area"`), `name`, `description`, `active` (boolean), `createdAt`, `updatedAt`, `updatedBy`.
  * *Geometry:* Points have numeric `lat` and `lng`. Areas have `coordinates` (list of maps with numeric `lat` and `lng`, minimum 3 points).
  * *Contract:* Android app reads active evacuation locations (`active == true`). Inactive locations are retired by admins (`active = false`).

---

## 7. Map and Evacuation Locations

* **Campus Center:** Latitude `7.313721`, longitude `125.670175` (defined in `CampusGraph.kt` and `MapScreen.kt`).
* **Return to Campus Action:** The map floating action button centers the map view on `7.313721, 125.670175`. *Crucial constraint:* The campus center is never used as a substitute for a device's real SOS GPS coordinates.
* **Evacuation Rendering:** Loaded dynamically via a live Firestore snapshot listener on `evacuationLocations` where `active == true`. Points are drawn as green circular markers (`Marker`), and areas are drawn as translucent green polygons (`Polygon`). Tapping either opens an overlay details card with the name and description.

---

## 8. Emergency Instructions and Hotlines

Defined in `EmergencyActionScreen.kt`:
* **Instructions Card:** Step-by-step emergency reporting guide (tap emergency type, GPS verification, 5-minute cooldown, admin approval workflow).
* **Panabo City Hotlines (One-Tap Dialing):**
  * **PNP (Police):** `0998 598 7104`
  * **Fire Station:** `0928 458 7586`
  * **CDRRMO:** `0930 238 5937`
  * **Power (Davao Light):** `0919 056 3572`

---

## 9. How to Use the App

1. **First Launch:** The app opens directly to the citizen dashboard (`HomeScreen`). No login required.
2. **View Alerts:** If an admin publishes an active alert, the dashboard and notification tray will alert you with alarm sound, vibration, and safety steps.
3. **Send an SOS:** Tap the bottom **SOS** tab, select an emergency tile (Flood, Earthquake, Storm, Fire), grant location permission when prompted, and confirm in the dialog. The report is submitted with real GPS coordinates.
4. **View Evacuation Map:** Tap the **Map** tab to view the campus map centered at `7.313721, 125.670175`, along with live evacuation points and polygon zones loaded from Firestore.

---

## 10. Testing and Verification

### Commands
* **Build Debug APK:** `./gradlew app:assembleDebug`
* **Run Unit Tests:** `./gradlew app:testDebugUnitTest`

### Manual Test Checklist
* [ ] **Auth / Role Check:** Verify regular users can send SOS without logging in, and admins cannot send SOS reports.
* [ ] **Location Permission:** Deny location permission and verify SOS submission is blocked with an error.
* [ ] **Report Submission:** Send an SOS report and verify it appears in the admin web queue as `pending` with correct GPS coordinates.
* [ ] **Active Alerts:** Approve a report on the admin website and verify the mobile app's dashboard updates live with alarm sound and vibration.
* [ ] **Evacuation Sync:** Add/retire an evacuation point or polygon on the admin website and verify it appears/disappears live on the app's map.

---

## 11. Build and Release

* **Debug Build:** `./gradlew app:assembleDebug` generates `app/build/outputs/apk/debug/app-debug.apk`.
* **Release Signing:** *To confirm* — Release signing keystores and passwords must be configured securely in `app/build.gradle.kts` using local `signingProperties` and must never be committed to Git.

---

## 12. Troubleshooting

* **Firebase Project Not Found:** Verify `google-services.json` is present in `app/` and root folders with project ID `smardrill-2b`.
* **Authentication Failure / Disabled Provider:** Ensure both **Email/Password** and **Anonymous** sign-in methods are enabled in the Firebase Console.
* **Firestore Permission Denied:** Check Firestore Security Rules to ensure signed-in users can create `reports` and read active `alerts` and `evacuationLocations`. *Do not weaken security rules.*
* **Location Permission Denied:** Ensure location services are enabled on the device/emulator and permissions are granted in app settings.

---

## 13. Future-You Checklist

### Setup & Run
1. Open project in Android Studio.
2. Sync Gradle and verify JDK 17.
3. Ensure `google-services.json` (`smardrill-2b`) is in place.
4. Run app on AVD API 34+.

### Still to Do (Pending Implementation)
* [ ] Implement the custom MaxFlow routing algorithm in the `routing/` package.
* [ ] Implement offline mesh packet relay persistence in background service if required for extended off-grid use.

---

## Summary of Inspection
* **Inspected Files:** `MainActivity.kt`, `EmergencyNotifier.kt`, `NearbyMeshManager.kt`, `CampusGraph.kt`, `AppShell.kt`, `HomeScreen.kt`, `MapScreen.kt`, `EmergencyActionScreen.kt`, `LoginScreen.kt`, `SignUpScreen.kt`, `UserRepository.kt`, `app/build.gradle.kts`, `google-services.json`.
* **Documentation File Created:** `docs/ANDROID_GUIDE.md` (and `README.md` updated with link to guide).
* **Confirmed Mismatches with Contract:** None; app data schemas for `users`, `reports`, `alerts`, and `evacuationLocations` match the admin contract precisely.
* **Items Marked "To Confirm":** Release signing keystore configuration in Section 11.
