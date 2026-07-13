# Plan: Add TestFX Tests to Achieve 80% Coverage on jrmfx

## Summary
Create comprehensive TestFX-based tests with mocking to increase code coverage on the jrmfx module from current state to 80%. Focus on complex controllers and UI components that are currently untested or minimally tested.

## Current State Analysis

### Infrastructure Already in Place
- **Test Frameworks**: JUnit 5, AssertJ, Mockito, TestFX (fx-labs)
- **Coverage Tool**: JaCoCo configured with XML reports
- **Existing Tests**: 21 test classes covering controls, utilities, and simple components
- **Pattern**: Tests use `@TestFxApplication` with custom `TestApp` extending `Application` and `TestFxRecordedStage`

### Coverage Gaps (Classes Needing Tests)
1. **MainFrameController** - Tab initialization, icon assignment
2. **ProfilePanelController** - Tree/table interactions, import/load operations
3. **ScannerPanelController** - File choosers, scan operations, settings binding
4. **SettingsPanelController** - Settings panels, choice boxes, drag-drop
5. **BatchToolsPanelController** - Batch operations, table management, progress tasks
6. **Dir2DatController** - Format selection, options, generate operation
7. **ProgressController** - Dynamic UI generation, progress updates
8. **ProfileViewerController** - Multi-table views, filters, context menus
9. **BaseController** - File/directory chooser helpers (abstract, test via concrete subclasses)
10. **ProgressTaskRunner** - Task execution, error handling

## Implementation Strategy

### Phase 1: Foundation Tests (High Impact, Lower Complexity)
**Target: ~15% coverage gain**

#### 1.1 BaseControllerTest (test via mock subclass)
- **What to test**: 
  - `chooseDir()` with various initial values and callbacks
  - `chooseOpenFile()` single and multiple selection
  - `chooseSaveFile()` with extension filters
  - `initFileChooser()` and `initDirectoryChooser()` edge cases
- **Mocking strategy**: Mock `Stage`, `Window`, `Scene`, `FileChooser`, `DirectoryChooser`
- **Expected coverage**: ~5%

#### 1.2 MainFrameControllerTest
- **What to test**:
  - `initialize()` method - icon creation, tab setup
  - Tab graphic assignments
  - Tab disable states (scannerPanelTab)
- **Mocking strategy**: Mock FXML fields, use reflection to inject mocks
- **Expected coverage**: ~3%

#### 1.3 ProgressTaskRunnerTest
- **What to test**:
  - `run()` success and exception paths
  - `handleInterruptedException()` 
  - `handleExecutionException()` with and without cause
  - `handleFailedException()` with BreakException and other exceptions
- **Mocking strategy**: Mock `Stage`, `ProgressTask`, `Dialogs`
- **Expected coverage**: ~2%

### Phase 2: Complex Controller Tests (Medium Impact, High Complexity)
**Target: ~30% coverage gain**

#### 2.1 ProfilePanelControllerTest
- **What to test**:
  - `initialize()` - tree/table setup, button configuration
  - Tree view selection handling
  - Table view selection and context menu
  - `btnLoad` action - profile loading
  - `btnImportDat` action - DAT file import with file chooser
  - `btnImportSL` action - software list import
  - Import operations with progress tasks
- **Mocking strategy**: 
  - Mock `Session`, `ProfileNFO`, `Dir` objects
  - Mock file choosers and directory choosers
  - Mock `ProgressTask` for import operations
- **Expected coverage**: ~10%

#### 2.2 SettingsPanelControllerTest
- **What to test**:
  - `initialize()` - all three sections (general, compressors, debug)
  - `initGeneral()` - threading choice box, backup destination, stylesheet
  - `initCompressors()` - ZIP level, 7z options, temp threshold
  - `initDebug()` - log level, GC button
  - Property change listeners
  - Drag-drop integration
- **Mocking strategy**:
  - Mock `Session`, `User`, `Settings`
  - Mock choice box selections
  - Mock file choosers
- **Expected coverage**: ~10%

#### 2.3 Dir2DatControllerTest
- **What to test**:
  - `initialize()` - checkbox bindings, text field setup
  - Property change listeners for all checkboxes
  - `generate` button action with format selection
  - File/directory choosers for src and dst
  - Drag-drop integration
- **Mocking strategy**:
  - Mock `Session`, `Settings`
  - Mock file choosers
  - Mock `ProgressTask` for generate operation
- **Expected coverage**: ~5%

#### 2.4 BatchToolsPanelControllerTest
- **What to test**:
  - `initialize()` - all three tabs (Dat2Dir, TrntChk, Compressor)
  - Table view setup and cell factories
  - Context menu actions (add/delete source directories)
  - Start button actions for each batch operation
  - Progress task creation
- **Mocking strategy**:
  - Mock `Session`, `Settings`
  - Mock table data
  - Mock `ProgressTask` for batch operations
- **Expected coverage**: ~5%

### Phase 3: Advanced UI Tests (Lower Impact, High Complexity)
**Target: ~25% coverage gain**

#### 3.1 ScannerPanelControllerTest
- **What to test**:
  - `initialize()` - all tabs and settings
  - Source list management (add/delete with context menu)
  - Destination path choosers (ROMs, disks, SW, samples, backup)
  - Checkbox bindings for destination options
  - Scan/Report/Fix button actions
  - Import/Export operations
  - Profile loader interface implementation
  - `loadProfile()` method
- **Mocking strategy**:
  - Mock `Session`, `Profile`, `ProfileNFO`
  - Mock file choosers
  - Mock `ProgressTask` for scan/fix operations
  - Mock `FilterSelectionHelper`
