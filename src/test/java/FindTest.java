import ed.od.ua.handler.Handler;
import ed.od.ua.handler.TopPopularHandler;
import ed.od.ua.service.Container;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindTest {

    @Test
    public void counterTest() {
        Handler handler = new TopPopularHandler(new Container(), 5);
        Path path = Path.of("README.md");
        try (Stream<String> lineStream = Files.lines(path)) {

            handler.run(lineStream.collect(Collectors.joining()));
            handler.getPopularWord(5)
                    .forEach(val -> System.out.println("word:" + val.getKey() + " count:" + val.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
