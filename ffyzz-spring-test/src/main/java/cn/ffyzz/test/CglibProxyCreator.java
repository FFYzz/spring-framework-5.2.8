package cn.ffyzz.test;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/8
 */
public class CglibProxyCreator implements ProxyCreator{

	private Object target;

	private MethodInterceptor methodInterceptor;

	public CglibProxyCreator(Object target, MethodInterceptor methodInterceptor) {
		this.target = target;
		this.methodInterceptor = methodInterceptor;
	}

	@Override
	public Object getProxy() {
		Enhancer enhancer = new Enhancer();
		// 设置代理类的父类
		enhancer.setSuperclass(target.getClass());
		// 设置代理逻辑
		enhancer.setCallback(methodInterceptor);
		// 创建代理对象
		return enhancer.create();
	}
}
