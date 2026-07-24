package jrm.fx.ui;

import static io.gitlab.fxlabs.testfx.assertj.FxAssertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link FilterSelectionHelper}.
 * Tests filter list selection operations including select all, unselect all,
 * invert selection, and filtered selection operations.
 */
@TestFxApplication(FilterSelectionHelperTest.TestApp.class)
@DisplayName("FilterSelectionHelper TestFX Tests")
class FilterSelectionHelperTest {

    /**
     * Test application that initializes JavaFX toolkit.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            primaryStage.setScene(new Scene(new StackPane(), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    /** The list of items used in selection tests. */
    private List<String> items;

    /** A map tracking selection state for each item. */
    private Map<String, Boolean> selectionMap;

    /** A JavaFX ListView populated with the test items. */
    private ListView<String> listView;

    /** A setter that writes selection state into the selection map. */
    private FilterSelectionHelper.SelectionSetter<String, Map<String, Boolean>> setter;

    /** A getter that reads selection state from the selection map. */
    private FilterSelectionHelper.SelectionGetter<String, Map<String, Boolean>> getter;

    @BeforeEach
    void setUp() {
        items = new ArrayList<>(Arrays.asList("Item1", "Item2", "Item3", "Item4", "Item5"));
        selectionMap = new HashMap<>();
        items.forEach(item -> selectionMap.put(item, false));
        listView = new ListView<>(FXCollections.observableArrayList(items));
        
        setter = (item, profile, selected) -> profile.put(item, selected);
        getter = (item, profile) -> profile.getOrDefault(item, false);
    }

    @Test
    @DisplayName("Should select all items")
    void shouldSelectAllItems() {
        FilterSelectionHelper.selectAll(items, selectionMap, setter, listView);
        
        assertThat(selectionMap.values())
                .as("All items should be selected")
                .allMatch(selected -> selected);
    }

    @Test
    @DisplayName("Should select all items when some are already selected")
    void shouldSelectAllItemsWhenSomeAreAlreadySelected() {
        selectionMap.put("Item1", true);
        selectionMap.put("Item3", true);
        
        FilterSelectionHelper.selectAll(items, selectionMap, setter, listView);
        
        assertThat(selectionMap.values())
                .as("All items should be selected")
                .allMatch(selected -> selected);
    }

    @Test
    @DisplayName("Should handle empty item list for select all")
    void shouldHandleEmptyItemListForSelectAll() {
        List<String> emptyItems = new ArrayList<>();
        
        FilterSelectionHelper.selectAll(emptyItems, selectionMap, setter, listView);
        
        assertThat(selectionMap)
                .as("Selection map should exist")
                .isNotNull();
    }

    @Test
    @DisplayName("Should unselect all items")
    void shouldUnselectAllItems() {
        items.forEach(item -> selectionMap.put(item, true));
        
        FilterSelectionHelper.unselectAll(items, selectionMap, setter, listView);
        
        assertThat(selectionMap.values())
                .as("All items should be unselected")
                .allMatch(selected -> !selected);
    }

    @Test
    @DisplayName("Should unselect all items when some are already unselected")
    void shouldUnselectAllItemsWhenSomeAreAlreadyUnselected() {
        items.forEach(item -> selectionMap.put(item, true));
        selectionMap.put("Item2", false);
        selectionMap.put("Item4", false);
        
        FilterSelectionHelper.unselectAll(items, selectionMap, setter, listView);
        
        assertThat(selectionMap.values())
                .as("All items should be unselected")
                .allMatch(selected -> !selected);
    }

    @Test
    @DisplayName("Should handle empty item list for unselect all")
    void shouldHandleEmptyItemListForUnselectAll() {
        List<String> emptyItems = new ArrayList<>();
        
        FilterSelectionHelper.unselectAll(emptyItems, selectionMap, setter, listView);
        
        assertThat(selectionMap)
                .as("Selection map should exist")
                .isNotNull();
    }

    @Test
    @DisplayName("Should invert selection from all unselected to all selected")
    void shouldInvertSelectionFromAllUnselectedToAllSelected() {
        FilterSelectionHelper.invertSelection(items, selectionMap, setter, getter, listView);
        
        assertThat(selectionMap.values())
                .as("All items should be selected after inversion")
                .allMatch(selected -> selected);
    }

    @Test
    @DisplayName("Should invert selection from all selected to all unselected")
    void shouldInvertSelectionFromAllSelectedToAllUnselected() {
        items.forEach(item -> selectionMap.put(item, true));
        
        FilterSelectionHelper.invertSelection(items, selectionMap, setter, getter, listView);
        
        assertThat(selectionMap.values())
                .as("All items should be unselected after inversion")
                .allMatch(selected -> !selected);
    }

