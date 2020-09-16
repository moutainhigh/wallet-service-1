

package org.web3j.protocol.websocket;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Web3j seems to fail when closing websocket in pubsub mode.  This causes
 * a deadlock when attempting to reconnect to the node, as the close latch in WebSocketClient
 * never gets released and the reconnection block indefinitely.
 *
 * This is workaround until web3j 4 which should hopefully have built in reconnections.
 */
@Slf4j
@Data
public class WalletWebSocketService extends WebSocketService {

    private WebSocketClient webSocketClient;

    public WalletWebSocketService(WebSocketClient webSocketClient,
                                  boolean includeRawResponses) {
        super(webSocketClient, includeRawResponses);

        this.webSocketClient = webSocketClient;
    }

    @Override
    void onWebSocketClose() {
        try {
            super.onWebSocketClose();
        } catch (Throwable t) {
            log.warn("Error when closing websocket, this is expected during a websocket reconnection (for now).", t);
        }
    }
}
