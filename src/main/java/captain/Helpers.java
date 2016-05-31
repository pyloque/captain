package captain;

public class Helpers {

	public static boolean isEmpty(String param) {
		return param == null || param.isEmpty();
	}

	public static boolean isInteger(String param) {
		try {
			Integer.parseInt(param);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean isLong(String param) {
		try {
			Long.parseLong(param);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
