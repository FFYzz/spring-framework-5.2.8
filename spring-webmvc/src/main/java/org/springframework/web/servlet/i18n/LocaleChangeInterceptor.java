/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Interceptor that allows for changing the current locale on every request,
 * via a configurable request parameter (default parameter name: "locale").
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 20.06.2003
 * @see org.springframework.web.servlet.LocaleResolver
 */

/**
 * Locale 变化拦截器
 */
public class LocaleChangeInterceptor extends HandlerInterceptorAdapter {

	/**
	 * 默认监听请求中的参数名
	 *
	 * Default name of the locale specification parameter: "locale".
	 */
	public static final String DEFAULT_PARAM_NAME = "locale";


	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 监听的 request 中的参数名
	 */
	private String paramName = DEFAULT_PARAM_NAME;


	/**
	 * 拦截的 Http 方法类型
	 * 为空表示所有的都拦截
	 */
	@Nullable
	private String[] httpMethods;

	/**
	 * 是否忽略无效的 Locale 值
	 */
	private boolean ignoreInvalidLocale = false;


	/**
	 * 设置监听的参数名
	 *
	 * Set the name of the parameter that contains a locale specification
	 * in a locale change request. Default is "locale".
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * Return the name of the parameter that contains a locale specification
	 * in a locale change request.
	 */
	public String getParamName() {
		return this.paramName;
	}

	/**
	 * 设置要拦截的 Http 方法
	 *
	 * Configure the HTTP method(s) over which the locale can be changed.
	 * @param httpMethods the methods
	 * @since 4.2
	 */
	public void setHttpMethods(@Nullable String... httpMethods) {
		this.httpMethods = httpMethods;
	}

	/**
	 * Return the configured HTTP methods.
	 * @since 4.2
	 */
	@Nullable
	public String[] getHttpMethods() {
		return this.httpMethods;
	}

	/**
	 * 设置受忽略无效的 locale 值
	 *
	 * Set whether to ignore an invalid value for the locale parameter.
	 * @since 4.2.2
	 */
	public void setIgnoreInvalidLocale(boolean ignoreInvalidLocale) {
		this.ignoreInvalidLocale = ignoreInvalidLocale;
	}

	/**
	 * Return whether to ignore an invalid value for the locale parameter.
	 * @since 4.2.2
	 */
	public boolean isIgnoreInvalidLocale() {
		return this.ignoreInvalidLocale;
	}

	/**
	 * Specify whether to parse request parameter values as BCP 47 language tags
	 * instead of Java's legacy locale specification format.
	 * <p><b>NOTE: As of 5.1, this resolver leniently accepts the legacy
	 * {@link Locale#toString} format as well as BCP 47 language tags.</b>
	 * @since 4.3
	 * @see Locale#forLanguageTag(String)
	 * @see Locale#toLanguageTag()
	 * @deprecated as of 5.1 since it only accepts {@code true} now
	 */
	@Deprecated
	public void setLanguageTagCompliant(boolean languageTagCompliant) {
		if (!languageTagCompliant) {
			throw new IllegalArgumentException("LocaleChangeInterceptor always accepts BCP 47 language tags");
		}
	}

	/**
	 * Return whether to use BCP 47 language tags instead of Java's legacy
	 * locale specification format.
	 * @since 4.3
	 * @deprecated as of 5.1 since it always returns {@code true} now
	 */
	@Deprecated
	public boolean isLanguageTagCompliant() {
		return true;
	}


	/**
	 * 前置拦截
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws ServletException
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws ServletException {
		// 获取 request 的参数值
		String newLocale = request.getParameter(getParamName());
		if (newLocale != null) {
			if (checkHttpMethod(request.getMethod())) {
				// 获取 LocaleResolver
				LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
				if (localeResolver == null) {
					// DispatcherServlet 中一定有定义 LocaleResolver
					// 没有的话就抛异常
					throw new IllegalStateException(
							"No LocaleResolver found: not in a DispatcherServlet request?");
				}
				try {
					// 将 Locale 设置为参数中解析出来的 Locale
					localeResolver.setLocale(request, response, parseLocaleValue(newLocale));
				}
				catch (IllegalArgumentException ex) {
					if (isIgnoreInvalidLocale()) {
						if (logger.isDebugEnabled()) {
							logger.debug("Ignoring invalid locale value [" + newLocale + "]: " + ex.getMessage());
						}
					}
					else {
						throw ex;
					}
				}
			}
		}
		// Proceed in any case.
		// 直接放过
		return true;
	}

	/**
	 * 检查 http 方法是否满足要求
	 *
	 * @param currentMethod
	 * @return
	 */
	private boolean checkHttpMethod(String currentMethod) {
		String[] configuredMethods = getHttpMethods();
		if (ObjectUtils.isEmpty(configuredMethods)) {
			return true;
		}
		for (String configuredMethod : configuredMethods) {
			if (configuredMethod.equalsIgnoreCase(currentMethod)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parse the given locale value as coming from a request parameter.
	 * <p>The default implementation calls {@link StringUtils#parseLocale(String)},
	 * accepting the {@link Locale#toString} format as well as BCP 47 language tags.
	 * @param localeValue the locale value to parse
	 * @return the corresponding {@code Locale} instance
	 * @since 4.3
	 */
	@Nullable
	protected Locale parseLocaleValue(String localeValue) {
		return StringUtils.parseLocale(localeValue);
	}

}
