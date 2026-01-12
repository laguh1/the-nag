# The Nag - Claude Development Guide

## Project Context

This is a complete rewrite of "The Nag" Android application, originally built with MIT App Inventor in 2016. The new version is being built as a native Android application using Kotlin and modern Android development practices.

**Original Application Analysis:**
- Created by Joana Socrates Dantas
- MIT App Inventor project (visual programming)
- Database structure documented in `tinydbFileStructure.ods`
- Design mockups in `Nag Beta.ai` (PDF format)
- Purpose: Reminder/notification management system that "nags" users about tasks

## Project Overview

**Purpose:** A reminder and notification management system that helps users stay on top of tasks and events through scheduled, customizable notifications.

**Tech Stack:**
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (SQLite)
- **Dependency Injection:** Hilt
- **Notifications:** WorkManager + AlarmManager
- **Design System:** Material Design 3

**Key Features:**
- Create and manage "nags" (reminders)
- Schedule notifications with precise timing
- Multiple notification messages per reminder
- Category/event type organization
- Track how many times reminders trigger
- Toggle reminders active/inactive
- Statistics and analytics

## Development Guidelines

### Code Style & Conventions

1. **Kotlin Style**
   - Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
   - Use meaningful variable names
   - Prefer `val` over `var`
   - Use data classes for models
   - Leverage extension functions

2. **Compose Best Practices**
   - Keep composables small and focused
   - Extract reusable components
   - Use `remember` and `derivedStateOf` appropriately
   - Hoist state when necessary
   - Use preview annotations for UI development

3. **Architecture Patterns**
   - Separate concerns: UI, Domain, Data layers
   - ViewModels handle business logic
   - Repository pattern for data access
   - Use cases for complex operations
   - Unidirectional data flow (UDF)

4. **Naming Conventions**
   - Composables: PascalCase (e.g., `NagListItem`)
   - ViewModels: `<Screen>ViewModel` (e.g., `HomeViewModel`)
   - Data classes: PascalCase (e.g., `NagItem`)
   - Functions: camelCase (e.g., `scheduleNotification`)
   - Constants: UPPER_SNAKE_CASE (e.g., `MAX_MESSAGE_LENGTH`)

### Project Structure Rules

```
data/          - Data layer (database, repository, workers)
domain/        - Business logic (models, use cases)
ui/            - Presentation layer (screens, components, theme)
notification/  - Notification system
di/            - Dependency injection modules
```

**Layer Dependencies:**
- UI → Domain → Data (one-way dependency)
- Domain layer has no Android dependencies
- Data layer contains Android-specific implementations

### Database Schema

**Entity: NagItem**
```kotlin
@Entity(tableName = "nag_items")
data class NagItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,                    // Required
    val event: String,                   // Category/type
    val timeRange: Int,                  // Duration in minutes
    val count: Int = 0,                  // Trigger counter
    val isActive: Boolean = true,        // Active toggle
    val isDateSet: Boolean = false,      // Has scheduled date
    val year: Int? = null,               // Scheduled year
    val month: Int? = null,              // Scheduled month
    val weekInYear: Int? = null,         // Week number
    val dayInYear: Int? = null,          // Day of year
    val hour: Int? = null,               // Hour (0-23)
    val minute: Int? = null,             // Minute (0-59)
    val scheduledTimestamp: Long? = null, // Unix timestamp
    val completeMessages: List<String>,  // Notification messages
    val createdAt: Long,                 // Creation timestamp
    val updatedAt: Long                  // Last update timestamp
)
```

**Type Converter for List<String>:**
```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Json.decodeFromString(value)
    }
}
```

### State Management

**UI State Pattern:**
```kotlin
data class <Screen>UiState(
    val data: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: FilterType = FilterType.ALL
)
```

**ViewModel Pattern:**
```kotlin
@HiltViewModel
class <Screen>ViewModel @Inject constructor(
    private val repository: NagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(<Screen>UiState())
    val uiState: StateFlow<<Screen>UiState> = _uiState.asStateFlow()

    fun onAction(action: <Screen>Action) {
        when (action) {
            // Handle actions
        }
    }
}
```

### Notification System Design

**Requirements:**
1. Notifications must fire at exact times (use AlarmManager)
2. Must work even if app is closed or device restarts
3. Must handle battery optimization gracefully
4. Must increment count when triggered
5. Must support snooze, complete, and dismiss actions

**Implementation Strategy:**
- WorkManager for reliable background work
- AlarmManager for precise timing (setExactAndAllowWhileIdle)
- PendingIntent for notification actions
- BroadcastReceiver for action handling
- Notification channels for user control

