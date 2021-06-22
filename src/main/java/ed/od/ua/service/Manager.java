package ed.od.ua.service;

import ed.od.ua.communicator.Connector;
import ed.od.ua.communicator.ConnectorImpl;
import ed.od.ua.handler.Handler;
import ed.od.ua.handler.TopPopularHandler;
import ed.od.ua.reader.Reader;
import org.apache.hc.core5.http.HttpHost;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class Manager {

    private final Connector connector;
    private final Handler handler;
    private volatile int findCounter;
    private final List<Reader> readerList;

    public Manager(String url, List<Reader> readerList, int controlLength, String login, String pass) {
        this.readerList = readerList;
        HttpHost httpHost = null;
        try {
            httpHost = HttpHost.create(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        connector = new ConnectorImpl(httpHost, login, pass);
        handler = new TopPopularHandler(new Container(), controlLength);
        init(readerList);
    }

    private void update(Boolean result) {
        if (result) {
            List<Map.Entry<String, Integer>> popularWord = handler.getPopularWord(findCounter);
            popularWord.forEach(val -> System.out.println(" -----   key:" + val.getKey() + " -----  actual:" + val.getValue()));
        } else {
            System.out.println("FAILED");
        }
    }

    public void find(int findCounter) {
        this.findCounter = findCounter;
        readerList.get(0).run(connector);
    }

    private void init(List<Reader> readerList){
        for (int position = 0; position < readerList.size(); position++) {
            Reader reader = readerList.get(position);
            if(position == readerList.size()-1){
                reader.set((data)->handler.run(data),(result)->update(result));
            }
            else {
                Reader newReader =  readerList.get(position+1);
                reader.set((data)->newReader.addData(data),(result)->newReader.run(connector));
            }
        }
    }
}