package jrm.profile.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

/**
 * A specialized List interface that supports dual-view operations on a collection of elements, providing both a raw, unfiltered
 * view and a filtered, cached view of elements based on the profile settings.
 * <p>
 * This interface is typically used to hold lists of {@link Anyware} elements or related domain model items in a profile, allowing
 * convenient toggle or automatic application of filter parameters (e.g. status, region, etc.) while maintaining the backing set
 * intact.
 * </p>
 *
 * @param <T> the type of elements contained within this list
 * 
 * @author optyfr
 * 
 * @since 1.0
 */
interface AWList<T> extends List<T> {

    /**
     * Returns the raw, unfiltered backing list of elements.
     *
     * @return the unfiltered {@link List} of elements of type {@code T}
     */
    public abstract List<T> getList();

    /**
     * Returns the size of the unfiltered backing list.
     *
     * @return the number of elements in the unfiltered list
     */
    @Override
    public default int size() {
        return getList().size();
    }

    /**
     * Checks if the unfiltered backing list is empty.
     *
     * @return {@code true} if the unfiltered list contains no elements, {@code false} otherwise
     */
    @Override
    public default boolean isEmpty() {
        return getList().isEmpty();
    }

    /**
     * Checks if the unfiltered backing list contains the specified element.
     *
     * @param o the element whose presence in this list is to be tested
     * 
     * @return {@code true} if this list contains the specified element, {@code false} otherwise
     */
    @Override
    public default boolean contains(final Object o) {
        return getList().contains(o);
    }

    /**
     * Returns an iterator over the elements in the unfiltered backing list in proper sequence.
     *
     * @return an {@link Iterator} over the unfiltered elements
     */
    @Override
    public default Iterator<T> iterator() {
        return getList().iterator();
    }

    /**
     * Returns an array containing all of the elements in the unfiltered backing list in proper sequence.
     *
     * @return an array containing all of the unfiltered elements
     */
    @Override
    public default Object[] toArray() {
        return getList().toArray();
    }

    /**
     * Returns an array containing all of the elements in the unfiltered backing list in proper sequence; the runtime type of the
     * returned array is that of the specified array.
     *
     * @param <E> the runtime type of the array to contain the collection
     * @param a the array into which the elements of this list are to be stored, if it is big enough; otherwise, a new array of the
     *        same runtime type is allocated for this purpose
     * 
     * @return an array containing the unfiltered elements
     */
    @Override
    public default <E> E[] toArray(final E[] a) {
        return getList().toArray(a);
    }

    /**
     * Appends the specified element to the end of the unfiltered backing list.
     *
     * @param e the element to be appended to this list
     * 
     * @return {@code true} if the element was successfully added, {@code false} otherwise
     */
    @Override
    public default boolean add(final T e) {
        return getList().add(e);
    }

    /**
     * Removes the first occurrence of the specified element from the unfiltered backing list, if it is present.
     *
     * @param o the element to be removed from this list, if present
     * 
     * @return {@code true} if this list contained the specified element, {@code false} otherwise
     */
    @Override
    public default boolean remove(final Object o) {
        return getList().remove(o);
    }

    /**
     * Checks if the unfiltered backing list contains all of the elements in the specified collection.
     *
     * @param c the collection to be checked for containment in this list
     * 
     * @return {@code true} if this list contains all of the elements in the specified collection, {@code false} otherwise
     */
    @Override
    public default boolean containsAll(final Collection<?> c) {
        return getList().containsAll(c);
    }

    /**
     * Appends all of the elements in the specified collection to the end of the unfiltered backing list, in the order that they are
     * returned by the specified collection's iterator.
     *
     * @param c the collection containing elements to be added to this list
     * 
     * @return {@code true} if this list changed as a result of the call, {@code false} otherwise
     */
    @Override
    public default boolean addAll(final Collection<? extends T> c) {
        return getList().addAll(c);
    }

    /**
     * Inserts all of the elements in the specified collection into the unfiltered backing list at the specified position, shifting
     * subsequent elements to the right.
     *
     * @param index the index at which to insert the first element from the specified collection
     * @param c the collection containing elements to be added to this list
     * 
     * @return {@code true} if this list changed as a result of the call, {@code false} otherwise
     */
    @Override
    public default boolean addAll(final int index, final Collection<? extends T> c) {
        return getList().addAll(index, c);
    }

