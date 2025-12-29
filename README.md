# Race Timing Application

An offline-first application for timing crews racing in rowing regattas, built for both web (GWT) and Android platforms with shared business logic.

## Overview

This application facilitates the accurate timing of crews racing in rowing regattas. It enables rowing regatta timers to:

1. Browse and update the rowing regatta's Start List
2. Receive notifications when changes are made to the Start List
3. Efficiently capture both timestamps and crew identifiers (bow or lane numbers) as crews cross race course timing milestones
4. Synchronize race results with the race results repository

## Project Structure

```
RaceTimingApp/
├── shared/           # Shared Java code (models, business logic)
│   ├── src/main/java/com/racingtimer/
│   │   ├── model/    # Data models (Crew, Race, TimingEntry)
│   │   ├── service/  # Business logic (TimingService, SyncService)
│   │   └── storage/  # Storage interfaces
│   └── pom.xml
├── web/              # GWT web application
│   ├── src/main/
│   │   ├── java/com/racingtimer/client/
│   │   │   ├── storage/  # LocalStorage implementation
│   │   │   └── ui/       # GWT UI components
│   │   └── webapp/       # HTML, CSS
│   └── pom.xml
├── android/          # Android application
│   └── app/
│       ├── src/main/java/com/racingtimer/android/
│       │   ├── storage/  # SQLite implementation
│       │   └── adapter/  # RecyclerView adapters
│       └── build.gradle
└── pom.xml           # Parent Maven POM
```

## Technology Stack

### Shared Module
- **Language**: Java 11
- **Build Tool**: Maven

### Web Application
- **Framework**: GWT 2.10.0
- **Storage**: LocalStorage API
- **Build Tool**: Maven
- **Deployment**: WAR file for servlet containers

### Android Application
- **Platform**: Android 7.0+ (API 24)
- **Storage**: SQLite database
- **Build Tool**: Gradle
- **UI**: Android Views with RecyclerView

## Features

### Race Finish Screen (Keypad Timing View)

The keypad-based timing interface includes:

1. **Title Bar**: Navigation to Home and Results screens
2. **Timestamp Capture Button**: Large button displaying running clock for quick timestamp recording
3. **Timing Table**: Displays sequence number, bib number, crew name, category, and race clock time
4. **Input Display**: Shows the bib number being entered
5. **Numeric Keypad**: Telephone-style keypad (0-9, backspace, "No Bib", "Enter")
6. **Bottom Toolbar**: Quick access to various timing functions

**Workflow**: Timer taps the timestamp button, then enters the bib number using the keypad to associate the timestamp with a crew.

### Grid Timing View

An alternative timing interface optimized for fast one-tap timing:

1. **Menu Bar**: Navigation to Home and Results screens
2. **Title Panel**: Displays race name and running clock
3. **Crew Button Grid**: Grid of buttons (8 columns), one button per crew
4. **Status Display**: Shows finish count and status messages
5. **Bottom Toolbar**: Quick access to various timing functions

**Workflow**:
- Each crew has a dedicated green button showing their bib number
- **Tap once**: Captures timestamp and bib number, button turns yellow (draft state)
- **Tap yellow button again**: Cancels the draft, button returns to green
- **After 5 seconds**: Draft automatically finalizes, button disappears

This view is ideal for races where crews finish in close succession, allowing one-tap timing without manual bib entry.

### Fast Tap Timing View

A hybrid interface that separates timestamp capture from bib assignment:

1. **Title Bar**: Navigation to Home and Results screens
2. **Timestamp Capture Button**: Large button with running clock for recording timestamps
3. **Bib Button Grid**: Scrollable grid of buttons (8 columns) showing all crew bib numbers
4. **Results Table**: Shows captured timing entries with sequence, bib, name, category, and race clock
5. **Status Display**: Shows assignment status and finish count
6. **Bottom Toolbar**: Quick access to various timing functions

**Workflow**:
- **Single finish**: Tap timestamp button → tap corresponding bib button (2 taps)
- **Group finishes**: Tap timestamp button multiple times → tap bib buttons in finish order
- Example: Three crews finish together → tap time, tap time, tap time → tap bib 201, tap bib 203, tap bib 204

**Key Features**:
- Unassigned timestamps shown in table with "Select a bib # above" placeholder
- Bib buttons remain green and available for multiple uses
- Status line shows last assigned crew with position and split time
- Ideal for mass start races where crews finish in groups

This view combines the speed of dedicated bib buttons with the flexibility to capture multiple timestamps before assignment.

### Offline-First Architecture

Both applications work offline and sync when connectivity is available:

- **Web**: Uses LocalStorage to persist timing data locally
- **Android**: Uses SQLite database for local persistence
- **Sync**: Queues unsynced entries and synchronizes with Race Results Repository when online

## Building the Applications

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven 3.6+
- For Android: Android SDK with API level 24+

### Build Web Application

```bash
# From project root
mvn clean install

# Run GWT development mode
cd web
mvn gwt:devmode

# Build for production
mvn clean package
```

The web application WAR file will be in `web/target/web-1.0.0-SNAPSHOT.war`

### Build Android Application

```bash
cd android
./gradlew build

# Install on connected device
./gradlew installDebug
```

The Android APK will be in `android/app/build/outputs/apk/debug/`

## Development

### Shared Code

The `shared/` module contains all business logic and data models. Any changes to core functionality should be made here to benefit both platforms.

Key classes:
- `TimingEntry`: Represents a recorded timestamp with crew information
- `Race`: Represents a rowing race
- `Crew`: Represents a crew competing in a race
- `TimingService`: Core business logic for timing operations
- `SyncService`: Handles synchronization with backend

### Storage Implementations

Both platforms implement the `TimingStorage` interface:

- **Web**: `LocalTimingStorage` uses browser LocalStorage
- **Android**: `SQLiteTimingStorage` uses SQLite database

### Adding New Features

1. Add data models to `shared/src/main/java/com/racingtimer/model/`
2. Add business logic to `shared/src/main/java/com/racingtimer/service/`
3. Implement UI in both `web/` and `android/` modules
4. Update storage implementations if needed

## Integration with Race Results Repository

The application integrates with an external Race Results Repository (GitHub: sdybiec/Race-Results-Repository) for:

- Downloading start lists
- Uploading timing results
- Receiving real-time updates

Integration points are in `SyncService.java`. Configure the API endpoint using:

```java
syncService.setApiEndpoint("https://your-api-endpoint.com");
```

## UI Design

The UI follows the provided mockup with:

- **Green timestamp capture button** for quick access
- **Yellow highlighting** for bib numbers and race clock times
- **Table view** for timing entries with scrolling
- **Telephone-style keypad** for efficient bib number entry
- **Dark bottom toolbar** for additional functions

## Future Enhancements

- Complete integration with Race Results Repository API
- Support for multiple timing milestones (start, intermediate, finish)
- GPS-based automatic timestamp capture
- Multi-device synchronization
- Offline race management and crew database
- Export timing data to various formats (CSV, PDF)

## License

[To be determined]

## Contributors

[To be added]
