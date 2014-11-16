class NewString {
	public String str;

	NewString() {
		str = "";
	}
}

public class StemWord {

	private String Clean(String inputWord) {
		int last = inputWord.length();

		char ch[] = inputWord.toCharArray();
		String temp = "";

		for (int i = 0; i < last; i++) {
			if (Character.isLetterOrDigit(ch[i]))
				temp += inputWord.charAt(i);
		}

		return temp;
	}

	private boolean hasSuffix(String word, String suffix, NewString stem) {

		String tmp = "";

		if (word.length() <= suffix.length())
			return false;
		if (suffix.length() > 1)
			if (word.charAt(word.length() - 2) != suffix
					.charAt(suffix.length() - 2))
				return false;

		stem.str = "";

		for (int i = 0; i < word.length() - suffix.length(); i++)
			stem.str += word.charAt(i);
		tmp = stem.str;

		for (int i = 0; i < suffix.length(); i++)
			tmp += suffix.charAt(i);

		if (tmp.compareTo(word) == 0)
			return true;
		else
			return false;
	}

	private boolean vowel(char ch, char prev) {
		switch (ch) {
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':
			return true;
		case 'y': {

			switch (prev) {
			case 'a':
			case 'e':
			case 'i':
			case 'o':
			case 'u':
				return false;

			default:
				return true;
			}
		}

		default:
			return false;
		}
	}

	private int measure(String word) {

		int i = 0, count = 0;
		int length = word.length();

		while (i < length) {
			for (; i < length; i++) {
				if (i > 0) {
					if (vowel(word.charAt(i), word.charAt(i - 1)))
						break;
				} else {
					if (vowel(word.charAt(i), 'a'))
						break;
				}
			}

			for (i++; i < length; i++) {
				if (i > 0) {
					if (!vowel(word.charAt(i), word.charAt(i - 1)))
						break;
				} else {
					if (!vowel(word.charAt(i), '?'))
						break;
				}
			}
			if (i < length) {
				count++;
				i++;
			}
		}

		return (count);
	}

	private boolean containsVowel(String word) {

		for (int i = 0; i < word.length(); i++)
			if (i > 0) {
				if (vowel(word.charAt(i), word.charAt(i - 1)))
					return true;
			} else {
				if (vowel(word.charAt(0), 'a'))
					return true;
			}

		return false;
	}

	private boolean charVowelCharCheck(String word) {
		int length = word.length();

		if (length < 3)
			return false;

		if ((!vowel(word.charAt(length - 1), word.charAt(length - 2)))
				&& (word.charAt(length - 1) != 'w')
				&& (word.charAt(length - 1) != 'x')
				&& (word.charAt(length - 1) != 'y')
				&& (vowel(word.charAt(length - 2), word.charAt(length - 3)))) {

			if (length == 3) {
				if (!vowel(word.charAt(0), '?'))
					return true;
				else
					return false;
			} else {
				if (!vowel(word.charAt(length - 3), word.charAt(length - 4)))
					return true;
				else
					return false;
			}
		}

		return false;
	}

	private String step1(String word) {
		NewString stem = new NewString();
		if (word.charAt(word.length() - 1) == 's') {
			if ((hasSuffix(word, "sses", stem))
					|| (hasSuffix(word, "ies", stem))) {
				String tmp = "";
				for (int i = 0; i < word.length() - 2; i++)
					tmp += word.charAt(i);
				word = tmp;
			} else {
				if ((word.length() == 1)
						&& (word.charAt(word.length() - 1) == 's')) {
					word = "";
					return word;
				}
				if (word.charAt(word.length() - 2) != 's') {
					String tmp = "";
					for (int i = 0; i < word.length() - 1; i++)
						tmp += word.charAt(i);
					word = tmp;
				}
			}
		}

		if (hasSuffix(word, "eed", stem)) {
			if (measure(stem.str) > 0) {
				String tmp = "";
				for (int i = 0; i < word.length() - 1; i++)
					tmp += word.charAt(i);
				word = tmp;
			}
		} else {
			if ((hasSuffix(word, "ed", stem)) || (hasSuffix(word, "ing", stem))) {
				if (containsVowel(stem.str)) {

					String tmp = "";
					for (int i = 0; i < stem.str.length(); i++)
						tmp += word.charAt(i);
					word = tmp;
					if (word.length() == 1)
						return word;

					if ((hasSuffix(word, "at", stem))
							|| (hasSuffix(word, "bl", stem))
							|| (hasSuffix(word, "iz", stem))) {
						word += "e";

					} else {
						int length = word.length();
						if ((word.charAt(length - 1) == word.charAt(length - 2))
								&& (word.charAt(length - 1) != 'l')
								&& (word.charAt(length - 1) != 's')
								&& (word.charAt(length - 1) != 'z')) {

							tmp = "";
							for (int i = 0; i < word.length() - 1; i++)
								tmp += word.charAt(i);
							word = tmp;
						} else if (measure(word) == 1) {
							if (charVowelCharCheck(word))
								word += "e";
						}
					}
				}
			}
		}
		return word;

	}

