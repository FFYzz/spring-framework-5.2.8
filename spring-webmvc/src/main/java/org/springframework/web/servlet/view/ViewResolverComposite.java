/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.servlet.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * A {@link org.springframework.web.servlet.ViewResolver} that delegates to others.
 *
 * @author Sebastien Deleuze
 * @author Rossen Stoyanchev
 * @since 4.1
 */

/**
 * 是 ViewResolver 的组合
 * 实现了 InitializingBean 接口
 */
public class ViewResolverComposite implements ViewResolver, Ordered, InitializingBean,
		ApplicationContextAware, ServletContextAware {

	/**
	 * 保存持有的 ViewResolver
	 */
	private final List<ViewResolver> viewResolvers = new ArrayList<>();

	/**
	 * 实现了 Order 接口
	 * 默认 Order 为最低的 Order
	 */
	private int order = Ordered.LOWEST_PRECEDENCE;


	/**
	 * Set the list of view viewResolvers to delegate to.
	 */
	public void setViewResolvers(List<ViewResolver> viewResolvers) {
		this.viewResolvers.clear();
		if (!CollectionUtils.isEmpty(viewResolvers)) {
			this.viewResolvers.addAll(viewResolvers);
		}
	}

	/**
	 * 返回持有的所有 ViewResolver
	 *
	 * Return the list of view viewResolvers to delegate to.
	 */
	public List<ViewResolver> getViewResolvers() {
		return Collections.unmodifiableList(this.viewResolvers);
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		for (ViewResolver viewResolver : this.viewResolvers) {
			if (viewResolver instanceof ApplicationContextAware) {
				((ApplicationContextAware)viewResolver).setApplicationContext(applicationContext);
			}
		}
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		for (ViewResolver viewResolver : this.viewResolvers) {
			if (viewResolver instanceof ServletContextAware) {
				((ServletContextAware)viewResolver).setServletContext(servletContext);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 遍历持有的 ViewResolver
		for (ViewResolver viewResolver : this.viewResolvers) {
			// 如果实现了 InitializingBean 接口
			if (viewResolver instanceof InitializingBean) {
				// 调用其 afterPropertiesSet 方法
				((InitializingBean) viewResolver).afterPropertiesSet();
			}
		}
	}

	/**
	 * 重点就是该方法
	 * @param viewName name of the view to resolve
	 * @param locale the Locale in which to resolve the view.
	 * ViewResolvers that support internationalization should respect this.
	 * @return
	 * @throws Exception
	 */
	@Override
	@Nullable
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		for (ViewResolver viewResolver : this.viewResolvers) {
			View view = viewResolver.resolveViewName(viewName, locale);
			if (view != null) {
				return view;
			}
		}
		return null;
	}

}
