package ed.od.ua.communicator;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.bootstrap.AsyncRequesterBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import java.util.AbstractMap;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ConnectorImpl implements Connector {

    private static final boolean CONNECT_OFF = false;
    private static final boolean CONNECT_ON = true;

    private final int TIME_OUT = 5;

    private final IOReactorConfig ioReactorConfig;
    private final HttpAsyncRequester requester;
    private final HttpHost target;
    private final Deque<AbstractMap.SimpleEntry<List<String>, Consumer<String>>> pairDeque = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean connect = new AtomicBoolean(CONNECT_OFF);
    private final String login;
    private final String pass;

    public ConnectorImpl(HttpHost httpHost, String login, String pass) {
        this.login = login;
        this.pass = pass;
        ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(TIME_OUT, TimeUnit.SECONDS).build();
        requester = AsyncRequesterBootstrap.bootstrap()
                .setIOReactorConfig(ioReactorConfig)
                .setStreamListener(new Http1StreamListener() {
                    @Override
                    public void onRequestHead(final HttpConnection connection, final HttpRequest request) {
                        System.out.println(connection.getRemoteAddress() + " " + new RequestLine(request));
                    }

                    @Override
                    public void onResponseHead(final HttpConnection connection, final HttpResponse response) {
                        System.out.println(connection.getRemoteAddress() + " " + new StatusLine(response));
                    }

                    @Override
                    public void onExchangeComplete(final HttpConnection connection, final boolean keepAlive) {
                        if (keepAlive) {
                            System.out.println(connection.getRemoteAddress() + " exchange completed (connection kept alive)");
                        } else {
                            System.out.println(connection.getRemoteAddress() + " exchange completed (connection closed)");
                        }
                    }
                })
                .create();
        target = httpHost;
    }

    @Override
    public void channel(List<String> requestUris, Consumer<String> handler) {
        requestUris.forEach(val -> System.out.println(val));
        pairDeque.push(new AbstractMap.SimpleEntry(requestUris, handler));
        if (!connect.get()) {
            try {
                send(login, pass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void send(String login, String pass) {
        connect.set(CONNECT_ON);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("HTTP requester shutting down");
            requester.close(CloseMode.GRACEFUL);
        }));
        requester.start();
        AbstractMap.SimpleEntry<List<String>, Consumer<String>> pair = pairDeque.poll();
        List<String> requestUris = pair.getKey();
        Consumer<String> consumer = pair.getValue();
        final CountDownLatch latch = new CountDownLatch(requestUris.size());

        String auth = login + pass;
        final String authVal = "Basic " + new String(Base64.getEncoder().encode(auth.getBytes()));

        requestUris.forEach(requestUri -> requester.execute(
                AsyncRequestBuilder
                        .get()
                        .setHeader(HttpHeaders.ACCEPT, "application/json")
                        .addHeader(HttpHeaders.AUTHORIZATION, authVal)
                        .setHttpHost(target)
                        .setPath(requestUri)
                        .build(),
                new BasicResponseConsumer<>(new StringAsyncEntityConsumer()),
                Timeout.ofSeconds(TIME_OUT),
                new FutureCallback<>() {
                    @Override
                    public void completed(final Message<HttpResponse, String> message) {
                        final String body = message.getBody();
                        consumer.accept(body);
                        latch.countDown();
                    }

                    @Override
                    public void failed(final Exception ex) {
                        System.out.println("->failed->" + requestUri + " " + ex);
                        ex.printStackTrace();
                        latch.countDown();
                    }

                    @Override
                    public void cancelled() {
                        System.out.println("->cancelled->" + requestUri);
                        latch.countDown();
                    }
                }));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!pairDeque.isEmpty()) send(login, pass);
        connect.set(CONNECT_OFF);
        System.out.println("Shutting down I/O reactor");
        requester.initiateShutdown();
    }

}
