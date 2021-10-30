
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileIO {
	
	public static final String fileSep = System.getProperty("file.separator");
	public static final String lineSep = System.getProperty("line.separator");
	
	public static ArrayList<String> read(String filename) throws IOException {
		ArrayList<String> output = new  ArrayList<String>();
		Scanner scanner = null;
		try {
			FileReader reader = new FileReader(filename);
			scanner = new Scanner(reader);
			
			while (scanner.hasNextLine())
				output.add(scanner.nextLine());
			
			return output;
		} finally {
			if (scanner != null)
				scanner.close();
		}
	}
	
	public static void write(String filename, ArrayList<String> filedata)  throws IOException {
		FileWriter writer = null;
		try {
			
			writer =  new FileWriter(filename);
			
			for (String line : filedata) {
				writer.write(line);
				writer.write(lineSep);
			}
		} finally {
			if (writer != null)
				writer.close();
		}
	}
	
}
