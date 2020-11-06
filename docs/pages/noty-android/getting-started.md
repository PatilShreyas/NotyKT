# Noty Android App

***You can Install and test latest Covid19 Notifier app from below 👇***

[![Noty Simple App](https://img.shields.io/github/v/release/patilshreyas/notykt?color=7885FF&label=Simple%20App&logo=android&style=for-the-badge)](https://github.com/patilshreyas/notykt/releases/latest/download/noty-android-simple.apk)
[![Noty Compose App](https://img.shields.io/github/v/release/patilshreyas/notykt?color=7885FF&label=Compose%20App&logo=android&style=for-the-badge)](https://github.com/patilshreyas/notykt/releases/latest/download/noty-android-compose.apk)

This is mobile application using which actual users will interact with. The application is developed using Modern tools/libraries with UI implementations with Navigation architecture as well as Modern UI development toolkit i.e. Jetpack Compose.

It connects with the Noty REST API to save and retrieve data.

## 👓 Features of Codebase

- [x] Single Activity Design
- [x] Offline Capability - Notes ✈️
- [ ] Offline Capability - Add/Update/Delete Notes ***(WIP)***
- [x] Clean and Simple Material UI 🎨
- [x] Dark mode 🌗
- [ ] Tests ***(WIP)***
- [ ] Jetpack Compose UI ***(WIP)***

## 📙 Overview of Codebase

This is Gradle based multi-module project having modules as following:

### Application (`app`)

This is the main Android application module which include Android specific features or code.

It has two submodules as:

- **Simple Application (`simpleapp`):** UI implementation using [_Navigation Architecture_](https://developer.android.com/guide/navigation/navigation-getting-started) using traditional XML resources.

- **JetPack Compose Application (`composeapp`) _(WIP)_:** UI implementation using [_Jetpack Compose UI Toolkit_](https://developer.android.com/jetpack/compose?gclid=Cj0KCQjwreT8BRDTARIsAJLI0KKRX0vsRWcQ-0AC6lCutEWwAB4t1wqWBi2MclQqm96gnSddahFRdkAaArbwEALw_wcB&gclsrc=aw.ds)

### Core (`core`)

Pure JVM module consist of utilities, interfaces and base boilerplate.

### Data (`data`)

This is a data source for the application. It has two sub-modules as following.

- **Local Data (`local`):** Persistent storage of data using Room (SQLite) database.

- **Remote Data(`remote`):** Network layer implemented using Retrofit.

### Repository (`repository`)

For single source of data. Implements `local` and `remote` modules.

### ViewModel (`viewmodel`)

ViewModel implementation. Used `repository` for source of data.

## Built with 🛠

- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android development.

- [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - For asynchronous and more..

- [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/) - A cold asynchronous data stream that sequentially emits values and completes normally or with an exception.

- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes. 
  - [ViewBinding](https://developer.android.com/topic/libraries/view-binding) - Generates a binding class for each XML layout file present in that module and allows you to more easily write code that interacts with views.
  - [Room](https://developer.android.com/topic/libraries/architecture/room) - SQLite object mapping library.

- [Navigation Component](https://developer.android.com/guide/navigation/navigation-getting-started) Navigation refers to the interactions that allow users to navigate across, into, and back out from the different pieces of content within your app.
    - [Safe args](https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args) - Gradle plugin that provides type safety when navigating and passing data between destinations. 

- [Jetpack Security](https://developer.android.com/topic/security/)
    - [Encrypted SharedPreference](https://developer.android.com/topic/security/data) - Used to store key-value data using encryption.

- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Jetpack DataStore is a data storage solution that allows you to store key-value pairs or typed objects with protocol buffers.

- [Dependency Injection](https://developer.android.com/training/dependency-injection) - 
  - [Hilt-Dagger](https://dagger.dev/hilt/) - Standard library to incorporate Dagger dependency injection into an Android application.
  - [Hilt-ViewModel](https://developer.android.com/training/dependency-injection/hilt-jetpack) - DI for injecting `ViewModel`.
  - [Hilt-WorkManager](https://developer.android.com/training/dependency-injection/hilt-jetpack) - DI for injecting `WorkManager`.
  - [Assisted Inject with Dagger](https://github.com/square/AssistedInject) - Manually injected dependencies for your JSR 330 configuration.

- [Retrofit](https://square.github.io/retrofit/) - A type-safe HTTP client for Android and Java.

- [Moshi](https://github.com/square/moshi) - A modern JSON library for Kotlin and Java.

- [Moshi Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/moshi) - A Converter which uses Moshi for serialization to and from JSON.

- [Material Components for Android](https://github.com/material-components/material-components-android) - Modular and customizable Material Design UI components for Android.

- [Jetpack Compose UI Toolkit](https://developer.android.com/jetpack/compose) - Modern UI development toolkit.

- [LeakCanary](https://square.github.io/leakcanary/) - Memory leak detection library for Android
