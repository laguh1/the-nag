# The Nag - Android App Implementation Plan

## Project Overview

Rebuild "The Nag" as a modern Android application in Kotlin. The app is a reminder and notification management system that "nags" users about tasks and events with scheduled, customizable notifications.

**Original Version:** MIT App Inventor (2016)
**New Version:** Native Android with Kotlin
**Target Audience:** Users who need persistent reminders for tasks and events

---

## Architecture & Tech Stack

### Core Technologies
- **Language:** Kotlin
- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 14 (API 34)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (SQLite wrapper)
- **Dependency Injection:** Hilt
- **UI:** Jetpack Compose + Material Design 3
- **Notifications:** WorkManager + AlarmManager
- **Async Operations:** Kotlin Coroutines + Flow
- **Testing:** JUnit, Mockito, Compose UI Testing

### Key Libraries
```gradle
// Core
androidx.core:core-ktx
androidx.lifecycle:lifecycle-viewmodel-compose
androidx.activity:activity-compose

// Compose
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.compose.ui:ui-tooling

// Room Database
androidx.room:room-runtime
androidx.room:room-ktx

// Hilt DI
com.google.dagger:hilt-android
androidx.hilt:hilt-navigation-compose

// WorkManager
androidx.work:work-runtime-ktx

// Navigation
androidx.navigation:navigation-compose

// Testing
junit:junit
androidx.test.ext:junit
androidx.compose.ui:ui-test-junit4
```

---

## Data Model

### Original Database Structure (MIT App Inventor)
From `tinydbFileStructure.ods`:
- **id** - Unique identifier
- **time range** - Duration specification
- **name** - Task/event name
- **event** - Category/type
- **count** - Trigger counter
- **complete message** - Notification messages (list)
- **dateSet** - Boolean flag
- **active** - Boolean flag
- **Date/Time fields:** year, month, week, day, hour, minute

### New Room Database Entity

