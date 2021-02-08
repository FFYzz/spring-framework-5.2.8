package cn.ffyzz.test;

import cn.ffyzz.test.entity.Tank59;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/8
 */
public class CglibProxyCreatorTest {

	public static void main(String[] args) {
		ProxyCreator proxyCreator = new CglibProxyCreator(new Tank59(), new TankRemanufacture());
		Tank59 tank59 = (Tank59) proxyCreator.getProxy();
		tank59.run();
	}

}
