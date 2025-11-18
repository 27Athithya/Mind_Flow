<div align="center">
  <h1>🧘 MindFlow</h1>
  <p><strong>Your Personal Wellness Companion</strong></p>
  
  <p>
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
    <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white" alt="Material Design" />
  </p>
</div>

---

## 📖 About MindFlow

**MindFlow** is a comprehensive wellness and mental health tracking Android application designed to help users maintain a healthy lifestyle through habit tracking, mood journaling, hydration monitoring, and step counting. Built with modern Android development practices, MindFlow provides an intuitive and beautiful interface to support your daily wellness journey.

### ✨ Key Features

- 🏠 **Dashboard Overview** - Centralized view of all your wellness metrics
- 🎯 **Habit Tracking** - Create, track, and maintain daily habits with customizable emojis
- 😊 **Mood Journal** - Log your daily moods with notes and visualize trends over time
- 💧 **Hydration Tracker** - Monitor water intake with smart reminders
- 👟 **Step Counter** - Track your daily steps using device sensors
- 🔔 **Smart Notifications** - Customizable reminders for hydration and habits
- 🌓 **Dark Mode Support** - Easy on the eyes, day or night
- 📊 **Visual Analytics** - Beautiful charts to track your progress
- 👤 **User Profiles** - Personalized experience with profile management

---

## 🎯 Project Information

- **Student ID:** IT23581852
- **Developer:** Athithya
- **Course:** Mobile Application Development (MAD)
- **Application ID:** com.wellness.hub
- **Version:** 1.0

---

## 🏗️ Architecture & Technical Stack

### Architecture Pattern
- **Single-Activity Architecture** with Navigation Component
- **MVVM-inspired** structure with ViewBinding
- **Fragment-based** navigation for modular UI components

### Technologies & Libraries

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 1.9.25 |
| Build System | Gradle (KTS) | 8.5.2 |
| Min SDK | API 24 (Android 7.0) | - |
| Target SDK | API 34 (Android 14) | - |

#### Core Android Components
- 📱 **AndroidX Core KTX** - Kotlin extensions for Android
- 🎨 **Material Design 3** - Modern UI components
- 🧭 **Navigation Component** - Single-Activity navigation
- 🔄 **ViewBinding** - Type-safe view access
- 🎭 **Fragments & Activities** - UI components

#### Data Management
- 💾 **Room Database** (v2.6.1) - Local data persistence
- 🗂️ **SharedPreferences** - Lightweight data storage
- 📦 **Gson** (v2.10.1) - JSON serialization

#### Background Processing
- ⏰ **WorkManager** (v2.9.1) - Background task scheduling
- 🔔 **Notification API** - Smart reminders

#### UI & Visualization
- 📊 **MPAndroidChart** (v3.1.0) - Interactive charts for mood trends
- ⭕ **CircularProgressBar** (v3.1.0) - Beautiful progress indicators
- ♻️ **RecyclerView** - Efficient list displays
- 🎴 **CardView** - Material card layouts

#### Sensors & Hardware
- 🚶 **SensorManager** - Step counter integration
- ⏱️ **AlarmManager** - Scheduled reminders

---

## 📱 Application Structure

### Activities
```
📂 activities/
├── 🎨 SplashActivity - App entry point with branding
├── 🔐 AuthActivity - Authentication flow container
└── 🏠 MainActivity - Main app container with bottom navigation
```

### Fragments
```
📂 fragments/
├── 🏠 HomeDashboardFragment - Central dashboard with all metrics
├── 🎯 HabitsFragment - Habit tracking and management
├── 😊 MoodJournalFragment - Mood logging and history
├── 👟 StepCounterFragment - Step tracking interface
├── 💧 WaterDetailsFragment - Detailed hydration tracking
├── 👤 ProfileFragment - User settings and preferences
├── 🔐 LoginFragment - User authentication
└── 📝 RegisterFragment - New user registration
```

### Models
```
📂 models/
├── 👤 User - User account data
├── 🎯 Habit - Habit tracking data with streaks
├── 😊 MoodEntry - Mood journal entries
└── 👟 Step - Step counter data
```

### Utilities
```
📂 utils/
├── 💾 SharedPrefsManager - Centralized data management
├── 🔔 NotificationHelper - Notification creation and management
├── 💧 HydrationScheduler - Water reminder scheduling
├── 📅 DateTimeUtils - Date/time formatting helpers
├── 🎨 EmojiPickerDialog - Custom emoji selection
├── ⚡ PerformanceManager - App performance monitoring
└── 📡 HydrationActionReceiver - Broadcast receiver for reminders
```

### Adapters
```
📂 adapters/
├── 🎯 HabitAdapter - Habit list display
├── 📊 HabitDashboardAdapter - Dashboard habit cards
└── 😊 MoodAdapter - Mood entry list
```

