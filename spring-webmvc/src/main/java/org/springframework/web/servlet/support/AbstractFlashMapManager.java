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

package org.springframework.web.servlet.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.util.UrlPathHelper;

/**
 * A base class for {@link FlashMapManager} implementations.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1.1
 */
public abstract class AbstractFlashMapManager implements FlashMapManager {

	/**
	 * mutex 对象
	 */
	private static final Object DEFAULT_FLASH_MAPS_MUTEX = new Object();


	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * flashmap 超时时间
	 */
	private int flashMapTimeout = 180;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();


	/**
	 * Set the amount of time in seconds after a {@link FlashMap} is saved
	 * (at request completion) and before it expires.
	 * <p>The default value is 180 seconds.
	 */
	public void setFlashMapTimeout(int flashMapTimeout) {
		this.flashMapTimeout = flashMapTimeout;
	}

	/**
	 * Return the amount of time in seconds before a FlashMap expires.
	 */
	public int getFlashMapTimeout() {
		return this.flashMapTimeout;
	}

	/**
	 * Set the UrlPathHelper to use to match FlashMap instances to requests.
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
	}

	/**
	 * Return the UrlPathHelper implementation to use.
	 */
	public UrlPathHelper getUrlPathHelper() {
		return this.urlPathHelper;
	}


