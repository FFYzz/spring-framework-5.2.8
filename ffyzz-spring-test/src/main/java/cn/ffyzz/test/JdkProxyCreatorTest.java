package cn.ffyzz.test;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/8
 */
public class JdkProxyCreatorTest {

	public static void main(String[] args) {
		ProxyCreator proxyCreator = new JdkProxyCreator(new UserServiceImpl());
		UserService userService = (UserService) proxyCreator.getProxy();
		userService.save(null);
	}

}
