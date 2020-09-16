

package io.jingwei.wallet.biz.sync.eth;


import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * A listener for new block events.
 *
 */
public interface EthBlockListener {

    void before(EthBlock.Block block);
    /**
     * Called when a new block is detected fron the ethereum node.
     *
     * @param block The new block
     */
    void after(EthBlock.Block block);
}
