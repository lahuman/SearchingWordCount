package lahuman;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordFinder {
	private final String wordTxtPath;
	private final String contentsTxtPath;
	private final String outputTxtPath;

	public WordFinder(String wordTxtPath, String contentsTxtPath, String outputTxtPath) {
		this.wordTxtPath = wordTxtPath;
		this.contentsTxtPath = contentsTxtPath;
		this.outputTxtPath = outputTxtPath;
	}

	public void run() {
		processWordCounting();
	}

	private Map<String, Integer> getWordMap() {
		Map<String, Integer> wordCnt = new HashMap<>();

		try (Stream<String> stream = Files.lines(Paths.get(wordTxtPath), Charset.forName("euc-kr"))) {
			stream.filter(s -> !"".equals(s.trim())).map(s -> s.split("\t")).forEach(array -> {
				Stream.of(array).filter(s -> !"".equals(s.trim())).forEach(s -> wordCnt.put(s, 0));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wordCnt;
	}

	private void processWordCounting() {
		final AtomicInteger count = new AtomicInteger();
		final Map<String, Integer> wordMap = getWordMap();
		System.out.println("Word Count = " + wordMap.size());

		try (Stream<String> stream = Files.lines(Paths.get(contentsTxtPath), Charset.forName("euc-kr"))) {

			Stream<String> outputStream = stream.map(sentenceStr -> {
				StringBuffer returnStr = new StringBuffer();
				String originalStr = sentenceStr.trim();
				count.incrementAndGet();
				if ("".equals(originalStr)) {
					return "";
				}
				wordMap.keySet().stream().forEach(s -> {
					int wordCnt = originalStr.split(s).length - 1;
					if (wordCnt > 0) {
						wordMap.put(s, wordMap.get(s) + wordCnt);
						returnStr.append("[" + s + "=" + wordCnt + "]|");
					}
				});

				returnStr.append("#" + count.get() + "|");
				returnStr.append(sentenceStr);
				System.out.println(returnStr.toString());
				return returnStr.toString();
			}).filter(s -> s.startsWith("["));
			Files.write(Paths.get(outputTxtPath), (Iterable<String>) outputStream::iterator);
			Files.write(Paths.get(outputTxtPath), ("\r\n" + wordMap.keySet().stream().filter(s -> (wordMap.get(s) > 0))
					.sorted((o1, o2) -> Integer.compare(wordMap.get(o2), wordMap.get(o1))).map(s -> {
						return s + "=" + wordMap.get(s);
					}).collect(Collectors.joining(",")) + "\r\nTOTAL COUNT="
					+ wordMap.keySet().stream().filter(s -> (wordMap.get(s) > 0)).mapToInt(wordMap::get).sum())
							.getBytes(),
					StandardOpenOption.APPEND);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}