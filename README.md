# FitStreak - Android Fitness Tracking Application
**Android Developer Nanodegree Capstone Project**

A comprehensive Android fitness tracking application built with modern Android development practices including MVVM architecture, Dagger dependency injection, and Material Design 3.

### **Android UI/UX Requirements**

#### **Navigable Interface with Multiple Screens**
- **3+ Distinct Screens**: Authentication, Dashboard, Recipes, Daily Progress, Goal/Activity Editing
- **Navigation Controller**: Fragment-based navigation using Android Navigation Component
  - `nav_graph.xml` with conditional navigation based on authentication state
  - Type-safe navigation with Safe Args plugin
- **Explicit Intents**: Activity-based navigation for external recipe URLs
- **Bundle Data Transfer**: Arguments passed between fragments using Navigation Component bundles

#### **Android Standards & Responsive Design**
- **ConstraintLayout Implementation**: All layouts use ConstraintLayout for flat UI hierarchy
  - No nested LinearLayouts or RelativeLayouts
  - Proper constraint relationships with IDs and vertical constraints
  - Examples: `fragment_dashboard.xml`, `fragment_recipes.xml`
- **Multiple Screen Sizes**: Landscape-specific layouts in `layout-land/` directory
  - Portrait/landscape optimization for phones and tablets
  - Responsive design with proper constraint relationships
- **Resource Organization**: Proper res directory structure
  - `values/colors.xml` - Material Design 3 color system
  - `values/dimens.xml` - Standardized dimensions and spacing
  - `values/strings.xml` - Internationalization ready
  - `drawable/` - Vector drawables and gradients

#### **Data Collections & ViewHolder Pattern**
- **RecyclerView Implementation**: Multiple RecyclerViews with ViewHolder pattern
  - `RecipeTypeListAdapter` - Categories with nested horizontal RecyclerViews
  - `RecipeChildAdapter` - Individual recipe items
  - Efficient view recycling and data binding

#### **UI Animations**
- **MotionLayout Implementation**: `recycler_item_layout.xml` with `recycler_item_layout_scene.xml`
  - **Complex Transitions**: Multi-keyframe animations with icon rotation and progress bar scaling
  - **ConstraintSet Definitions**: Start, end, and completed states with proper constraints
- **Shimmer Effects**: Loading animations using Facebook Shimmer library

### **Local and Network Data Requirements**

#### **Remote Data Source Integration**
- **RESTful API**: Spoonacular API integration using Retrofit
  - `SpoonacularApiService.kt` - Type-safe API definitions
  - JSON parsing with Moshi converter
  - Proper HTTP error handling and quota management
- **Background Threading**: All network operations on IO dispatcher
  - Coroutines with `Dispatchers.IO` in repositories
  - No UI thread blocking

#### **Dynamic Image Loading**
- **Asynchronous Image Loading**: Glide implementation for recipe images
  - Placeholder images during loading
  - Error handling for failed network requests
  - Memory-efficient image caching

#### **Local Data Storage**
- **Room Database**: `ActivityDatabase` with proper entity relationships
  - Local fitness data persistence across sessions
  - Background thread operations with coroutines
- **DataStore**: User preferences and settings storage
  - Diet preferences, authentication state
  - Type-safe preference management

### **Android System and Hardware Integration**

#### **MVVM Architecture**
- **Separation of Concerns**:
  - **Views**: Fragments (`DashboardFragment`, `RecipesFragment`) control UI
  - **Models**: Data classes (`RecipeTrackUiState`, domain models)
  - **ViewModels**: Business logic (`RecipeViewModel`, `DashboardViewModel`)
- **Observer Pattern**: StateFlow/LiveData preventing memory leaks
- **Lifecycle Awareness**: ViewModelScope for proper resource management

#### **Lifecycle & System Events**
- **Splash Screen**: Android 12+ SplashScreen API implementation
  - Custom splash theme with app logo 
  - Conditional display based on authentication state loading
  - Smooth transition to main app theme
- **Orientation Changes**: Landscape layouts and state preservation
- **Application Switching**: ViewModel state retention
- **Bundle Management**: Fragment arguments and state restoration
- **Intent Handling**: External URL navigation and authentication flows
- **Permission Handling**: Google Fit permissions with proper user prompts

