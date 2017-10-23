package scripts;

import java.util.regex.Pattern;

public class TEST {

	public static void main(String[] args) {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory());
		System.out.println(java.lang.Runtime.getRuntime().totalMemory());
		System.out.println(java.lang.Runtime.getRuntime().availableProcessors());
		
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

}
