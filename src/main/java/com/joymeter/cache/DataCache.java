package com.joymeter.cache;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 对接收的数据进行缓存
 * 
 * @author Pan Shuoting
 *
 */
public class DataCache {
	
	private static final Queue<Map<String, String>> EVENT_DATA = new ConcurrentLinkedQueue<>();
	
	/**
	 * 添加到Cache
	 * 
	 * @param value
	 */
	public static void add(Map<String,String> value) {
		EVENT_DATA.add(value);
	}
	
	/**
	 * 取数据
	 * 
	 * @return
	 */
	public static Map<String, String> poll() {
		if(EVENT_DATA.isEmpty()) {
			return null;
		}
		return EVENT_DATA.poll();
	}
	
	/**
	 * 判断是否为空
	 * 
	 * @return
	 */
	public static boolean isEmpty() {
		return EVENT_DATA.isEmpty();
	}
}
