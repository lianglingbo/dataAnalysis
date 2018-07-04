package com.joymeter.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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

	public static Object getBean(String name) {
		return springContext.getBean(name);
	}
	
	public static <T> T getBean(Class<T> clazz) {
		return springContext.getBean(clazz);
	}
}
