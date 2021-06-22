package ed.od.ua.handler;

import ed.od.ua.service.Container;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TopPopularHandler implements Handler {

    private static final String[] correct = {",", ".", "?", "»", "!", ":", "\n"};

    private static final int letterCritCorrect = 2;
    private final Container container;
    private final int letterLong;

    public TopPopularHandler(Container container, int letterLong) {
        this.container = container;
        this.letterLong = letterLong;
    }

    @Override
    public void run(String lineStream) {
        Stream.of(lineStream)
                .flatMap(line -> Stream.of(line.split("\n")))
                .flatMap(line -> Stream.of(line.split(" ")))
                .map(word -> correct(word))
                .filter(word -> word.length() > letterLong)
                .forEach(val -> container.add(val));
    }

    @Override
    public List<Map.Entry<String, Integer>> getPopularWord(int popularSize) {
        List<Map.Entry<String, Integer>> collect = container.getHashMapVariable()
                .entrySet()
                .parallelStream()
                .sorted(((Comparator<Map.Entry<String, Integer>>) (a, b) -> a.getValue().compareTo(b.getValue())).reversed())
                .limit(popularSize)
                .collect(Collectors.toList());
        return collect;
    }

    private String correct(String word) {
        if (word.length() > letterCritCorrect && Stream.of(correct).anyMatch(corectValue -> word.endsWith(corectValue))) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
