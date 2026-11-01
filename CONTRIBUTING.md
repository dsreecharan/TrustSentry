# Contributing to TrustSentry

Thank you for your interest in contributing to **TrustSentry**! We welcome bug reports, feature proposals, and pull requests to strengthen cryptographic verification and mobile security standards.

## Code of Conduct
We are committed to providing a welcoming and harassment-free environment for all contributors. Please be respectful and constructive in all issues and pull requests.

## Development Workflow

### 1. Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java Development Kit 17 or 21
- **Android SDK**: API Level 36 (Android 16), Minimum SDK 24

### 2. Fork & Clone
```bash
git clone https://github.com/<your-username>/trustsentry.git
cd trustsentry
```

### 3. Build & Test Locally
Before submitting changes, make sure all unit and Robolectric tests pass and that the project builds cleanly:
```bash
# Run test suite
./gradlew :app:testDebugUnitTest

# Build debug APK
./gradlew :app:assembleDebug
```

### 4. Code Guidelines
- **Kotlin & Compose**: Use Kotlin idiomatically and follow Jetpack Compose Material 3 design patterns.
- **State Management**: Adhere to MVVM with `StateFlow` and immutable data classes.
- **Testing**: When adding new cryptographic parsers or verification rules, add corresponding unit tests to `ExampleRobolectricTest.kt`.
- **Zero Mock Data**: Never substitute genuine cryptographic algorithms with placeholder mocks.

### 5. Submitting a Pull Request
1. Create a feature branch: `git checkout -b feature/my-new-feature`
2. Commit with concise, descriptive messages.
3. Push to your fork: `git push origin feature/my-new-feature`
4. Open a Pull Request targeting the `main` branch.
