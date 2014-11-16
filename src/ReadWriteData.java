import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class ReadWriteData {

	public void writeData(TreeMap<String, HashMap<Integer, Postings>> dataMap,
			String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName, true);
		Set<String> words = dataMap.keySet();
		StringBuilder entry = null;
		HashMap<Integer, Postings> docList = null;
		for (String word : words) {
			if (word.length() > 0) {
				entry = new StringBuilder();
				docList = dataMap.get(word);
				entry.append(word + "-");
				Set<Integer> idSet = docList.keySet();
				Postings p = null;
				int i = 0;
				for (Integer docID : idSet) {
					i++;
					p = docList.get(docID);
					entry.append(docID);
					if (p.t != 0)
						entry.append("t" + p.t);
					if (p.i != 0)
						entry.append("i" + p.i);
					if (p.b != 0)
						entry.append("b" + p.b);
					if (p.c != 0)
						entry.append("c" + p.c);
					if (p.e != 0)
						entry.append("e" + p.e);
					if (i != idSet.size())
						entry.append("|");
				}
				fw.write(entry + "\n");
			}
		}
		fw.close();
	}

}
