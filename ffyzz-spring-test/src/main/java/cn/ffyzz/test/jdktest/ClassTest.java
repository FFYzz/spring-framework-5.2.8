package cn.ffyzz.test.jdktest;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/15
 */
public class ClassTest {
	public static void main(String[] args) {
		Father f = new Father();
		System.out.println(f.getClass().isAssignableFrom(Father.class));
		Son s = new Son();
		System.out.println(f.getClass().isAssignableFrom(Son.class));
		System.out.println(s instanceof Father);
	}

}

class Father {

}

class Son extends Father {

}
