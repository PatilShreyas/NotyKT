# 🚀 Architecture Revamp (v2.0.0)

## Introduction

Since the beginning of the development of NotyKT application, the Android app followed pure MVVM architecture in which UI layer performs actions via ViewModel and then ViewModel reactively gives response to the UI. Also, this repository is maintaining two different variants of UI development i.e.

1. `simpleapp` for demonstrating usage of traditional UI development with XMLs and binding.
2. `composeapp` for demonstrating usage of modern Android's UI development with Jetpack Compose.

While working on the both variants and maintaining common ViewModels with the current architecture was not looking maintainable in the long run. That's the intention behind this refactoring. Most of the changes happened are about state management around ViewModel and UI layer.

## 🐛 Problems

### ***ViewModel exposing a stream of state for every possible N events***

For example, see this ViewModel

```kotlin
class NoteDetailViewModel(...) : ViewModel() {

    private val _note = MutableSharedFlow<Note>()
    val note: SharedFlow<Note> = _note.shareWhileObserved(viewModelScope)

    private val _updateNoteState = MutableSharedFlow<UIDataState<Unit>>()
    val updateNoteState = _updateNoteState.shareWhileObserved(viewModelScope)

    private val _deleteNoteState = MutableSharedFlow<UIDataState<Unit>>()
    val deleteNoteState = _deleteNoteState.shareWhileObserved(viewModelScope)
}
```

Here, this ViewModel has exposed three streams for possible three different states i.e. note, update note and delete note. For every state, there's a private mutable stream created since it has to be protected from UI layer from the direct mutation.

### ***Composable doesn't fits well with sealed states***

While doing imperative UI development, sealed class-based states helped to properly manage the state in Activities and Fragments. But Jetpack Compose follows declarative UI design paradigms. Also, sealed-class-based state models doesn't play well with the composable and if tried to forcefully use, it ends up making composable component stateful where it can be made stateless.

For example, see this composable which is listening to all the three state streams which are declared in the above ViewModel and there's no initial value provided since it can't be provided. This way, Composable functions doesn't look good since there's no single source of state on which composable should be dependent. *Also, if think of writing UI tests for this function, it would be somehow challenging*

```kotlin
@Composable
fun NoteDetailsScreen(...) {
    val note = viewModel.note.collectAsState(initial = null).value
    val updateState = viewModel.updateNoteState.collectAsState(initial = null)
    val deleteState = viewModel.deleteNoteState.collectAsState(initial = null)
}
```

### ***Some business logic reside on UI side***

Some of the business logic like validating note, title, username, password, etc was executed on UI side instead it should be responsibility of ViewModel to execute it and update the state accordingly.

### ***ViewModel testability***

While using `SharedFlow`s for streaming the state updates, ViewModel's test was not looking well. SharedFlow is hot in nature so while validating the UI states in testing, it's becoming the overhead for collecting states in collection and also need to make sure to collect state changes into the collection from child coroutine and cancelling the job before assertion. See the below example.

```kotlin
Given("A note for updating") {
    And("Note is not yet synced") {
        val updateStates = mutableListOf<UIDataState<Unit>>()
        val collectUpdateStates = launch { 
            // This is launched in another coroutine because it 🔴
            // suspends the call to `toList()` because this is SharedFlow 🔴
            viewModel.updateNoteState.toList(updateStates)
        }

        When("Note is updated") {
            viewModel.updateNote(title, note)

            Then("Valid UI states should be get emitted") {
                // 🔴 If this is not cancelled, test will run infinitely 🔴
                collectUpdateStates.cancel() 
                updateStates[0].isLoading shouldBe true
                updateStates[1].isSuccess shouldBe true
            }
        }
    }
}
```

## 🏗️ Refactoring

### Single `State`

Maintaining different state streams for ***N*** use cases was not looking good earlier. Then `State` came into the picture which is **interface** for all the UI states.

```kotlin
/**
 * Represents a base type for the state of a UI component
 */
interface State
```

Example of state:

*This is a single state class for the UI of Note details having initial state predefined.* UI will just listen to this class updates and will update state accordingly without having any extra checks.

```kotlin
data class NoteDetailState(
    val isLoading: Boolean = false,
    val title: String? = null,
    val note: String? = null,
    val showSave: Boolean = false,
    val finished: Boolean = false,
    val error: String? = null
) : State
```

### Origin of `BaseViewModel`