```kotlin
@Entity(tableName = "nag_items")
data class NagItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Basic info
    val name: String,
    val event: String,              // Category/type
    val timeRange: Int,             // Duration in minutes

    // Tracking
    val count: Int = 0,             // Times triggered
    val isActive: Boolean = true,
    val isDateSet: Boolean = false,

    // Date/Time
    val year: Int? = null,
    val month: Int? = null,
    val weekInYear: Int? = null,
    val dayInYear: Int? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val scheduledTimestamp: Long? = null,

    // Messages
    val completeMessages: List<String>,

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## Project Structure

```
the-nag/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/thenag/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── database/
│   │   │   │   │   │   │   ├── NagDatabase.kt
│   │   │   │   │   │   │   ├── NagDao.kt
│   │   │   │   │   │   │   └── Converters.kt
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       └── NagItem.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── NagRepository.kt
│   │   │   │   │   └── worker/
│   │   │   │   │       └── NagNotificationWorker.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── Nag.kt
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── CreateNagUseCase.kt
│   │   │   │   │       ├── UpdateNagUseCase.kt
│   │   │   │   │       ├── DeleteNagUseCase.kt
│   │   │   │   │       └── ScheduleNotificationUseCase.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   │   ├── create/
│   │   │   │   │   │   │   ├── CreateNagScreen.kt
│   │   │   │   │   │   │   └── CreateNagViewModel.kt
│   │   │   │   │   │   ├── edit/
│   │   │   │   │   │   │   ├── EditNagScreen.kt
│   │   │   │   │   │   │   └── EditNagViewModel.kt
│   │   │   │   │   │   └── stats/
│   │   │   │   │   │       ├── StatsScreen.kt
│   │   │   │   │   │       └── StatsViewModel.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── NagListItem.kt
│   │   │   │   │   │   ├── CategoryChip.kt
│   │   │   │   │   │   └── DateTimePicker.kt
│   │   │   │   │   └── navigation/
│   │   │   │   │       └── NavGraph.kt
│   │   │   │   ├── notification/
│   │   │   │   │   ├── NotificationHelper.kt
│   │   │   │   │   └── NotificationChannels.kt
│   │   │   │   └── di/
│   │   │   │       ├── AppModule.kt
│   │   │   │       └── DatabaseModule.kt
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   ├── colors.xml
│   │   │       │   └── themes.xml
│   │   │       └── drawable/
│   │   └── test/
│   │       └── java/com/thenag/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── PLAN.md (this file)
└── CLAUDE.md
```

---

## Feature Breakdown

### 1. Home Screen
**Purpose:** Display all nags and manage them

**UI Components:**
- Top app bar with search icon and menu
- Filter chips (All, Active, Inactive, By Category)
- Lazy column of nag items
- FAB (Floating Action Button) to add new nag
- Empty state illustration

**Interactions:**
- Tap item → Navigate to edit screen
- Swipe right → Toggle active/inactive
- Swipe left → Delete with confirmation
- Long press → Multi-select mode

**State Management:**
```kotlin
data class HomeUiState(
    val nags: List<Nag> = emptyList(),
    val filteredNags: List<Nag> = emptyList(),
    val selectedFilter: FilterType = FilterType.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### 2. Create/Edit Nag Screen
**Purpose:** Add or modify a nag

**Form Fields:**
- Name (text input, required)
- Category/Event type (dropdown or chips)
- Date picker (Material DatePicker)
- Time picker (Material TimePicker)
- Time range (slider or number input, minutes)
- Messages list (add/remove multiple messages)
- Active toggle (switch)

**Validation:**
- Name cannot be empty
- At least one message required
- Date/time must be in the future (for new nags)

**State Management:**
```kotlin
data class CreateNagUiState(
    val name: String = "",
    val event: String = "",
    val timeRange: Int = 60,
    val messages: List<String> = listOf(""),
    val isActive: Boolean = true,
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val nameError: String? = null,
    val messagesError: String? = null,
    val isSaving: Boolean = false
)
```

### 3. Notification System
**Purpose:** Deliver timely reminders

**Implementation:**
- **WorkManager:** Schedule one-time or periodic work
- **AlarmManager:** For exact-time notifications
- **NotificationChannel:** Separate channel for user control
- **Notification Actions:** Snooze, Complete, Dismiss

**Notification Content:**
- Title: Nag name
- Text: First message from complete messages
- Expanded: All messages
- Category color indicator

**Background Processing:**
```kotlin
class NagNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Fetch nag from database
        // Create and display notification
        // Update count
        // Reschedule if recurring
        return Result.success()
    }
}
```

### 4. Categories Management
**Purpose:** Organize nags by type

**Predefined Categories:**
- Work 💼 (Blue)
- Personal 👤 (Green)
- Health 🏥 (Red)
- Finance 💰 (Yellow)
- Social 👥 (Purple)
- Custom (User-defined)

**Features:**
- Create custom categories
- Assign colors
- Filter by category
- Category statistics

### 5. Statistics Screen
**Purpose:** Show usage analytics

**Metrics:**
- Total nags created
- Active vs Inactive count
- Most frequent category (pie chart)
- Average trigger count
- Completion rate
- Recent activity timeline

**Charts:**
- Category distribution (pie chart)
- Weekly trigger frequency (bar chart)
- Active/inactive ratio (donut chart)

---

## Implementation Phases

### Phase 1: Foundation (Week 1)
**Goal:** Setup project infrastructure

**Tasks:**
- [ ] Create Android Studio project
- [ ] Configure build.gradle with dependencies
- [ ] Setup Hilt for dependency injection
- [ ] Create Room database structure
  - [ ] NagDatabase.kt
  - [ ] NagDao.kt
  - [ ] NagItem entity
  - [ ] Type converters
- [ ] Create base MVVM structure
  - [ ] Base ViewModel
  - [ ] Repository pattern
- [ ] Setup navigation graph
- [ ] Create Material 3 theme
- [ ] Setup version control (Git)

**Deliverables:**
- Compilable project with all dependencies
- Empty screens with navigation
- Database ready for CRUD operations

### Phase 2: Core Features (Week 2-3)
**Goal:** Implement main user flows

**Week 2 Tasks:**
- [ ] Home Screen
  - [ ] NagListItem composable
  - [ ] HomeViewModel with StateFlow
  - [ ] Fetch nags from database
  - [ ] Display empty state
  - [ ] Implement search functionality
- [ ] Create Nag Screen
  - [ ] Form UI with validation
  - [ ] CreateNagViewModel
  - [ ] Date/Time pickers
  - [ ] Save to database

**Week 3 Tasks:**
- [ ] Edit Nag Screen
  - [ ] Pre-populate form with existing data
  - [ ] Update functionality
- [ ] Delete Nag
  - [ ] Confirmation dialog
  - [ ] Remove from database
- [ ] Filter and sort
  - [ ] Category filter chips
  - [ ] Sort options (date, name, count)
- [ ] Category management
  - [ ] Add/edit categories
  - [ ] Category selector

**Deliverables:**
- Fully functional CRUD operations
- Search and filter working
- Categories implemented

### Phase 3: Notifications (Week 4)
**Goal:** Implement reminder system

**Tasks:**
- [ ] Create NotificationHelper
- [ ] Setup notification channels
- [ ] Implement WorkManager
  - [ ] NagNotificationWorker
  - [ ] Schedule notifications
  - [ ] Handle exact alarms (AlarmManager)
- [ ] Notification actions
  - [ ] Snooze (reschedule)
  - [ ] Complete (increment count, mark done)
  - [ ] Dismiss
- [ ] Update count on trigger
- [ ] Recurring notifications support
- [ ] Permission handling (Android 13+)

**Testing:**
- Test notifications at scheduled times
- Verify actions work correctly
- Test background restrictions
- Test battery optimization impact

**Deliverables:**
- Working notification system
- Reliable delivery with WorkManager
- User-controllable notification channels

### Phase 4: Polish & Advanced (Week 5)
**Goal:** Enhance UX and add advanced features

**Tasks:**
- [ ] Statistics Screen
  - [ ] Calculate metrics
  - [ ] Create charts (Compose Charts library)
  - [ ] Timeline view
- [ ] Dark mode support
  - [ ] Theme switcher
  - [ ] System theme detection
- [ ] Animations
  - [ ] List item animations
  - [ ] Screen transitions
  - [ ] FAB animations
- [ ] Settings Screen
  - [ ] Notification preferences
  - [ ] Default time range
  - [ ] Snooze duration
  - [ ] App theme
- [ ] Export/Import
  - [ ] Export to JSON
  - [ ] Import from JSON
  - [ ] Backup reminder data
- [ ] Widget
  - [ ] Today's nags widget
  - [ ] Quick add widget

**Deliverables:**
- Polished UI with smooth animations
- Statistics dashboard
- Settings and preferences
- Data portability

### Phase 5: Testing & Release (Week 6)
**Goal:** Ensure quality and prepare for release

**Tasks:**
- [ ] Unit Tests
  - [ ] ViewModel tests
  - [ ] Use case tests
  - [ ] Repository tests
- [ ] Integration Tests
  - [ ] Database tests
  - [ ] Worker tests
- [ ] UI Tests
  - [ ] Compose UI testing
  - [ ] Navigation tests
  - [ ] End-to-end flows
- [ ] Performance optimization
  - [ ] Database query optimization
  - [ ] LazyColumn optimization
  - [ ] Memory leak checks
- [ ] Accessibility
  - [ ] Content descriptions
  - [ ] Screen reader testing
  - [ ] Color contrast checks
- [ ] Play Store preparation
  - [ ] App icon
  - [ ] Screenshots
  - [ ] Store listing
  - [ ] Privacy policy
- [ ] Beta testing
  - [ ] Internal testing
  - [ ] Closed beta
  - [ ] Bug fixes

**Deliverables:**
- Test coverage > 80%
- Performance benchmarks met
- Play Store listing ready
- Beta release

---

## Key Improvements Over Original

### 1. Modern UI/UX
- **Material Design 3:** Latest design system
- **Jetpack Compose:** Modern declarative UI
- **Smooth Animations:** Professional feel
- **Dark Mode:** System-wide theme support
- **Responsive:** Works on all screen sizes

### 2. Reliable Notifications
- **WorkManager:** Guaranteed delivery even with battery optimization
- **AlarmManager:** Precise timing for important reminders
- **Rich Notifications:** Actions, expandable content
- **User Control:** Notification channels
- **Smart Snooze:** Intelligent rescheduling

### 3. Better Data Management
- **Room Database:** Type-safe, efficient queries
- **Migrations:** Future-proof data structure
- **Export/Import:** Data portability
- **Search:** Fast full-text search
- **Filtering:** Multiple filter options

### 4. Enhanced Functionality
- **Categories:** Better organization
- **Statistics:** Usage insights
- **Recurring:** Repeat patterns support
- **Widget:** Quick access from home screen
- **Multi-select:** Batch operations

### 5. Performance & Quality
- **Native Performance:** No interpreter overhead
- **Memory Efficient:** Optimized for Android
- **Battery Friendly:** Doze mode compatible
- **Tested:** Comprehensive test coverage
- **Accessible:** WCAG compliant

---

## Success Criteria

### Must Have (MVP)
- ✅ Create, read, update, delete nags
- ✅ Schedule notifications for specific date/time
- ✅ Display multiple messages in notifications
- ✅ Toggle nags active/inactive
- ✅ Track trigger count
- ✅ Category organization

### Should Have
- ✅ Search and filter
- ✅ Statistics dashboard
- ✅ Dark mode
- ✅ Export/import data
- ✅ Snooze functionality
- ✅ Settings screen

### Nice to Have
- Widget support
- Recurring patterns (daily, weekly, monthly)
- Voice input for nag creation
- Notification sound customization
- Priority levels
- Notification history

---

## Risk Assessment

### Technical Risks
1. **Background Execution Restrictions**
   - **Risk:** Android's battery optimization may prevent notifications
   - **Mitigation:** Use WorkManager + AlarmManager, request battery optimization exemption

2. **Notification Permissions (Android 13+)**
   - **Risk:** Users may deny notification permission
   - **Mitigation:** Clear onboarding explaining value, graceful degradation

3. **Database Migrations**
   - **Risk:** Data loss during schema changes
   - **Mitigation:** Comprehensive migration tests, backup/restore functionality

### User Experience Risks
1. **Notification Fatigue**
   - **Risk:** Too many notifications annoy users
   - **Mitigation:** Smart defaults, easy disable, snooze options

2. **Complexity**
   - **Risk:** Too many features overwhelm users
   - **Mitigation:** Progressive disclosure, simple defaults, tooltips

### Project Risks
1. **Scope Creep**
   - **Risk:** Adding too many features delays launch
   - **Mitigation:** Strict MVP definition, phased roadmap

2. **Testing Coverage**
   - **Risk:** Insufficient testing leads to bugs
   - **Mitigation:** TDD approach, automated CI/CD

---

## Future Roadmap

### Version 1.1
- Recurring patterns (daily, weekly, monthly)
- Priority levels (high, medium, low)
- Notification history

### Version 1.2
- Cloud sync (Firebase)
- Cross-device support
- Shared nags (family/team)

### Version 2.0
- AI-powered suggestions
- Smart scheduling based on habits
- Integration with calendar apps
- Wear OS support

---

## Resources & References

### Documentation
- [Android Developer Docs](https://developer.android.com)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Material Design 3](https://m3.material.io)

### Libraries
- [Hilt](https://dagger.dev/hilt/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)

### Design Resources
- [Material Icons](https://fonts.google.com/icons)
- [Material Color Tool](https://material.io/resources/color/)

---

**Plan Created:** January 12, 2026
**Project Start:** TBD
**Estimated Completion:** 6 weeks from start
**Last Updated:** January 12, 2026
