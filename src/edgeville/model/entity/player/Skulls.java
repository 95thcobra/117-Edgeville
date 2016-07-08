package edgeville.model.entity.player;

public enum Skulls {
	WHITE_SKUL(0);

	private int skullId;

	Skulls(int skullId) {
		this.skullId = skullId;
	}

	public int getSkullId() {
		return skullId;
	}
}
