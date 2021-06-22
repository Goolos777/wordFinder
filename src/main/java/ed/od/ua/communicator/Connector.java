package ed.od.ua.communicator;

import java.util.List;
import java.util.function.Consumer;

public interface Connector {
    void channel(List<String> requestUris, Consumer<String> handler);
}
