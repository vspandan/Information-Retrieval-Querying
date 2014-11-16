import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class IndexFiles {
	private static String fileName = "";
	private static String indexFolder = "";
	private static File file = null;
	private static Parser parser = null;

	public static void main(String[] args) {
		try {
			if (args.length == 2) {
				fileName = args[0];
				indexFolder = args[1];
				if (!fileName.substring(fileName.length() - 3,
						fileName.length()).equals("xml"))
					throw new HandleUserException(
							"Invalid File. Data file must be XML");
				file = new File(fileName);
				if (!file.exists()) {
					throw new HandleUserException("No Such file exists");
				}
				parser = new Parser(indexFolder);
				parser.parseData(new File(fileName));
			} else if (args.length != 2)
				throw new HandleUserException("Invalied Number of arguments");
		} catch (HandleUserException e) {
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
		} catch (SAXException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

}
