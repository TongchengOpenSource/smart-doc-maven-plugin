package com.smartdoc.demo.project.web.no2;

import com.smartdoc.demo.project.api.constants.ResponseMessage;
import com.smartdoc.demo.project.api.pojo.FooPojo;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制类
 * @author zongzi
 */
@RestController
@RequestMapping("/foo")
public class FooController {

	/**
	 * 更新FooPojo
	 * @param fooInt foo pojo's id
	 * @param fooPojo the update infos
	 * @return 1 success 2 failed
	 */
	@PutMapping("update/{fooInt}")
	public ResponseMessage<Integer> updateFooPojo(@PathVariable("fooInt") int fooInt, @RequestBody FooPojo fooPojo) {
		return ResponseMessage.success(1);
	}
}
