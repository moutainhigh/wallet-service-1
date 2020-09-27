package io.jingwei.wallet.biz.sync.tron;

import io.jingwei.wallet.biz.sync.AbstractBlockWatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TronBlockWatcher extends AbstractBlockWatcher {


    @Override
    protected long getStartBlock() {
        return 0;
    }

    @Override
    protected void replayBlock(long startBlock) {

    }

    @Override
    public void stop() {

    }


}
