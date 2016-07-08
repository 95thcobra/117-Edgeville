package edgeville.net.message;

import io.netty.channel.Channel;

/**
 * @author Simon on 8/22/2014.
 */
public class LoginRequestMessage {

	private Channel channel;
	private String username;
	private String password;
	private int[] isaacSeed;
	private int[] crcs;
	private int revision;
	private byte[] random_dat;
	private int displayMode;
	private boolean resizableInterfaces;

	public LoginRequestMessage(Channel channel, String username, String password, int[] isaacSeed, int[] crcs,
	                           int revision, byte[] random_dat, boolean resizableInterfaces) {
		this.channel = channel;
		this.username = username;
		this.password = password;
		this.isaacSeed = isaacSeed;
		this.crcs = crcs;
		this.revision = revision;
		this.random_dat = random_dat;
		this.resizableInterfaces = resizableInterfaces;
	}

	public String username() {
		return username;
	}

	public String password() {
		return password;
	}

	public int[] isaacSeed() {
		return isaacSeed;
	}

	public int[] crcs() {
		return crcs;
	}

	public int revision() {
		return revision;
	}

	public byte[] randomDat() {
		return random_dat;
	}

	public boolean resizableInterfaces() {
		return resizableInterfaces;
	}

	public Channel channel() {
		return channel;
	}

}
