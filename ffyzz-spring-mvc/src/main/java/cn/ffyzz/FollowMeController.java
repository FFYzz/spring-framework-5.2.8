package cn.ffyzz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/11/19
 */
@Controller
@SessionAttributes("articleId")
public class FollowMeController {

	private final Log logger = LogFactory.getLog(FollowMeController.class);
	private final String[] sensitiveWords = new String[]{"k1", "s2"};

	@ModelAttribute("comment")
	public String replaceSensitiveWords(String comment) {
		if (comment != null) {
			logger.info("origin comment: " + comment);
			for (String str : sensitiveWords) {
				comment = comment.replaceAll(str, "**");
			}
			logger.info("modified comment: " + comment);
		}
		return comment;
	}

	@RequestMapping(value = {"/articles/{articleId}/comment"})
	public String doComment(@PathVariable String articleId, RedirectAttributes attributes, Model model) {
		attributes.addFlashAttribute("comment", model.asMap().get("comment"));
		model.addAttribute("articleId", articleId);
		return "redirect:/showArticle";
	}

	@RequestMapping(value = "showArticle", method = RequestMethod.POST)
	public String showArticle(Model model, SessionStatus sessionStatus) {
		String articleId = (String) model.asMap().get("articleId");
		model.addAttribute("articleTitle", articleId + "号文章标题");
		model.addAttribute("article", articleId + "号文章内容");
		sessionStatus.setComplete();
		return "article";
	}


}