- **Expected coverage**: ~12%

#### 3.2 ScannerPanelSettingsControllerTest
- **What to test**:
  - `initialize()` - all checkboxes and combo boxes
  - Property change listeners
  - Exclude glob list management
  - Context menu for exclude patterns
  - Descriptor combo box cell factories
- **Mocking strategy**:
  - Mock `ProfileSettings`
  - Mock `Descriptor` objects
- **Expected coverage**: ~8%

#### 3.3 ProgressControllerTest
- **What to test**:
  - `initialize()` - progress bar visibility
  - `setInfos()` with different thread counts
  - `extendInfos()` dynamic panel addition
  - `clearInfos()` cleanup
  - `buildView()` helper method
  - Time formatting
  - Cancel button action
- **Mocking strategy**:
  - Mock `ProgressTask` and `PData`
  - Use real JavaFX components where possible
- **Expected coverage**: ~5%

### Phase 4: Profile Viewer Tests (Lower Impact, Very High Complexity)
**Target: ~10% coverage gain**

#### 4.1 ProfileViewerControllerTest
- **What to test**:
  - `initialize()` - table setup, column configuration
  - Machine list table (tableWL) with filters
  - Entry table (tableW) with status/name/description columns
  - Entity table (tableEntity) for ROMs/disks/samples
  - Toggle button filters (unknown/missing/partial/complete)
  - Search text field filtering
  - Context menu actions (copy hash, web search, export)
  - `reset()` method with profile changes
  - `clear()` and `reload()` methods
- **Mocking strategy**:
  - Mock `Profile`, `MachineList`, `SoftwareList`
  - Mock `Machine`, `Software`, `Rom`, `Disk`, `Sample`
  - Mock `Session`
- **Expected coverage**: ~10%

## Test File Structure

```
jrmfx/src/test/java/jrm/fx/ui/
├── BaseControllerTest.java (new)
├── MainFrameControllerTest.java (new)
├── ProgressTaskRunnerTest.java (new)
├── ProfilePanelControllerTest.java (new)
├── SettingsPanelControllerTest.java (new)
├── Dir2DatControllerTest.java (new)
├── BatchToolsPanelControllerTest.java (new)
├── ScannerPanelControllerTest.java (new)
├── ScannerPanelSettingsControllerTest.java (new)
├── progress/
│   └── ProgressControllerTest.java (new)
└── profile/
    └── ProfileViewerControllerTest.java (new)
```

## Technical Guidelines

### TestFX Application Pattern
```java
@TestFxApplication(ClassNameTest.TestApp.class)
@DisplayName("ClassName TestFX Tests")
class ClassNameTest {
    
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        
        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            primaryStage.setScene(new Scene(new StackPane(), 800, 600));
            primaryStage.show();
        }
        
        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }
}
```

### Mocking Strategy for Controllers
1. **Session Mocking**: Use Mockito to mock `Sessions.getSingleSession()` via reflection
2. **FXML Field Injection**: Use reflection to set `@FXML` annotated fields
3. **File Choosers**: Mock `FileChooser` and `DirectoryChooser` to return predefined paths
4. **Progress Tasks**: Mock `ProgressTask` to avoid actual task execution
5. **Dialogs**: Mock static `Dialogs` methods to avoid UI blocking

### Reflection Pattern for FXML Fields
```java
private void setFxmlField(Object target, String fieldName, Object value) {
    try {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
```

### Assertion Strategy
- Use `FxAssertions.assertThat()` for JavaFX components
- Use standard `assertThat()` for non-UI objects
- Verify mock interactions with `verify()` and `times()`

## Verification Steps

1. **Build and Test Execution**
   ```bash
   ./gradlew clean test
   ```

2. **Coverage Report Generation**
   ```bash
   ./gradlew codeCoverageReport
   ```

3. **Review Coverage**
   - Open `build/reports/jacoco/codeCoverageReport/html/index.html`
   - Navigate to jrmfx module
   - Verify line coverage ≥ 80%
   - Check branch coverage ≥ 70%

4. **SonarCloud Analysis** (if configured)
   ```bash
   ./gradlew sonar
   ```
   - Review coverage metrics on SonarCloud dashboard

## Success Criteria

- **Line Coverage**: ≥ 80% on jrmfx module
- **Branch Coverage**: ≥ 70% on jrmfx module
- **Test Count**: ~50-70 new test methods
- **Test Execution Time**: < 60 seconds total
- **No Flaky Tests**: All tests pass consistently
- **Code Quality**: Tests follow existing patterns and conventions

## Risk Mitigation

1. **JavaFX Thread Safety**: Always use `Platform.runLater()` for UI updates in tests
2. **Mock Complexity**: Start with simple mocks, add complexity incrementally
3. **FXML Loading**: Test controllers without loading FXML when possible
4. **Static Methods**: Use Mockito's `mockStatic()` for static method mocking (Dialogs, Sessions)
5. **Resource Loading**: Mock `MainFrame.getIcon()` to avoid resource loading issues

## Dependencies

No new dependencies required. All needed libraries are already configured:
- JUnit 5 (junit-jupiter)
- AssertJ (assertj-core)
- Mockito (mockito-core)
- TestFX (testfx-junit, testfx-assertj)

## Conclusion

This plan provides a structured approach to achieve 80% coverage on the jrmfx module through TestFX-based tests. The phased approach allows incremental progress tracking and prioritizes high-impact, lower-complexity tests first. Mocking is used extensively to isolate controller logic from external dependencies while maintaining test reliability.