	@Override
	@Nullable
	public final FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response) {
		// 模板方法，由子类实现
		List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
		// 为空则直接返回
		if (CollectionUtils.isEmpty(allFlashMaps)) {
			return null;
		}

		// 获取过期的 flashMap 列表
		List<FlashMap> mapsToRemove = getExpiredFlashMaps(allFlashMaps);
		// 获取与当前 request匹配的 FlashMap
		FlashMap match = getMatchingFlashMap(allFlashMaps, request);
		if (match != null) {
			// 添加到待删除的 maps 中
			mapsToRemove.add(match);
		}

		// 将过期的和匹配的从 session 保存的对应属性中删除
		if (!mapsToRemove.isEmpty()) {
			Object mutex = getFlashMapsMutex(request);
			if (mutex != null) {
				synchronized (mutex) {
					allFlashMaps = retrieveFlashMaps(request);
					if (allFlashMaps != null) {
						allFlashMaps.removeAll(mapsToRemove);
						updateFlashMaps(allFlashMaps, request, response);
					}
				}
			}
			else {
				allFlashMaps.removeAll(mapsToRemove);
				updateFlashMaps(allFlashMaps, request, response);
			}
		}

		// 返回匹配的
		return match;
	}

	/**
	 * 返回一组过期的 flashMap
	 *
	 * Return a list of expired FlashMap instances contained in the given list.
	 */
	private List<FlashMap> getExpiredFlashMaps(List<FlashMap> allMaps) {
		List<FlashMap> result = new LinkedList<>();
		// 遍历
		for (FlashMap map : allMaps) {
			if (map.isExpired()) {
				result.add(map);
			}
		}
		return result;
	}

	/**
	 * 返回与 erquest 匹配的 flashMap
	 *
	 * Return a FlashMap contained in the given list that matches the request.
	 * @return a matching FlashMap or {@code null}
	 */
	@Nullable
	private FlashMap getMatchingFlashMap(List<FlashMap> allMaps, HttpServletRequest request) {
		List<FlashMap> result = new LinkedList<>();
		// 遍历所有的 flashMap
		for (FlashMap flashMap : allMaps) {

			if (isFlashMapForRequest(flashMap, request)) {
				result.add(flashMap);
			}
		}
		if (!result.isEmpty()) {
			// 排序
			Collections.sort(result);
			if (logger.isTraceEnabled()) {
				logger.trace("Found " + result.get(0));
			}
			// 返回第一个
			return result.get(0);
		}
		return null;
	}

	/**
	 * Whether the given FlashMap matches the current request.
	 * Uses the expected request path and query parameters saved in the FlashMap.
	 */
	protected boolean isFlashMapForRequest(FlashMap flashMap, HttpServletRequest request) {
		// flashMap 的 TargetRequestPath
		String expectedPath = flashMap.getTargetRequestPath();
		if (expectedPath != null) {
			String requestUri = getUrlPathHelper().getOriginatingRequestUri(request);
			// path 都不匹配则返回 false
			if (!requestUri.equals(expectedPath) && !requestUri.equals(expectedPath + "/")) {
				return false;
			}
		}
		// 获取 请求的参数
		MultiValueMap<String, String> actualParams = getOriginatingRequestParams(request);
		// flashMap 中期望的参数
		MultiValueMap<String, String> expectedParams = flashMap.getTargetRequestParams();
		// 遍历 flashMap 中的 expectedParams
		for (Map.Entry<String, List<String>> entry : expectedParams.entrySet()) {
			List<String> actualValues = actualParams.get(entry.getKey());
			if (actualValues == null) {
				return false;
			}
			for (String expectedValue : entry.getValue()) {
				if (!actualValues.contains(expectedValue)) {
					return false;
				}
			}
		}
		return true;
	}

	private MultiValueMap<String, String> getOriginatingRequestParams(HttpServletRequest request) {
		String query = getUrlPathHelper().getOriginatingQueryString(request);
		return ServletUriComponentsBuilder.fromPath("/").query(query).build().getQueryParams();
	}

	/**
	 * 保存 flashMap 方法
	 *
	 * @param flashMap the FlashMap to save
	 * @param request the current request
	 * @param response the current response
	 */
	@Override
	public final void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response) {
		// flashMap 为空，直接返回
		if (CollectionUtils.isEmpty(flashMap)) {
			return;
		}

		String path = decodeAndNormalizePath(flashMap.getTargetRequestPath(), request);
		// 设置到 flashMap 中
		flashMap.setTargetRequestPath(path);

		// 设置过期时间
		flashMap.startExpirationPeriod(getFlashMapTimeout());

		Object mutex = getFlashMapsMutex(request);
		if (mutex != null) {
			// 同步代码块
			synchronized (mutex) {
				// 获取 flashmaps
				List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
				// 如果为 null 则创建一个 COWArrayList
				allFlashMaps = (allFlashMaps != null ? allFlashMaps : new CopyOnWriteArrayList<>());
				// 将当前的 flashMap 放入到 allFlashMaps 中
				allFlashMaps.add(flashMap);
				// 更新 flashMaps
				updateFlashMaps(allFlashMaps, request, response);
			}
		}
		else {
			// 获取 flashmaps
			List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
			// 使用 LinkedList 保存
			allFlashMaps = (allFlashMaps != null ? allFlashMaps : new LinkedList<>());
			// 添加
			allFlashMaps.add(flashMap);
			// 更新
			updateFlashMaps(allFlashMaps, request, response);
		}
	}

	@Nullable
	private String decodeAndNormalizePath(@Nullable String path, HttpServletRequest request) {
		if (path != null && !path.isEmpty()) {
			// 根据 request 中的编码规范对 path 进行解码
			path = getUrlPathHelper().decodeRequestString(request, path);
			if (path.charAt(0) != '/') {
				String requestUri = getUrlPathHelper().getRequestUri(request);
				path = requestUri.substring(0, requestUri.lastIndexOf('/') + 1) + path;
				path = StringUtils.cleanPath(path);
			}
		}
		return path;
	}

	/**
	 * 从 request 中获取所有的 flashMap 实例
	 *
	 * Retrieve saved FlashMap instances from the underlying storage.
	 * @param request the current request
	 * @return a List with FlashMap instances, or {@code null} if none found
	 */
	@Nullable
	protected abstract List<FlashMap> retrieveFlashMaps(HttpServletRequest request);

	/**
	 * Update the FlashMap instances in the underlying storage.
	 * @param flashMaps a (potentially empty) list of FlashMap instances to save
	 * @param request the current request
	 * @param response the current response
	 */
	protected abstract void updateFlashMaps(
			List<FlashMap> flashMaps, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Obtain a mutex for modifying the FlashMap List as handled by
	 * {@link #retrieveFlashMaps} and {@link #updateFlashMaps},
	 * <p>The default implementation returns a shared static mutex.
	 * Subclasses are encouraged to return a more specific mutex, or
	 * {@code null} to indicate that no synchronization is necessary.
	 * @param request the current request
	 * @return the mutex to use (may be {@code null} if none applicable)
	 * @since 4.0.3
	 */
	@Nullable
	protected Object getFlashMapsMutex(HttpServletRequest request) {
		return DEFAULT_FLASH_MAPS_MUTEX;
	}

}
