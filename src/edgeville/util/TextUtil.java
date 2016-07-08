package edgeville.util;

import org.apache.commons.lang3.StringUtils;

public class TextUtil {
	public enum Colors {
		BLACK("000000"), BLUE("0066ff"), RED("FF0000");

		private String hex;

		Colors(String hex) {
			this.hex = hex;
		}
	}

	public static String colorString(String text, Colors color) {
		return getSyntax(color) + text + getSyntax(Colors.BLACK);
	}

	private static String getSyntax(Colors color) {
		return "<col=" + color.hex + ">";
	}

	public static String formatEnum(String text) {
		return StringUtils.capitalize(text.replace("_", " ").toLowerCase());
	}
}
