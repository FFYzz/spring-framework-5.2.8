package cn.ffyzz.advice;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/28
 */

@ControllerAdvice
public class ControllerAdviceBeanTest {

	@InitBinder
	public void testDataBinder22(WebDataBinder binder) {


	}

	@ModelAttribute("test")
	public String testModelAttribute(String comment){
		return comment + " handled by testModelAttribute";
	}


}
