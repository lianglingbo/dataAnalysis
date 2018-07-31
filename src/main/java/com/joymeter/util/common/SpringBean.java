package com.joymeter.util.common;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 获取bean的辅助类
 * 用于多线程获取容器bean
 * @author Pan Shuoting
 *
 */
public class SpringBean implements ServletContextListener{
	private static WebApplicationContext springContext = null;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if(springContext == null) {
			springContext = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
		}
		
	}

	/**
	 * 根据名称获取bean
	 * @param name
	 * @return
	 */
	public static Object getBean(String name) {
		return springContext.getBean(name);
	}
	
	/**
	 * 根据类获取bean
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		return springContext.getBean(clazz);
	}
}