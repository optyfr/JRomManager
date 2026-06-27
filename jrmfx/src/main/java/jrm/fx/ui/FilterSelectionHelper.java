package jrm.fx.ui;

import java.util.function.Predicate;

import javafx.scene.control.ListView;
import jrm.fx.ui.profile.ProfileViewer;

/**
 * Utility class for handling filter list selection operations.
 */
final class FilterSelectionHelper {
	private FilterSelectionHelper() {
		// utility class
	}

	@FunctionalInterface
	interface SelectionSetter<T, P> {
		void setSelected(T item, P profile, boolean selected);
	}

	@FunctionalInterface
	interface SelectionGetter<T, P> {
		boolean isSelected(T item, P profile);
	}

	/**
	 * Select all items in a filter list.
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
