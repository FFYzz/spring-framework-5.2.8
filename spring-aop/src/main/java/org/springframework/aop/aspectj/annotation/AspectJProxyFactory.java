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

package org.springframework.aop.aspectj.annotation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJProxyUtils;
import org.springframework.aop.aspectj.SimpleAspectInstanceFactory;
import org.springframework.aop.framework.ProxyCreatorSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 生成 AOP 代理对象的一种方式
 *
 * AspectJ-based proxy factory, allowing for programmatic building
 * of proxies which include AspectJ aspects (code style as well
 * Java 5 annotation style).
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 * @see #addAspect(Object)
 * @see #addAspect(Class)
 * @see #getProxy()
 * @see #getProxy(ClassLoader)
 * @see org.springframework.aop.framework.ProxyFactory
 */
@SuppressWarnings("serial")
public class AspectJProxyFactory extends ProxyCreatorSupport {

	/** Cache for singleton aspect instances. */
	private static final Map<Class<?>, Object> aspectCache = new ConcurrentHashMap<>();

	/**
	 * 保存所有的 AspectJAdvisor 工厂类
	 */
	private final AspectJAdvisorFactory aspectFactory = new ReflectiveAspectJAdvisorFactory();


	/**
	 * Create a new AspectJProxyFactory.
	 */
	public AspectJProxyFactory() {
	}

	/**
	 * Create a new AspectJProxyFactory.
	 * <p>Will proxy all interfaces that the given target implements.
	 * <p>
	 *     会代理 target 实现的所有的接口
	 * @param target the target object to be proxied
	 *               代理对象
	 */
	public AspectJProxyFactory(Object target) {
		Assert.notNull(target, "Target object must not be null");
		setInterfaces(ClassUtils.getAllInterfaces(target));
		setTarget(target);
	}

	/**
	 * Create a new {@code AspectJProxyFactory}.
	 * No target, only interfaces. Must add interceptors.
	 */
	public AspectJProxyFactory(Class<?>... interfaces) {
		// 设置代理的接口
		setInterfaces(interfaces);
	}


	/**
	 * Add the supplied aspect instance to the chain. The type of the aspect instance
	 * supplied must be a singleton aspect. True singleton lifecycle is not honoured when
	 * using this method - the caller is responsible for managing the lifecycle of any
	 * aspects added in this way.
	 * <p>
	 *     添加切面，切面必须是单例的。添加的是实例
	 * </p>
	 * @param aspectInstance the AspectJ aspect instance
	 */
	public void addAspect(Object aspectInstance) {
		// 切面 class
		Class<?> aspectClass = aspectInstance.getClass();
		// 切面类名
		String aspectName = aspectClass.getName();
		// 创建切面元信息对象
		AspectMetadata am = createAspectMetadata(aspectClass, aspectName);
		if (am.getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON) {
			throw new IllegalArgumentException(
					"Aspect class [" + aspectClass.getName() + "] does not define a singleton aspect");
		}
		// 添加到 advisor 中去
		addAdvisorsFromAspectInstanceFactory(
				// 返回一个工厂类
				new SingletonMetadataAwareAspectInstanceFactory(aspectInstance, aspectName));
	}

	/**
	 * <p>
	 * 		添加切面，添加的是类。
	 * </p>
	 * Add an aspect of the supplied type to the end of the advice chain.
	 * @param aspectClass the AspectJ aspect class
	 */
	public void addAspect(Class<?> aspectClass) {
		String aspectName = aspectClass.getName();
		// 创建 AspectMetadata 元信息
		AspectMetadata am = createAspectMetadata(aspectClass, aspectName);
		// 创建工厂
		MetadataAwareAspectInstanceFactory instanceFactory = createAspectInstanceFactory(am, aspectClass, aspectName);
		// 将工厂中的 advisor 添加到 chain 中去
		addAdvisorsFromAspectInstanceFactory(instanceFactory);
	}


