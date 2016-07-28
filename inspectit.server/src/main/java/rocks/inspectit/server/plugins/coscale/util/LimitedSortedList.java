/**
 *
 */
package rocks.inspectit.server.plugins.coscale.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * {@link LimitedSortedList} is a {@link List} implementation that has a fixed capacity and that
 * keeps only the largest elements (using the given comparator) in the list if the capacity is
 * reached. If the list ist full and a new element is added to the list, this element is only
 * inserted into the list if it is at least bigger than the last element in the list. In this case,
 * the new element x is inserted between the elements a and b, where a >= x >= b.
 *
 * @author Alexander Wert
 *
 * @param <E>
 *            Type of the elements to add.
 */
public class LimitedSortedList<E> extends ArrayList<E> {

	/**
	 *
	 */
	private static final long serialVersionUID = 7711642200426882524L;

	/**
	 * Capacity of the list.
	 */
	private final int capacity;

	/**
	 * {@link Comparator} to use for ordering of the list elements.
	 */
	private final Comparator<E> comparator;

	/**
	 * Constructor.
	 *
	 * @param capacity
	 *            The capacity of the limited list. The list will contain at most as many elements
	 *            as defined by the capacity.
	 */
	LimitedSortedList(int capacity) {
		super(capacity);
		this.capacity = capacity;
		this.comparator = null;
	}

	/**
	 * Constructor.
	 *
	 * @param capacity
	 *            The capacity of the limited list. The list will contain at most as many elements
	 *            as defined by the capacity.
	 * @param comparator
	 *            The comparator to use for ordering the elements.
	 */
	public LimitedSortedList(int capacity, Comparator<E> comparator) {
		super(capacity);
		this.capacity = capacity;
		this.comparator = comparator;
	}

	/**
	 * {@inheritDoc}
	 */

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(E e) {
		if (null == comparator && !(e instanceof Comparable)) {
			throw new IllegalStateException("Cannot compare elements! Comparator is null and added element is not of type Comparable.");
		}
		int index = size();
		E reference;
		do {
			index--;
			if (index < 0) {
				break;
			}
			reference = get(index);
		} while ((comparator == null ? ((Comparable<E>) e).compareTo(reference) : comparator.compare(e, reference)) > 0);

		int indexToInsert = index + 1;

		if (indexToInsert < capacity) {
			super.add(indexToInsert, e);
			while (size() > capacity) {
				super.remove(capacity);
			}
		}
		return false;
	}

	/**
	 * This method has the same effect as calling {@link LimitedSortedList#add(E)}.
	 *
	 * @param index
	 *            the index parameter will be ignored by this {@link List} implementation.
	 * @param element
	 *            the element to add to the list.
	 */
	@Override
	public void add(int index, E element) {
		this.add(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean result = false;
		for (E e : c) {
			if (add(e)) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * This method has the same effect as calling {@link LimitedSortedList#addAll(Collection)}.
	 *
	 * @param index
	 *            the index parameter will be ignored by this {@link List} implementation.
	 * @param elements
	 *            a collection of elements to add
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> elements) {
		return this.addAll(elements);
	}
}
