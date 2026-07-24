package jrm.fx.ui;

import java.util.function.Predicate;

import javafx.scene.control.ListView;
import jrm.fx.ui.profile.ProfileViewer;

/**
 * Utility class for handling filter list selection operations.
 * <p>
 * Provides static methods for selecting, deselecting, and toggling items in
 * filter lists. Uses functional interfaces {@link SelectionSetter} and
 * {@link SelectionGetter} to abstract selection state management, making the
 * helper reusable with any item and profile type.
 */
final class FilterSelectionHelper {

	private FilterSelectionHelper() {
		// utility class
	}

	/**
	 * Functional interface to set the selection state of an item in a filter list.
	 *
	 * @param <T> the type of item in the filter list
	 * @param <P> the type of profile associated with the filter list
	 */
	@FunctionalInterface
	interface SelectionSetter<T, P> {
		/**
		 * Sets the selection state of the specified item for the given profile.
		 *
		 * @param item     the item whose selection state is to be set
		 * @param profile  the profile associated with the filter list
		 * @param selected {@code true} to select the item, {@code false} to deselect
		 */
		void setSelected(T item, P profile, boolean selected);
	}

	/**
	 * Functional interface to get the selection state of an item in a filter list.
	 *
	 * @param <T> the type of items in the filter list
	 * @param <P> the type of profiles associated with the selection state
	 */
	@FunctionalInterface
	interface SelectionGetter<T, P> {
		/**
		 * Returns the selection state of the specified item for the given profile.
		 *
		 * @param item    the item whose selection state is to be retrieved
		 * @param profile the profile associated with the selection state
		 * @return {@code true} if the item is selected, {@code false} otherwise
		 */
		boolean isSelected(T item, P profile);
	}

	/**
	 * Selects all items in the filter list.
	 * <p>
	 * Iterates through the provided items and sets each to selected for the
	 * given profile. After processing, the list view is refreshed and the reset
	 * counter in {@link ProfileViewer} is incremented.
	 *
	 * @param items    the iterable collection of items in the filter list
	 * @param profile  the profile associated with the selection state
	 * @param setter   the function to set the selection state of an item
	 * @param listView the list view component displaying the filter list
	 */
	static <T, P> void selectAll(Iterable<T> items, P profile, SelectionSetter<T, P> setter, ListView<?> listView) {
		for (final var item : items) {
			setter.setSelected(item, profile, true);
		}
		listView.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	/**
	 * Deselects all items in the filter list.
	 * <p>
	 * Iterates through the provided items and sets each to unselected for the
	 * given profile. After processing, the list view is refreshed and the reset
	 * counter in {@link ProfileViewer} is incremented.
	 *
	 * @param items    the iterable collection of items in the filter list
	 * @param profile  the profile associated with the selection state
	 * @param setter   the function to set the selection state of an item
	 * @param listView the list view component displaying the filter list
	 */
	static <T, P> void unselectAll(Iterable<T> items, P profile, SelectionSetter<T, P> setter, ListView<?> listView) {
		for (final var item : items) {
			setter.setSelected(item, profile, false);
		}
		listView.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	/**
	 * Inverts the selection state of every item in the filter list.
	 * <p>
	 * Iterates through the provided items and toggles each item's selection
	 * state for the given profile. After processing, the list view is refreshed
	 * and the reset counter in {@link ProfileViewer} is incremented.
	 *
	 * @param items    the iterable collection of items in the filter list
	 * @param profile  the profile associated with the selection state
	 * @param setter   the function to set the selection state of an item
	 * @param getter   the function to get the current selection state of an item
	 * @param listView the list view component displaying the filter list
	 */
	static <T, P> void invertSelection(Iterable<T> items, P profile, SelectionSetter<T, P> setter, SelectionGetter<T, P> getter, ListView<?> listView) {
		for (final var item : items) {
			setter.setSelected(item, profile, !getter.isSelected(item, profile));
		}
		listView.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	/**
	 * Selects items in the filter list that match the given predicate.
	 * <p>
	 * Iterates through the provided items, tests each against the filter
	 * predicate, and selects those that match. After processing, the list view
	 * is refreshed and the reset counter in {@link ProfileViewer} is incremented.
	 *
	 * @param items    the iterable collection of items in the filter list
	 * @param profile  the profile associated with the selection state
	 * @param setter   the function to set the selection state of an item
	 * @param filter   the predicate used to determine which items to select
	 * @param listView the list view component displaying the filter list
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
	 * Deselects items in the filter list that match the given predicate.
	 * <p>
	 * Iterates through the provided items, tests each against the filter
	 * predicate, and deselects those that match. After processing, the list view
	 * is refreshed and the reset counter in {@link ProfileViewer} is incremented.
	 *
	 * @param items    the iterable collection of items in the filter list
	 * @param profile  the profile associated with the selection state
	 * @param setter   the function to set the selection state of an item
	 * @param filter   the predicate used to determine which items to deselect
	 * @param listView the list view component displaying the filter list
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
