# Route Calculation Details Feature - Implementation Guide

## Conversation Summary
User requested a button to show detailed route calculation information including:
- Number of steps taken
- How long the route takes
- Why that specific route was chosen

## Analysis of Current Routing System

### Algorithms Used
- **RouteFinder.java**: Dijkstra's algorithm for finding safest evacuation routes
- **Graph.java**: Graph structure with nodes (locations) and edges (roads)
- **CampusGraph.kt**: Campus mapping with 31 locations and road connections

### Key Metrics Calculated
- Distance using Haversine formula
- Walking time at 1.4 m/s (~5.04 km/h)
- Hazard penalties added to edge costs

## 7 Implementation Options

### Option 1: Modal Bottom Sheet Dialog (Recommended)
Adds a FAB button that opens detailed route breakdown
- Pros: Non-intrusive, shows all info at once
- Cons: Requires FAB space

### Option 2: Expandable Route Info Section
Converts route info into expandable card
- Pros: Integrated with existing UI
- Cons: Takes vertical space

### Option 3: Route Step-by-Step List
Shows each Dijkstra algorithm step with step count and edge costs
- Pros: Educational, shows algorithm process
- Cons: More complex implementation

### Option 4: Algorithm Visualization Overlay
Visualizes the search process on the map
- Pros: Very intuitive visualization
- Cons: Complex, needs animation

### Option 5: Route Justification Panel
Compares with other evacuation options
- Pros: Shows why route was optimal
- Cons: Requires calculating multiple routes

### Option 6: Detailed Log Dialog
Scrollable dialog with all algorithmic details
- Pros: Comprehensive info
- Cons: Less visual

### Option 7: Inline Tooltip/Hover Card
Small tooltip with basic details
- Pros: Minimal UI impact
- Cons: Limited information

## Recommended Implementation (Complete Code)

### 1. Update RouteFinder.java
Add detailed result tracking:
```java
public static class DetailedResult extends Result {
    public final int searchSteps;
    public final List<String> explorationLog;
    // ... constructor and fields
}
```

### 2. Create RouteDetailsDialog.kt
Modal bottom sheet with route metrics:
- Steps count
- Distance in meters
- Estimated walking time
- Algorithm name
- Path segment list

### 3. Add Button to MapScreen.kt
Floating action button with info icon that triggers the dialog.

## Files Created/Modified
- `ROUTE_CALCULATION_FEATURE.md` (this file)
- `RouteDetailsDialog.kt` (new)
- `RouteFinder.java` (modified)
- `MapScreen.kt` (modified)

---

## Full Implementation Details

### Route Data Classes

RouteInfo currently shows:
- `distanceMeters: Double`
- `arrivalText: String` (e.g., "3 min 45 sec")
- `directionText: String` (turn instructions)
- `destinationName: String`

To enhance, we need:
- `stepCount: Int` (number of path segments)
- `searchSteps: Int` (algorithm nodes evaluated)
- `explanation: String` (why this route)

### Algorithm Explanation Text

The Dijkstra algorithm:
1. Starts at your current position (or gate ID 1)
2. Explores all reachable nodes, tracking lowest cost
3. For each node: cost = distance + hazard_penalty
4. Stops when reaching nearest evacuation point
5. Backtracks to find optimal path

### Sample Button Implementation

```kotlin
// Add info icon next to arrival time in MapScreen.kt
Row(verticalAlignment = Alignment.CenterVertically) {
    Text("You will arrive at: ${route.arrivalText}")
    IconButton(onClick = { /* show details */ }) {
        Icon(Icons.Default.Info, contentDescription = "Route details")
    }
}
```

---

## Development Steps
1. Create `RouteDetailsDialog.kt` with modal UI
2. Modify `RouteFinder.java` to track search steps
3. Add FAB button to `MapScreen.kt`
4. Connect button to dialog
5. Test on device/emulator

## Date
Generated: 2026-11-21