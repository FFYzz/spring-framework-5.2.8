package cn.ffyzz.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/8
 */
public class JdkProxyCreator implements ProxyCreator, InvocationHandler {

	Object target;

	public JdkProxyCreator(Object target) {
		this.target = target;
	}

	@Override
	public Object getProxy() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<?>[] interfaces = target.getClass().getInterfaces();
		return Proxy.newProxyInstance(classLoader, interfaces, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("before");
		Object retVal = method.invoke(target, args);
		System.out.println("after");
		return retVal;
	}
}
