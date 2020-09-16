package io.jingwei.wallet.biz.sync;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBlockWatcher implements BlockWatcher {

    @Override
    public void start() {
        long startBlockNumber = getStartBlock();

        log.info("startBlockNumber={}", startBlockNumber);
        replayBlock(startBlockNumber);
    }

    protected abstract long getStartBlock();

    protected abstract void replayBlock(long startBlock);
}
