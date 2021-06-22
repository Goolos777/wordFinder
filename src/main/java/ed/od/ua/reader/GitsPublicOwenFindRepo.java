package ed.od.ua.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ed.od.ua.communicator.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GitsPublicOwenFindRepo implements Reader {

    private static final int DEFAULT_COUNTER = 0;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> findApiList = new ArrayList<>();
    private volatile Consumer<String> handler;
    private Consumer<Boolean> finish;
    private AtomicInteger counter = new AtomicInteger(DEFAULT_COUNTER);

    public void addUser(String username){
        counter.decrementAndGet();
        findApiList.add("/users/"+username+"/repos");
    }

    @Override
    public void set(Consumer<String> handler, Consumer<Boolean> finish){
        this.handler = handler;
        this.finish = finish;
    }

    @Override
    public void run(Connector connector) {
        connector.channel(findApiList, (val) -> find(val));
    }

    @Override
    public void addData(String data) {

    }

    private void find(String data) {
        boolean result = true;
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            jsonNode.elements().forEachRemaining(val -> {
                String url = val.get("url").asText();
                if(url!=null && !url.isEmpty() && handler!=null){
                    handler.accept(url);
                }
            });
        } catch (JsonProcessingException e) {
            result = false;
            e.printStackTrace();
        }
        if(counter.incrementAndGet() == 0){
            finish.accept(result);
        };
    }
}
