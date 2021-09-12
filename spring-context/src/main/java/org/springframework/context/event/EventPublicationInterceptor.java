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

package org.springframework.context.event;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link MethodInterceptor Interceptor} that publishes an
 * {@code ApplicationEvent} to all {@code ApplicationListeners}
 * registered with an {@code ApplicationEventPublisher} after each
 * <i>successful</i> method invocation.
 *
 * <p>Note that this interceptor is only capable of publishing <i>stateless</i>
 * events configured via the
 * {@link #setApplicationEventClass "applicationEventClass"} property.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @author Rick Evans
 * @see #setApplicationEventClass
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.ApplicationEventPublisher
 * @see org.springframework.context.ApplicationContext
 */
public class EventPublicationInterceptor
		implements MethodInterceptor, ApplicationEventPublisherAware, InitializingBean {

	/**
	 * 这个构造器必须是包含一个参数的构造器，需要提前通过 set 方法注入
	 */
	@Nullable
	private Constructor<?> applicationEventClassConstructor;

	@Nullable
	private ApplicationEventPublisher applicationEventPublisher;


	/**
	 * Set the application event class to publish.
	 * <p>
	 *     设置要发布的应用事件类
	 * <p>The event class <b>must</b> have a constructor with a single
	 * {@code Object} argument for the event source. The interceptor
	 * will pass in the invoked object.
	 * @throws IllegalArgumentException if the supplied {@code Class} is
	 * {@code null} or if it is not an {@code ApplicationEvent} subclass or
	 * if it does not expose a constructor that takes a single {@code Object} argument
	 */
	public void setApplicationEventClass(Class<?> applicationEventClass) {
		if (ApplicationEvent.class == applicationEventClass ||
				!ApplicationEvent.class.isAssignableFrom(applicationEventClass)) {
			throw new IllegalArgumentException("'applicationEventClass' needs to extend ApplicationEvent");
		}
		try {
			// 有一个限制，必须有一个包含一个参数，且参数类型是 Object 类型的构造方法，
			this.applicationEventClassConstructor = applicationEventClass.getConstructor(Object.class);
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException("ApplicationEvent class [" +
					applicationEventClass.getName() + "] does not have the required Object constructor: " + ex);
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.applicationEventClassConstructor == null) {
			throw new IllegalArgumentException("Property 'applicationEventClass' is required");
		}
	}


	/**
	 * MethodInterceptor#invoke 调用逻辑
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// 执行方法逻辑
		Object retVal = invocation.proceed();

		Assert.state(this.applicationEventClassConstructor != null, "No ApplicationEvent class set");
		// 创建一个应用事件
		ApplicationEvent event = (ApplicationEvent)
				this.applicationEventClassConstructor.newInstance(invocation.getThis());

		Assert.state(this.applicationEventPublisher != null, "No ApplicationEventPublisher available");
		// 发布事件
		this.applicationEventPublisher.publishEvent(event);

		// 返回方法的执行结果
		return retVal;
	}

}
