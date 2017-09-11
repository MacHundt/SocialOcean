package utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.FileLocator;

public class FilesUtil {
	
	private static String referenceFile = "graphs/graphURI.txt";;    // a reference File to get the path you need  // a hack :/
	
	public static String getReferenceFile() {
		return referenceFile;
	}

	public void setReferenceFile(String referenceFile) {
		this.referenceFile = referenceFile;
	}
	
	
	public static BufferedImage readIconFile(String fileName) {
		URL url = null;
		BufferedImage image = null;
		try {
			url = new URL("platform:/plugin/"
		    + "SocialOcean/"
		    + fileName);

		    } catch (MalformedURLException e1) {
		      e1.printStackTrace();
		    }
		
		try {
			url = FileLocator.toFileURL(url);
			InputStream input = new FileInputStream(new File(url.getPath()));
			image = ImageIO.read(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return image;
	}
	
	

	public static String readTextFile(String fileName) throws IOException {
		URL url = null;
		try {
		  url = new URL("platform:/plugin/"
		    + "SocialOcean/"
		    + fileName);

		    } catch (MalformedURLException e1) {
		      e1.printStackTrace();
		}
		url = FileLocator.toFileURL(url);
		InputStream input = new FileInputStream(new File(url.getPath()));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		
		String output = "";
		while (br.ready()) {
			output += br.readLine();
		}
		
		return output;
	}

	public static List<String> readTextFileByLines(String fileName) throws IOException {
		URL url = null;
		try {
		  url = new URL("platform:/plugin/"
		    + "SocialOcean/"
		    + fileName);

		    } catch (MalformedURLException e1) {
		      e1.printStackTrace();
		}
		url = FileLocator.toFileURL(url);
		InputStream input = new FileInputStream(new File(url.getPath()));
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		List<String> output = new ArrayList<>();
		while (br.ready()) {
			output.add(br.readLine());
		}
		return output;
	}
	
	
	public static String getPathOfRefFile() throws IOException {
		URL url = null;
		try {
		  url = new URL("platform:/plugin/"
		    + "SocialOcean/"
		    + referenceFile);

		    } catch (MalformedURLException e1) {
		      e1.printStackTrace();
		}
		url = FileLocator.toFileURL(url);
		
		return url.getPath();
	}

	
	
	public static void writeToTextFile(String fileName, String content) throws IOException {
		
		URL url = null;
		try {
		  url = new URL("platform:/plugin/"
		    + "SocialOcean/"
		    + referenceFile);

		    } catch (MalformedURLException e1) {
		      e1.printStackTrace();
		}
//		Files.write(path, content.getBytes(), StandardOpenOption.CREATE);
		url = FileLocator.toFileURL(url);
		
		String path = url.getPath();
		path = path.replaceAll(referenceFile, fileName);
		
		File file = new File(path);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file);
		
		// get the content in bytes
		byte[] contentInBytes = content.getBytes();
		
		out.write(contentInBytes);
		out.flush();
		out.close();
		
		System.out.println("Writen "+fileName+" ... DONE");
		
	}

}