	/**
	 * Add all {@link Advisor Advisors} from the supplied {@link MetadataAwareAspectInstanceFactory}
	 * to the current chain. Exposes any special purpose {@link Advisor Advisors} if needed.
	 * <p>
	 *     将工厂中的 advisor 添加到 chain 中
	 * </p>
	 * @see AspectJProxyUtils#makeAdvisorChainAspectJCapableIfNecessary(List)
	 */
	private void addAdvisorsFromAspectInstanceFactory(MetadataAwareAspectInstanceFactory instanceFactory) {
		// 获取目前所有的 advisor
		List<Advisor> advisors = this.aspectFactory.getAdvisors(instanceFactory);
		// 代理类对象
		Class<?> targetClass = getTargetClass();
		Assert.state(targetClass != null, "Unresolvable target class");
		// 能够应用在 targetClass 上的 advisor
		advisors = AopUtils.findAdvisorsThatCanApply(advisors, targetClass);
		AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(advisors);
		// 排序
		AnnotationAwareOrderComparator.sort(advisors);
		// 添加 advisor
		addAdvisors(advisors);
	}

	/**
	 * Create an {@link AspectMetadata} instance for the supplied aspect type.
	 */
	private AspectMetadata createAspectMetadata(Class<?> aspectClass, String aspectName) {
		AspectMetadata am = new AspectMetadata(aspectClass, aspectName);
		if (!am.getAjType().isAspect()) {
			throw new IllegalArgumentException("Class [" + aspectClass.getName() + "] is not a valid aspect type");
		}
		return am;
	}

	/**
	 * Create a {@link MetadataAwareAspectInstanceFactory} for the supplied aspect type. If the aspect type
	 * has no per clause, then a {@link SingletonMetadataAwareAspectInstanceFactory} is returned, otherwise
	 * a {@link PrototypeAspectInstanceFactory} is returned.
	 */
	private MetadataAwareAspectInstanceFactory createAspectInstanceFactory(
			AspectMetadata am, Class<?> aspectClass, String aspectName) {

		MetadataAwareAspectInstanceFactory instanceFactory;
		// 判断 AspectMetadata 是否是单例
		if (am.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
			// Create a shared aspect instance.
			// 根据类型去查找对象实例
			Object instance = getSingletonAspectInstance(aspectClass);
			// 查找到的 Aspect 对象实例封装进 SingletonMetadataAwareAspectInstanceFactory 中去
			instanceFactory = new SingletonMetadataAwareAspectInstanceFactory(instance, aspectName);
		}
		else {
			// Create a factory for independent aspect instances.
			instanceFactory = new SimpleMetadataAwareAspectInstanceFactory(aspectClass, aspectName);
		}
		// 返回工厂
		return instanceFactory;
	}

	/**
	 * Get the singleton aspect instance for the supplied aspect type. An instance
	 * is created if one cannot be found in the instance cache.
	 * <p>
	 *     根据类型查找对象实例
	 * </p>
	 */
	private Object getSingletonAspectInstance(Class<?> aspectClass) {
		// Quick check without a lock...
		// 先查缓存
		Object instance = aspectCache.get(aspectClass);
		if (instance == null) {
			// 加锁查找
			synchronized (aspectCache) {
				// To be safe, check within full lock now...
				instance = aspectCache.get(aspectClass);
				if (instance == null) {
					// 创建
					instance = new SimpleAspectInstanceFactory(aspectClass).getAspectInstance();
					aspectCache.put(aspectClass, instance);
				}
			}
		}
		return instance;
	}


	/**
	 * Create a new proxy according to the settings in this factory.
	 * <p>Can be called repeatedly. Effect will vary if we've added
	 * or removed interfaces. Can add and remove interceptors.
	 * <p>Uses a default class loader: Usually, the thread context class loader
	 * (if necessary for proxy creation).
	 * @return the new proxy
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProxy() {
		return (T) createAopProxy().getProxy();
	}

	/**
	 * Create a new proxy according to the settings in this factory.
	 * <p>Can be called repeatedly. Effect will vary if we've added
	 * or removed interfaces. Can add and remove interceptors.
	 * <p>Uses the given class loader (if necessary for proxy creation).
	 * @param classLoader the class loader to create the proxy with
	 * @return the new proxy
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProxy(ClassLoader classLoader) {
		return (T) createAopProxy().getProxy(classLoader);
	}

}
