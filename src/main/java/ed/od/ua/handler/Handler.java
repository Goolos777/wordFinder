package ed.od.ua.handler;

import java.util.List;
import java.util.Map;

public interface Handler {

    void run(String line);

    List<Map.Entry<String, Integer>> getPopularWord(int findCounter);

}
