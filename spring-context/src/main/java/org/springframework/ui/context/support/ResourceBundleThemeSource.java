/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.ui.context.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.HierarchicalThemeSource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;

/**
 * {@link ThemeSource} implementation that looks up an individual
 * {@link java.util.ResourceBundle} per theme. The theme name gets
 * interpreted as ResourceBundle basename, supporting a common
 * basename prefix for all themes.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @see #setBasenamePrefix
 * @see java.util.ResourceBundle
 * @see org.springframework.context.support.ResourceBundleMessageSource
 */

/**
 * 层次型结构
 * 基于 Properties 文件的 theme 资源文件
 */
public class ResourceBundleThemeSource implements HierarchicalThemeSource, BeanClassLoaderAware {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 父 themeSource
	 */
	@Nullable
	private ThemeSource parentThemeSource;

	/**
	 * 前缀
	 */
	private String basenamePrefix = "";

	/**
	 * 默认编码
	 */
	@Nullable
	private String defaultEncoding;

	/**
	 * 是否以系统 locale 进行兜底
	 */
	@Nullable
	private Boolean fallbackToSystemLocale;

	@Nullable
	private ClassLoader beanClassLoader;

	/**
	 * theme 缓存
	 * theme name -> Theme
	 */
	/** Map from theme name to Theme instance. */
	private final Map<String, Theme> themeCache = new ConcurrentHashMap<>();


	@Override
	public void setParentThemeSource(@Nullable ThemeSource parent) {
		this.parentThemeSource = parent;

		// Update existing Theme objects.
		// Usually there shouldn't be any at the time of this call.
		synchronized (this.themeCache) {
			for (Theme theme : this.themeCache.values()) {
				initParent(theme);
			}
		}
	}

	@Override
	@Nullable
	public ThemeSource getParentThemeSource() {
		return this.parentThemeSource;
	}

	/**
	 * Set the prefix that gets applied to the ResourceBundle basenames,
	 * i.e. the theme names.
	 * E.g.: basenamePrefix="test.", themeName="theme" -> basename="test.theme".
	 * <p>Note that ResourceBundle names are effectively classpath locations: As a
	 * consequence, the JDK's standard ResourceBundle treats dots as package separators.
	 * This means that "test.theme" is effectively equivalent to "test/theme",
	 * just like it is for programmatic {@code java.util.ResourceBundle} usage.
	 * @see java.util.ResourceBundle#getBundle(String)
	 */
	public void setBasenamePrefix(@Nullable String basenamePrefix) {
		this.basenamePrefix = (basenamePrefix != null ? basenamePrefix : "");
	}

	/**
	 * Set the default charset to use for parsing resource bundle files.
	 * <p>{@link ResourceBundleMessageSource}'s default is the
	 * {@code java.util.ResourceBundle} default encoding: ISO-8859-1.
	 * @since 4.2
	 * @see ResourceBundleMessageSource#setDefaultEncoding
	 */
	public void setDefaultEncoding(@Nullable String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Set whether to fall back to the system Locale if no files for a
	 * specific Locale have been found.
	 * <p>{@link ResourceBundleMessageSource}'s default is "true".
	 * @since 4.2
	 * @see ResourceBundleMessageSource#setFallbackToSystemLocale
	 */
	public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
		this.fallbackToSystemLocale = fallbackToSystemLocale;
	}

	@Override
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	/**
	 * 根据 theme name 获取 Theme 实例
	 *
	 * This implementation returns a SimpleTheme instance, holding a
	 * ResourceBundle-based MessageSource whose basename corresponds to
	 * the given theme name (prefixed by the configured "basenamePrefix").
	 * <p>SimpleTheme instances are cached per theme name. Use a reloadable
	 * MessageSource if themes should reflect changes to the underlying files.
	 * @see #setBasenamePrefix
	 * @see #createMessageSource
	 */
	@Override
	@Nullable
	public Theme getTheme(String themeName) {
		// 先查询缓存
		Theme theme = this.themeCache.get(themeName);
		if (theme == null) {
			synchronized (this.themeCache) {
				theme = this.themeCache.get(themeName);
				if (theme == null) {
					// 获取完整的 theme name
					String basename = this.basenamePrefix + themeName;
					// 获取 MessageSource
					MessageSource messageSource = createMessageSource(basename);
					// 创建一个 Theme
					theme = new SimpleTheme(themeName, messageSource);
					// 初始化 themeSource 的 parent 的 messageSource
					initParent(theme);
					// 放入缓存
					this.themeCache.put(themeName, theme);
					if (logger.isDebugEnabled()) {
						logger.debug("Theme created: name '" + themeName + "', basename [" + basename + "]");
					}
				}
			}
		}
		return theme;
	}

	/**
	 * 为给定的 theme name 创建一个 MessageSource
	 * 返回一个 ResourceBundleMessageSource
	 *
	 * Create a MessageSource for the given basename,
	 * to be used as MessageSource for the corresponding theme.
	 * <p>Default implementation creates a ResourceBundleMessageSource.
	 * for the given basename. A subclass could create a specifically
	 * configured ReloadableResourceBundleMessageSource, for example.
	 * @param basename the basename to create a MessageSource for
	 * @return the MessageSource
	 * @see org.springframework.context.support.ResourceBundleMessageSource
	 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
	 */
	protected MessageSource createMessageSource(String basename) {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(basename);
		if (this.defaultEncoding != null) {
			messageSource.setDefaultEncoding(this.defaultEncoding);
		}
		if (this.fallbackToSystemLocale != null) {
			messageSource.setFallbackToSystemLocale(this.fallbackToSystemLocale);
		}
		if (this.beanClassLoader != null) {
			messageSource.setBeanClassLoader(this.beanClassLoader);
		}
		return messageSource;
	}

	/**
	 * 设置当前 themeSource 的 parentThemeSource 的 messageSource 为 themeSource parent 绑定的 messageSource
	 *
	 * Initialize the MessageSource of the given theme with the
	 * one from the corresponding parent of this ThemeSource.
	 * @param theme the Theme to (re-)initialize
	 */
	protected void initParent(Theme theme) {
		// 如果是层次性 MessageSource
		if (theme.getMessageSource() instanceof HierarchicalMessageSource) {
			// 转成 HierarchicalMessageSource 类型
			HierarchicalMessageSource messageSource = (HierarchicalMessageSource) theme.getMessageSource();
			// themeSource 的 parent 不为 null 但是与 themeSource 绑定的 MessageSource 的 parent 为 null
			if (getParentThemeSource() != null && messageSource.getParentMessageSource() == null) {
				// 获取 parent theme
				Theme parentTheme = getParentThemeSource().getTheme(theme.getName());
				if (parentTheme != null) {
					// 给 themeSource 绑定的 messageSource 设置 parent
					messageSource.setParentMessageSource(parentTheme.getMessageSource());
				}
			}
		}
	}

}
