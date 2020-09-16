package io.jingwei.wallet.biz.sync.eth;

import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;

import javax.annotation.PostConstruct;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class EthBlockCacheManager {
    private SortedMap<Long, EthBlock.Block> heightCache;

    @PostConstruct
    private void init() {
        this.heightCache = new TreeMap<>();
    }

    public void put(EthBlock.Block b) {
        heightCache.put(b.getNumber().longValue(), b);
    }

    public void remove(EthBlock.Block b) {
        heightCache.remove(b.getNumber().longValue());
    }

    public EthBlock.Block get(long height) {
        return heightCache.get(height);
    }

    public boolean isEmpty() {
        return heightCache.isEmpty();
    }

    public int size() {
        return heightCache.size();
    }

    public void clear(){
        heightCache.clear();
    }



}
