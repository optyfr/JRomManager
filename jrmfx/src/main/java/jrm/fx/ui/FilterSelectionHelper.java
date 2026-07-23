package jrm.fx.ui;

import java.util.function.Predicate;

import javafx.scene.control.ListView;
import jrm.fx.ui.profile.ProfileViewer;

/**
 * Utility class for handling filter list selection operations.
 * <p>
 * This class provides static methods for handling filter list selection operations, such as selecting or deselecting all items in a
 * filter list. The methods use functional interfaces to define how the selection state of each item is managed and retrieved. These
 * methods are designed to work with any type of item and profile, making them flexible and reusable in various scenarios.
 * <p>
 * The class includes two functional interfaces, `SelectionSetter` and `SelectionGetter`, which define the methods for setting and
 * getting the selection state of an item in a filter list. These interfaces are used to encapsulate the logic for managing the
 * selection state, allowing for flexibility in how the selection state is implemented and retrieved for different types of items
 * and profiles. This design promotes code reuse and separation of concerns, making the utility class easier to maintain and extend
 * in the future.
 */
final class FilterSelectionHelper {

	private FilterSelectionHelper() {
		// utility class
	}

	/**
	 * Functional interface to set the selection state of an item in a filter list for a given profile. *
	 * <p>
	 * The method `setSelected` takes three parameters: the item, the profile, and a boolean value indicating whether the item
	 * should be selected or not. This interface is used to encapsulate the logic for setting the selection state of items in a
	 * filter list, allowing for flexibility in how the selection state is managed for different types of items and profiles. *
	 * <p>
	 * This functional interface is designed to work with any type of item and profile, making it reusable in various scenarios
	 * where the selection state of items in a filter list needs to be managed. *
	 * <p>
	 * The method `setSelected` is responsible for setting the selection state of the specified item for the given profile. The
	 * implementation of this method will depend on the specific requirements of the application, such as updating the UI, modifying
	 * data structures, or performing other operations based on the selection state of the items. *
	 * <p>
	 * This functional interface promotes code reuse and separation of concerns, making it easier to maintain and extend the utility
	 * class in the future. *
	 * <p>
	 * The method `setSelected` can be implemented using various techniques, such as updating a flag in the item internal state,
	 * modifying a data structure, or interacting with a database. The exact implementation will depend on the specific use case and
	 * requirements of the application. *
	 * 
	 * @param <T> The type of item in the filter list. *
	 * @param <P> The type of profile associated with the filter list. *
	 */
	@FunctionalInterface
	interface SelectionSetter<T, P> {
		/**
		 * Sets the selection state of the item in the given profile. The specific implementation of this method will depend on the
		 * requirements of the application, such as updating the UI, modifying data structures, or performing other operations based
		 * on the selection state of the items. *
		 * 
		 * @param item The item whose selection state is to be set. *
		 * @param profile The profile associated with the filter list. *
		 * @param selected A boolean value indicating whether the item should be selected or not. *
		 */
		void setSelected(T item, P profile, boolean selected);
	}

	/**
	 * Functional interface to get the selection state of an item in a filter list for a given profile. *
	 * <p>
	 * * The method `isSelected` takes two parameters: the item and the profile. It returns a boolean value indicating whether the
	 * item is selected or not. This interface is used to encapsulate the logic for retrieving the selection state of items in a
	 * filter list, allowing for flexibility in how the selection state is managed for different types of items and profiles. *
	 * <p>
	 * * This functional interface is designed to work with any type of item and profile, making it reusable in various scenarios
	 * where the selection state of items in a filter list needs to be managed. *
	 * <p>
	 * * The method `isSelected` is responsible for determining the selection state of the specified item for the given profile. The
	 * implementation of this method will depend on the specific requirements of the application, such as querying the UI, accessing
	 * data structures, or performing other operations based on the selection state of the items. *
	 * <p>
	 * * This functional interface promotes code reuse and separation of concerns, making it easier to maintain and extend the
	 * utility class in the future. *
	 * <p>
	 * * The method `isSelected` can be implemented using various techniques, such as checking a flag in the item's internal state,
	 * 
	 * @param <T> the type of items in the filter list,
	 * @param <P> the type of profiles associated with the selection state of the items.
	 */
	@FunctionalInterface
	interface SelectionGetter<T, P> {
		/**
		 * The selection state of the specified item for the given profile.
		 * 
		 * @param item The item whose selection state is to be retrieved. *
		 * @param profile The profile associated with the selection state of the items. *
		 * 
		 * @return A boolean value indicating whether the item is selected or not. *
		 */
		boolean isSelected(T item, P profile);
	}