**Notification Actions:**
1. **Snooze:** Reschedule for 10 minutes later
2. **Complete:** Increment count, mark as done (if one-time)
3. **Dismiss:** Just dismiss notification

### Testing Strategy

**Unit Tests:**
- ViewModel logic
- Use cases
- Repository functions
- Data transformations

**Integration Tests:**
- Database operations (Room)
- Worker execution (WorkManager)

**UI Tests:**
- Compose UI testing
- Navigation flows
- User interactions

**Test Naming Convention:**
```kotlin
@Test
fun `given_when_then`() {
    // Example: given_emptyNagList_when_addingNag_then_listContainsOneItem
}
```

### Common Patterns

**1. Loading Data in ViewModel:**
```kotlin
init {
    viewModelScope.launch {
        repository.getAllNags()
            .catch { e ->
                _uiState.update { it.copy(error = e.message) }
            }
            .collect { nags ->
                _uiState.update { it.copy(data = nags, isLoading = false) }
            }
    }
}
```

**2. Handling User Actions:**
```kotlin
fun onNagClick(nagId: Int) {
    viewModelScope.launch {
        // Navigate or perform action
    }
}
```

**3. Form Validation:**
```kotlin
private fun validateForm(): Boolean {
    var isValid = true

    if (name.isBlank()) {
        _uiState.update { it.copy(nameError = "Name is required") }
        isValid = false
    }

    if (messages.all { it.isBlank() }) {
        _uiState.update { it.copy(messagesError = "At least one message required") }
        isValid = false
    }

    return isValid
}
```

**4. Scheduling Notifications:**
```kotlin
suspend fun scheduleNagNotification(nag: NagItem) {
    val workRequest = OneTimeWorkRequestBuilder<NagNotificationWorker>()
        .setInputData(workDataOf("NAG_ID" to nag.id))
        .setInitialDelay(calculateDelay(nag.scheduledTimestamp), TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}
```

## Implementation Checklist

### Phase 1: Foundation ✓
- [ ] Create Android project with Kotlin & Compose
- [ ] Add all dependencies in build.gradle
- [ ] Setup Hilt
- [ ] Create Room database
  - [ ] NagItem entity
  - [ ] NagDao interface
  - [ ] NagDatabase class
  - [ ] Type converters
- [ ] Create Repository
- [ ] Setup Navigation
- [ ] Create Material 3 theme
- [ ] Initialize Git repository

### Phase 2: Core Features
- [ ] Home Screen
  - [ ] Layout with LazyColumn
  - [ ] HomeViewModel
  - [ ] NagListItem composable
  - [ ] Empty state
  - [ ] Search bar
  - [ ] Filter chips
  - [ ] FAB for add
- [ ] Create Nag Screen
  - [ ] Form fields
  - [ ] Date/Time pickers
  - [ ] Message list management
  - [ ] Validation
  - [ ] Save to database
- [ ] Edit Nag Screen
  - [ ] Load existing nag
  - [ ] Pre-populate form
  - [ ] Update database
- [ ] Delete Nag
  - [ ] Confirmation dialog
  - [ ] Remove from database
  - [ ] Cancel scheduled notification
- [ ] Categories
  - [ ] Predefined categories
  - [ ] Custom category creation
  - [ ] Category filter

### Phase 3: Notifications
- [ ] Create NotificationHelper
- [ ] Setup notification channels
- [ ] Implement NagNotificationWorker
- [ ] Schedule with WorkManager
- [ ] Implement AlarmManager for exact times
- [ ] Create notification layout
- [ ] Add notification actions (Snooze, Complete, Dismiss)
- [ ] Handle action broadcasts
- [ ] Update count on trigger
- [ ] Handle device reboot (BOOT_COMPLETED)
- [ ] Request notification permission (Android 13+)

### Phase 4: Polish
- [ ] Statistics screen
  - [ ] Calculate metrics
  - [ ] Create charts
  - [ ] Timeline view
- [ ] Dark mode
  - [ ] Theme switcher
  - [ ] System theme detection
- [ ] Animations
  - [ ] Enter/exit animations
  - [ ] Shared element transitions
- [ ] Settings screen
- [ ] Export/Import functionality
- [ ] Widget (optional)

### Phase 5: Testing & Release
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Write UI tests
- [ ] Performance optimization
- [ ] Accessibility audit
- [ ] Create app icon
- [ ] Prepare Play Store assets
- [ ] Write privacy policy
- [ ] Beta testing

