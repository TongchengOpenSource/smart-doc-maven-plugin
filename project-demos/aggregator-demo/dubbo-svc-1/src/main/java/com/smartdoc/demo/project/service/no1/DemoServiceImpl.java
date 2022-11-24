package com.smartdoc.demo.project.service.no1;

import java.util.List;

import com.smartdoc.demo.project.api.pojo.FooPojo;
import com.smartdoc.demo.project.api.service.DemoService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 *
 * 测试Dubbo服务实现类
 * @author zongzi
 */
@DubboService
public class DemoServiceImpl implements DemoService {

	@Override
	public FooPojo updateFooPojo(FooPojo fooPojo) {
		return null;
	}

	@Override
	public List<FooPojo> findFooPojos(String fooString) {
		return null;
	}

}
