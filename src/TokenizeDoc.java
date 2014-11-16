import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TokenizeDoc {

	public  String[] splitWords(String inpString) {
		String regex="[^a-zA-Z]+";
		if(inpString!=null)
		{
		String[] words = inpString.split(regex);
		return words;
		}
return null;
	}
	
}
