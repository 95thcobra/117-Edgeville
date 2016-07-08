package edgeville.services.serializers;

/**
 * @author Simon on 4-3-2015.
 */
public enum PlayerLoadResult {

	OK(2), INVALID_DETAILS(3), BANNED(4), ALREADY_ONLINE(5) ,WORLD_FULL(7), ERROR_LOADING(24);

	private int code;

	PlayerLoadResult(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

}
