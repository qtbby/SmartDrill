# 🔥 Smart Drill: Firebase & Admin Website Integration Guide

This guide explains step-by-step how the **Smart Drill** Android application connects to Firebase and synchronizes in real time with the companion **Admin Website**.

---

## 1. Firebase Architecture Overview

Smart Drill uses a serverless architecture powered by **Firebase** (`smardrill-2b`):
* **Firebase Authentication:** Manages administrator accounts (Email/Password) and instant anonymous sessions for students sending emergency reports without login friction.
* **Cloud Firestore:** Acts as the real-time database bridge connecting the Android app and the admin website.

---

## 2. Connecting the Android App to Firebase

### Step 1: Add `google-services.json`
1. Download your app's `google-services.json` configuration file from the [Firebase Console](https://console.firebase.google.com/) (Project Settings > General > Your Apps > Android app with package name `com.example.smartdrill`).
2. Place `google-services.json` in two locations within your project repository:
   * Project root folder (`/google-services.json`)
   * App module folder (`/app/google-services.json`)

### Step 2: Enable Authentication Providers
In the Firebase Console under **Authentication > Sign-in method**, ensure:
* **Email/Password:** Enabled (used by safety officers/admins to sign in).
* **Anonymous:** Enabled (allows students to tap SOS and report emergencies instantly without a login prompt).

### Step 3: Configure Firestore Security Rules
In **Firestore Database > Rules**, ensure your security rules allow signed-in users to create reports and read active alerts and evacuation locations (while restricting reviews and approvals to admin roles).

---

## 3. Connecting the Android App to the Admin Website (Firestore Bridge)

The Android app and the admin website communicate exclusively through shared **Cloud Firestore** collections. There is no separate backend server.

### 🔄 Data Flow 1: Emergency SOS Reporting (App ➔ Website)
1. **Student Action (Android App):**
   * A student taps **SOS** on the app, selects a disaster tile (Flood, Earthquake, Storm, or Fire), grants location permission, and confirms.
2. **Firestore Write:**
   * The app reads the device's real GPS coordinates (`lat`, `lng`), generates a unique Sender ID (`senderId`), captures device metadata (`deviceInfo`), and writes a document to **`reports/{reportId}`** with `status: "pending"`.
   * At the same time, it updates `users/{uid}.lastReportAt` to enforce a 5-minute anti-spam cooldown.
3. **Admin Website Action:**
   * The admin review queue on the website listens to the `reports` collection in real time, displaying incoming pending reports with their GPS coordinates, sender IDs, and device models.

### 🔄 Disaster Alert Broadcasting (Website ➔ App)
1. **Admin Action (Website):**
   * An administrator reviews a pending report on the website and clicks **Approve**.
2. **Firestore Write:**
   * The website creates or updates an active alert document in **`alerts/{alertId}`** with `active: true`, along with safety steps, radius, and message.
3. **App Real-Time Sync (`MainActivity.kt`):**
   * The Android app maintains a live Firestore snapshot listener on the `alerts` collection.
   * The moment `active == true` is detected, the app instantly triggers:
     * High-priority push notifications with loud alarm sounds and custom vibration.
     * Real-time dashboard update (`HomeScreen`) displaying the disaster warning and safety steps.

### 🔄 Evacuation Locations Sync (Website ➔ App Map)
1. **Admin Action (Website):**
   * Admins add, edit, or retire (`active: false`) evacuation points and polygon boundary areas on the website.
2. **Firestore Write:**
   * Changes are saved to the **`evacuationLocations/{locationId}`** collection in Firestore.
3. **App Map Sync (`MapScreen.kt`):**
   * The app's live snapshot listener on `evacuationLocations` (filtered by `active == true`) instantly updates the osmdroid map.
   * **Points** appear as green circular map markers; **Areas** appear as translucent green polygon boundaries. Tapping either opens an interactive details card with safety instructions.

---

## 4. End-to-End Testing Checklist

* [ ] **Firebase Connection:** Run `./gradlew app:assembleDebug` and verify the app builds and connects to project `smardrill-2b`.
* [ ] **Anonymous SOS Report:** Open the app on an emulator/device, go to SOS, select a disaster, and confirm. Verify in Firebase Console > Firestore > `reports` that a new document appears with `status: "pending"` and real GPS coordinates.
* [ ] **Admin Approval & Live Alert:** On the admin website, approve the report and publish an alert (`active: true`). Verify that the Android app instantly plays the alarm sound, vibrates, and displays the emergency banner on the dashboard.
* [ ] **Evacuation Locations on Map:** Add an evacuation point or polygon area on the admin website. Verify that it appears live on the Android app's Map screen.
