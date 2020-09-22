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
     * PARSE_ETH_COMPLETE_TOPIC 交易解析完成通知协议
     */
    PARSE_ETH_COMPLETE_TOPIC("PARSE_ETH_COMPLETE_TOPIC",
            "PID_PARSE_ETH_COMPLETE_TOPIC",
            "CID_PARSE_ETH_COMPLETE_TOPIC",
            "交易解析完成通知协议"),
    /**
     * ORDER_STATUS_UPDATE_TOPIC 返回创建订单账务冻结结果
     */
    ORDER_STATUS_UPDATE_TOPIC("ORDER_STATUS_UPDATE_TOPIC",
            "PID_ORDER_STATUS_UPDATE_TOPIC",
            "CID_ORDER_STATUS_UPDATE_TOPIC",
            "返回创建订单账务冻结结果协议"),


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