Having private mutable stream and pubic immutable stream was overhead. So after thinking from that perspective, `BaseViewModel` came into the picture which was strongly typed with `State`. `StateFlow` is exposed to the UI layer which emits recent state to the UI subscribers.

```kotlin
/**
 * Base for all the ViewModels
 */
abstract class BaseViewModel<STATE : State>(initialState: STATE) : ViewModel() {

    /**
     * Mutable State of this ViewModel
     */
    private val _state = MutableStateFlow(initialState)

    /**
     * State to be exposed to the UI layer
     */
    val state: StateFlow<STATE> = _state.asStateFlow()

    /**
     * Retrieves the current UI state
     */
    val currentState: STATE get() = state.value

    /**
     * Updates the state of this ViewModel and returns the new state
     *
     * @param update Lambda callback with old state to calculate a new state
     * @return The updated state
     */
    protected fun setState(update: (old: STATE) -> STATE): STATE = 
        _state.updateAndGet(update)
}
```

So the implementation looks like the follows. Whenever the need for mutation of state is occurred, the method `setState()` is called and the current state is copied with changing the required fields. This is how state is managed through ViewModel.

```kotlin
class NoteDetailViewModel(...) : BaseViewModel<NoteDetailState>(initialState = NoteDetailState()) {
    fun setTitle(title: String) {
        setState { state -> state.copy(title = title) }
    }

    fun setNote(note: String) {
        setState { state -> state.copy(note = note) }
    }
}
```

### UI state management in Fragments

In the `simpleapp` module, `BaseFragment` then strictly bounded with `State`. Then there's a method `render()` which is invoked whenever state is updated from the ViewModel. Here, the state handling is done.

```kotlin
class NoteDetailFragment : BaseFragment<NoteDetailFragmentBinding, NoteDetailState, NoteDetailViewModel>() {
    // ...
    override fun render(state: NoteDetailState) {
        showProgressDialog(state.isLoading)

        binding.fabSave.isVisible = state.showSave

        binding.noteLayout.fieldTitle.setText(title)
        binding.noteLayout.fieldNote.setText(note)

        if (state.finished) {
            findNavController().navigateUp()
        }

        val errorMessage = state.error
        if (errorMessage != null) {
            toast("Error: $errorMessage")
        }
    }
}
```

### UI state management in Composables

After the refactoring, composables improved a lot. The state management became better than earlier in `composeapp` module. Like in the example follows, `NoteDetailScreen` listens to the ViewModel state changes and thus becomes stateful and it provided all the necessary states to the `NoteDetailContent()` composable and also listen to the events and passes events as an action to the ViewModel. Thus, `NoteDetailContent()` completely becomes a Stateless composable.

```kotlin
@Composable
fun NoteDetailsScreen(navController: NavHostController, viewModel: NoteDetailViewModel) {
    val state by viewModel.collectState()

    NoteDetailContent(
        title = state.title ?: "",
        note = state.note ?: "",
        error = state.error,
        showSaveButton = state.showSave,
        onTitleChange = viewModel::setTitle,
        onNoteChange = viewModel::setNote,
        onSaveClick = viewModel::save,
        onDeleteClick = { showDeleteNoteConfirmation = true },
        onNavigateUp = { navController.navigateUp() }
    )
}
```

### All business logic in ViewModel, only UI and rendering process on UI side

After the refactoring, all the core business logic of the application moved to the ViewModel. ViewModels executes the business, updates the state and accordingly UI processes the state and renders the components.

### Testing of states and ViewModel became easy

The testing of ViewModel and validating the UI states became very easy after the refactoring which was not good with the previous implementation. See the example:

```kotlin
Given("Note contents") {
    And("Note contents are invalid") {
        val title = "hi"
        val note = ""

        When("When note contents are set") {
            viewModel.setTitle(title)
            viewModel.setNote(note)

            Then("UI state should have validation details") {
                viewModel.withState {
                    this.title shouldBe title
                    this.note shouldBe note
                    showSave shouldBe false
                }
            }
        }
    }
}
```

## 🤔 Conclusion

This refactoring of the NotyKT application improved the quality of codebase and taught lessons to improvise the architecture of the application. These learnings will be beneficial for the readers for choosing the right architecture for their application and that's the main aim of writing this document.

---

> ***Refer to [this Pull request](https://github.com/PatilShreyas/NotyKT/pull/398) to see the changes made to revamp the architecture.***