#### **Hardware Integration**
  - **Google Fit API**: Hardware sensor data integration
  - **Accelerometer**: Step counting and activity detection
  - **Runtime Permissions**: Requested at time of use for Google Fit access
  - **Permission Guards**: Features accessed only after permission grants

## Project Architecture

- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Dagger 2
- **UI Framework**: Material Design 3 with Data Binding
- **Navigation**: Navigation Component with conditional navigation graphs
- **Database**: Room with coroutines
- **Network**: Retrofit with Moshi for JSON parsing
- **Authentication**: Firebase Auth with Google Sign-In
- **API Integration**: Spoonacular API for recipes

## Key Features

### Authentication & Onboarding
- Firebase Google Sign-In integration
- Goal selection and user onboarding flow
- Conditional navigation based on authentication state

### Dashboard & Progress Tracking
- Real-time fitness data visualization using MPAndroidChart
- Google Fit integration for health data
- Water intake tracking with interactive charts
- Activity progress monitoring (steps, calories, sleep)

### Recipe Discovery
- Spoonacular API integration with 4 diet types (Vegetarian, Vegan, Paleo, Keto)
- Recipe categorization by meal types (Breakfast, Main Course, Snack, Salad)
- Horizontal scrolling recipe lists with shimmer loading effects
- Quota management with user-friendly error handling

### UI/UX Excellence
- Material Design 3 color system implementation
- Responsive layouts for both portrait and landscape orientations
- Dynamic bottom navigation with conditional visibility
- Custom AppBar behavior with proper fragment padding
- Consistent button styling with tertiary color scheme

## Quick Setup for Review

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK 34
- Git

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone [your-repository-url]
   cd FitStreakProject
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned `FitStreakProject` directory
   - Wait for Gradle sync to complete

3. **Configuration Files**
   - `apikey.properties` - Contains Spoonacular API key (will be provided when submitted for review)
   - `app/google-services.json` - Firebase configuration (already included)
   - All necessary configurations are provided for review purposes

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's build button

## Testing

### Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Instrumentation Tests
```bash
./gradlew connectedDebugAndroidTest
```

### Test Coverage
- ViewModels: Comprehensive unit testing with fake data sources
- Repositories: Mocked network and database interactions
- Fragments: UI testing with Espresso
- Custom Test Runner: `FitStreakTestRunner` for instrumentation tests

## Project Structure

```
app/src/main/java/com/apptimistiq/android/fitstreak/
├── authentication/          # Login and onboarding components
├── main/
│   ├── dashboard/          # Dashboard with charts and user info
│   ├── progressTrack/      # Daily progress and activity tracking
│   ├── recipe/            # Recipe discovery and management
│   ├── data/              # Repositories and data sources
│   └── home/              # Home screen and navigation
├── di/                    # Dependency injection modules
├── network/               # API interfaces and DTOs
└── utils/                 # Utility classes and extensions
```

## UI Highlights

### Material Design 3 Implementation
- Comprehensive color system with proper semantic naming
- Dynamic theming for light/dark modes
- Consistent elevation and spacing using dimension resources

### Responsive Design
- Landscape-specific layouts for optimal tablet experience
- Adaptive navigation patterns
- Proper constraint-based layouts

### User Experience
- Shimmer loading effects for better perceived performance
- Intuitive gesture navigation
- Contextual error handling with user-friendly messages

## Technical Implementation

### State Management
- StateFlow/SharedFlow for reactive programming
- Proper lifecycle-aware data observation
- Centralized UI state management in ViewModels

### Data Layer
- Repository pattern with clean separation of concerns
- Room database with proper entity relationships
- Retrofit with custom converters and error handling

### Performance Optimizations
- Efficient RecyclerView usage with proper view recycling
- Image loading optimization with Glide
- Coroutines for background operations

## App Flow

1. **Launch** → Authentication check → Navigate to appropriate screen
2. **Onboarding** → Goal selection → Dashboard setup
3. **Dashboard** → Real-time data display → Activity tracking
4. **Recipes** → API-driven content → Diet-based filtering
5. **Progress** → Google Fit integration → Data visualization

## API Keys & Configuration

**For Reviewers**: All necessary configuration files are included in this repository for evaluation purposes:

- **Spoonacular API**: Recipe data and nutritional information
- **Firebase**: Authentication and user management
- **Google Fit**: Health and fitness data integration

## Learning Outcomes Demonstrated

