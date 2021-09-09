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

package org.springframework.aop.target;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Base class for dynamic {@link org.springframework.aop.TargetSource} implementations
 * that create new prototype bean instances to support a pooling or
 * new-instance-per-invocation strategy.
 * <p>
 *     实现动态 TargetSource 的基类。该动态 TargetSource 能够创建 prototype 类型的 bean，以支持
 *     Pooling 或者 每次调用得到新实例的策略。
 * </p>
 *
 * <p>Such TargetSources must run in a {@link BeanFactory}, as it needs to
 * call the {@code getBean} method to create a new prototype instance.
 * Therefore, this base class extends {@link AbstractBeanFactoryBasedTargetSource}.
 * <p>
 *     这种类型的 TargetSources 必须在 BeanFactory 中运行，应为需要调用 getBean 方法
 *     获取到一个新的 prototype 实例。所以该类继承了 AbstractBeanFactoryBasedTargetSource。
 * </p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see PrototypeTargetSource
 * @see ThreadLocalTargetSource
 * @see CommonsPool2TargetSource
 */
@SuppressWarnings("serial")
public abstract class AbstractPrototypeBasedTargetSource extends AbstractBeanFactoryBasedTargetSource {

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);

		// Check whether the target bean is defined as prototype.
		// 检查 target bean 是否为 prototype 类型
		if (!beanFactory.isPrototype(getTargetBeanName())) {
			throw new BeanDefinitionStoreException(
					"Cannot use prototype-based TargetSource against non-prototype bean with name '" +
					getTargetBeanName() + "': instances would not be independent");
		}
	}

	/**
	 * Subclasses should call this method to create a new prototype instance.
	 * <p>
	 *     创建一个 prototype 类型的 bean
	 * </p>
	 * @throws BeansException if bean creation failed
	 */
	protected Object newPrototypeInstance() throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new instance of bean '" + getTargetBeanName() + "'");
		}
		// 通过 getBean 创建
		return getBeanFactory().getBean(getTargetBeanName());
	}

	/**
	 * Subclasses should call this method to destroy an obsolete prototype instance.
	 * <p>
	 *     销毁一个 prototype 类型的 bean
	 * </p>
	 * @param target the bean instance to destroy
	 */
	protected void destroyPrototypeInstance(Object target) {
		if (logger.isDebugEnabled()) {
			logger.debug("Destroying instance of bean '" + getTargetBeanName() + "'");
		}
		if (getBeanFactory() instanceof ConfigurableBeanFactory) {
			// 调用 destroyBean 销毁
			// 其实就是封装成 DisposableBeanAdapter，并调用其 destroy 方法
			((ConfigurableBeanFactory) getBeanFactory()).destroyBean(getTargetBeanName(), target);
		}
		else if (target instanceof DisposableBean) {
			try {
				// 直接调用 DisposableBean#destroy 方法
				((DisposableBean) target).destroy();
			}
			catch (Throwable ex) {
				logger.warn("Destroy method on bean with name '" + getTargetBeanName() + "' threw an exception", ex);
			}
		}
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("A prototype-based TargetSource itself is not deserializable - " +
				"just a disconnected SingletonTargetSource or EmptyTargetSource is");
	}

	/**
	 * Replaces this object with a SingletonTargetSource on serialization.
	 * Protected as otherwise it won't be invoked for subclasses.
	 * (The {@code writeReplace()} method must be visible to the class
	 * being serialized.)
	 * <p>With this implementation of this method, there is no need to mark
	 * non-serializable fields in this class or subclasses as transient.
	 */
	protected Object writeReplace() throws ObjectStreamException {
		if (logger.isDebugEnabled()) {
			logger.debug("Disconnecting TargetSource [" + this + "]");
		}
		try {
			// Create disconnected SingletonTargetSource/EmptyTargetSource.
			Object target = getTarget();
			return (target != null ? new SingletonTargetSource(target) :
					EmptyTargetSource.forClass(getTargetClass()));
		}
		catch (Exception ex) {
			String msg = "Cannot get target for disconnecting TargetSource [" + this + "]";
			logger.error(msg, ex);
			throw new NotSerializableException(msg + ": " + ex);
		}
	}

}