    @Test
    @DisplayName("Should invert mixed selection")
    void shouldInvertMixedSelection() {
        selectionMap.put("Item1", true);
        selectionMap.put("Item2", false);
        selectionMap.put("Item3", true);
        selectionMap.put("Item4", false);
        selectionMap.put("Item5", true);
        
        FilterSelectionHelper.invertSelection(items, selectionMap, setter, getter, listView);
        
        assertThat(selectionMap.get("Item1"))
                .as("Item1 should be unselected")
                .isFalse();
        assertThat(selectionMap.get("Item2"))
                .as("Item2 should be selected")
                .isTrue();
        assertThat(selectionMap.get("Item3"))
                .as("Item3 should be unselected")
                .isFalse();
        assertThat(selectionMap.get("Item4"))
                .as("Item4 should be selected")
                .isTrue();
        assertThat(selectionMap.get("Item5"))
                .as("Item5 should be unselected")
                .isFalse();
    }

    @Test
    @DisplayName("Should handle empty item list for invert selection")
    void shouldHandleEmptyItemListForInvertSelection() {
        List<String> emptyItems = new ArrayList<>();
        
        FilterSelectionHelper.invertSelection(emptyItems, selectionMap, setter, getter, listView);
        
        assertThat(selectionMap)
                .as("Selection map should exist")
                .isNotNull();
    }

    @Test
    @DisplayName("Should select items matching predicate")
    void shouldSelectItemsMatchingPredicate() {
        FilterSelectionHelper.selectFiltered(items, selectionMap, setter, 
            item -> item.contains("1") || item.contains("3"), listView);
        
        assertThat(selectionMap.get("Item1"))
                .as("Item1 should be selected")
                .isTrue();
        assertThat(selectionMap.get("Item2"))
                .as("Item2 should not be selected")
                .isFalse();
        assertThat(selectionMap.get("Item3"))
                .as("Item3 should be selected")
                .isTrue();
        assertThat(selectionMap.get("Item4"))
                .as("Item4 should not be selected")
                .isFalse();
        assertThat(selectionMap.get("Item5"))
                .as("Item5 should not be selected")
                .isFalse();
    }

    @Test
    @DisplayName("Should select all items when predicate matches all")
    void shouldSelectAllItemsWhenPredicateMatchesAll() {
        FilterSelectionHelper.selectFiltered(items, selectionMap, setter, 
            item -> true, listView);
        
        assertThat(selectionMap.values())
                .as("All items should be selected")
                .allMatch(selected -> selected);
    }

    @Test
    @DisplayName("Should select no items when predicate matches none")
    void shouldSelectNoItemsWhenPredicateMatchesNone() {
        FilterSelectionHelper.selectFiltered(items, selectionMap, setter, 
            item -> false, listView);
        
        assertThat(selectionMap.values())
                .as("No items should be selected")
                .allMatch(selected -> !selected);
    }

    @Test
    @DisplayName("Should handle empty item list for select filtered")
    void shouldHandleEmptyItemListForSelectFiltered() {
        List<String> emptyItems = new ArrayList<>();
        
        FilterSelectionHelper.selectFiltered(emptyItems, selectionMap, setter, 
            item -> true, listView);
        
        assertThat(selectionMap)
                .as("Selection map should exist")
                .isNotNull();
    }

    @Test
    @DisplayName("Should unselect items matching predicate")
    void shouldUnselectItemsMatchingPredicate() {
        items.forEach(item -> selectionMap.put(item, true));
        
        FilterSelectionHelper.unselectFiltered(items, selectionMap, setter, 
            item -> item.contains("2") || item.contains("4"), listView);
        
        assertThat(selectionMap.get("Item1"))
                .as("Item1 should remain selected")
                .isTrue();
        assertThat(selectionMap.get("Item2"))
                .as("Item2 should be unselected")
                .isFalse();
        assertThat(selectionMap.get("Item3"))
                .as("Item3 should remain selected")
                .isTrue();
        assertThat(selectionMap.get("Item4"))
                .as("Item4 should be unselected")
                .isFalse();
        assertThat(selectionMap.get("Item5"))
                .as("Item5 should remain selected")
                .isTrue();
    }

    @Test
    @DisplayName("Should unselect all items when predicate matches all")
    void shouldUnselectAllItemsWhenPredicateMatchesAll() {
        items.forEach(item -> selectionMap.put(item, true));
        
        FilterSelectionHelper.unselectFiltered(items, selectionMap, setter, 
            item -> true, listView);
        
        assertThat(selectionMap.values())
                .as("All items should be unselected")
                .allMatch(selected -> !selected);
    }

    @Test
    @DisplayName("Should unselect no items when predicate matches none")
    void shouldUnselectNoItemsWhenPredicateMatchesNone() {
        items.forEach(item -> selectionMap.put(item, true));
        
        FilterSelectionHelper.unselectFiltered(items, selectionMap, setter, 
            item -> false, listView);
        
        assertThat(selectionMap.values())
                .as("All items should remain selected")
                .allMatch(selected -> selected);
    }

    @Test
    @DisplayName("Should handle empty item list for unselect filtered")
    void shouldHandleEmptyItemListForUnselectFiltered() {
        List<String> emptyItems = new ArrayList<>();
        
        FilterSelectionHelper.unselectFiltered(emptyItems, selectionMap, setter, 
            item -> true, listView);
        
        assertThat(selectionMap)
                .as("Selection map should exist")
                .isNotNull();
    }
}
