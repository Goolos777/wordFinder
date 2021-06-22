package ed.od.ua.reader;

import ed.od.ua.communicator.Connector;

import java.util.function.Consumer;

public interface Reader {

    void set(Consumer<String> handler, Consumer<Boolean> finish);

    void run(Connector connector);

    void addData(String data);
}
