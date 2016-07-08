package edgeville.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import edgeville.model.Entity;
import edgeville.model.Tile;

/**
 * @author Simon on 8/22/2014.
 */
public class EntityList<T extends Entity> {

	private Entity[] entries;
	private List<Entity> entriesList;
	private int size; // Current size, not capacity
	private Random shufflingRandom = new Random();

	public EntityList(int size) {
		entries = new Entity[size];
		entriesList = Arrays.asList(entries);
	}

	public T get(int index) {
		if (index >= 1 && index < entries.length) {
			return (T) entries[index];
		}

		return null;
	}

	public int add(T obj) {
		for (int i=1; i<entries.length; i++) {
			if (entries[i] == null) {
				entries[i] = obj;
				size++;
				return i;
			}
		}

		return -1;
	}

	public boolean remove(T obj) {
		for (int i=1; i<entries.length; i++) {
			if (entries[i] == obj) {
				entries[i] = null;
				size--;
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public T remove(int index) {
		if (index < 1 || index >= entries.length)
			return null;

		T old = (T) entries[index];
		entries[index] = null;

		if (old != null)
			size--;

		return old;
	}

	@SuppressWarnings("unchecked") public void forEach(Consumer<? super T>... actions) {
		int size = entriesList.size();

		for (Consumer<? super T> a : actions) {
			for (int i = 0; i < size; i++) {
				Entity e = entriesList.get(i);
				if (e != null)
					((Consumer<? super Entity>) a).accept(e);
			}
		}
	}

	@SuppressWarnings("unchecked") public void forEachShuffled(Consumer<? super T>... actions) {
		List<Entity> shuffled = new LinkedList<>(entriesList);
		Collections.shuffle(shuffled, shufflingRandom);

		for (Consumer<? super T> a : actions) {
			shuffled.stream().filter(e -> e != null).forEach((Consumer<? super Entity>) a);
		}
	}

	public void forEachWithinDistance(Tile tile, int distance, Consumer<? super T>... actions) {
		for (Consumer<? super T> a : actions)
			entriesList.stream().filter(e -> e != null && e.getTile().distance(tile) <= distance).forEach((Consumer<? super Entity>) a);
	}

	public Stream<T> stream() {
		return (Stream<T>) entriesList.stream();
	}

	public int size() {
		return size;
	}

}
