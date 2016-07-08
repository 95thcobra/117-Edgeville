package edgeville.util;

import java.lang.ref.WeakReference;

/**
 * @author Simon on 10/6/2014.
 */
public class WeakOptional<T> {

	private WeakReference<T> optional;

	public WeakOptional(T object) {
		optional = new WeakReference<T>(object);
	}

	public boolean present() {
		return optional.get() != null;
	}

	public T get() {
		return optional.get();
	}

}
