/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.serviceloader;

import java.util.ServiceLoader;

import org.springframework.beans.factory.BeanClassLoaderAware;

/**
 * {@link org.springframework.beans.factory.FactoryBean} that exposes the
 * JDK 1.6 {@link java.util.ServiceLoader} for the configured service class.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */

/**
 * 通过 SPI 的方式为接口提供实现类
 * 并且获得实现类的实例
 * 该 FactoryBean 的作用就是生成一个 ServiceLoader 类型的 Bean
 */
public class ServiceLoaderFactoryBean extends AbstractServiceLoaderBasedFactoryBean implements BeanClassLoaderAware {

	/**
	 * 直接返回 serviceLoader，自己处理
	 * 返回类型为 ServiceLoader
	 *
	 * @param serviceLoader the ServiceLoader for the configured service class
	 * @return
	 */
	@Override
	protected Object getObjectToExpose(ServiceLoader<?> serviceLoader) {
		// 返回 serviceLoader
		return serviceLoader;
	}

	@Override
	public Class<?> getObjectType() {
		return ServiceLoader.class;
	}

}
