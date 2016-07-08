package edgeville.combat;

public class Graphic {
	private int id;
	private int delay;
	private int height;

	public Graphic(int id) {
		this(id, 0, 0);
	}

	public Graphic(int id, int height, int delay) {
		this.id = id;
		this.height = height;
		this.delay = delay;
	}

	public int getId() {
		return id;
	}

	public int getDelay() {
		return delay;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return id + " " + delay + " " + height;
	}
}
