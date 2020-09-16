package io.jingwei.wallet.biz.config;

import io.jingwei.base.utils.tx.TxTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TransactionConfig {
    @Autowired
    private TransactionTemplate defaultTx;

    @Bean
    public TxTemplateService txTemplateService() {
        return new TxTemplateService(defaultTx);
    }
}
