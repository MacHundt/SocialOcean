package scripts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class TEST {

	public static void main(String[] args) {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory());
		System.out.println(java.lang.Runtime.getRuntime().totalMemory());
		System.out.println(java.lang.Runtime.getRuntime().availableProcessors());
		
		
		String token = "Test:Me,";
		token = token.replaceAll("[,:]", "");
		System.out.println(token);
		
		String date = "2013-04-24";
		Pattern datePattern = Pattern.compile("[1-2][0-9]{3}-[0-1][0-9]-[0-3][0-9]");
		if (Pattern.matches(datePattern.pattern(), date)) {
			System.out.println("TRUE");
		}
		else
			System.out.println("FALSE");
		
		String time = "33:45:17";
		Pattern timePattern = Pattern.compile("[0-2][0-9]:[0-6][0-9]:[0-9][0-9]");
		if (Pattern.matches(timePattern.pattern(), time)) {
			System.out.println("TRUE");
		}
		else
			System.out.println("FALSE");
		
		
		String tzid = "UTC";
	    TimeZone tz = TimeZone.getTimeZone(ZoneOffset.UTC);

	    ZoneOffset off = ZoneOffset.UTC;
//	    off.getLong(field)
	    
	    long utc = 1365905080;  // supply your timestamp here
	  
	    Date d = new Date(utc*1000L); 

	    // timezone symbol (z) included in the format pattern for debug
	    DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a z");

	    // format date in default timezone
	    System.err.println(format.format(d));

	    // format date in target timezone
	    format.setTimeZone(tz);
	    System.err.println(format.format(d));
		
		
		Pattern p = Pattern.compile("[^a-zA-z0-9+-=._/*(),@'$:;&#!?]");
//		String http = "http"
		String http = "https://www.overleaf.com/971392~5nvkzvfxgqqgv#/43483150/";
		if (Pattern.matches(p.pattern(), http)) {
			System.out.println("FALSE");
			http = http.replaceFirst(p.pattern(), " ");
			http = http.substring(0, http.indexOf(" "));
		}
		else
			System.out.println("TRUE");
		
	
		System.out.println(http);
		
		
//		String s = "Nuku'alofa";
//		String query = "Select user_id, user_timezone from test where user_timezone = '" + s +"'";
		
		
//		String name = "??? Hugo Arnold ?* ";
//		name = name.replaceAll("[^ a-zA-Z']", "");
//		
//		System.out.println(name.trim());
//		
//	
//		
//		name = "R A N A";
////		name = "a ";
//		
//		if (!name.endsWith(" "))
//			name +=" ";
//		
//		if (Pattern.matches("(([a-zA-Z]{1})([ ]{1}))*", name)) {
//			name = name.replaceAll(" ", "");
//			System.out.println(name);
//		}
//		
//		name ="'happ";
//		if (name.startsWith("'")) {
//			name = name.substring(1, name.length());
//			System.out.println(name);
//		}
		
	}
	
	public static long getLocalToUtcDelta() {
	    Calendar local = Calendar.getInstance();
	    local.clear();
	    local.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
	    return local.getTimeInMillis();
	}

}