    /**
     * Removes from the unfiltered backing list all of its elements that are contained in the specified collection.
     *
     * @param c the collection containing elements to be removed from this list
     * 
     * @return {@code true} if this list changed as a result of the call, {@code false} otherwise
     */
    @Override
    public default boolean removeAll(final Collection<?> c) {
        return getList().removeAll(c);
    }

    /**
     * Retains only the elements in the unfiltered backing list that are contained in the specified collection.
     *
     * @param c the collection containing elements to be retained in this list
     * 
     * @return {@code true} if this list changed as a result of the call, {@code false} otherwise
     */
    @Override
    public default boolean retainAll(final Collection<?> c) {
        return getList().retainAll(c);
    }

    /**
     * Removes all of the elements from the unfiltered backing list.
     */
    @Override
    public default void clear() {
        getList().clear();
    }

    /**
     * Returns the element at the specified position in the unfiltered backing list.
     *
     * @param index the index of the element to return
     * 
     * @return the element at the specified position in this list
     */
    @Override
    public default T get(final int index) {
        return getList().get(index);
    }

    /**
     * Replaces the element at the specified position in the unfiltered backing list with the specified element.
     *
     * @param index the index of the element to replace
     * @param element the element to be stored at the specified position
     * 
     * @return the element previously at the specified position
     */
    @Override
    public default T set(final int index, final T element) {
        return getList().set(index, element);
    }

    /**
     * Inserts the specified element at the specified position in the unfiltered backing list. Shifts the element currently at that
     * position and any subsequent elements to the right.
     *
     * @param index the index at which the specified element is to be inserted
     * @param element the element to be inserted
     */
    @Override
    public default void add(final int index, final T element) {
        getList().add(index, element);
    }

    /**
     * Removes the element at the specified position in the unfiltered backing list. Shifts any subsequent elements to the left.
     *
     * @param index the index of the element to be removed
     * 
     * @return the element that was removed from the list
     */
    @Override
    public default T remove(final int index) {
        return getList().remove(index);
    }

    /**
     * Returns the index of the first occurrence of the specified element in the unfiltered backing list, or -1 if this list does
     * not contain the element.
     *
     * @param o the element to search for
     * 
     * @return the index of the first occurrence of the specified element in this list, or -1 if not found
     */
    @Override
    public default int indexOf(final Object o) {
        return getList().indexOf(o);
    }

    /**
     * Returns the index of the last occurrence of the specified element in the unfiltered backing list, or -1 if this list does not
     * contain the element.
     *
     * @param o the element to search for
     * 
     * @return the index of the last occurrence of the specified element in this list, or -1 if not found
     */
    @Override
    public default int lastIndexOf(final Object o) {
        return getList().lastIndexOf(o);
    }

    /**
     * Returns a list iterator over the elements in the unfiltered backing list (in proper sequence).
     *
     * @return a {@link ListIterator} of the elements in this list (in proper sequence)
     */
    @Override
    public default ListIterator<T> listIterator() {
        return getList().listIterator();
    }

    /**
     * Returns a list iterator over the elements in the unfiltered backing list (in proper sequence), starting at the specified
     * position in the list.
     *
     * @param index index of the first element to be returned from the list iterator (by a call to next)
     * 
     * @return a {@link ListIterator} of the elements in this list (in proper sequence), starting at the specified index
     */
    @Override
    public default ListIterator<T> listIterator(final int index) {
        return getList().listIterator(index);
    }

    /**
     * Returns a view of the portion of the unfiltered backing list between the specified {@code fromIndex}, inclusive, and
     * {@code toIndex}, exclusive.
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * 
     * @return a view of the specified range within the backing list
     */
    @Override
    public default List<T> subList(final int fromIndex, final int toIndex) {
        return getList().subList(fromIndex, toIndex);
    }

    /**
     * Retrieves a cached, filtered list of elements.
     * <p>
     * The filters are typically evaluated based on the current active profile filter configuration, such as status filters,
     * visibility preferences, or regional attributes.
     * </p>
     *
     * @return a {@link List} containing only the elements that match the current filter criteria
     */
    public abstract List<T> getFilteredList();

    /**
     * Retrieves a cached, filtered stream of elements.
     * <p>
     * This is a stream-based equivalent of {@link #getFilteredList()}, allowing fluent functional pipelines over the filtered
     * elements.
     * </p>
     *
     * @return a {@link Stream} of filtered elements
     */
    public abstract Stream<T> getFilteredStream();

}
