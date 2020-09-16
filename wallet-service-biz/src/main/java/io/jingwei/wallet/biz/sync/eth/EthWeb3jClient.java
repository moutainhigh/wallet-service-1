package io.jingwei.wallet.biz.sync.eth;

import io.jingwei.wallet.biz.config.SyncConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.http.HttpService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * 以太坊web3j
 */
@Slf4j
@Component
public class EthWeb3jClient {

    @Autowired
    private SyncConfig syncConfig;

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(25, 10, TimeUnit.SECONDS))
            .build();

    private List<Web3j> web3jNode = new ArrayList<>(3);
    private Web3j web3j;

    @PostConstruct
    public void init() {
        loadEthNodes();

        setBestClient();
    }

    public Web3j getClient() {
        return web3j;
    }

    private void loadEthNodes() {
        if (CollectionUtils.isEmpty(web3jNode)) {
            String nodesStr = syncConfig.getNodes();
            String[] nodes = nodesStr.split(",");

            if (nodes == null || nodes.length < 1) {
                log.error("no available node, please check full node config :{}", nodesStr);
            }

            for (String node : nodes) {
                HttpService httpService = new HttpService("http://" + node, okHttpClient, false);
                web3jNode.add(new JsonRpc2_0Web3j(httpService));
            }
        }
    }

    @SneakyThrows
    public void setBestClient() {
        long currentBestHeight = 0;

        if (this.web3j != null) {
            currentBestHeight = getNodeHeight(web3j);
        }

        if (web3jNode.size() == 1) {
            this.web3j = web3jNode.get(0);
            return;
        }

        for (Web3j web3j : web3jNode) {
            long nodeHeight = getNodeHeight(web3j);

            if (nodeHeight > currentBestHeight + 1) {
                currentBestHeight = nodeHeight;
                this.web3j = web3j;
            }
        }
    }

    @SneakyThrows
    private long getNodeHeight(Web3j web3j) {
        return web3j.ethBlockNumber().send().getBlockNumber().longValue();
    }

}
