package cn.ffyzz.test;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/8
 */
public class TankRemanufacture implements MethodInterceptor {
	@Override
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
		if (method.getName().equals("run")) {
			System.out.println("正在重造59坦克...");
			System.out.println("重造成功，已获取 ✨59改 之 超音速飞行版✨");
			System.out.println("已起飞，正在突破音障。");
			methodProxy.invokeSuper(o, objects);
			System.out.println("已击落黑鸟 SR-71，正在返航...");
			return null;
		}
		return methodProxy.invokeSuper(o, objects);
	}
}
