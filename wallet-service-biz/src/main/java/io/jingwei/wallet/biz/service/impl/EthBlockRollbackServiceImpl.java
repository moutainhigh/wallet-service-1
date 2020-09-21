package io.jingwei.wallet.biz.service.impl;

import io.jingwei.base.utils.tx.TxTemplateService;
import io.jingwei.wallet.biz.service.IEthBlockService;
import io.jingwei.wallet.biz.service.IEthLatestService;
import io.jingwei.wallet.biz.service.IEthRollbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EthBlockRollbackServiceImpl implements IEthRollbackService {

    @Autowired
    private TxTemplateService txTemplateService;

    @Autowired
    private IEthBlockService  ethBlockService;

    @Autowired
    private IEthLatestService ethLatestService;

    @Override
    public void rollback(long blockHeight) {
        txTemplateService.doInTransaction(()->{
            ethBlockService.deleteGreaterBlock(blockHeight);
            ethLatestService.updateByOldBlock(blockHeight);
        });
    }
}
