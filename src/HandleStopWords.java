import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class HandleStopWords {

	static File f = null;

	public  HashSet<String> readStopWords(String stopWordsFilename)
			throws HandleUserException {
		HashSet<String> stopWordsList = new HashSet<String>();

		f = new File(stopWordsFilename);
		
		if (!f.exists()) {
			throw new HandleUserException("No Stopword file is found");
		}
		Scanner stopWordsFile;
		try {
			stopWordsFile = new Scanner(f);
		} catch (FileNotFoundException e) {
			throw new HandleUserException("No Stopword file is found");
		}
		while (stopWordsFile.hasNext())
			stopWordsList.add(stopWordsFile.next());
		stopWordsFile.close();
		return stopWordsList;
	}


}
