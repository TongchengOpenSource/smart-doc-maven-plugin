package com.smartdoc.demo.project.api.service;


import com.smartdoc.demo.project.api.pojo.FooPojo;

/**
 * Foo 测试服务
 * @author zongzi
 */
public interface FooService {

	/**
	 * 测试查找fooPojo
 	 * @param fooPojo foo pojo object
	 * @return FooPojo
	 */
	FooPojo findFoo(FooPojo fooPojo);

	/**
	 * Add One Foo Pojo
	 * @param fooPojo foo pojo object
	 */
	void addFooPojo(FooPojo fooPojo);


}
