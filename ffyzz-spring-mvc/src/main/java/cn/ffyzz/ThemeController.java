package cn.ffyzz;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/12/2
 */

@Controller
public class ThemeController {

	@GetMapping("/theme")
	public String getDefaultTheme() {
		return "theme";
	}

}
