# Changelog

You can see [GitHub releases](https://github.com/PatilShreyas/NotyKT/releases) where this is officially released.

---

## _v2.0.0_ (2022-02-20)

This release includes codebase refactoring changes.

### 🎯 Codebase Improvements

- [[#397](https://github.com/PatilShreyas/NotyKT/issues/397)] Revamp architecture of NotyKT Android app.

> [**Read more**](/pages/noty-android/architecture-revamp-v2.0.0.md) about the refactoring.

## _v1.3.2_ (2021-11-30)

This release includes a few improvements and fixes in the Jetpack Compose and Simple Application to make them better. All features mentioned below are contributed by [@kasem-sm](https://github.com/kasem-sm)

### 🐛 Bug Fixes

- [[#329](https://github.com/PatilShreyas/NotyKT/issues/329)] Fix crashes when user clicks logout (in Simple App).
- [[#337](https://github.com/PatilShreyas/NotyKT/issues/337)] Fix not able to add note again once already added.

### ✅ Improvements
 
- [[#338](https://github.com/PatilShreyas/NotyKT/issues/338)] Show confirmation dialog on note deletion and logout in Simple app.
- [[#339](https://github.com/PatilShreyas/NotyKT/issues/339)] Fixed Save note button hides behind the keyboard in note detail and add new note screen.
- [[#336](https://github.com/PatilShreyas/NotyKT/issues/336)] Improved touch region for icons in compose app.
- [[#336](https://github.com/PatilShreyas/NotyKT/issues/336)] Changed background color at About Screen to match with the background color at Note Detail Screen.
- [[#336](https://github.com/PatilShreyas/NotyKT/issues/336)] Improved style of text fields for notes in Compose app.

### 🎯 Codebase Improvements

- Replaced `lifecycleScope.launch` with `viewLifecycleOwner.lifecycleScope`.
- Refactored Noty Dialogs's `ConfirmationDialog` to use Default `AlertDialog` composable instead of Default Dialog composable.

_**Many Thanks to superstar ⭐ contributor [@kasem-sm](https://github.com/kasem-sm) for contributing and for making this project better!**_

---

## _v1.3.1_ (2021-11-16)

This release includes few improvements and fixes in the Jetpack Compose Application to make it better.

### 🐛 Bug Fixes

- [[#281](https://github.com/PatilShreyas/NotyKT/issues/281)] Earlier, After signup, navigating back takes to log in screen. Now it closes the app.
- [[#282](https://github.com/PatilShreyas/NotyKT/issues/282)] Show proper cards with proper shadow in About screen. (_Contributed by [@yogeshpaliyal](https://github.com/yogeshpaliyal)_)
- [[#284](https://github.com/PatilShreyas/NotyKT/issues/284)] Removed focus (cursor) from fields while sharing image of a note. (_Contributed by [@yogeshpaliyal](https://github.com/yogeshpaliyal)_)
- [[#286](https://github.com/PatilShreyas/NotyKT/issues/286)] Improved dark mode visibility. (_Contributed by [@yogeshpaliyal](https://github.com/yogeshpaliyal)_)
- [[#294](https://github.com/PatilShreyas/NotyKT/issues/294)] Earlier, flickering (recompositions) were happening after performing navigation through screens.

### ✅ Improvements

- [[#280](https://github.com/PatilShreyas/NotyKT/issues/280)] Provided helper message for input fields like username and password for better UX in Login/Signup screens. 
- [[#283](https://github.com/PatilShreyas/NotyKT/issues/283)] Improved touch region area of note input fields. (_Contributed by [@yogeshpaliyal](https://github.com/yogeshpaliyal)_)
- [[#287](https://github.com/PatilShreyas/NotyKT/issues/287)] Show confirmation dialog before deleting a note.
- [[#297](https://github.com/PatilShreyas/NotyKT/issues/297)] Show confirmation dialog before logging out.

### 🎯 Codebase Improvements

- Used `decorationBox` property of Composable TextField to show/hide placeholder instead of manually handling in a box.
- Removed `jcenter()` from Gradle repositories (_Contributed by [@sairajsawant](https://github.com/sairajsawant)_)

_**Many Thanks to superstar ⭐ contributors [@yogeshpaliyal](https://github.com/yogeshpaliyal), [@sairajsawant](https://github.com/sairajsawant) for contributing PRs and [@kasem-sm](https://github.com/kasem-sm) for raising issues**_

---

## _v1.3.0_ (2021-10-24)

This release includes new feature and some fixes in the Jetpack Compose Application.

### 🔮 What's New?

- [[#119](https://github.com/PatilShreyas/NotyKT/issues/119)] Added support in the Jetpack Compose app to Share note as an Image (_Contributed by [@ch8n](https://github.com/ch8n)_).

### ✅ Bug Fixes / Improvements

- Fixed saving/syncing note information

### 🎯 Codebase Improvements

- Extracted out common used utility code of `composeapp` and `simpleapp` into a common utility functions.
- Created a common `@Composable` component `Capturable` for capturing composable component in the form of a Bitmap.
- Updated Jetpack Compose to 1.0.4 and Kotlin version to 1.5.31.

_**Many Thanks to superstar ⭐ contributor [@ch8n](https://github.com/ch8n) for the [PR](https://github.com/PatilShreyas/NotyKT/pull/269)**_

---

## _v1.2.0_ (2021-08-29)

This release includes User experience improvements in the Jetpack Compose Application. Minor fixes in Simple app.

### 🔮 What's New?

- [[#209](https://github.com/PatilShreyas/NotyKT/issues/209)] Added connectivity indicator in compose app.

### ✅ Bug Fixes / Improvements

- [[#202](https://github.com/PatilShreyas/NotyKT/issues/202)] Fixed continuous flickering issue after Signup/Login in compose app.
- [[#203](https://github.com/PatilShreyas/NotyKT/issues/203)] Avoided/Fixed re-syncing of notes after configuration changes in compose app.

### 🎯 Codebase Improvements

- [[#201](https://github.com/PatilShreyas/NotyKT/issues/201)] Optimized APK size by enabling R8.
- [[#206](https://github.com/PatilShreyas/NotyKT/issues/206)] Fixed memory leak of `mAdapter` in simple app.
- Set flag `android:exported="true"` for Activity to support Android 12 and above.
- Provide content padding to `LazyColumn` _(to achieve same behavior as `clipToPadding` in RecyclerView)_.
- Cleaned up code.

---

## _v1.1.0_ (2021-08-06)

This release includes User experience improvements in the Jetpack Compose Application. No change in simple app.

### 🔮 What's New?

- [[#117](https://github.com/PatilShreyas/NotyKT/issues/117)] Added screen: **About** for the details regarding application.
- [[#118](https://github.com/PatilShreyas/NotyKT/issues/118)] Added ***Swipe to refresh*** support in Notes screen to re-load the notes.

### ✅ Bug Fixes / Improvements

All below fixes and improvements are done in the Compose application.

- [[#117](https://github.com/PatilShreyas/NotyKT/issues/117)] Clear all previous screens from backstack after successful login/signup.
- [[#117](https://github.com/PatilShreyas/NotyKT/issues/117)] Added validation for input text fields in Login and Signup screen.
- [[#120](https://github.com/PatilShreyas/NotyKT/issues/120)] Fix Background of Login screen in Dark mode (_Earlier, it's not supporting dark theme well_)
- [[#151](https://github.com/PatilShreyas/NotyKT/issues/117)] Added transition while navigating through the screens.
- [[#196](https://github.com/PatilShreyas/NotyKT/issues/197)] Avoid re-syncing notes every time whenever notes screen is launched
(_after returning to notes screen from other screens like About or note details_).
- [[#197](https://github.com/PatilShreyas/NotyKT/issues/197)] Improved UI/UX of the input Text fields throughout the application.
- Fix: Back button pressed in note details screen creates new Notes screen instead of going back.

### 🎯 Codebase Improvements

- [[#117](https://github.com/PatilShreyas/NotyKT/issues/117)] Create re-usable Composable components to reduce the repetitive code.
- Using the stable release of Jetpack Compose 1.0.1 with Kotlin 1.5.21.
- Renamed color components of theme.
- Use Hilt Compose navigation.
- Cleaned up code, refactored classes and composable methods.

---

## _v1.0.0_ (2021-02-07)

This release includes major changes and improvements.

### 🔮 What's New?

- [[#15](https://github.com/PatilShreyas/NotyKT/issues/15)] Implemented App UI with Jetpack Compose UI toolkit.

### ✅ Bug Fixes / Improvements

- Fix crash when pressed back from Note details.

### 🎯 Codebase Improvements

- [[#15](https://github.com/PatilShreyas/NotyKT/issues/15)] Added module for Jetpack Compose implementation: `:app:composeapp`.
- Migrated to the latest version of Dagger 2.31.2.
- Use Hilt Assisted Injection for ViewModel and WorkManager.

---

## _v0.1.1_ (2020-12-06)

This release includes some minor fixes and improvements.

### 🔮 What's New?

- [[#88](https://github.com/PatilShreyas/NotyKT/issues/88)] Added menu for sharing note content as Image to external apps.
Now there're be two sub-menus for sharing menu i.e. _'Share as Text'_ and _'Share as Image'_
- [[#92](https://github.com/PatilShreyas/NotyKT/issues/92)] Added dialogs for showing loading progress or errors for better understanding with interactive animations.

### ✅ Bug Fixes / Improvements

- [[#90](https://github.com/PatilShreyas/NotyKT/issues/90)] Username field was earlier taking multi-line inputs. This has been fixed and it only takes single-line input.

### 🎯 Codebase Improvements

- [[#81](https://github.com/PatilShreyas/NotyKT/issues/81)] Migrated from `LiveData` to `Flow` in _ViewModels_. This has been implemented so that we can effectively manage states in future when integrated with Jetpack Compose UI.

---

## _v0.1.0_ (2020-11-29)

This release includes some major feature and improvements

### 🔮 What's New?

- [[#36](https://github.com/PatilShreyas/NotyKT/issues/36)] Added Offline capability in the application

Now onwards, internet connectivity isn't necessary to interact with _NotyKT app_. If connectivity is not available it'll still allow you to add, update and delete notes. It'll persist state of notes locally and will process updates once connectivity is back.

---

## _v0.0.2_ (2020-11-08)

This release includes some fixes and improvements

### 🔮 What's New?

- [[#54](https://github.com/PatilShreyas/NotyKT/issues/54)] Added About screen in the application with app details.

### ✅ Bug Fixes / Improvements

- [[#59](https://github.com/PatilShreyas/NotyKT/issues/59)] Layout of Login and Register was lying above the status bar.
- [[#56](https://github.com/PatilShreyas/NotyKT/issues/56)] Note content layout in Add/details was not smooth to handle. Now it's flexible with smooth Scroll-ability.
- [[#53](https://github.com/PatilShreyas/NotyKT/issues/53)] Shared message (_When sharing note to external apps_) content was not valid.

---

## _v0.0.1_ (2020-10-30)

This is the initial version of Noty Android application.

### Features

- Authentication (Login/Signup)
- List all notes.
- Create a new note.
- Update/delete note.
- Dark Mode/Light Mode support.

Noty Simple application which uses Navigation architecture is ready to test.
ou can test `noty-android-simple` APK which is ready for testing.

_**Noty Compose App is not yet developed, it's development is WIP.**_
