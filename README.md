# 🚨 Smart Drill - Android Emergency & Campus Safety App

**Smart Drill** is a robust real-time emergency drill and disaster alert application designed for campus safety (built with reference to DNSC campus coordinates). It empowers students and staff with instant disaster warnings, offline-capable safety steps, evacuation routing, and advanced anti-prank security features.

---

## ✨ Key Features

1. **Instant Access (No Login Required):**
   * Users land directly on the main dashboard upon launching the app, ensuring emergency warnings and safety instructions are accessible immediately without authentication barriers.

2. **High-Priority Emergency Push Notifications:**
   * Triggers system-level high-importance notifications accompanied by loud alarm sounds and custom vibration patterns (`longArrayOf(0, 500, 200, 500, 200, 500)`).

3. **Campus Geofencing:**
   * Prevents false and prank SOS alerts by verifying via GPS whether the user is physically inside the campus boundaries (1 km radius around the campus center). Attempts to send SOS from outside campus are automatically blocked.

4. **Unique Sender ID & Device Fingerprinting:**
   * Automatically assigns a persistent unique Sender ID (e.g., `Sender-#4892`) and captures device hardware/OS metadata (e.g., `Samsung Galaxy S23 (Android 14)`) to broadcast alongside every SOS alert. This ensures complete accountability and traceability for false alarms.

5. **Interactive Evacuation Map:**
   * Integrated with `osmdroid` to provide campus routing, hazard avoidance, and navigation to designated evacuation areas.

---

## 📁 Project Architecture & File Structure

* **`MainActivity.kt`**: Central entry point managing Compose navigation (`HOME`, `MAP`, `SOS`), permission handlers, and alert state.
* **`EmergencyNotifier.kt`**: Core utility handling notification channels, alarm audio tones, GPS campus geofencing, unique sender ID generation, and device fingerprinting.
* **`AppShell.kt`**: Persistent bottom navigation bar, screen gradients, and shared alert banners (`DisasterAlertContent`).
* **`HomeScreen.kt`**: Citizen dashboard displaying active disaster status, recommended safety steps, and quick refresh actions.
* **`MapScreen.kt`**: OpenStreetMap (`osmdroid`) canvas rendering campus nodes, evacuation pathways, and real-time location.
* **`AdminLoginScreen.kt` / `AdminHomeScreen`**: Administrator portal for broadcasting alerts and managing emergency hotlines.
* **`data/`**: Room database definitions (`AppDatabase.kt`), user entities (`Entities.kt`), DAOs (`Daos.kt`), and session state (`SessionManager.kt`).
* **`routing/`**: Graph theory algorithms (`CampusGraph.kt`, `RouteFinder.java`, `MaxFlow.java`) for optimal campus evacuation routing.

---

## 🛠️ Tech Stack

* **Language:** Kotlin & Java
* **UI Framework:** Jetpack Compose (Material 3)
* **Architecture:** Unidirectional Data Flow (UDF) with Compose State
* **Local Storage:** Room Database & SharedPreferences
* **Maps:** osmdroid (OpenStreetMap for Android)
* **Notifications & Audio:** NotificationCompat, NotificationChannel, ToneGenerator

---

## 👥 Acknowledgments & Credits

Developed with the collaborative development assistance of **Claude** and **Gemini**.