---

## 🚀 Getting Started

### Prerequisites

- 📱 Android Studio (Arctic Fox or later)
- ☕ JDK 11 or higher
- 🤖 Android SDK with API Level 34
- 📦 Gradle 8.5.2+

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/27Athithya/Mind_Flow.git
   cd Mind_Flow
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on emulator or device**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio
   - Or use command: `./gradlew installDebug`

### Configuration

The app requires the following permissions (automatically handled):
- 🔔 `POST_NOTIFICATIONS` - For habit and hydration reminders
- 🔋 `WAKE_LOCK` - For background notifications
- 📱 `RECEIVE_BOOT_COMPLETED` - To reschedule reminders after reboot
- 🚶 `ACTIVITY_RECOGNITION` - For step counting
- ⏰ `SCHEDULE_EXACT_ALARM` - For precise reminder timing

---

## 💡 Key Features Explained

### 🎯 Habit Tracking
- Create custom habits with emojis and icons
- Track daily completion with streak counting
- View weekly progress at a glance
- Set custom reminder times
- Calculate completion percentages

### 😊 Mood Journaling
- Log moods on a 1-5 scale with emoji representation
- Add notes and tags to entries
- Visualize mood trends with interactive charts
- Track emotional patterns over time
- Color-coded mood levels for easy identification

### 💧 Hydration Monitoring
- Set daily water intake goals
- Log water consumption throughout the day
- Smart reminder system with customizable intervals
- Visual progress tracking with circular indicators
- Detailed hydration history

### 👟 Step Counter
- Real-time step tracking using device sensors
- Daily step goals and progress
- Integration with dashboard overview
- Automatic data persistence

### 🎨 User Experience
- Beautiful Material Design 3 UI
- Smooth animations and transitions
- Dark mode support
- Intuitive navigation with bottom bar
- Responsive layouts for all screen sizes

---

## 📊 Data Storage

### SharedPreferences Implementation
The app uses a custom `SharedPrefsManager` for all data operations:
- **User Data**: Login credentials and profile information
- **Habits**: JSON-serialized habit objects with Gson
- **Moods**: Mood entries with timestamps and notes
- **Hydration**: Water intake logs and goals
- **Steps**: Daily step counts and history
- **Settings**: Theme preferences and notification settings

### Room Database
Room is integrated for more complex data operations and future scalability:
- Type-safe database access
- Compile-time verification of SQL queries
- Built-in LiveData support for reactive UI updates

---

## 🔔 Background Services

### WorkManager Integration
- **HydrationReminderWorker**: Periodic water intake reminders
- Survives app restarts and device reboots
- Battery-efficient scheduling
- Customizable reminder intervals (30, 60, 120 minutes)

### Notification System
- Rich notifications with action buttons
- "Drink Water" action for quick logging
- Persistent notification channels
- Priority-based notification management

---

## 🎨 UI Components & Design

### Material Design 3
- Modern card layouts with elevation
- Floating Action Buttons (FABs)
- Bottom Navigation Bar
- Custom dialogs and bottom sheets
- Smooth transitions and animations

### Custom Components
- **Emoji Picker Dialog**: Beautiful emoji selection interface
- **Circular Progress Bars**: Visual progress indicators
- **Interactive Charts**: MPAndroidChart integration for mood trends
- **Custom CardViews**: Styled habit and mood cards

---

## 🧪 Testing

### Unit Tests
Located in `app/src/test/`:
- Model validation tests
- Utility function tests
- Data serialization tests

### Instrumented Tests
Located in `app/src/androidTest/`:
- UI component tests
- Fragment navigation tests
- Sensor integration tests

**Run tests:**
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

---

## 📦 Build & Release

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

The APK will be generated in:
`app/build/outputs/apk/debug/` or `app/build/outputs/apk/release/`

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Maintain consistent indentation

---

## 📝 License

This project is developed as part of academic coursework for Mobile Application Development (MAD).

**Student Project** - IT23581852_Athithya_MAD

---

## 👨‍💻 Developer

**Athithya**
- Student ID: IT23581852
- GitHub: [@27Athithya](https://github.com/27Athithya)
- Repository: [Mind_Flow](https://github.com/27Athithya/Mind_Flow)

---

## 🙏 Acknowledgments

- **Material Design** for beautiful UI components
- **MPAndroidChart** for stunning data visualizations
- **Android Jetpack** for modern Android development tools
- Course instructors and peers for guidance and feedback

---

## 📞 Support

For questions or support, please:
- 📧 Open an issue on GitHub
- 📝 Check the documentation in the code
- 💬 Review the inline comments for implementation details

---

<div align="center">
  <p><strong>Built with ❤️ for wellness and mindfulness</strong></p>
  <p>MindFlow - Your journey to better health starts here</p>
</div>
