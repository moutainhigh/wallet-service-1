package io.jingwei.wallet.biz.service;

public interface IEthRollbackService {
    /**
     * rollback from blockHeight.
     *
     * @param blockHeight
     */
    void rollback(long blockHeight);
}
