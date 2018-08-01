package com.joymeter.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.joymeter.entity.RocketmqEvent;

/**
 * 监听消费者消费事件
 * @author Pan Shuoting
 *
 */
@Component
public class ConsumerListener {
	@EventListener(condition = "#event.msgs[0].topic=='TopicTest1' && #event.msgs[0].tags=='TagA'")
	public void rocketmqMsgListen(RocketmqEvent event) {
		try {
			System.out.println("监听到一个消息达到：" + event.getMsgs().get(0).getMsgId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}