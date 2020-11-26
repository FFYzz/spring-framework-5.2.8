package cn.ffyzz.test;

import cn.ffyzz.test.entity.User;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/1
 */
public class UserServiceImpl implements UserService {
	@Override
	public void save(User user) {
		System.out.println("save User");
	}

	@Override
	public void update(User user) {
		System.out.println("update user");
	}
}
