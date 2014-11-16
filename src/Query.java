import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class QueryT implements Comparable<QueryT> {
	String qT = "";
	char field = 0;
	double score = 0;
	static int differentiator = 0;

	public double getScore() {
		score = 0.4 * p.t + 0.25 * p.i + 0.1 * p.b + 0.2 * p.c + 0.05 * p.e;
		return score;
	}

	Postings p = new Postings();
	HashSet<String> qList = new HashSet<String>();

	@Override
	public String toString() {
		return "qT:" + qT + "-field:" + field + "-score:" + score
				+ "-postings:" + p;
	}

	@Override
	public int compareTo(QueryT arg0) {
		if (arg0.p.t != 0 && this.p.t != 0) {
			if (arg0.p.t < this.p.t)
				differentiator--;
			else
				differentiator++;
			return differentiator;
		} else if (arg0.p.t != 0 && this.p.t == 0) {
			return 1;
		} else if (arg0.p.t == 0 && this.p.t != 0) {
			return -1;
		} else if (arg0.p.t == 0 && this.p.t == 0) {
			if (this.getScore() < arg0.getScore())
				return 1;
			else
				return -1;
		}
		return 0;
	}
}

class ValueComparator implements Comparator {

	Map map;

	public ValueComparator(Map map) {
		this.map = map;
	}

	public int compare(Object keyA, Object keyB) {

		Comparable valueA = (Comparable) map.get(keyA);
		Comparable valueB = (Comparable) map.get(keyB);
		return valueA.compareTo(valueB);

	}
}

public class Query {

	// private static final int THRESHOLD = 50;
	private static final double THRESOLD_IDF = 3.671;
	static Map<String, QueryT> docL = new TreeMap<String, QueryT>();
	static Map<String, String> hm[] = null;
	static Map<String, Integer> docNScores = null;
	static String files[] = null;
	static String indexFilePath = "";
	static int noOfDoc = 0;

