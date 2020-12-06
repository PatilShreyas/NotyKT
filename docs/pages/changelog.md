# Changelog

You can see [GitHub releases](https://github.com/PatilShreyas/NotyKT/releases) where this is officially released.

---

## _v0.1.1_ (2020-12-06)

This release includes some minor fixes and improvements.

**🔮 What's New?**

- [[#88](https://github.com/PatilShreyas/NotyKT/issues/88)] Added menu for sharing note content as Image to external apps.
Now there're be two sub-menus for sharing menu i.e. _'Share as Text'_ and _'Share as Image'_
- [[#92](https://github.com/PatilShreyas/NotyKT/issues/92)] Added dialogs for showing loading progress or errors for better understanding with interactive animations.

**✅ Bug Fixes / Improvements**

- [[#90](https://github.com/PatilShreyas/NotyKT/issues/90)] Username field was earlier taking multi-line inputs. This has been fixed and it only takes single-line input.

**🎯 Codebase Improvements**

- [[#81](https://github.com/PatilShreyas/NotyKT/issues/81)] Migrated from `LiveData` to `Flow` in _ViewModels_. This has been implemented so that we can effectively manage states in future when integrated with Jetpack Compose UI.

---

## _v0.1.0_ (2020-11-29)

This release includes some major feature and improvements

**🔮 What's New?**

- [[#36](https://github.com/PatilShreyas/NotyKT/issues/36)] Added Offline capability in the application

Now onwards, internet connectivity isn't necessary to interact with _NotyKT app_. If connectivity is not available it'll still allow you to add, update and delete notes. It'll persist state of notes locally and will process updates once connectivity is back.

---

## _v0.0.2_ (2020-11-08)

This release includes some fixes and improvements

**🔮 What's New?**

- [[#54](https://github.com/PatilShreyas/NotyKT/issues/54)] Added About screen in the application with app details.

**✅ Bug Fixes / Improvements**

- [[#59](https://github.com/PatilShreyas/NotyKT/issues/59)] Layout of Login and Register was lying above the status bar.
- [[#56](https://github.com/PatilShreyas/NotyKT/issues/56)] Note content layout in Add/details was not smooth to handle. Now it's flexible with smooth Scroll-ability.
- [[#53](https://github.com/PatilShreyas/NotyKT/issues/53)] Shared message (_When sharing note to external apps_) content was not valid.

---

## _v0.0.1_ (2020-10-30)

This is the initial version of Noty Android application.

**Features:**

- Authentication (Login/Signup)
- List all notes.
- Create a new note.
- Update/delete note.
- Dark Mode/Light Mode support.

Noty Simple application which uses Navigation architecture is ready to test.
ou can test `noty-android-simple` APK which is ready for testing.

_**Noty Compose App is not yet developed, it's development is WIP.**_