	private String step2(String word) {
		String[][] suffixes = { { "ational", "ate" }, { "tional", "tion" },
				{ "enci", "ence" }, { "anci", "ance" }, { "izer", "ize" },
				{ "iser", "ize" }, { "abli", "able" }, { "alli", "al" },
				{ "entli", "ent" }, { "eli", "e" }, { "ousli", "ous" },
				{ "ization", "ize" }, { "isation", "ize" }, { "ation", "ate" },
				{ "ator", "ate" }, { "alism", "al" }, { "iveness", "ive" },
				{ "fulness", "ful" }, { "ousness", "ous" }, { "aliti", "al" },
				{ "iviti", "ive" }, { "biliti", "ble" } };
		NewString stem = new NewString();

		for (int index = 0; index < suffixes.length; index++) {
			if (hasSuffix(word, suffixes[index][0], stem)) {
				if (measure(stem.str) > 0) {
					word = stem.str + suffixes[index][1];
					return word;
				}
			}
		}

		return word;
	}

	private String step3(String word) {
		String[][] suffixes = { { "icate", "ic" }, { "ative", "" },
				{ "alize", "al" }, { "alise", "al" }, { "iciti", "ic" },
				{ "ical", "ic" }, { "ful", "" }, { "ness", "" } };
		NewString stem = new NewString();

		for (int index = 0; index < suffixes.length; index++) {
			if (hasSuffix(word, suffixes[index][0], stem))
				if (measure(stem.str) > 0) {
					word = stem.str + suffixes[index][1];
					return word;
				}
		}
		return word;
	}

	private String step4(String word) {
		String[] suffixes = { "al", "ance", "ence", "er", "ic", "able", "ible",
				"ant", "ement", "ment", "ent", "sion", "tion", "ou", "ism",
				"ate", "iti", "ous", "ive", "ize", "ise" };

		NewString stem = new NewString();

		for (int index = 0; index < suffixes.length; index++) {
			if (hasSuffix(word, suffixes[index], stem)) {

				if (measure(stem.str) > 1) {
					word = stem.str;
					return word;
				}
			}
		}
		return word;
	}

	private String step5(String word) {
		if (word.charAt(word.length() - 1) == 'e') {
			if (measure(word) > 1) {
				String tmp = "";
				for (int i = 0; i < word.length() - 1; i++)
					tmp += word.charAt(i);
				word = tmp;
			} else if (measure(word) == 1) {
				String stem = "";
				for (int i = 0; i < word.length() - 1; i++)
					stem += word.charAt(i);

				if (!charVowelCharCheck(stem))
					word = stem;
			}
		}
		if (word.length() == 1)
			return word;
		if ((word.charAt(word.length() - 1) == 'l')
				&& (word.charAt(word.length() - 2) == 'l')
				&& (measure(word) > 1))
			if (measure(word) > 1) {
				String tmp = "";
				for (int i = 0; i < word.length() - 1; i++)
					tmp += word.charAt(i);
				word = tmp;
			}
		return word;
	}

	private String stripPrefixes(String inputWord) {

		String[] prefixes = { "kilo", "micro", "milli", "intra", "ultra",
				"mega", "nano", "pico", "pseudo" };
		int last = prefixes.length;
		for (int i = 0; i < last; i++) {
			if (inputWord.startsWith(prefixes[i])) {
				String temp = "";
				for (int j = 0; j < inputWord.length() - prefixes[i].length(); j++)
					temp += inputWord.charAt(j + prefixes[i].length());
				return temp;
			}
		}
		return inputWord;
	}

	private String stripSuffixes(String inputWord) {
		inputWord = step1(inputWord);
		if (inputWord.length() >= 1)
			inputWord = step2(inputWord);
		if (inputWord.length() >= 1)
			inputWord = step3(inputWord);
		if (inputWord.length() >= 1)
			inputWord = step4(inputWord);
		if (inputWord.length() >= 1)
			inputWord = step5(inputWord);
		return inputWord;
	}

	public String stem(String inputWord) {

		inputWord = inputWord.toLowerCase();
		inputWord = Clean(inputWord);
		if ((inputWord != "") && (inputWord.length() > 2)) {
			inputWord = stripPrefixes(inputWord);
			if (inputWord != "")
				inputWord = stripSuffixes(inputWord);
		}
		return inputWord;
	}

	public String stemWord(String inputWord) {
		inputWord = stem(inputWord);
		return "";
	}

	public static void main(String[] args) {
		StemWord sw = new StemWord();
		System.out.println(sw.stem("chess"));
	}
}