	public static void main(String[] args) throws InterruptedException {
		LinkedHashMap<String, String> qL = new LinkedHashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"index.conf"));
			String p[] = br.readLine().split(":");
			indexFilePath = p[1].trim();
			p = br.readLine().split(":");
			int noOfDoc = Integer.parseInt(p[1]);
			br.close();
			if (args.length == 0)
				throw new HandleUserException(
						"Please provide input fields to query");
			StemWord s = new StemWord();
			int count = 0, temp = 0;
			hm = new HashMap[args.length];
			SearchThread st[] = new SearchThread[args.length];
			for (int i = 0; i < args.length; i++) {
				String qTerm = args[i];
				if (qTerm.indexOf(':') > 0) {
					if (qTerm.charAt(1) == ':') {
						char f1 = qTerm.charAt(0);
						if (f1 == 't' || f1 == 'b' || f1 == 'c' || f1 == 'e'
								|| f1 == 'i') {
							qTerm = qTerm.substring(2);
							qTerm = s.stem(qTerm);
							qL.put(qTerm.toLowerCase(), "" + f1);
						} else
							throw new HandleUserException(
									"Invalid Input: No such field");
					} else
						throw new HandleUserException(
								"Invalid Input: Enter plain text or Example \"t:sachin\" ");
				} else {
					qTerm = s.stem(qTerm);
					qL.put(qTerm.toLowerCase(), "");
				}

				temp = count++;
				st[temp] = new SearchThread(qTerm, temp);
				st[temp].start();
				st[temp].join();
			}
			int i = 0;
			String tfData = "";
			Set<String> qTerms = qL.keySet();
			for (String q : qTerms) {
				tfData = hm[i++].get(q);
				char field = qL.get(q).length() > 0 ? qL.get(q).charAt(0) : 0;
				if (tfData != null) {
					String tfD[] = tfData.split("\\|");
					double idf = 0;
					int tf = 0;
					int df = tfD.length;
					idf = Math.log10(noOfDoc / df);
					// if(idf>THRESOLD_IDF||qTerms.size()==1 )
					{
						for (String td : tfD) {
							try {
								Postings posting = new Postings();
								String docId = "";
								int ti = td.indexOf('t'), ibi = td.indexOf('i'), bi = td
										.indexOf('b'), ci = td.indexOf('c'), ei = td
										.indexOf('e');

								if (ti > 0) {
									docId = td.substring(0, ti);
									if (ibi > ti)
										posting.t = Integer.parseInt(td
												.substring(ti + 1, ibi));
									else if (bi > ti)
										posting.t = Integer.parseInt(td
												.substring(ti + 1, bi));
									else if (ci > ti)
										posting.t = Integer.parseInt(td
												.substring(ti + 1, ci));
									else if (ei > ti)
										posting.t = Integer.parseInt(td
												.substring(ti + 1, ei));
									else
										posting.t = Integer.parseInt(td
												.substring(ti + 1));
								}
								if (ibi > 0) {
									if (docId.length() < 1)
										docId = td.substring(0, ibi);
									if (bi > ibi)
										posting.i = Integer.parseInt(td
												.substring(ibi + 1, bi));
									else if (ci > ibi)
										posting.i = Integer.parseInt(td
												.substring(ibi + 1, ci));
									else if (ei > ibi)
										posting.i = Integer.parseInt(td
												.substring(ibi + 1, ei));
									else
										posting.i = Integer.parseInt(td
												.substring(ibi + 1));
								}
								if (bi > 0) {
									if (docId.length() < 1)
										docId = td.substring(0, bi);
									if (ci > bi)
										posting.b = Integer.parseInt(td
												.substring(bi + 1, ci));
									else if (ei > bi)
										posting.b = Integer.parseInt(td
												.substring(bi + 1, ei));
									else
										posting.b = Integer.parseInt(td
												.substring(bi + 1));
								}
								if (ci > 0) {
									if (docId.length() < 1)
										docId = td.substring(0, ci);
									if (ei > ci)
										posting.c = Integer.parseInt(td
												.substring(ci + 1, ei));
									else
										posting.c = Integer.parseInt(td
												.substring(ci + 1));
								}
								if (ei > 0) {
									if (docId.length() < 1)
										docId = td.substring(0, ei);
									posting.e = Integer.parseInt(td
											.substring(ei + 1));
								}
								tf = (posting.t + posting.i + posting.b
										+ posting.c + posting.e);
								if (field != 0)
									if (td.indexOf(field) < 0)
										continue;
								// if (tf <= THRESHOLD)
								{
									QueryT qtt = new QueryT();
									if (!docL.containsKey(docId)) {
										qtt.qList.add(q);
										qtt.p = posting;
										docL.put(docId, qtt);
									} else {
										qtt = docL.get(docId);
										posting.t += qtt.p.t;
										posting.i += qtt.p.i;
										posting.b += qtt.p.b;
										posting.c += qtt.p.c;
										posting.e += qtt.p.e;
										qtt.p = posting;
										docL.put(docId, qtt);
									}
								}
							} catch (NumberFormatException e) {
								continue;
							}
						}
					}

				} else
					System.out.println(" ");
			}
			if (docL.size() > 0) {
				Map sortedMap = new TreeMap(new ValueComparator(docL));
				sortedMap.putAll(docL);
				Set<String> fDocId = sortedMap.keySet();
				Set<String> finalDocId = new LinkedHashSet<String>();
				for (String finalDoc : fDocId) {
					String title = lookUp4Title(finalDoc);
					if (finalDocId.add(title) && title.length() > 0
							&& !title.equals("Anarchism"))
					System.out.println(title);
					if (finalDocId.size() == 11)
						break;
				}
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (HandleUserException e) {
			System.out.println(e.getMessage());
		}
		System.out.println(" ");
	}

	private static String lookUp4Title(String finalDoc) throws IOException {
		// System.out.println(finalDoc);
		RandomAccessFile sf = null;
		long s_len = 0, s_mid = 0, s_end = 0, s_start = 0;
		long len = 0, mid = 0, end = 0, start = 0;
		int ch = 0;
		sf = new RandomAccessFile(indexFilePath + "/TitleList", "r");
		s_end = s_len = sf.length();
		String s = "";
		while (s_start < s_end) {
			s_mid = (s_start + s_end) / 2;
			sf.seek(s_mid);
			while ((ch = sf.read()) != '\n' && ch != -1)
				;
			if (ch == -1)
				break;
			s = sf.readLine();
			if (s == null || s.indexOf('-') == -1)
				break;
			String[] temp = s.split("-");
			if (finalDoc.compareTo(temp[0]) > 0) {
				s_start = s_mid + 1;
			} else if (finalDoc.compareTo(temp[0]) < 0) {
				s_end = s_mid;
			} else
				break;
		}
		sf.seek(s_mid);
		while ((ch = sf.read()) != '\n')
			;
		String temp = sf.readLine();
		String[] tm = null;
		if (temp != null) {
			tm = temp.split("-");
			if (tm != null && tm.length == 2)
				return temp;
		}
		return "";
	}

	/*
	 * THREAD BEGINS HERE
	 */
	static class SearchThread extends Thread {
		RandomAccessFile sf = null;
		RandomAccessFile f = null;
		String qTerm = "";

		int count = 0;

		public SearchThread(String term, int count) {
			this.qTerm = term;
			this.setName(term);
			this.count = count;
			hm[count] = new HashMap<String, String>();
		}

		@Override
		public void run() {
			try {
				long s_len = 0, s_mid = 0, s_end = 0, s_start = 0;
				long len = 0, mid = 0, end = 0, start = 0;
				int ch = 0;
				sf = new RandomAccessFile(indexFilePath + "/Index_s", "r");
				s_end = s_len = sf.length();
				String s = "";
				while (s_start < s_end) {
					s_mid = (s_start + s_end) / 2;
					sf.seek(s_mid);
					while ((ch = sf.read()) != '\n' && ch != -1)
						;
					if (ch == -1)
						break;
					s = sf.readLine();
					if (s == null || s.indexOf('-') == -1)
						break;
					String[] temp = s.split("-");

					// Binary Search Pointer moves
					if (qTerm.compareTo(temp[0]) > 0) {
						s_start = s_mid + 1;
					} else if (qTerm.compareTo(temp[0]) < 0) {
						s_end = s_mid;
					} else
						break;
				}
				sf.seek(s_mid);
				while (sf.read() != '\n')
					;
				String[] temp = sf.readLine().split("-");
				long offset1 = 0, offset2 = 0;
				if (temp[0].compareTo(qTerm) < 0) {
					offset1 = Long.parseLong(temp[1]);
					String t[] = sf.readLine().split("-");
					offset2 = Long.parseLong(t[1]);
				} else {
					long filePointer = sf.getFilePointer() - 3
							- temp[0].length() - temp[1].length();
					while (true) {
						long fp = --filePointer;
						if (fp < 0)
							break;
						sf.seek(fp);
						if (sf.read() == 10)
							break;
					}
					String t[] = sf.readLine().split("-");
					offset1 = Long.parseLong(t[1]);
					offset2 = Long.parseLong(temp[1]);
				}
				sf.close();
				f = new RandomAccessFile(indexFilePath + "/Index_p", "r");
				f.seek(offset1);
				String result[] = null;
				boolean found = false;
				while (true) {
					temp = f.readLine().split("-");
					if (temp[0].compareTo(qTerm) == 0) {
						found = true;
						break;
					}
					if (f.getFilePointer() > offset2)
						break;
				}
				if (found) {
					if (!hm[count].containsKey(this.getName()))
						hm[count].put(this.getName(), temp[1]);
					else {
						String t = hm[count].get(this.getName()) + "" + temp[1];
						hm[count].put(this.getName(), t);
					}
				}
				f.close();
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

	}
}
