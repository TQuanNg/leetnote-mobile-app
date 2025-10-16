# Theme Switching Feature Documentation

## Overview
The app now supports dynamic theme switching between light and dark modes. The theme preference is persisted using DataStore and automatically applied across the entire application.

## Implementation Details

### Components Added/Modified:

1. **ThemeManager.kt** - Manages theme state persistence using DataStore
2. **ThemeModule.kt** - Hilt module for dependency injection
3. **SettingViewModel.kt** - Updated to handle theme state management
4. **SettingScreen.kt** - Updated to use ViewModel for theme control
5. **MainActivity.kt** - Updated to observe and apply theme changes

### How it Works:

1. **Theme Persistence**: User's theme preference is saved to DataStore preferences
2. **Real-time Updates**: Theme changes are immediately reflected across the app
3. **System Integration**: The app uses Material Design 3 color schemes for both light and dark themes

### Usage:

1. Navigate to the Settings screen
2. Toggle the "Dark Theme" switch
3. The theme change is applied immediately and persisted for future app launches

### Technical Implementation:

- **State Management**: Uses StateFlow for reactive theme state
- **Persistence**: DataStore Preferences for storing theme choice
- **Dependency Injection**: Hilt for providing ThemeManager instance
- **Compose Integration**: LeetNoteTheme composable receives darkTheme parameter

The implementation ensures smooth theme transitions and maintains the user's preference across app restarts.
