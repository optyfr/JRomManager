/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An abstract base class that encapsulates a list of game code names.
 * <p>
 * This class implements the {@link java.util.List} interface by delegating all
 * operations to an underlying {@link java.util.ArrayList}. It provides a
 * reusable base for specific types of game lists, such as category listings or
 * player mode listings.
 * </p>
 * 
 * @author optyfr
 * @since 1.0
 */
abstract class GamesList implements List<String> {
    /**
     * The underlying list containing the code names of the games.
     */
    protected final List<String> games = new ArrayList<>();

    /**
     * Default protected constructor for subclasses to initialize the games list.
     */
    protected GamesList() {
    }

    /**
     * Returns an iterator over the game code names in this list.
     * 
     * @return an {@link Iterator} over the elements in this list in proper sequence
     */
    @Override
    public Iterator<String> iterator() {
        return games.iterator();
    }

    /**
     * Returns the number of game code names in this list.
     * 
     * @return the number of game code names in this list
     */
    @Override
    public int size() {
        return games.size();
    }

    /**
     * Returns {@code true} if this list contains no game code names.
     * 
     * @return {@code true} if this list contains no game code names, otherwise
     *         {@code false}
     */
    @Override
    public boolean isEmpty() {
        return games.isEmpty();
    }

    /**
     * Returns {@code true} if this list contains the specified game code name.
     * 
     * @param o element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element, otherwise
     *         {@code false}
     */
    @Override
    public boolean contains(final Object o) {
        return games.contains(o);
    }

    /**
     * Returns an array containing all of the game code names in this list in proper
     * sequence.
     * 
     * @return an array containing all of the game code names in this list
     */
    @Override
    public Object[] toArray() {
        return games.toArray();
    }

    /**
     * Returns an array containing all of the game code names in this list in proper
     * sequence; the runtime type of the returned array is that of the specified
     * array.
     * 
     * @param <T> the component type of the array to contain the collection
     * @param a   the array into which the elements of this list are to be stored,
     *            if it is big enough; otherwise, a new array of the same runtime
     *            type is allocated for this purpose.
     * @return an array containing the game code names of this list
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return games.toArray(a);
    }

    /**
     * Appends the specified game code name to the end of this list.
     * 
     * @param e game code name to be appended to this list
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean add(final String e) {
        return games.add(e);
    }

    /**
     * Removes the first occurrence of the specified game code name from this list,
     * if it is present.
     * 
     * @param o game code name to be removed from this list, if present
     * @return {@code true} if this list contained the specified element, otherwise
     *         {@code false}
     */
    @Override
    public boolean remove(final Object o) {
        return games.remove(o);
    }

    /**
     * Returns {@code true} if this list contains all of the elements of the
     * specified collection.
     * 
     * @param c collection to be checked for containment in this list
     * @return {@code true} if this list contains all of the elements of the
     *         specified collection
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return games.containsAll(c);
    }

    /**
     * Appends all of the elements in the specified collection to the end of this
     * list, in the order that they are returned by the specified collection's
     * iterator.
     * 
     * @param c collection containing game code names to be added to this list
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean addAll(final Collection<? extends String> c) {
        return games.addAll(c);
    }

    /**
     * Inserts all of the elements in the specified collection into this list at the
     * specified position.
     * 
     * @param index index at which to insert the first element from the specified
     *              collection
     * @param c     collection containing game code names to be inserted into this
     *              list
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends String> c) {
        return games.addAll(index, c);
    }

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection.
     * 
     * @param c collection containing game code names to be removed from this list
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return games.removeAll(c);
    }

    /**
     * Retains only the elements in this list that are contained in the specified
     * collection.
     * 
     * @param c collection containing game code names to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return games.retainAll(c);
    }

    /**
     * Removes all of the elements from this list.
     */
    @Override
    public void clear() {
        games.clear();
    }

    /**
     * Returns the game code name at the specified position in this list.
     * 
     * @param index index of the element to return
     * @return the game code name at the specified position in this list
     */
    @Override
    public String get(final int index) {
        return games.get(index);
    }

    /**
     * Replaces the game code name at the specified position in this list with the
     * specified element.
     * 
     * @param index   index of the element to replace
     * @param element game code name to be stored at the specified position
     * @return the game code name previously at the specified position
     */
    @Override
    public String set(final int index, final String element) {
        return games.set(index, element);
    }

    /**
     * Inserts the specified game code name at the specified position in this list.
     * 
     * @param index   index at which the specified element is to be inserted
     * @param element game code name to be inserted
     */
    @Override
    public void add(final int index, final String element) {
        games.add(index, element);
    }

    /**
     * Removes the game code name at the specified position in this list.
     * 
     * @param index the index of the element to be removed
     * @return the game code name previously at the specified position
     */
    @Override
    public String remove(final int index) {
        return games.remove(index);
    }

    /**
     * Returns the index of the first occurrence of the specified game code name in
     * this list, or -1 if this list does not contain the element.
     * 
     * @param o element to search for
     * @return the index of the first occurrence of the specified element, or -1 if
     *         not found
     */
    @Override
    public int indexOf(final Object o) {
        return games.indexOf(o);
    }

    /**
     * Returns the index of the last occurrence of the specified game code name in
     * this list, or -1 if this list does not contain the element.
     * 
     * @param o element to search for
     * @return the index of the last occurrence of the specified element, or -1 if
     *         not found
     */
    @Override
    public int lastIndexOf(final Object o) {
        return games.lastIndexOf(o);
    }

    /**
     * Returns a list iterator over the game code names in this list.
     * 
     * @return a list iterator over the game code names in this list
     */
    @Override
    public ListIterator<String> listIterator() {
        return games.listIterator();
    }

    /**
     * Returns a list iterator over the game code names in this list, starting at
     * the specified position.
     * 
     * @param index index of the first element to be returned from the list iterator
     * @return a list iterator over the game code names in this list, starting at
     *         the specified position
     */
    @Override
    public ListIterator<String> listIterator(final int index) {
        return games.listIterator(index);
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * 
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex   high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     */
    @Override
    public List<String> subList(final int fromIndex, final int toIndex) {
        return games.subList(fromIndex, toIndex);
    }

}