- Modern Android development with Kotlin
- MVVM architecture implementation
- Dependency injection with Dagger 2
- RESTful API integration and error handling
- Database design with Room
- UI/UX design following Material Design guidelines
- Testing strategies (unit, integration, UI)
- Version control with Git
- Performance optimization techniques

## **Standout Features (Going Beyond Requirements)**

### **Advanced UI/UX Implementation**
- **Material Design 3**: Complete color system implementation with semantic naming
- **Responsive Design**: Comprehensive landscape layouts for optimal tablet experience
- **Visual Hierarchy**: Proper spacing, elevation, and typography consistency
- **Custom Animations**: Shimmer loading effects and smooth layout transitions

### **Performance & Caching**
- **Data Caching**: Local Room database for offline functionality
- **Image Caching**: Glide integration with memory-efficient loading
- **Network Optimization**: Coroutines for non-blocking API calls
- **State Management**: Efficient StateFlow usage preventing memory leaks

### **User Experience Excellence**
- **Multiple User Support**: Firebase authentication with user-specific data
- **Personal Value**: Addresses real fitness tracking needs beyond static content
- **Intuitive Navigation**: Clear user flow with contextual navigation
- **Error Handling**: User-friendly quota exceeded messages and graceful failures

### **Enterprise-Level Architecture**
- **Dependency Injection**: Dagger 2 for testable, maintainable code
- **Repository Pattern**: Clean data layer abstraction
- **Testing Strategy**: Comprehensive unit and instrumentation testing
- **Code Quality**: Proper separation of concerns and SOLID principles

## **Key File References for Review**

### **Architecture Implementation**
- `MainActivity.kt` - Navigation setup, lifecycle management, and splash screen integration using AuthenticationViewModel
- `DashboardViewModel.kt` - MVVM pattern with StateFlow
- `RecipeRemoteRepository.kt` - Repository pattern with error handling
- `AppComponent.kt` - Dagger dependency injection setup

### **UI/UX Excellence**
- `fragment_dashboard.xml` - ConstraintLayout with proper constraints
- `layout-land/fragment_dashboard.xml` - Responsive landscape layout
- `nav_graph.xml` - Navigation Component implementation
- `values/colors.xml` - Material Design 3 color system
- `values/themes.xml` - Splash screen theme configuration with animation
- `recycler_item_layout.xml` - MotionLayout implementation for daily progress items
- `xml/recycler_item_layout_scene.xml` - MotionScene with multiple transitions and keyframes

### **Network & Data**
- `SpoonacularWs.kt` - Retrofit API service definition
- `ActivityDatabase.kt` - Room database with relationships
- `UserProfileDataSource.kt` - DataStore preferences management
- `RecipeViewModel.kt` - Network error handling and quota management

### **Hardware Integration**
- `DailyProgressFragment.kt` - Google Fit API integration
- `PermissionEvent.kt` - Runtime permission handling
- `GoogleFitManager.kt` - Hardware sensor data collection

### **Testing Implementation**
- `src/test/` - Unit tests for ViewModels and repositories
- `src/androidTest/` - Instrumentation tests with Espresso
- `FitStreakTestRunner.kt` - Custom test runner configuration


**Value Over Website**: 
- Hardware sensor integration (steps, heart rate)
- Offline functionality with local data storage
- Native Android UI optimized for mobile interaction

## Support

For any questions regarding this project, please refer to the code comments and documentation within the source files. The project follows standard Android development practices and conventions.

### **Specific Rubric Compliance Verification**
- **Navigation**: Check `nav_graph.xml` and `MainActivity.kt` for Navigation Component implementation
- **ConstraintLayout**: All layout files use ConstraintLayout with proper constraints
- **MotionLayout**: `recycler_item_layout.xml` with `xml/recycler_item_layout_scene.xml` - complex animations with multiple ConstraintSets and transitions
- **Splash Screen**: `MainActivity.kt` with Android 12+ SplashScreen API and `values/themes.xml` theme configuration
- **MVVM**: Clear separation in `ViewModel`, `Repository`, and `Fragment` classes
- **Hardware**: Google Fit integration in `DailyProgressFragment.kt`
- **Networking**: Retrofit implementation in `SpoonacularWs.kt` and `RecipeRemoteRepository.kt`
- **Local Storage**: Room database in `ActivityDatabase.kt` and DataStore in `UserProfileDataSource.kt`

---

**Note**: This project is submitted for academic evaluation. All APIs and services are configured for educational review purposes.