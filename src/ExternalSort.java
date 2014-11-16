import java.util.*;
import java.io.*;

public class ExternalSort {

	static int linecount = 0;
	static int fileoffset = 0;
	public static long estimateBestSizeOfBlocks(File filetobesorted) {
		long sizeoffile = filetobesorted.length();
		final int MAXTEMPFILES = 1024;
		long blocksize = sizeoffile / MAXTEMPFILES;
		long freemem = Runtime.getRuntime().freeMemory();
		if (blocksize < freemem / 2)
			blocksize = freemem / 2;
		else {
			if (blocksize >= freemem)
				System.err.println("We expect to run out of memory. ");
		}
		return blocksize;
	}

	public static List<File> sortInBatch(File file, Comparator<String> cmp)
			throws IOException {
		List<File> files = new ArrayList<File>();
		BufferedReader fbr = new BufferedReader(new FileReader(file));
		long blocksize = estimateBestSizeOfBlocks(file);// in bytes
		try {
			List<String> tmplist = new ArrayList<String>();
			String line = "";
			try {
				while (line != null) {
					long currentblocksize = 0;// in bytes
					while ((currentblocksize < blocksize)
							&& ((line = fbr.readLine()) != null)) { 
						tmplist.add(line);
						currentblocksize += line.length(); 
					}
					files.add(sortAndSave(tmplist, cmp));
					tmplist.clear();
				}
			} catch (EOFException oef) {
				if (tmplist.size() > 0) {
					files.add(sortAndSave(tmplist, cmp));
					tmplist.clear();
				}
			}
		} finally {
			fbr.close();
		}
		return files;
	}

	public static File sortAndSave(List<String> tmplist, Comparator<String> cmp)
			throws IOException {
		Collections.sort(tmplist, cmp); //
		File newtmpfile = File.createTempFile("sortInBatch", "flatfile");
		newtmpfile.deleteOnExit();
		BufferedWriter fbw = new BufferedWriter(new FileWriter(newtmpfile));
		try {
			for (String r : tmplist) {
				fbw.write(r);
				fbw.newLine();
			}
		} finally {
			fbw.close();
		}
		return newtmpfile;
	}

	public static int mergeSortedFiles(String inputfile, String outputfile,
			final Comparator<String> cmp) throws IOException {
		List<File> files = sortInBatch(new File(inputfile), cmp);
		PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(
				11, new Comparator<BinaryFileBuffer>() {
					public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
						return cmp.compare(i.peek(), j.peek());
					}
				});
		for (File f : files) {
			BinaryFileBuffer bfb = new BinaryFileBuffer(f);
			pq.add(bfb);
		}
		BufferedWriter fbw = new BufferedWriter(new FileWriter(outputfile));
		int rowcounter = 0;
		String[] reg_str1 = null;
		String[] reg_str2 = null;
		String lastLine = null;
		try {
			while (pq.size() > 0) {
				BinaryFileBuffer bfb = pq.poll();
				String r = bfb.pop();
				if (!r.isEmpty()) {
					reg_str1 = r.split("-");
				}
				if (lastLine != null) {
					reg_str2 = lastLine.split("-");
				}
				if (!r.equals(lastLine)) {
					if (!r.isEmpty()) {
						if (reg_str2 != null && reg_str2[0].equals(reg_str1[0])) {
							if ((reg_str2[0] + "|" + reg_str1[1]).split("\\|").length < 3000) {
								fbw.write("|" + reg_str1[1]);
								fileoffset = fileoffset + reg_str1[1].length()
										+ 1;
							}
						} else {
							fbw.newLine();
							if (r.split("\\|").length < 3000) {
								fbw.write(r);
								fileoffset = fileoffset + r.length() + 1;
								linecount++;
							}
						}
					}
					lastLine = r;
				}
				++rowcounter;
				if (bfb.empty()) {
					bfb.fbr.close();
					bfb.originalfile.delete();
				} else {
					pq.add(bfb);
				}
			}
		} finally {
			fbw.close();
			for (BinaryFileBuffer bfb : pq)
				bfb.close();
		}
		File f = new File(inputfile + "_s");
		if (f.exists())
			f.delete();
		f.createNewFile();
		FileWriter fwr = new FileWriter(f, true);
		RandomAccessFile fr = new RandomAccessFile(inputfile + "_p", "r");
		long lineCount1 = 0;
		long presentOffset = 0;
		String temp = "";
		int ch = 0;
		while ((ch = fr.read()) != -1) {
			if (ch == '\n')
				lineCount1++;
			if (lineCount1 % 100 == 0) {
				presentOffset = fr.getFilePointer();
				temp = fr.readLine();
				if (temp == null)
					break;
				String t[] = temp.split("-");
				fwr.write(t[0] + "-" + presentOffset + "\n");
				lineCount1++;
			}
		}
		new File(inputfile).delete();
		return rowcounter;
	}
}

class BinaryFileBuffer {
	public static int BUFFERSIZE = 2048;
	public BufferedReader fbr;
	public File originalfile;
	private String cache;
	private boolean empty;

	public BinaryFileBuffer(File f) throws IOException {
		originalfile = f;
		fbr = new BufferedReader(new FileReader(f), BUFFERSIZE);
		reload();
	}

	public boolean empty() {
		return empty;
	}

	private void reload() throws IOException {
		try {
			if ((this.cache = fbr.readLine()) == null) {
				empty = true;
				cache = null;
			} else {
				empty = false;
			}
		} catch (EOFException oef) {
			empty = true;
			cache = null;
		}
	}

	public void close() throws IOException {
		fbr.close();
	}

	public String peek() {
		if (empty())
			return null;
		return cache.toString();
	}

	public String pop() throws IOException {
		String answer = peek();
		reload();
		return answer;
	}

}