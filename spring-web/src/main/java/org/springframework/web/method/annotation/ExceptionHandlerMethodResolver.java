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

package org.springframework.web.method.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ExceptionDepthComparator;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Discovers {@linkplain ExceptionHandler @ExceptionHandler} methods in a given class,
 * including all of its superclasses, and helps to resolve a given {@link Exception}
 * to the exception types supported by a given {@link Method}.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */

/**
 * 是添加了 @ExceptionHandler 注解的方法的解析器
 */
public class ExceptionHandlerMethodResolver {

	/**
	 * 过滤器，检查方法上是否标注了 @ExceptionHandler 注解
	 *
	 * A filter for selecting {@code @ExceptionHandler} methods.
	 */
	public static final MethodFilter EXCEPTION_HANDLER_METHODS = method ->
			AnnotatedElementUtils.hasAnnotation(method, ExceptionHandler.class);


	/**
	 * 能处理的异常与 method 的关系
	 */
	private final Map<Class<? extends Throwable>, Method> mappedMethods = new HashMap<>(16);

	/**
	 * 已经处理过的上述 k-v 对
	 */
	private final Map<Class<? extends Throwable>, Method> exceptionLookupCache = new ConcurrentReferenceHashMap<>(16);


	/**
	 * A constructor that finds {@link ExceptionHandler} methods in the given type.
	 * @param handlerType the type to introspect
	 */
	public ExceptionHandlerMethodResolver(Class<?> handlerType) {
		// 遍历所有 @ExceptionHandler 注解的方法
		for (Method method : MethodIntrospector.selectMethods(handlerType, EXCEPTION_HANDLER_METHODS)) {
			// 遍历 @ExceptionHandler 注解能够处理的所有的异常类
			for (Class<? extends Throwable> exceptionType : detectExceptionMappings(method)) {
				// 将每个异常与其能够处理的 method 对应起来，放到 map 中
				addExceptionMapping(exceptionType, method);
			}
		}
	}


	/**
	 * Extract exception mappings from the {@code @ExceptionHandler} annotation first,
	 * and then as a fallback from the method signature itself.
	 */
	@SuppressWarnings("unchecked")
	private List<Class<? extends Throwable>> detectExceptionMappings(Method method) {
		List<Class<? extends Throwable>> result = new ArrayList<>();
		// 获得 @ExceptionHandler 注解的属性中获取能处理的异常类型列表
		detectAnnotationExceptionMappings(method, result);
		if (result.isEmpty()) {
			// 如果获取不到，则从注解方法的参数上获取异常类型
			for (Class<?> paramType : method.getParameterTypes()) {
				if (Throwable.class.isAssignableFrom(paramType)) {
					// 放到 result 中
					result.add((Class<? extends Throwable>) paramType);
				}
			}
		}
		// 还为空就抛出异常
		if (result.isEmpty()) {
			throw new IllegalStateException("No exception types mapped to " + method);
		}
		return result;
	}

	/**
	 * 返回 method 上 @ExceptionHandler 注解中标注的能够处理的 Throwable 的类型
	 *
	 * @param method
	 * @param result
	 */
	private void detectAnnotationExceptionMappings(Method method, List<Class<? extends Throwable>> result) {
		ExceptionHandler ann = AnnotatedElementUtils.findMergedAnnotation(method, ExceptionHandler.class);
		Assert.state(ann != null, "No ExceptionHandler annotation");
		result.addAll(Arrays.asList(ann.value()));
	}

	private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {
		Method oldMethod = this.mappedMethods.put(exceptionType, method);
		// 不能存在一个异常对应两个处理方法的情况
		// 如果有该种情况，则抛出异常
		if (oldMethod != null && !oldMethod.equals(method)) {
			throw new IllegalStateException("Ambiguous @ExceptionHandler method mapped for [" +
					exceptionType + "]: {" + oldMethod + ", " + method + "}");
		}
	}

	/**
	 * 是否存在 异常-method 映射关系
	 *
	 * Whether the contained type has any exception mappings.
	 */
	public boolean hasExceptionMappings() {
		return !this.mappedMethods.isEmpty();
	}

	/**
	 * 返回能够处理该异常的方法
	 *
	 * Find a {@link Method} to handle the given exception.
	 * Use {@link ExceptionDepthComparator} if more than one match is found.
	 * @param exception the exception
	 * @return a Method to handle the exception, or {@code null} if none found
	 */
	@Nullable
	public Method resolveMethod(Exception exception) {
		return resolveMethodByThrowable(exception);
	}

	/**
	 * 返回能够处理该异常的方法
	 *
	 * Find a {@link Method} to handle the given Throwable.
	 * Use {@link ExceptionDepthComparator} if more than one match is found.
	 * @param exception the exception
	 * @return a Method to handle the exception, or {@code null} if none found
	 * @since 5.0
	 */
	@Nullable
	public Method resolveMethodByThrowable(Throwable exception) {
		// 根据异常寻找处理该异常的方法
		Method method = resolveMethodByExceptionType(exception.getClass());
		// 如果为 null
		if (method == null) {
			Throwable cause = exception.getCause();
			if (cause != null) {
				method = resolveMethodByExceptionType(cause.getClass());
			}
		}
		return method;
	}

	/**
	 * 根据异常类型查找能够处理的 Method
	 *
	 * Find a {@link Method} to handle the given exception type. This can be
	 * useful if an {@link Exception} instance is not available (e.g. for tools).
	 * @param exceptionType the exception type
	 * @return a Method to handle the exception, or {@code null} if none found
	 */
	@Nullable
	public Method resolveMethodByExceptionType(Class<? extends Throwable> exceptionType) {
		// 先查 cache
		Method method = this.exceptionLookupCache.get(exceptionType);
		if (method == null) {
			// 获取该异常匹配的 Method
			method = getMappedMethod(exceptionType);
			// 放入到缓存中
			this.exceptionLookupCache.put(exceptionType, method);
		}
		// 返回找到的 Method
		return method;
	}

	/**
	 * Return the {@link Method} mapped to the given exception type, or {@code null} if none.
	 */
	@Nullable
	private Method getMappedMethod(Class<? extends Throwable> exceptionType) {
		List<Class<? extends Throwable>> matches = new ArrayList<>();
		// 遍历所有能够处理的异常
		for (Class<? extends Throwable> mappedException : this.mappedMethods.keySet()) {
			// 如果 exceptionType 是 mappedException 的子类
			if (mappedException.isAssignableFrom(exceptionType)) {
				// 加入候选
				matches.add(mappedException);
			}
		}
		// 有多个的话进行排序
		if (!matches.isEmpty()) {
			matches.sort(new ExceptionDepthComparator(exceptionType));
			// 返回第一个
			return this.mappedMethods.get(matches.get(0));
		}
		else {
			return null;
		}
	}

}
