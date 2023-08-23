package com.seailz.discordjar.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Abstracts the Spring Websocket implementation to make it easier to use, understand, and maintain.
 */
public class WebSocket extends TextWebSocketHandler {

    private final String url;
    private final boolean debug;
    private final boolean newSystemForMemoryManagement;
    private WebSocketClient client;
    private WebSocketSession session;
    private List<Consumer<TextMessage>> messageConsumers = new ArrayList<>();
    private List<Consumer<CloseStatus>> onDisconnectConsumers = new ArrayList<>();
    private List<Runnable> onConnectConsumers = new ArrayList<>();
    private Function<CloseStatus, Boolean> reEstablishConnection = (e) -> true;
    private double nsfgmmPercentOfTotalMemory;

    public WebSocket(String url, boolean newSystemForMemoryManagement, boolean debug, int nsfgmmPercentOfTotalMemory) {
        this.url = url;
        this.newSystemForMemoryManagement = newSystemForMemoryManagement;
        this.debug = debug;
        this.nsfgmmPercentOfTotalMemory = nsfgmmPercentOfTotalMemory;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public WebSocketClient getClient() {
        return client;
    }

    public WebsocketAction<Void> connect() {
        // Start connection cycle
        WebsocketAction<Void> action = new WebsocketAction<>();
        try {
            connect(url);
        } catch (Exception e) {
            action.fail(new WebsocketExceptionError(e));
            return action;
        }
        action.complete(null);

        onConnectConsumers.forEach(Runnable::run);
        return action;
    }

    public WebsocketAction<Void> disconnect() {
        WebsocketAction<Void> action = new WebsocketAction<>();
        try {
            session.close();
        } catch (Exception e) {
            action.fail(new WebsocketExceptionError(e));
            return action;
        }
        action.complete(null);
        return action;
    }

    public WebsocketAction<Void> send(String message) {
        WebsocketAction<Void> action = new WebsocketAction<>();
        try {
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            action.fail(new WebsocketExceptionError(e));
            return action;
        }
        action.complete(null);
        return action;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        new Thread(() -> {
            messageConsumers.forEach(consumer -> {
                consumer.accept(message);
            });
        }).start();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Force session disconnect in case it failed to disconnect
        try {
            session.close();
        } catch (Exception e) {
        }
        new Thread(() -> {
            onDisconnectConsumers.forEach(consumer -> consumer.accept(status));
        }).start();

        if (reEstablishConnection.apply(status)) {
            try {
                connect(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Allows you to specify the logic for if a re-establishment of the connection should be attempted after a disconnect.
     */
    public void setReEstablishConnection(Function<CloseStatus, Boolean> reEstablishConnection) {
        this.reEstablishConnection = reEstablishConnection;
    }

    public void addMessageConsumer(Consumer<TextMessage> consumer) {
        messageConsumers.add(consumer);
    }

    public void addOnDisconnectConsumer(Consumer<CloseStatus> consumer) {
        onDisconnectConsumers.add(consumer);
    }

    public void addOnConnect(Runnable runnable) {
        onConnectConsumers.add(runnable);
    }

    public void removeMessageConsumer(Consumer<TextMessage> consumer) {
        messageConsumers.remove(consumer);
    }

    public void removeOnDisconnectConsumer(Consumer<CloseStatus> consumer) {
        onDisconnectConsumers.remove(consumer);
    }

    private void connect(String customUrl) throws ExecutionException, InterruptedException {
        WebSocketClient client = new StandardWebSocketClient();
        this.client = client;
        this.session = client.execute(this, new WebSocketHttpHeaders(), URI.create(customUrl)).get();
        // Allocate 25% of the bot's total **AVAILABLE** memory to the gateway.
        if (newSystemForMemoryManagement) {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();

            if (debug) {
                Logger.getLogger("WS")
                        .info("[WS] NSGFMM POTM:" + nsfgmmPercentOfTotalMemory + "%");
                Logger.getLogger("WS")
                        .info("[WS] " + "As NSGFMMPOTM is " + nsfgmmPercentOfTotalMemory + "%, the allocated memory will be " + (maxMemory * (nsfgmmPercentOfTotalMemory / 100)) + " bytes (" + ((maxMemory * (nsfgmmPercentOfTotalMemory / 100)) / 1000000) + " MB" + " NSGFM / 100 =" + (nsfgmmPercentOfTotalMemory / 100));
            }
            maxMemory = (long) (maxMemory * (nsfgmmPercentOfTotalMemory / 100));

            if (debug) {
                Logger.getLogger("WS")
                        .info("[WS] " + "Allocated memory: " + maxMemory + " bytes (" + (maxMemory / 1000000) + " MB) / " + runtime.maxMemory() + " bytes (" + (runtime.maxMemory() / 1000000) + " MB)");
            }

            session.setTextMessageSizeLimit((int) maxMemory);
            session.setBinaryMessageSizeLimit((int) maxMemory);
        } else {
            session.setTextMessageSizeLimit(100000000);
            session.setBinaryMessageSizeLimit(100000000);
        }
    }


    public class WebsocketAction<T> {
        private final CompletableFuture<T> onSuccessful = new CompletableFuture<>();
        private final CompletableFuture<WebsocketError> onFailed = new CompletableFuture<>();

        protected WebsocketAction() {}

        public WebsocketAction<T> onSuccess(Consumer<T> consumer) {
            onSuccessful.thenAccept(consumer);
            return this;
        }

        public WebsocketAction<T> onFailed(Consumer<WebsocketExceptionError> consumer) {
            onFailed.thenAccept((e) -> {
                if (e instanceof WebsocketExceptionError) consumer.accept((WebsocketExceptionError) e);
            });
            return this;
        }

        public WebsocketAction<T> onFailedDisconnect(Consumer<WebsocketDisconnectError> consumer) {
            onFailed.thenAccept((e) -> {
                if (e instanceof WebsocketDisconnectError) consumer.accept((WebsocketDisconnectError) e);
            });
            return this;
        }

        protected void complete(T t) {
            onSuccessful.complete(t);
        }

        protected void fail(WebsocketError t) {
            onFailed.complete(t);
        }

        public CompletableFuture<T> getOnSuccessful() {
            return onSuccessful;
        }

        public CompletableFuture<WebsocketError> getOnFailed() {
            return onFailed;
        }
    }

    public interface WebsocketError {}

    public class WebsocketExceptionError implements WebsocketError {
        private Throwable throwable;

        public WebsocketExceptionError(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    public class WebsocketDisconnectError implements WebsocketError {
        private int code;
        private String reason;

        public WebsocketDisconnectError(int code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        public int getCode() {
            return code;
        }

        public String getReason() {
            return reason;
        }
    }

}
