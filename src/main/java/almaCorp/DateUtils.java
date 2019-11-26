package almaCorp;

import java.util.Date;

public class DateUtils {

	static Date ref = new Date(2500+1900, 1, 1, 0, 0, 0);
	
	public static String createDate(Date date) {
		String res = String.valueOf(ref.getTime()-date.getTime());
		while (res.length()<15) {
			res = "0"+res;
		}
		return res;
	}
	
}