	/**
	 * Select all items in a filter list.
	 * <p>
	 * This method iterates through the provided collection of items and sets their selection state to selected for a specific
	 * profile. The selection state of each item is managed by the provided setter function. After processing all items, the list
	 * view is refreshed to reflect the changes, and the reset counter in the ProfileViewer is incremented to indicate that the
	 * selection state has been modified. This ensures that the UI remains consistent with the updated selection state of the filter
	 * list items.
	 * 
	 * @param items The iterable collection of items in the filter list.
	 * @param profile The profile associated with the selection state of the items.
	 * @param setter The function to set the selection state of an item for a given profile.
	 * @param listView The list view component that displays the filter list. This method will refresh the list view and increment
	 *        the reset counter after performing the selection operation.
	 */
	static <T, P> void selectAll(Iterable<T> items, P profile, SelectionSetter<T, P> setter, ListView<?> listView) {
		for (final var item : items) {
			setter.setSelected(item, profile, true);
		}
		listView.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	/**
	 * Unselect all items in a filter list.
	 * <p>
	 * This method iterates through the provided collection of items and sets their selection state to unselected for a specific
	 * profile. The selection state of each item is managed by the provided setter function. After processing all items, the list
	 * view is refreshed to reflect the changes, and the reset counter in the ProfileViewer is incremented to indicate that the
	 * selection state has been modified. This ensures that the UI remains consistent with the updated selection state of the filter
	 * list items.
	 * 
	 * @param items The iterable collection of items in the filter list.
	 * @param profile The profile associated with the selection state of the items.
	 * @param setter The function to set the selection state of an item for a given profile.
	 * @param listView The list view component that displays the filter list. This method will refresh the list view and increment
	 *        the reset counter after performing the selection operation.
	 */
	static <T, P> void unselectAll(Iterable<T> items, P profile, SelectionSetter<T, P> setter, ListView<?> listView) {
		for (final var item : items) {
			setter.setSelected(item, profile, false);
		}
		listView.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	/**
	 * Invert selection in a filter list.
	 * <p>
	 * This method iterates through the provided collection of items and inverts the selection state of each item for a specific
	 * profile. The selection state of each item is managed by the provided setter and getter functions. After processing all items,
	 * the list view is refreshed to reflect the changes, and the reset counter in the ProfileViewer is incremented to indicate that
	 * the selection state has been modified. This ensures that the UI remains consistent with the updated selection state of the
	 * filter list items.
	 * 
	 * @param items The iterable collection of items in the filter list. * @param profile The profile associated with the selection
	 *        state of the items.
	 * @param setter The function to set the selection state of an item for a given profile.
	 * @param getter The function to get the current selection state of an item for a given profile.
	 * @param listView The list view component that displays the filter list. This method will refresh the list view and increment
	 *        the reset counter after performing the selection operation.
	 */
	static <T, P> void invertSelection(Iterable<T> items, P profile, SelectionSetter<T, P> setter, SelectionGetter<T, P> getter, ListView<?> listView) {
		for (final var item : items) {
			setter.setSelected(item, profile, !getter.isSelected(item, profile));
		}
		listView.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	/**
	 * Select items matching a predicate.
	 * <p>
	 * This method iterates through the provided collection of items, checks each item against the given filter predicate, and
	 * selects the items that match the filter criteria. The selection state of each item for a specific profile is managed by the
	 * provided setter function. After processing all items, the list view is refreshed to reflect the changes, and the reset
	 * counter in the ProfileViewer is incremented to indicate that the selection state has been modified. This ensures that the UI
	 * remains consistent with the updated selection state of the filter list items.
	 * 
	 * @param items The iterable collection of items in the filter list.
	 * @param profile The profile associated with the selection state of the items.
	 * @param setter The function to set the selection state of an item for a given profile.
	 * @param filter The predicate to determine which items should be selected based on certain criteria.
	 * @param listView The list view component that displays the filter list. This method will refresh the list view and increment
	 *        the reset counter after performing the selection operation.
	 */
	static <T, P> void selectFiltered(Iterable<T> items, P profile, SelectionSetter<T, P> setter, Predicate<T> filter, ListView<?> listView) {
		for (final var item : items) {
			if (filter.test(item)) {
				setter.setSelected(item, profile, true);
			}
		}
		listView.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	/**
	 * Unselect items matching a predicate.
	 * <p>
	 * This method iterates through the provided collection of items, checks each item against the given filter predicate, and
	 * unselects the items that match the filter criteria. The selection state of each item for a specific profile is managed by the
	 * provided setter function. After processing all items, the list view is refreshed to reflect the changes, and the reset
	 * counter in the ProfileViewer is incremented to indicate that the selection state has been modified. This ensures that the UI
	 * remains consistent with the updated selection state of the filter list items.
	 * 
	 * @param items The iterable collection of items in the filter list.
	 * @param profile The profile associated with the selection state of the items.
	 * @param setter The function to set the selection state of an item for a given profile. * @param filter The predicate to
	 *        determine which items should be unselected based on certain criteria.
	 * @param listView The list view component that displays the filter list. This method will refresh the list view and increment
	 *        the reset counter after performing the unselection operation.
	 */
	static <T, P> void unselectFiltered(Iterable<T> items, P profile, SelectionSetter<T, P> setter, Predicate<T> filter, ListView<?> listView) {
		for (final var item : items) {
			if (filter.test(item)) {
				setter.setSelected(item, profile, false);
			}
		}
		listView.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}
}
