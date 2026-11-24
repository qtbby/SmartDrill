# Smart Drill - Android Emergency & Campus Safety App - Documentation

## 📖 Table of Contents

1. [What is Smart Drill?](#what-is-smart-drill)
2. [Project Overview](#project-overview)
3. [Technology Stack](#technology-stack)
4. [App Architecture](#app-architecture)
5. [Key Components](#key-components)
6. [How Everything Works](#how-everything-works)
7. [Data Flow Diagram](#data-flow-diagram)
8. [Build & Run Instructions](#build--run-instructions)
9. [Permissions Required](#permissions-required)
10. [Future Considerations](#future-considerations)
11. [FAQ](#faq)

---

## What is Smart Drill?

**Smart Drill** is an Android emergency drill and disaster alert application designed for campus safety. It helps students and staff respond quickly to emergencies like earthquakes, floods, storms, and fires.

### Key Features Explained Simply:

1. **Instant Access (No Login Required)** - Open the app, you're immediately on the main dashboard. No username or password needed.

2. **High-Priority Emergency Push Notifications** - When an emergency happens, the app sends loud alarm sounds, strong vibrations, and red alert notifications to everyone's phones.

3. **Campus Geofencing** - If you try to send a fake SOS alert from outside campus, the app blocks it. This prevents pranks and false alarms.

4. **Unique Sender ID & Device Fingerprinting** - Every SOS alert shows WHO sent it and WHAT PHONE they used. This makes everyone accountable.

5. **Interactive Evacuation Map** - Shows you how to walk safely off campus during an emergency, avoiding any blocked roads.

---

## Project Overview

### 📁 Project Structure

```
SmartDrill/
├── app/                           # Main Android application module
│   ├── src/main/                  # Source code
│   │   ├── java/com/example/smartdrill/           # Main Kotlin/Java files
│   │   │   ├── MainActivity.kt          # App entry point, navigation
│   │   │   ├── EmergencyNotifier.kt      # Notification system, geofencing
│   │   │   ├── NearbyMeshManager.kt      # Offline mesh networking via Bluetooth/WiFi
│   │   │   │
│   │   │   ├── routing/                 # Graph theory algorithms for evacuation routes
│   │   │   │   ├── CampusGraph.kt        # Campus map data (nodes and connections)
│   │   │   │   ├── Graph.java            # Graph data structure (places and roads)
│   │   │   │   ├── RouteFinder.java      # Dijkstra's algorithm for safest path
│   │   │   │   └── RouteFinder.java      # Dijkstra's algorithm for safest path
│   │   │   │
│   │   │   └── ui/                      # User interface screens
│   │   │       ├── AppShell.kt           # Shared layout (gradient background, bottom bar)
│   │   │       ├── HomeScreen.kt         # Dashboard showing status and map preview
│   │   │       ├── MapScreen.kt          # Interactive OpenStreetMap with routes
│   │   │       ├── EmergencyActionScreen.kt  # SOS button and disaster selection
│   │   │       ├── Disaster.kt / BottomTab.kt  # Enums for disasters and tabs
│   │   │       └── theme/               # Material 3 color scheme and typography
│   │   │
│   │   └── res/                      # Resources (drawings, layouts, values)
│   │
│   ├── src/test/                   # Unit tests
│   ├── src/androidTest/                 # Instrumented tests
│   └── build.gradle.kts              # Module build configuration
│
├── build.gradle.kts                 # Top-level build configuration
├── settings.gradle.kts              # Project settings
├── gradle.properties                # Gradle settings
├── gradlew / gradlew.bat            # Gradle wrapper scripts
└── libs.versions.toml               # Dependency versions
```

---

## Technology Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **Language** | Kotlin (primary), Java | App coding |
| **UI Framework** | Jetpack Compose | Modern declarative Android UI |
| **Architecture** | Unidirectional Data Flow (UDF) | Predictable state management |
| **Maps** | osmdroid | Open-source OpenStreetMap |
| **Networking** | Google Play Services Nearby Connections | Peer-to-peer mesh networking |
| **Notifications** | NotificationCompat, NotificationChannel | High-priority alerts |
| **Audio** | ToneGenerator, RingtoneManager | Alarm sounds |
| **Storage** | SharedPreferences | Save Sender ID |
| **Testing** | JUnit 4, JUnit 5 | Unit and instrumentation tests |

---

## App Architecture

### 🏗️ Overall Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      MainActivity (Entry Point)                 │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐ │
│  │   Home Screen   │  │    Map Screen   │  │  SOS Screen      │ │
│  │  (Dashboard)    │  │  (Evacuation)   │  │  (Send Alert)    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────────┘ │
│                                                                 │
│  Navigation via Bottom Tab: HOME | MAP | SOS                     │
│                                                                 │
│  State Management: RememberSaveable (persists across config     │
│  changes like screen rotation)                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
       ┌──────────────────────┼──────────────────────┐
       ▼                      ▼                      ▼
┌───────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ EmergencyNotifier│  │ NearbyMeshManager│    │  CampusGraph     │
│ (Notifications &│    │ (Mesh Networking)│    │ (Routing)        │
│  Geofencing)   │    │                  │    │                  │
└───────────────┘    └──────────────────┘    └──────────────────┘
```

### Key Design Patterns

1. **Stateless UI** - Screens receive data through parameters and call back when user interacts
2. **Single Source of Truth** - State lives in MainActivity and flows down to screens
3. **Separation of Concerns** - Each file has a specific, well-defined responsibility

---

## Key Components

### 1. MainActivity.kt - The Heart of the App

**Location:** `app/src/main/java/com/example/smartdrill/MainActivity.kt`

**What it does (simply explained):**

- This is the main entrance point when you open the app
- It keeps track of:
  - Which screen is currently shown (HOME, MAP, or SOS)
  - Whether there's an active disaster alert
  - Who sent the last alert (Sender ID)
  - Where the alert came from (location info)
  - What type of phone was used (device info)

- It sets up:
  - The Nearby Mesh Manager (for offline SOS broadcasting)
  - Notification permission (Android 13+)

**Key functions:**
```kotlin
SmartDrillApp()        // Main composable that shows the right screen
goToTab()              // Switches between HOME, MAP, SOS screens
toast()                // Shows brief messages to user
```

---

### 2. EmergencyNotifier.kt - The Alert System

**Location:** `app/src/main/java/com/example/smartdrill/EmergencyNotifier.kt`

**What it does (simply explained):**

This is the emergency alert system. It handles:

1. **Geofencing (Campus Boundary Check)**
   - Defines the campus center: Latitude 7.3135228, Longitude 125.67033
   - Uses a 1km radius around this point
   - Only allows SOS alerts if you're inside this circle
   - Prevents prank alerts from outside campus

2. **Sender ID Generation**
   - Creates a unique ID like "Sender-#4892" for each device
   - Saves it permanently in SharedPreferences
   - Every alert shows this ID so people know who sent it

3. **Device Fingerprinting**
   - Captures phone brand, model, and Android version
   - Example: "Samsung Galaxy S23 (Android 14)"

4. **Location Info**
   - Gets the GPS coordinates from the device
   - Shows accuracy (how precise the GPS is)
   - Example: "GPS location unavailable" or "📍 7.3145, 125.6712 (GPS accuracy 15m)"

5. **Emergency Notifications**
   - Creates a special notification channel called "Emergency Alerts"
   - Uses HIGH IMPORTANCE and loud alarm sounds
   - Vibration pattern: 500ms on, 200ms off, 500ms on, 200ms off, 500ms on
   - Shows sender ID, device info, location, and safety steps

**Key functions:**
```kotlin
isInsideCampus()     // Check if GPS is within 1km of campus center
getSenderId()        // Get or create unique sender ID
getDeviceInfo()      // Get phone model info
getLocationInfo()    // Get GPS coordinates
showEmergencyAlert() // Show the push notification with all info
cancelAlert()        // Remove notification
```

---

### 3. NearbyMeshManager.kt - Offline SOS Broadcasting

**Location:** `app/src/main/java/com/example/smartdrill/NearbyMeshManager.kt`

**What it does (simply explained):**

This enables offline messaging between phones without internet! It uses Google's Nearby Connections API.

**How it works:**

1. **Advertise** - Your phone broadcasts that it's a SmartDrill user
2. **Discover** - Finds other SmartDrill users nearby
3. **Connect** - Automatically connects when phones are close
4. **Broadcast SOS** - Sends the alert message to all connected phones

**Message Format:**
```
DISASTER_TYPE|SENDER_ID|LOCATION_INFO|DEVICE_INFO
Example: "FLOOD|Sender-#4892|📍 7.3145, 125.6712 (GPS accuracy 15m)|Samsung Galaxy S23 (Android 14)"
```

**Why this matters:**
- Works WITHOUT internet (important during disasters)
- Sends alerts to everyone nearby immediately
- Uses Bluetooth and WiFi Direct (fast and battery-efficient)

---

### 4. ui/ Package - The Screens

#### AppShell.kt
**What it does:** Provides the shared layout framework
- Gradient background (orange to red = emergency colors)
- Bottom navigation bar with Home/Map/SOS tabs

#### HomeScreen.kt - Dashboard
**What it shows:**
- App name: "Smart Drill - Emergency Alert System"
- Status: "NO DISASTER HAS BEEN DETECTED" (green when calm) or disaster info (red when alert)
- A sad face ":)" when everything is safe
- Map preview
- Refresh button to update status

#### MapScreen.kt - Interactive Map
**What it shows:**
- OpenStreetMap of the campus
- Blue dot = your current location
- Colored markers:
  - Orange = Your location
  - Green = Evacuation areas
  - Red = Blocked roads (when hazard is active)
- Blue line = Recommended evacuation route
- Buttons: Find Location, Zoom In, Zoom Out

**How routing works:**
- Uses Dijkstra's algorithm to find the safest path
- Automatically snaps GPS to the road network within 120 meters
- Shows estimated walking time (speed: 1.4 m/s ≈ 5 km/h)

#### EmergencyActionScreen.kt - SOS Screen
**What it does:**
- Shows buttons for each disaster type:
  - FLOOD 🌊
  - EARTHQUAKE 🏚️
  - STORM ⛈️
  - FIRE 🔥
- Each shows recommended safety steps
- Emergency hotlines (PNP, Fire, CDRRMO, Power)
- Confirmation dialog before sending alert

---

### 5. routing/ Package - Graph Theory Algorithms

#### Graph.java
**What it is:** The campus map stored as a graph

**Think of it like:**
- **Nodes** = Places (buildings, junctions, gates, evacuation areas)
- **Edges** = Roads/pathways between places

**Example:**
```
Node 1 (Gate) --Edge--> Node 3 (Admin Building)
Node 3 --Edge--> Node 4 (Admin Building East)
Node 4 --Edge--> Node 5 (East Road)
```

**Features:**
- Roads can be blocked (hazard mode)
- Each road has a distance (meters)
- Can set hazard penalties (makes path less desirable)

#### CampusGraph.kt
**What it does:** Defines the actual campus layout

**Contains:**
- 31 nodes (places) on campus
- Coordinates converted from pixel positions to GPS
- 5 designated evacuation areas:
  1. DNSC Sports Complex
  2. Food Technology Department Assembly Area
  3. DNSC Hostel Assembly Area
  4. Central Assembly Area
  5. Science Building Assembly Area

**Coordinate conversion:**
- Uses 3 anchor points to convert map pixels to real GPS
- Academic Building center: (775, 190) → (7.314586, 125.671390)

#### RouteFinder.java
**What it does:** Finds the safest path using Dijkstra's algorithm

**How it works:**
1. Start from your current location
2. Look at all possible paths to evacuation areas
3. Calculate cost = distance + any hazard penalties
4. Pick the path with the lowest total cost
5. Return the step-by-step directions

**Example output:**
- Total distance: 245 meters
- Walking time: 2 minutes 58 seconds
- Direction: Turn Right after 50 meters
- Destination: DNSC Sports Complex

---

## How Everything Works

### 🔄 Complete User Journey

**Step 1: User Opens App**
```
[User clicks app icon]
    ↓
[MainActivity.kt starts]
    ↓
[SmartDrillApp() composable runs]
    ↓
[Bottom tab set to HOME automatically]
    ↓
[User sees dashboard: "NO DISASTER DETECTED"]
```

**Step 2: User Sends SOS (Emergency Situation)**
```
[User taps SOS tab]
    ↓
[EmergencyActionScreen.kt shows 4 disaster buttons]
    ↓
[User selects "EARTHQUAKE"]
    ↓
[Dialog: "Send EARTHQUAKE alert?"]
    ↓
[User taps "Send alert"]
    ↓
[MainActivity checks: isInsideCampus?]
    ↓
[Yes → Generate:
     - Sender ID
     - Device info  
     - Location info]
    ↓
[Broadcast to nearby phones via NearbyMeshManager]
    ↓
[Show local notification via EmergencyNotifier]
    ↓
[Dashboard shows red alert banner]
```

**Step 3: Nearby User Receives Alert**
```
[Nearby phone receives SOS message]
    ↓
[NearbyMeshManager parses: "EARTHQUAKE|Sender-#4892|..."]
    ↓
[MainActivity updates state]
    ↓
[Local notification appears]
    ↓
[Dashboard shows alert from Sender-#4892]
```

**Step 4: User Navigates to Safety**
```
[User goes to MAP tab]
    ↓
[MapScreen.kt shows GPS location]
    ↓
[RouteFinder finds safest path to nearest evacuation]
    ↓
[Map shows blue route line with directions]
    ↓
[User follows map to exit campus safely]
```

---

## Data Flow Diagram

```
                    ┌────────────────────────────────────────┐
                    │           USER INTERACTION            │
                    │  Tap SOS → Select Disaster → Confirm   │
                    └─────────────────┬──────────────────────┘
                                      │
                                      ▼
                    ┌────────────────────────────────────────┐
                    │         MainActivity.kt              │
                    │  - Tracks current screen (HOME/MAP/SOS)│
                    │  - Holds active disaster state         │
                    │  - Manages navigation tabs             │
                    └─────────────────┬────────────────────┘
                                      │
           ┌──────────────────────────┼──────────────────────────┐
           │                          │                          │
           ▼                          ▼                          ▼
┌───────────────────────┐  ┌───────────────────────┐  ┌───────────────────────┐
│  EmergencyNotifier.kt │  │ NearbyMeshManager.kt  │  │   CampusGraph.kt      │
│                       │  │                       │  │                       │
│ • isInsideCampus()    │  │ • startMeshService()  │  │ • Campus layout data  │
│ • showEmergencyAlert()│  │ • broadcastSOS()      │  │ • Node definitions    │
│ • getSenderId()       │  │ • onAlertReceived()   │  │ • Route calculations  │
│ • getDeviceInfo()     │  │                       │  │ • Evacuation areas    │
│ • getLocationInfo()   │  │ Uses Bluetooth/WiFi  │  │                       │
└───────────────────────┘  └───────────────────────┘  └───────────────────────┘
           │                          │                          │
           │                          │                          │
           ▼                          ▼                          ▼
┌───────────────────────┐  ┌───────────────────────┐  ┌───────────────────────┐
│   Android System      │  │   Nearby Connections  │  │   Osmdroid Maps       │
│                       │  │                       │  │                       │
│ • Vibration           │  │ • Peer discovery      │  │ • Map rendering       │
│ • Notification        │  │ • Data transfer       │  │ • GPS overlay         │
│ • Audio (alarm tone)  │  │ • Connection mgmt     │  │ • Route overlays      │
└───────────────────────┘  └───────────────────────┘  └───────────────────────┘
```

---

## Build & Run Instructions

### Prerequisites

1. **Android Studio Hedgehog (2023.1.1) or later**
2. **Android SDK 37** (compileSdkVersion)
3. **JDK 17**
4. **Google Play Services** (for Nearby Connections)

### Build Steps

```bash
# Navigate to project directory
cd C:\Users\USER\AndroidStudioProjects\SmartDrill

# Option 1: Using Gradle wrapper (command line)
./gradlew assembleDebug

# Option 2: Using Android Studio
# 1. Open the folder in Android Studio
# 2. Click "Sync Project with Gradle Files"
# 3. Run → Run 'app'
```

### Generate Signed APK (for release)

```bash
# 1. Create keystore (if you don't have one)
keytool -genkeypair -v -keystore smartdrill.keystore \
  -alias smartdrill -keyalg RSA -keysize 2048 -validity 10000

# 2. Add to local.properties
MYAPP_RELEASE_STORE_FILE=smartdrill.keystore
MYAPP_RELEASE_KEY_ALIAS=smartdrill
MYAPP_RELEASE_STORE_PASSWORD=***
MYAPP_RELEASE_KEY_PASSWORD=***

# 3. Build release APK
./gradlew assembleRelease
```

---

## Permissions Required

The app requires these Android permissions (declared in `AndroidManifest.xml`):

| Permission | Why Needed |
|------------|------------|
| `INTERNET` | For map tile downloads from OpenStreetMap |
| `ACCESS_NETWORK_STATE` | Check if WiFi/mobile is connected |
| `ACCESS_FINE_LOCATION` | GPS for geofencing and map positioning |
| `ACCESS_COARSE_LOCATION` | Network location as backup |
| `POST_NOTIFICATIONS` | Show emergency alerts (Android 13+) |
| `VIBRATE` | Emergency vibration pattern |
| `BLUETOOTH*` | Nearby mesh networking |
| `ACCESS_WIFI_STATE` | WiFi Direct for mesh |
| `CHANGE_WIFI_STATE` | WiFi Direct for mesh |
| `NEARBY_WIFI_DEVICES` | Scan nearby devices |

**User Prompt:** The app asks for location permission when using the map, and notification permission on Android 13+.

---

## Future Considerations

### Current Limitations

1. **Campus Coordinates** - The current coordinates (7.3135228, 125.67033) are placeholder values. For production, use actual GPS coordinates of your campus.

2. **Evacuation Areas** - Currently 5 hardcoded evacuation areas. Could be made configurable via API.

3. **Hazard Simulation** - The demo hazard at Admin Building front could be replaced with real-time hazard data from sensors.

4. **Authentication** - Currently no login system. Could add user accounts for tracking drill history.

5. **Offline Maps** - Currently downloads map tiles. Could bundle campus map for true offline use.

6. **Multi-language** - Currently English only. Could add local language support.

7. **Crowd Flow Analysis** - MaxFlow algorithm was removed. Custom evacuation capacity algorithm will be implemented.

### Current Development Status
- **Routing algorithms** are being enhanced with custom implementations
- **Algorithm development** is ongoing - the MaxFlow (AI-generated) implementation has been removed for custom development
- This is an active development project with evolving features

### Proposed Improvements

1. **Admin Dashboard** - Separate screen for safety officers to trigger drills
2. **Drill History** - Track when drills happen and attendance
3. **Push Notifications** - From server instead of SMS
4. **IoT Integration** - Connect to campus sensors (water level, seismic)
5. **QR Check-in** - At evacuation areas to confirm safe exit

---

## FAQ

### Q: Can users opt out of location permission?
**A:** Yes, but then SOS geofencing won't work. Users outside the campus won't be able to send alerts.

### Q: What happens if GPS is not available?
**A:** The app shows "GPS location unavailable" and the SOS is blocked (failsafe - better no alert than false alert).

### Q: Does the app work without internet?
**A:** Yes! Push notifications require internet initially, but the mesh networking for SOS works offline. Maps need internet to download tile images.

### Q: How is the Sender ID generated?
**A:** It's a random 4-digit number: "Sender-#XXXX" saved in SharedPreferences. Persists across app reinstalls unless data is cleared.

### Q: What's the difference between the red alert banner and push notification?
**A:** Both appear. The banner is on the phone screen. The push notification appears in the notification shade and makes sound.

### Q: Can I test the geofencing?
**A:** Temporarily change `CAMPUS_LAT` and `CAMPUS_LNG` in EmergencyNotifier.kt to your test location, then run the app.

### Q: How do I update the campus map?
**A:** Edit the `anchors` list in CampusGraph.kt to add your own reference points with real GPS coordinates.

### Q: What disasters are supported?
**A:** FLOOD, EARTHQUAKE, STORM, FIRE. Each has specific safety steps defined in `Disaster.kt`.

---

## Quick Reference Cheat Sheet

| File | Purpose |
|------|---------|
| `MainActivity.kt` | App entry point, state management |
| `EmergencyNotifier.kt` | Notifications, geofencing, IDs |
| `NearbyMeshManager.kt` | Offline mesh SOS broadcasting |
| `HomeScreen.kt` | Dashboard display |
| `MapScreen.kt` | Interactive map with routing |
| `EmergencyActionScreen.kt` | SOS button flow |
| `CampusGraph.kt` | Campus nodes, edges, routes |
| `Graph.java` | Graph data structure |
| `RouteFinder.java` | Dijkstra pathfinding |

---

*This documentation was generated for educational and development purposes.*
*Last updated: November 2026*