package com.smartdoc.demo.project.web.no1;




import java.util.List;

import com.smartdoc.demo.project.api.constants.ResponseMessage;
import com.smartdoc.demo.project.api.pojo.FooPojo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DemoController At torna.demo.torna.web.no1
 * @author zongzi
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

	/**
	 * find foo pojos by foo string
	 * @param foo fooString
	 * @return List of foo pojos;
	 */
	@GetMapping("/list/{foo}")
	public ResponseMessage<List<FooPojo>> findFooPojos(@PathVariable("foo") String foo) {
		return ResponseMessage.success(null);
	}


	@PostMapping("add")
	public ResponseMessage<FooPojo> insertNewFooPojo(@RequestBody FooPojo fooPojo) {
		return ResponseMessage.success(fooPojo);
	}

}
