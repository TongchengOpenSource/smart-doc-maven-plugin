package com.smartdoc.plugin.test.mojo;

import java.util.Properties;

import com.smartdoc.constant.MojoConstants;
import com.smartdoc.plugin.test.BaseMojoTest;
import org.apache.maven.plugin.Mojo;
import org.junit.Test;

/**
 * Test docx html mojo
 * @author zongzi
 */
public class TestDocxHtmlMojo extends BaseMojoTest {
	@Override
	protected Properties setUserProperties() {
		return null;
	}

	@Override
	protected String mavenProjectDir() {
		return "../demo-projects/";
	}


	/**
	 * test docx mojo's running
	 */
	@Test
	public void testRunningDocxMojoRunning() throws Exception {
		Mojo runnableMojo = findRunnableMojo(MojoConstants.DOCX_MOJO);
		runnableMojo.execute();
	}
}
