package scripts;

import java.util.regex.Pattern;

public class TEST {

	public static void main(String[] args) {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory());
		System.out.println(java.lang.Runtime.getRuntime().totalMemory());
		System.out.println(java.lang.Runtime.getRuntime().availableProcessors());
		
		String name = "??? Hugo Arnold ?* ";
		name = name.replaceAll("[^ a-zA-Z']", "");
		
		System.out.println(name.trim());
		
	
		
		name = "R A N A";
//		name = "a ";
		
		if (!name.endsWith(" "))
			name +=" ";
		
		if (Pattern.matches("(([a-zA-Z]{1})([ ]{1}))*", name)) {
			name = name.replaceAll(" ", "");
			System.out.println(name);
		}
		
		name ="'happ";
		if (name.startsWith("'")) {
			name = name.substring(1, name.length());
			System.out.println(name);
		}
		
			
		
	}

}
