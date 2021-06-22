package ed.od.ua.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ed.od.ua.communicator.Connector;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GitsPublicOwenRepoFindFiles implements Reader {

    private static final int CONTROL_LENGTH = 6;
    private static final int REPO_POZ = 5;
    private static final int USER_POZ = 4;
    private static final int DEFAULT_COUNTER = 0;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile Consumer<String> handler;
    private String codeWord = "README";
    private List<String> findApiList = new ArrayList<>();
    private volatile Consumer<Boolean> finish;
    private AtomicInteger counter = new AtomicInteger(DEFAULT_COUNTER);


    public void addCodeWord(String codeWord) {
        this.codeWord = codeWord;
    }

    public void addUser(String username, String repoName) {
        findApiList.add("/repos/" + username + "/" + repoName + "/contents/");
        counter.incrementAndGet();
    }

    @Override
    public void addData(String url) {
        String[] split = url.split("/");
        if (split.length == CONTROL_LENGTH) {
            addUser(split[USER_POZ], split[REPO_POZ]);
        }
    }

    @Override
    public void set(Consumer<String> handler, Consumer<Boolean> finish) {
        this.handler = handler;
        this.finish = finish;
    }

    @Override
    public void run(Connector connector) {
        connector.channel(findApiList, (val) -> find(val));
    }

    private void find(String data) {
        boolean result = true;
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            jsonNode.elements().forEachRemaining(val -> {
                JsonNode download_url = val.get("download_url");
                System.out.println(val.get("name").asText());
                if (download_url != null && download_url.asText().contains(codeWord)) {
                    String fileContent = null;
                    try {
                        fileContent = IOUtils.toString(new URI(download_url.asText()), Charset.defaultCharset());
                        handler.accept(fileContent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result = false;
        }
        if(counter.decrementAndGet() == 0){
            finish.accept(result);
        };
    }
}
