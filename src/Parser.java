import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Parser {
	private StemWord stemWrd = new StemWord();

	private TreeMap<String, HashMap<Integer, Postings>> dataMap = null;
	private HashMap<Integer, Postings> hm = null;

	private List<String> catogeryWords = null;
	private HashSet<String> stopWordList = null;

	private String[] titleWords = null;
	private String[] bodyWords = null;
	private String[] extWords = null;
	private String[] infoWords = null;

	private String bodyText = "";
	private String extLinksText = "";

	private StringBuilder infoBoxText = null;
	private StringBuilder textData = null;
	int infoBoxEndPointer = 0;
	HandleStopWords handleStopWords = new HandleStopWords();
	ReadWriteData readWriteData = new ReadWriteData();
	TokenizeDoc tokenizeDoc = new TokenizeDoc();
	private Postings p = null;
	String title = "";
	String indexFolder = "";
	String indexFile = "";
	FileWriter titleWriter = null;
	int noOfDc = 0;
	int id = 0;
	int bracketCount = 0;
	int typ = 2;

	public Parser(String indexFolder) throws IOException {
		this.indexFile = indexFolder + "/Index";
		this.indexFolder = indexFolder;
		File dir = new File(indexFolder);
		delete(dir);
		if (!dir.isDirectory() || !dir.exists()) {
			dir.mkdirs();
		}
		this.titleWriter = new FileWriter(indexFolder + "/TitleList", true);
	}

	public Parser() {
	}

	public DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			boolean handleText = false;
			boolean isTitle = false;
			boolean inPage = false;
			boolean isId = false;
			boolean isRevision = false;

			public void startDocument() throws SAXException {

			}

			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				if (qName.equalsIgnoreCase("page")) {
					inPage = true;
				}
				if (qName.equalsIgnoreCase("title") && inPage) {
					isTitle = true;
				}
				if (qName.equalsIgnoreCase("id") && !isRevision) {
					noOfDc++;
					isId = true;
				}
				if (qName.equalsIgnoreCase("revision")) {
					isRevision = true;
				}
				if (qName.equalsIgnoreCase("text") && isRevision) {
					typ = 2;
					bracketCount = 0;
					handleText = true;
				}
			}

			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (qName.equalsIgnoreCase("page")) {
					inPage = false;
				}
				if (qName.equalsIgnoreCase("revision")) {
					isRevision = false;
				}
				if (qName.equalsIgnoreCase("title")) {

				}
				if (qName.equalsIgnoreCase("text")) {
					handleText = false;
					try {
						titleWriter.write(id + "-" + title + "\n");
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
					try {
						tokenizeData();
						textData = null;
						infoBoxText = null;
					} catch (HandleUserException e) {
						System.out.println(e.getMessage());
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
				}
			}

			public void characters(char ch[], int start, int length)
					throws SAXException {
				if (isTitle) {
					title = (new String(ch, start, length));
					isTitle = false;
				}
				if (isId) {
					id = Integer.parseInt(new String(ch, start, length));
					textData = new StringBuilder();
					infoBoxText = new StringBuilder();
					isId = false;
				}

				if (handleText) {

					for (int i = start; i < length; i++) {
						switch (ch[i]) {
						case '}':
							bracketCount--;
							break;
						case '{':
							bracketCount++;
							break;
						default:
							if (typ == 2 && bracketCount == 2)
								infoBoxText.append(ch[i]);
							else if (ch[i] != 10 && ch[i] < 33 && ch[i] > 126)
								textData.append(' ');
							else
								textData.append(ch[i]);
						}
						if (bracketCount == 0) {
							bracketCount = 3;
							typ = 0;
						}
					}
				}
			}

			public void endDocument() throws SAXException {

			}
		};
		return handler;
	}

	public void parseData(File file) throws ParserConfigurationException,
			SAXException, IOException, HandleUserException {
		stopWordList = handleStopWords.readStopWords("Stopword");
		InputStream inps = new FileInputStream(file);
		InputSource is = new InputSource(inps);
		// is.setEncoding("UTF-8");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(is, getHandler());
		writeDate2File();
		File conf = new File("index.conf");
		FileWriter fos = null;
		if (conf.exists())
			conf.delete();
		conf.createNewFile();
		fos = new FileWriter(conf);
		String absoluteIndexFilePath = "IndexPath:" + indexFolder;
		fos.write(absoluteIndexFilePath+"\n");
		fos.write("Total Docs:" + noOfDc);
		fos.close();
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String r1, String r2) {
				return r1.compareTo(r2);
			}
		};
		titleWriter.close();

		ExternalSort.mergeSortedFiles(indexFile, indexFile + "_p", comparator);

	}

	public void tokenizeData() throws HandleUserException, IOException {
		if (dataMap == null)
			dataMap = new TreeMap<String, HashMap<Integer, Postings>>();
		infoBoxEndPointer = 0;
		parseInfoBox(textData.toString());
		if (infoBoxText != null) {
			infoWords = tokenizeDoc.splitWords(infoBoxText.toString());
			for (String temp : infoWords) {
				updatePosting(temp, 2);
			}
		}
		if (bodyText != null) {

			bodyWords = tokenizeDoc.splitWords(bodyText);
			for (String temp : bodyWords) {
				updatePosting(temp, 3);
			}
		}
		if (extLinksText != null) {

			extLinksText = retrieveCatogeryWords(extLinksText);
			extWords = tokenizeDoc.splitWords(extLinksText);
			for (String temp : extWords) {
				updatePosting(temp, 4);
			}
			for (String temp : catogeryWords) {
				updatePosting(temp, 5);
			}
		}
		titleWords = tokenizeDoc.splitWords(title);
		for (String temp : titleWords) {
			updatePosting(temp, 1);
		}
		if (dataMap.size() >= 100000) {
			writeDate2File();
		}
	}

	private void parseInfoBox(String inputString) {
		String endString = "==External links==";
		int endStrlen = endString.length();
		// startPos = inputString.indexOf(endString);
		int startPos = inputString.indexOf(endString);
		if (startPos > 0 && (startPos + 1 + endStrlen) <= inputString.length()) {
			bodyText = inputString.substring(0, startPos);
			extLinksText = inputString.substring(startPos + 1 + endStrlen);
		} else
			bodyText = inputString;
	}

	public void delete(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				if (file.list().length == 0) {
					file.delete();
				} else {
					String files[] = file.list();
					for (String temp : files) {
						File fileDelete = new File(file, temp);
						delete(fileDelete);
					}
					if (file.list().length == 0) {
						file.delete();
					}
				}
			} else {
				file.delete();
			}
		}
	}

	public String removeUrl(String input) {
		String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http|[a-z]+):((//)|(\\\\))+[A-Za-z0-9:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern p = Pattern.compile(urlPattern, Pattern.MULTILINE);
		Matcher m = p.matcher(input);
		for (int i = 0; i < m.groupCount(); i++) {
			if (m.find() && m.group(i) != null) {
				try {
					input = input.replace(m.group(i), "");
				} catch (Exception e) {
					continue;
				}
			}
		}
		return input;
	}

	public void updatePosting(String word, int type) {
		if (!stopWordList.contains(word)) {
			if (word.length() >= 3) {
				word = stemWrd.stem(word);
				if (!stopWordList.contains(word)) {
					if (!dataMap.containsKey(word)) {
						p = new Postings();
						hm = new HashMap<Integer, Postings>();
					} else {
						hm = dataMap.get(word);
						p = hm.get(id) != null ? hm.get(id) : new Postings();
						//p = hm.get(noOfDc) != null ? hm.get(noOfDc)
						//		: new Postings();
					}
					if (type == 1)
						p.t++;
					if (type == 2)
						p.i++;
					if (type == 3)
						p.b++;
					if (type == 4)
						p.e++;
					if (type == 5)
						p.c++;
					hm.put(id, p);
					//hm.put(noOfDc, p);
					dataMap.put(word, hm);
				}
			}
		}
	}

	public String retrieveCatogeryWords(String inpString) {
		String extT = "";
		// System.out.println(inpString);
		String regex = "\r\n|[\r\n]";
		catogeryWords = new ArrayList<String>();
		String[] words = inpString.split(regex);
		String temp = "";
		for (int i = 0; i < words.length; i++) {
			temp = words[i];
			String[] temp1 = temp.split(":");
			if (temp.indexOf(':') > 0) {
				if (temp1[0].equalsIgnoreCase("[[category") && temp1.length > 1) {
					for (String s : temp1[1].split("[^a-zA-Z]+"))
						catogeryWords.add(s);
				} else
					extT += removeUrl(words[i]) + "\n";
			}
		}
		return extT;
	}

	public void writeDate2File() throws IOException {
		readWriteData.writeData(dataMap, indexFile);
		dataMap = null;
		titleWords = null;
		catogeryWords = null;
		bodyWords = null;
		infoWords = null;
		extWords = null;
		infoBoxText = null;
		bodyText = null;
		extLinksText = null;
		textData = null;
		System.gc();
	}
}
