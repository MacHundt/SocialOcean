package scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Tff_To_CSV {
	
	private static String filename = "subjclueslen";		//without ending
	private static String inPath = "./lexicons/"+filename+".tff";
	private static String outPath = "./lexicons/"+filename+".csv";
	private static String SEPARATOR = ";";
	private static String[] headers;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inPath));
		FileWriter fstream = new FileWriter(outPath);
		BufferedWriter out = new BufferedWriter(fstream);
		try {
		    String line = br.readLine();
		    headers = line.split(" ");
		    String header = "";
		    for (int i = 0; i < headers.length; i++) {
		    	header += headers[i].substring(0, headers[i].indexOf("="))+SEPARATOR;
		    }
		    header = header.substring(0, header.length()-SEPARATOR.length());
		    out.write(header+"\n");
		    out.flush();
		    int count = 0;
		    while (line != null) {
		    	writeLineAsCSV(line, out);
		        line = br.readLine();
		        count++;
		    }
		    System.out.println(count);
		} finally {
		    br.close();
		    out.close();
		}

	}


	private static void writeLineAsCSV(String line, BufferedWriter out) throws IOException {
		// Create file 
		
		line = line.replace(" m ", "");
		String[] cols = line.split(" ");
		String content = "";
		for (int i = 0; i < cols.length; i++) {
			content += cols[i].substring(cols[i].indexOf("=")+1)+SEPARATOR;
		}
		content = content.substring(0, content.length() - SEPARATOR.length());

		out.write(content+"\n");
		
	}

}
