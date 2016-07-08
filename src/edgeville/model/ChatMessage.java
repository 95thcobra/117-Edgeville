package edgeville.model;

/**
 * @author Simon on 7/19/15.
 */
public class ChatMessage {

	private int effects;
	private int colors;
	private String text;

	public ChatMessage(String text, int effects, int colors) {
		this.text = text;
		this.effects = effects;
		this.colors = colors;
	}

	public String text() {
		return text;
	}

	public int colors() {
		return colors;
	}

	public int effects() {
		return effects;
	}

}
