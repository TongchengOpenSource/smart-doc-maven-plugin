package com.smartdoc.demo.project.api.service;

import java.util.List;

import com.smartdoc.demo.project.api.pojo.FooPojo;

/**
 * 测试DemoService
 * @author zongzi
 */
public interface DemoService {


	/**
	 * 更新FooPojo对象
	 * @param fooPojo 测试FooPojo对象
	 * @return {@link FooPojo}
	 */
	FooPojo updateFooPojo(FooPojo fooPojo);

	/**
	 * 查找FooPojo列表
	 * @param fooString 查找字符串
	 * @return 返回结果，对象列表
	 */
	List<FooPojo> findFooPojos(String fooString);
}
