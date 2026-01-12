# The Nag - Android Reminder App

A modern Android application built with Kotlin and Jetpack Compose that helps users manage reminders and notifications.

## Project Status

**Phase 1: Foundation** ✅ COMPLETED
**Phase 2: Core Features** ✅ COMPLETED

All CRUD operations are now functional!

## What's Been Implemented

### ✅ Phase 1: Foundation (COMPLETED)

- **Project Structure**: Complete Android project with proper folder organization
- **Build Configuration**: Gradle setup with all necessary dependencies
- **Dependency Injection**: Hilt fully configured for DI
- **Database**: Room database with:
  - `NagItem` entity matching original MIT App Inventor structure
  - Comprehensive DAO with queries for all operations
  - Type converters for List<String>
  - Repository pattern implementation
- **Architecture**: MVVM architecture set up with:
  - ViewModel with StateFlow
  - Unidirectional data flow
  - Proper separation of concerns
- **UI Theme**: Material Design 3 theme with:
  - Light and dark color schemes
  - Dynamic colors support (Android 12+)
  - Typography scale
  - Proper theme composition
- **Navigation**: Navigation graph with sealed class routes
- **Home Screen**: Basic functional home screen with:
  - List of nags
  - Loading/error/empty states
  - Filter and search support (in ViewModel)
  - Toggle active status
  - Navigation to create/edit screens
  - Search functionality
  - Filter chips (All, Active, Inactive)

### ✅ Phase 2: Core Features (COMPLETED)

- **Create Nag Screen**: Complete form for creating new nags with:
  - Name input with validation
  - Category dropdown (Work, Personal, Health, Finance, Social)
  - Material 3 Date Picker
  - Material 3 Time Picker
  - Time range slider (5-180 minutes)
  - Multiple notification messages
  - Active/inactive toggle
  - Form validation
- **Edit Nag Screen**: Full edit functionality with:
  - Pre-populated form from existing nag
  - All create features
  - Delete button in top bar
  - Delete confirmation dialog
  - Update database on save
- **Enhanced Home Screen**:
  - Search bar (toggle with search icon)
  - Filter chips (All, Active, Inactive)
  - Improved layout and organization
- **UI Components**:
  - DatePickerDialog (Material 3)
  - TimePickerDialog (Material 3)
  - Reusable category dropdown
  - Delete confirmation dialog

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Asynchronous**: Coroutines + Flow
- **Background Work**: WorkManager (to be implemented)
- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 34 (Android 14)

## Project Structure

```
app/src/main/java/com/thenag/
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── NagDatabase.kt       ✅
│   │   │   ├── NagDao.kt            ✅
│   │   │   └── Converters.kt        ✅
│   │   └── entity/
│   │       └── NagItem.kt           ✅
│   ├── repository/
│   │   └── NagRepository.kt         ✅
│   └── worker/
│       └── NagNotificationWorker.kt ⏳
├── domain/
│   ├── model/
│   │   └── Nag.kt                   ⏳
│   └── usecase/
│       ├── CreateNagUseCase.kt      ⏳
│       └── ...
├── ui/
│   ├── theme/
│   │   ├── Color.kt                 ✅
│   │   ├── Theme.kt                 ✅
│   │   └── Type.kt                  ✅
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt        ✅
│   │   │   └── HomeViewModel.kt     ✅
│   │   ├── create/
│   │   │   ├── CreateNagScreen.kt   ✅
│   │   │   └── CreateNagViewModel.kt ✅
│   │   ├── edit/
│   │   │   ├── EditNagScreen.kt     ✅
│   │   │   └── EditNagViewModel.kt  ✅
│   │   └── stats/                   ⏳
│   ├── components/
│   │   ├── DatePickerDialog.kt     ✅
│   │   └── TimePickerDialog.kt     ✅
│   └── navigation/
│       └── NavGraph.kt              ✅
├── notification/                    ⏳
├── di/
│   ├── AppModule.kt                 ✅
│   └── DatabaseModule.kt            ✅
├── MainActivity.kt                  ✅
└── NagApplication.kt                ✅
```

