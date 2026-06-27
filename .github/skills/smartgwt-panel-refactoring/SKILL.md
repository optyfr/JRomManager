---
name: smartgwt-panel-refactoring
description: Refactor SmartGWT UI panels to eliminate double-brace initialization, extract factory methods, replace anonymous classes with lambdas, and reduce SonarQube violations. Use when working on WebClient UI classes, reviewing SmartGWT panels, or remediating SonarQube issues in GWT/SmartGWT code.
---

# SmartGWT Panel Refactoring

Systematic approach to refactoring SmartGWT UI panels, proven on ProfilePanel.java (60+ violations reduced to 2 framework-constrained residuals).

## Anti-Patterns to Eliminate

### 1. Double-Brace Initialization `{{ }}`

The root cause of most SonarQube violations (S3599, S110, S1604). Creates anonymous inner classes that capture the enclosing instance.

**Before (ScannerPanel.java pattern):**
```java
new ToolStrip() {
    {
        setWidth100();
        addButton(new ToolStripButton() {
            {
                setAutoFit(true);
                setTitle("Info");
                addClickHandler(event -> doSomething());
            }
        });
    }
}
```

**After (ProfilePanel.java pattern):**
```java
private ToolStrip buildToolStrip() {
    ToolStrip strip = new ToolStrip();
    strip.setWidth100();
    strip.addButton(buildInfoButton());
    return strip;
}

private ToolStripButton buildInfoButton() {
    ToolStripButton btn = new ToolStripButton();
    btn.setAutoFit(true);
    btn.setTitle("Info");
    btn.addClickHandler(event -> doSomething());
    return btn;
}
```

### 2. Double-Brace HashMap Initialization

**Before (SettingsForm.java pattern):**
```java
final static protected Map<String, String> fname2name = new HashMap<String, String>() {
    {
        put("key1", "value1");
        put("key2", "value2");
    }
};
```

**After:**
```java
private static final Map<String, String> FNAME_TO_NAME = Map.ofEntries(
    Map.entry("key1", "value1"),
    Map.entry("key2", "value2")
);
```

### 3. String Literal Duplication

Extract repeated strings to `private static final` constants at class top.

```java
private static final String PARENT = "Parent";
private static final String TITLE = "title";
private static final String PATH = "Path";
private static final String ICON_FOLDER_ADD = "icons/folder_add.png";
```

### 4. Anonymous Inner Classes -> Lambdas

Replace single-method anonymous classes with lambdas or method references.

| Anonymous Class | Lambda / Method Reference |
|---|---|
| `new RecordDoubleClickHandler() { onDblClick(e) {...} }` | `this::loadProfile` |
| `new DSCallback() { execute(resp, data, req) {...} }` | `(resp, data, req) -> {...}` |
| `new HoverCustomizer() { hoverHTML(...) {...} }` | Keep anonymous (SonarQube exception S6213) |
| `new EnableIfCondition() { enable(...) {...} }` | `(target, menu, item) -> condition` |

## Refactoring Process

1. **Identify** double-brace blocks — search for `{{` or `new Widget() {`
2. **Extract** each widget creation into a `build*()` factory method
3. **Extract** string literals used 2+ times to static constants
4. **Convert** anonymous handlers to lambdas/method references
5. **Name** fields with camelCase (`idField` not `IDField`)
6. **Unbox** `Boolean.TRUE` where primitive `boolean` suffices
7. **Remove** unused imports, commented-out code, empty statements
8. **Verify** with Grep that no external code references removed symbols

## SmartGWT Framework Constraints (Acceptable Residuals)

These cannot be fixed without breaking SmartGWT functionality:

- **S110** (too many parents): SmartGWT base class hierarchy is deep — unavoidable
- **S6213** (HoverCustomizer): Must use anonymous class to override `hoverHTML` with SonarQube suppression

When these remain, add `@SuppressWarnings("java:S6213")` on the method.

## Reference File

The canonical example is `ProfilePanel.java` in `WebClient/src/main/java/jrm/webui/client/ui/`. Study its `build*()` method decomposition pattern.

## Panels Needing Refactoring

| File | Severity | Primary Issue |
|---|---|---|
| `ScannerPanel.java` | High | Deeply nested double-brace (5+ levels) |
| `SettingsForm.java` | Medium | Double-brace HashMap |
| `ProfileViewer.java` | Check | Large file (788 lines) |
| `RemoteFileChooser.java` | Check | Large file (936 lines) |
