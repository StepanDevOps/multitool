# Project Context: Multitool (Android)

### 1. General Info
* **Project Name:** Multitool
* **Platform:** Android (Java)
* **Target:** Educational/Utility app with a plugin system
* **Architecture:** Clean MVP (Model-View-Presenter)

### 2. Architectural Rules (Crucial for Copilot)
* **Core Classes:**
    * `BaseView`: Interface for all views (defines `showLoading`, `hideLoading`, `showError`).
    * `BasePresenter<T extends BaseView>`: Abstract class handling View lifecycle (`attachView`, `detachView`).
* **The Contract Pattern:** Each feature MUST have a `Contract` interface containing nested `View` and `Presenter` interfaces.
* **Inheritance Rule:** `Presenter` implementation classes must `extends BasePresenter<Contract.View>` AND `implements Contract.Presenter`.
* **Constraint:** Interfaces in Contracts MUST NOT extend `BasePresenter`. They only define business logic.

### 3. Tech Stack
* **UI:** ConstraintLayout, Material Design Components, GridLayout.
* **Database:** Room Persistence Library (Entity, DAO, AppDatabase).
* **Threading:** Future implementation for Room queries (Repository pattern).

### 4. Team Context (Names used in comments/tasks)
* **Stepan (Lead):** Responsible for Core architecture and Presenter logic.
* **Daniyar (UI):** Focuses on XML layouts and View implementations.
* **Yaroslav (Backend/DB):** Handles Room entities, DAOs, and local data storage.

### 5. Current Task / Known Issues
* **Refactoring Notes Feature:** * Correcting `NotesContract.java` to ensure `Presenter` is a pure interface.
    * Implementing `NotesPresenter` to handle fake data loading before Room is ready.
    * Setting up `GridLayout` in `activity_main.xml` for feature selection.
* **Dependencies Needed:** `androidx.gridlayout:gridlayout:1.0.0` and Room components.
