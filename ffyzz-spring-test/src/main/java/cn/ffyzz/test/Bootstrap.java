package cn.ffyzz.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/1
 */
public class Bootstrap {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/spring-config.xml");
		UserService userService = (UserService) applicationContext.getBean("userService");
		System.out.println(userService.getName("FFYzz", "Tizs"));
	}

}
