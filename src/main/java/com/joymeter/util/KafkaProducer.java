package com.joymeter.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import com.joymeter.cache.DataCache;

/**
 * kafka消息生产者
 * @author Pan Shuoting
 *
 */
@Component
public class KafkaProducer {
	private static final Logger logger = Logger.getLogger(KafkaProducer.class.getName());

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	private Map<String, String> datamap = new HashMap<>();

	/**
	 * 发送消息
	 * @param topic
	 * @param msg
	 */
	public void sendMessage(String topic, String msg) {
		ListenableFuture<?> future = kafkaTemplate.send(topic,msg);
		future.addCallback(o -> logger.log(Level.INFO,"主题 "+topic+" 消息发送成功：" + msg),
				throwable -> {
					logger.log(Level.SEVERE,"主题 "+topic+" 消息发送失败：" + msg);
					datamap.put("topic", topic);
					datamap.put("value", msg);
					DataCache.add(datamap);   //存入缓存，等待再次发送
				});
	}
}
