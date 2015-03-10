package swiprolog.language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * class to store a set of elements that do not implement HashCode. We don't
 * extend ArrayList to shield it from use.
 * 
 * @author W.Pasman 10mar15
 *
 * @param <T>
 *            the type of the set elements.
 */
public class SetWithoutHash<T> implements Set<T> {
	private ArrayList<T> set = new ArrayList<T>();

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return set.iterator();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	@Override
	public boolean add(T e) {
		if (!set.contains(e)) {
			return set.add(e);
		}
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return set.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed=false;
		for (T element : c) {
			changed |= add(element);
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return set.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return set.removeAll(c);
	}

	@Override
	public void clear() {
		set.clear();
	}

}
