package com.joymeter.config;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.joymeter.entity.RocketmqEvent;

/**
 * 初始化Producer和consumer
 * @author Pan Shuoting
 *
 */
@Configuration
@EnableConfigurationProperties(RocketmqProperties.class)
@ConditionalOnProperty(prefix = RocketmqProperties.PREFIX, value = "namesrvAddr")
public class RocketmqAutoConfiguration {
	private static final Logger log = Logger.getLogger(RocketmqAutoConfiguration.class.getName());

	@Autowired
	private RocketmqProperties properties;
	@Autowired
	private ApplicationEventPublisher publisher;

	private static boolean isFirstSub = true;
	private static long startTime = System.currentTimeMillis();

	/**
	 * 初始化向rocketmq发送普通消息的生产者
	 */
	@Bean
	@ConditionalOnProperty(prefix = RocketmqProperties.PREFIX, value = "producerInstanceName")
	public DefaultMQProducer defaultMQProducer() throws MQClientException {
		DefaultMQProducer producer = new DefaultMQProducer(properties.getProducerGroupName());
		producer.setNamesrvAddr(properties.getNamesrvAddr());
		producer.setInstanceName(properties.getProducerInstanceName());
		producer.setVipChannelEnabled(false);
		producer.setRetryTimesWhenSendAsyncFailed(10);

		producer.start();
		log.info("RocketMq defaultProducer Started.");
		return producer;
	}

	/**
	 * 初始化向rocketmq发送事务消息的生产者
	 */
	@Bean
	@ConditionalOnProperty(prefix = RocketmqProperties.PREFIX, value = "producerTranInstanceName")
	public TransactionMQProducer transactionProducer() throws MQClientException {
		TransactionMQProducer producer = new TransactionMQProducer(properties.getTransactionProducerGroupName());
		producer.setNamesrvAddr(properties.getNamesrvAddr());
		producer.setInstanceName(properties.getProducerTranInstanceName());
		producer.setRetryTimesWhenSendAsyncFailed(10);

		// 事务回查最小并发数
		producer.setCheckThreadPoolMinSize(2);
		// 事务回查最大并发数
		producer.setCheckThreadPoolMaxSize(2);
		// 队列数
		producer.setCheckRequestHoldMax(2000);

		producer.start();
		log.info("RocketMq TransactionMQProducer  Started.");
		return producer;
	}

	/**
	 * 初始化rocketmq消息监听方式的消费者
	 */
	@Bean
	@ConditionalOnProperty(prefix = RocketmqProperties.PREFIX, value = "consumerInstanceName")
	public DefaultMQPushConsumer pushConsumer() throws MQClientException {
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getConsumerGroupName());
		consumer.setNamesrvAddr(properties.getNamesrvAddr());
		consumer.setInstanceName(properties.getConsumerInstanceName());
		if (properties.isConsumerBroadcasting()) {
			consumer.setMessageModel(MessageModel.BROADCASTING);
		}
		consumer.setConsumeMessageBatchMaxSize(
				properties.getConsumerBatchMaxSize() == 0 ? 1 : properties.getConsumerBatchMaxSize());
		/**
		 * 订阅指定topic下tags
		 */
		List<String> subscribeList = properties.getSubscribe();
		for (String subscribe : subscribeList) {
			String[] subStrings = subscribe.split(":");
			consumer.subscribe(subStrings[0], subStrings[1]);
		}
		if (properties.isEnableOrderConsumer()) {
			consumer.registerMessageListener((List<MessageExt> msgs, ConsumeOrderlyContext context) -> {
				try {
					context.setAutoCommit(true);
					msgs = filter(msgs);
					if (msgs.size() == 0)
						return ConsumeOrderlyStatus.SUCCESS;
					this.publisher.publishEvent(new RocketmqEvent(msgs, consumer));
				} catch (Exception e) {
					e.printStackTrace();
					return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
				}
				// 如果没有return success，consumer会重复消费此信息，直到success。
				return ConsumeOrderlyStatus.SUCCESS;
			});
		} else {
			consumer.registerMessageListener((List<MessageExt> msgs, ConsumeConcurrentlyContext context) -> {
				try {
					msgs = filter(msgs);
					if (msgs.size() == 0)
						return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
					this.publisher.publishEvent(new RocketmqEvent(msgs, consumer));
				} catch (Exception e) {
					e.printStackTrace();
					return ConsumeConcurrentlyStatus.RECONSUME_LATER;
				}
				// 如果没有return success，consumer会重复消费此信息，直到success。
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			});
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(5000);// 延迟5秒再启动，主要是等待spring事件监听相关程序初始化完成

					try {
						consumer.start();
					} catch (Exception e) {
						log.info("RocketMq pushConsumer Start failure!!!.");
						log.severe(e.toString());
					}
					log.info("RocketMq pushConsumer Started.");
				} catch (InterruptedException e) {
					log.severe(e.toString());
				}
			}
		}).start();
		return consumer;
	}

	private List<MessageExt> filter(List<MessageExt> msgs) {
		if (isFirstSub && !properties.isEnableHisConsumer()) {
			msgs = msgs.stream().filter(item -> startTime - item.getBornTimestamp() < 0).collect(Collectors.toList());
		}
		if (isFirstSub && msgs.size() > 0) {
			isFirstSub = false;
		}
		return msgs;
	}
}
