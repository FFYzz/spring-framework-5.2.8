/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * {@link org.springframework.web.servlet.ThemeResolver} implementation that
 * uses a theme attribute in the user's session in case of a custom setting,
 * with a fallback to the default theme. This is most appropriate if the
 * application needs user sessions anyway.
 *
 * <p>Custom controllers can override the user's theme by calling
 * {@code setThemeName}, e.g. responding to a theme change request.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 17.06.2003
 * @see #setThemeName
 */

/**
 * 主题保存到 session 中，可以修改
 */
public class SessionThemeResolver extends AbstractThemeResolver {

	/**
	 * Name of the session attribute that holds the theme name.
	 * Only used internally by this implementation.
	 * Use {@code RequestContext(Utils).getTheme()}
	 * to retrieve the current theme in controllers or views.
	 * @see org.springframework.web.servlet.support.RequestContext#getTheme
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getTheme
	 */
	public static final String THEME_SESSION_ATTRIBUTE_NAME = SessionThemeResolver.class.getName() + ".THEME";


	@Override
	public String resolveThemeName(HttpServletRequest request) {
		// 获取 session 属性名为 THEME_SESSION_ATTRIBUTE_NAME 的 themeName
		String themeName = (String) WebUtils.getSessionAttribute(request, THEME_SESSION_ATTRIBUTE_NAME);
		// A specific theme indicated, or do we need to fallback to the default?
		// 如果不为空则返回，否则返回默认的
		return (themeName != null ? themeName : getDefaultThemeName());
	}

	/**
	 * 将 themeName 设置到 session 中
	 * @param request the request to be used for theme name modification
	 * @param response the response to be used for theme name modification
	 * @param themeName the new theme name ({@code null} or empty to reset it)
	 */
	@Override
	public void setThemeName(
			HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable String themeName) {

		WebUtils.setSessionAttribute(request, THEME_SESSION_ATTRIBUTE_NAME,
				(StringUtils.hasText(themeName) ? themeName : null));
	}

}
