

package io.jingwei.wallet.biz.sync.eth.listener;


import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * A listener for new block events.
 *
 */
public interface EthBlockListener {

    /**
     * Called when a new block is detected from the eth node.
     *
     * @param block The new block
     */
    void onBlock(EthBlock.Block block);
}