✅ = Implemented
⏳ = To be implemented

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 34

### Building the Project

1. **Clone or navigate to the project**:
   ```bash
   cd "/Users/joanasocrates/Local Documents/claude/the-nag"
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - File → Open
   - Select the `the-nag` folder

3. **Sync Gradle**:
   - Android Studio will automatically sync
   - Or: File → Sync Project with Gradle Files

4. **Run the app**:
   - Connect an Android device or start an emulator
   - Click the Run button (▶️) or press Shift+F10

### Building from Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

## Key Features (Planned)

### MVP Features
- ✅ View list of nags
- ✅ Create new nags with:
  - Name
  - Category
  - Date & time
  - Multiple notification messages
  - Time range (duration)
- ✅ Edit existing nags
- ✅ Delete nags
- ✅ Toggle nags active/inactive
- ✅ Search nags
- ✅ Filter by status (All, Active, Inactive)
- ⏳ Schedule notifications (WorkManager)
- ⏳ Track trigger count (automatic)

### Future Enhancements
- Statistics dashboard
- Search and filtering
- Categories management
- Dark mode
- Export/import data
- Widget support
- Recurring notifications

## Learning Resources

This project includes comprehensive documentation for learning:

- **LEARNING_GUIDE.md**: Complete guide to Android development with Kotlin, tailored for Java backend developers
- **PLAN.md**: Detailed implementation plan with all phases
- **CLAUDE.md**: Development guide with patterns and best practices

## Original Version

This is a complete rewrite of "The Nag" application originally built with MIT App Inventor in 2016. The original version used:
- TinyDB for local storage
- Visual block programming
- Basic notification system

The new version leverages modern Android development practices and provides:
- Native performance
- Modern UI with Material Design 3
- Reliable notifications with WorkManager
- Better data management with Room
- Scalable architecture

## Database Schema

The app uses a single table that mirrors the original TinyDB structure:

```kotlin
@Entity(tableName = "nag_items")
data class NagItem(
    val id: Int,
    val name: String,              // Nag name
    val event: String,             // Category
    val timeRange: Int,            // Duration in minutes
    val count: Int,                // Trigger count
    val isActive: Boolean,         // Active status
    val isDateSet: Boolean,        // Has scheduled date
    val year/month/day/hour/minute,// Date/time components
    val scheduledTimestamp: Long,  // Computed timestamp
    val completeMessages: List<String>, // Notifications
    val createdAt: Long,
    val updatedAt: Long
)
```

## Architecture

The app follows Clean Architecture principles with MVVM pattern:

```
UI Layer (Compose) → ViewModel (StateFlow) → Repository → Data Source (Room)
```

**Key Concepts**:
- **Unidirectional Data Flow**: State flows down, events flow up
- **Single Source of Truth**: Repository manages data
- **Separation of Concerns**: Each layer has specific responsibility
- **Dependency Injection**: Hilt provides dependencies

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests HomeViewModelTest
```

## Next Steps

### Phase 3: Notifications (In Progress)

See `PLAN.md` for the complete roadmap. Next priorities:

1. Notification system with WorkManager
2. Schedule notifications for nags
3. Handle notification actions (Snooze, Complete, Dismiss)
4. Update trigger count
5. Handle device reboot (reschedule notifications)

## Contributing

This is a personal learning project, but suggestions and feedback are welcome!

## License

Private project - All rights reserved.

## Author

Joana Socrates Dantas
Original MIT App Inventor version: 2016
Kotlin rewrite: 2026

---

**Project Documentation**:
- [Implementation Plan](PLAN.md)
- [Learning Guide](LEARNING_GUIDE.md)
- [Development Guide](CLAUDE.md)
