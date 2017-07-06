package coolsquid.react.util;

public class Util {

	public static double getRelativeNumber(Object number, double oldNumber) {
		if (number instanceof String) {
			String s = ((String) number).trim();
			if (s.startsWith("~")) {
				if (s.length() > 1) {
					return oldNumber + Double.parseDouble(s.substring(1));
				} else {
					return oldNumber;
				}
			}
		} else if (number instanceof Number) {
			return ((Number) number).doubleValue();
		}
		Log.error("Failed to parse parameter: %s", number);
		throw new RuntimeException("Failed to parse parameter");
	}
}