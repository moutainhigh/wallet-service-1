package io.jingwei.wallet.biz.job;


import com.xxl.job.core.handler.annotation.XxlJob;
import io.jingwei.wallet.biz.sync.eth.EthBlockWatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EthBlockSyncJob  {

    @Autowired
    private EthBlockWatcher ethBlockWatcher;

    @XxlJob("ethBlockSyncJob")
    public void syncEthBlock() {
        log.info("===>start ethBlockSyncJob");
        ethBlockWatcher.start();
        log.info("===>end ethBlockSyncJob");
    }

}
