/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.aop.framework.adapter;

import java.io.Serializable;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * MethodBeforeAdvice 对 AdvisorAdapter 的实现，
 * 将 Advisor 适配成 MethodBeforeAdviceInterceptor
 *
 * Adapter to enable {@link org.springframework.aop.MethodBeforeAdvice}
 * to be used in the Spring AOP framework.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
class MethodBeforeAdviceAdapter implements AdvisorAdapter, Serializable {

	/**
	 * advice 是否为 MethodBeforeAdvice
	 *
	 * @param advice an Advice such as a BeforeAdvice
	 * @return
	 */
	@Override
	public boolean supportsAdvice(Advice advice) {
		return (advice instanceof MethodBeforeAdvice);
	}

	/**
	 * 适配器，将 Advisor 转为 MethodInterceptor
	 *
	 * @param advisor the Advisor. The supportsAdvice() method must have
	 * returned true on this object
	 * @return
	 */
	@Override
	public MethodInterceptor getInterceptor(Advisor advisor) {
		// 上面判断为是的话下面直接强转
		MethodBeforeAdvice advice = (MethodBeforeAdvice) advisor.getAdvice();
		// 封装成 MethodBeforeAdviceInterceptor
		return new MethodBeforeAdviceInterceptor(advice);
	}

}