## Important Considerations

### 1. Background Execution
- **Challenge:** Android restricts background execution
- **Solution:** Use WorkManager (Doze-compatible) + AlarmManager (exact timing)
- **Permission:** REQUEST_IGNORE_BATTERY_OPTIMIZATIONS (optional, user choice)

### 2. Notification Permissions (Android 13+)
- Request POST_NOTIFICATIONS permission at runtime
- Show rationale if denied
- Provide alternative (in-app reminders)

### 3. Date/Time Handling
- Use java.time.* APIs (API 26+)
- Store as Unix timestamps for consistency
- Handle timezone changes
- Respect user's date/time format preferences

### 4. Data Migration
- Plan for future schema changes
- Implement Room migrations
- Test migrations thoroughly
- Provide data export before major updates

### 5. Performance
- Use LazyColumn for lists (not Column)
- Implement pagination if needed
- Optimize database queries (indices)
- Profile memory usage
- Test on low-end devices

### 6. Accessibility
- Provide content descriptions
- Ensure touch targets are 48dp minimum
- Test with TalkBack
- Check color contrast (WCAG AA)
- Support dynamic type sizing

## Debugging Tips

### Common Issues

**1. Notifications Not Appearing**
- Check notification channel creation
- Verify notification permission granted
- Test without battery optimization
- Check WorkManager constraints
- Use `adb shell dumpsys notification` to debug

**2. Database Issues**
- Clear app data and reinstall for schema changes
- Use Database Inspector in Android Studio
- Check Room migration logs
- Verify type converters are registered

**3. Compose Preview Not Working**
- Ensure @Preview annotation present
- Provide preview data
- Check for missing dependencies in preview
- Refresh preview manually

**4. Navigation Issues**
- Verify route strings match
- Check NavHost setup
- Ensure ViewModel scope is correct
- Use navigation testing utilities

### Useful Commands

```bash
# Install debug build
./gradlew installDebug

# Run tests
./gradlew test

# Check code style
./gradlew ktlintCheck

# Generate APK
./gradlew assembleRelease

# View notification channels
adb shell dumpsys notification

# Clear app data
adb shell pm clear com.thenag

# View logs
adb logcat | grep TheNag
```

## Resources

### Documentation
- [Android Developer Docs](https://developer.android.com)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Hilt](https://dagger.dev/hilt/)

### Design Resources
- [Material Design 3](https://m3.material.io)
- [Material Icons](https://fonts.google.com/icons)
- [Color Tool](https://material.io/resources/color/)

### Example Projects
- [Now in Android](https://github.com/android/nowinandroid) - Modern Android app architecture
- [Compose Samples](https://github.com/android/compose-samples) - Jetpack Compose examples

## Questions to Ask User

When implementing features, clarify:

1. **Notification Behavior:**
   - Should nags repeat (daily, weekly, monthly)?
   - What's the default snooze duration?
   - Maximum number of messages per nag?

2. **Categories:**
   - Pre-defined categories sufficient?
   - Allow custom category colors?
   - Category icons needed?

3. **UI Preferences:**
   - Default theme (light/dark/system)?
   - List view vs grid view for nags?
   - Sort preference (date, name, count)?

4. **Data Management:**
   - Cloud sync needed?
   - Export format preference (JSON, CSV)?
   - Backup frequency?

## Next Steps

1. **Setup Development Environment:**
   - Install Android Studio (latest stable)
   - Install Android SDK (API 26+)
   - Setup emulator or physical device

2. **Initialize Project:**
   - Create new Android project
   - Configure build.gradle
   - Add dependencies
   - Setup version control

3. **Start with Foundation:**
   - Begin with Phase 1 tasks
   - Create database schema
   - Setup basic navigation
   - Create theme

4. **Iterative Development:**
   - Build feature by feature
   - Test thoroughly
   - Get user feedback
   - Iterate

---

**Guide Created:** January 12, 2026
**Last Updated:** January 12, 2026
**Status:** Ready to start development

## Notes for Claude

- Always reference PLAN.md for implementation roadmap
- Follow Android best practices and Material Design guidelines
- Ask clarifying questions before implementing ambiguous features
- Test thoroughly on different Android versions
- Keep user privacy and data security in mind
- Write clean, maintainable code with proper documentation
- Suggest improvements based on modern Android practices

When starting work:
1. Read this file and PLAN.md completely
2. Ask about any unclear requirements
3. Suggest implementation approach
4. Get approval before major architectural decisions
5. Implement, test, and document

Happy coding! 🚀
