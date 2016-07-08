package edgeville.model;

public class ForceMovement {

	public int dx1;
	public int dx2;
	public int dz1;
	public int dz2;
	public int speed1;
	public int speed2;
	public int direction;

	public ForceMovement(int dx, int dy) {
		this(dx, dy, FaceDirection.forTargetTile(dx, dy));
	}

	public ForceMovement(int dx, int dy, int speed1, int speed2) {
		this(dx, dy, speed1, speed2, FaceDirection.forTargetTile(dx, dy));
	}

	public ForceMovement(int dx, int dy, int direction) {
		this(dx, dy, 35, 90, direction);
	}

	public ForceMovement(int dx, int dy, FaceDirection direction) {
		this(dx, dy, 35, 90, direction.bitDirection);
	}

	public ForceMovement(int dx, int dy, int speed1, int speed2, FaceDirection direction) {
		this(dx, dy, speed1, speed2, direction.bitDirection);
	}

	public ForceMovement(int dx, int dy, int speed1, int speed2, int direction) {
		this.dx1 = dx;
		this.dz1 = dy;
		this.speed1 = speed1;
		this.speed2 = speed2;
		this.direction = direction;
	}

}
