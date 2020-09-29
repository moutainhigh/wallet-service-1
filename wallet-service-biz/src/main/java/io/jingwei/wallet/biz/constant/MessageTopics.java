package io.jingwei.wallet.biz.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 *
 */
@Getter
@AllArgsConstructor
public enum MessageTopics {

    /**
     * ETH_TX_CONFIRMED_TOPIC 交易确认通知协议
     */
    ETH_TX_CONFIRMED_TOPIC("ETH_TX_CONFIRMED_TOPIC",
            "PID_TX_CONFIRMED_TOPIC",
            "CID_TX_CONFIRMED_TOPIC",
            "eth交易确认通知协议"),

    /**
     * ETH_TX_UNCONFIRMED_TOPIC 交易被挖矿通知协议
     */
    ETH_TX_UNCONFIRMED_TOPIC("ETH_TX_UNCONFIRMED_TOPIC",
            "PID_TX_UNCONFIRMED_TOPIC",
            "CID_TX_UNCONFIRMED_TOPIC",
            "eth交易被挖矿通知协议"),


    ;
    /**
     * 消息主题
     */
    private String topic;
    /**
     * 生产者组
     */
    private String producerGroup;
    /**
     * 消费者组
     */
    private String consumerGroup;
    /**
     * 消息描述
     */
    private String desc;

}
