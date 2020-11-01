package cn.ffyzz.test;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/1
 */
public class UserServiceImpl implements UserService {
	@Override
	public String getName(String firstName, String lastName) {
		return String.format("I am %s%s", firstName, lastName);
	}
}
