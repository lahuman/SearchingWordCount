package lahuman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class WordGroup {
	private long count = 0;
	private List<String> wordGroup = new ArrayList<>();
	
	public WordGroup(String rowString) {
		Arrays.stream(rowString.split("\t")).forEach(array -> {
			Stream.of(array).filter(s -> !"".equals(s.trim())).forEach(s -> wordGroup.add(s));
		});
	}
	
	public String searchingAndIncrease(String contentesString) {
		StringBuffer searchingWordInfo = new StringBuffer();
		wordGroup.stream().forEach(word->{
			int wordCnt = contentesString.split(word).length - 1;
			if (wordCnt > 0) {
				count +=wordCnt;
				searchingWordInfo .append("[" + word + "=" + wordCnt + "]|");
			}	
		});
		return searchingWordInfo.toString();
	}
	
	public long getWordGroupCount() {
		return count;
	}
	
	public String getMasterWord() {
		return wordGroup.get(0);
	}
}
