package com.smartdoc.plugin.test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

import com.power.common.util.CollectionUtil;
import com.power.common.util.StringUtil;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.MojoExecutionEvent;
import org.apache.maven.execution.MojoExecutionListener;
import org.apache.maven.execution.scope.internal.MojoExecutionScope;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.lifecycle.internal.DependencyContext;
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.internal.DefaultLegacySupport;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.session.scope.internal.SessionScope;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.DefaultLocalRepositoryProvider;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.junit.Assert;
import org.junit.Rule;


public abstract class BaseMojoTest {


	/**
	 * <b>Please pass `smart-doc.test.maven.local.repository=${you local repository basedir}' to system's properties when running test's</b>
	 * <br>
	 * it while be like
	 * <code> java -Dsmart-doc.test.local.repository=${user.home}/.m2/repository</code>
	 *
	 */
	private final static String SYSTEM_REPOSITORY_PATH = System.getProperty("smart-doc.test.local.repository");
	private static LocalRepository LOCAL_REPOSITORY = null;
	private DefaultRepositorySystemSession defaultRepositorySystemSession = null;

	private MavenProject mavenProject;
	private MavenSession mavenSession;
	@Rule
	public MojoRule rule = new MojoRule() {
		@Override
		protected void before() throws Throwable {
			if (SYSTEM_REPOSITORY_PATH == null) {
				throw new RuntimeException("Please setting your local maven repository before run tests!");
			}
			File systemRepositoryPath = new File(SYSTEM_REPOSITORY_PATH);
			if (!systemRepositoryPath.exists()) {
				throw new RuntimeException("Please setting your local maven repository correctly before run tests! path:" + SYSTEM_REPOSITORY_PATH);
			}
			LOCAL_REPOSITORY = new LocalRepository(systemRepositoryPath);
			defaultRepositorySystemSession = MavenRepositorySystemUtils.newSession();
			defaultRepositorySystemSession.setLocalRepositoryManager(createLocalRepositoryManager());
			defaultRepositorySystemSession.setSystemProperties(createSystemProperties());
			String testMavenProjectDir = mavenProjectDir();
			if (StringUtil.isEmpty(testMavenProjectDir)) {
				throw new RuntimeException("Please specify the maven project which will be tested!");
			}
			File mavenProjectFile = new File(testMavenProjectDir);
			if (!mavenProjectFile.exists()) {
				throw new RuntimeException("Please specify the maven project correctly! path:" + mavenProjectFile);
			}
			mavenProject = readNewMavenProject(mavenProjectFile);
			mavenSession = createNewMavenSession(mavenProject);
		}

		/**
		 * Create a new maven project by reading the `pom.xml` file under the basedir you provided.
		 * @param basedir the basedir of the project you want to test ( must have pom.xml file).
		 * @return MavenProject
		 * @throws ComponentLookupException if rule can't find ProjectBuilder class in plexus container.
		 */
		MavenProject readNewMavenProject(File basedir) throws ComponentLookupException {
			File pom = new File(basedir, "pom.xml");
			ProjectBuildingRequest projectBuildingRequest = createProjectBuildingRequest(basedir);
			ProjectBuilder builder = rule.lookup(ProjectBuilder.class);
			ProjectBuildingResult projectBuildingResult;
			try {

				projectBuildingResult = builder.build(pom, projectBuildingRequest);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			MavenProject project = projectBuildingResult.getProject();
			Assert.assertNotNull(project);
			return project;
		}

		/**
		 * Create a new maven session of the maven project you crete.
		 * 1. the `-D` settings will be set to this maven session. {@link BaseMojoTest#setUserProperties}
		 * 2. you need to provide your local maven repository's path
		 * @param project maven project
		 * @return MavenSession
		 */
		MavenSession createNewMavenSession(MavenProject project) {
			MavenExecutionRequest request = new DefaultMavenExecutionRequest();
			// 设置用户的请求参数
			request.setUserProperties(setUserProperties());
			MavenExecutionResult result = new DefaultMavenExecutionResult();
			MavenSession session = new MavenSession(rule.getContainer(), defaultRepositorySystemSession, request, result);
			session.setCurrentProject(project);
			session.setProjects(Collections.singletonList(project));

			// 保存mavenSession
			new DefaultLegacySupport().setSession(session);
			return session;
		}

		@Override
		protected void after() {
			mavenProject = null;
			mavenSession = null;
			defaultRepositorySystemSession = null;
		}
	};

	/**
	 * abstract method which can allow you to pass parameters to you mojos.
	 * @return the properties
	 */
	protected abstract Properties setUserProperties();

	/**
	 * find a runnable mojo of the goal you provided.
	 * @param goalName mojo's goal name
	 * @return Mojo which can run in your test method
	 * @throws Exception any exception
	 */
	protected Mojo findRunnableMojo(String goalName) throws Exception {
		return findRunnableMojo(goalName, true);
	}

	/**
	 * find a runnable mojo of the goal you provided
	 * @param goalName  mojo's goal name
	 * @param resolveDependencies resolve the dependencies of the tested maven project if true
	 * @return Mojo which can run in your test method
	 * @throws Exception any exception
	 */
	protected Mojo findRunnableMojo(String goalName, boolean resolveDependencies) throws Exception {
		MojoExecution executionGoal = rule.newMojoExecution(goalName);
		prepareMojoExecution(executionGoal, resolveDependencies);
		return rule.lookupConfiguredMojo(mavenSession, executionGoal);

	}

	/**
	 * prepare for mojo to execution
	 * @param execution MojoExecution
	 * @param resolveDependencies resolve the dependencies of the tested maven project if true
	 * @throws LifecycleExecutionException
	 * @throws ComponentLookupException
	 */
	protected void prepareMojoExecution(MojoExecution execution, boolean resolveDependencies) throws LifecycleExecutionException, ComponentLookupException {

		appendExecutionConfigurations(mavenSession, execution);
		// resolve the maven dependencies or not
		if (resolveDependencies) {
			resolveDependencies(execution, mavenSession);
		}
	}

	protected void executeMojo(String goalName, boolean resolveDependencies, Function<Mojo, Boolean> executeMojoFunction) throws Exception {

		MojoExecution executionGoal = rule.newMojoExecution(goalName);
		Mojo mojo = findRunnableMojo(goalName, resolveDependencies);
		Boolean apply = executeMojoFunction.apply(mojo);
		if (apply) {
			executeMojo(mojo, mavenSession, mavenProject, executionGoal);
		}
	}


	private void executeMojo(Mojo mojo, MavenSession session, MavenProject project, MojoExecution execution) throws Exception {
		SessionScope sessionScope = rule.lookup(SessionScope.class);
		try {
			sessionScope.enter();
			sessionScope.seed(MavenSession.class, session);
			MojoExecutionScope executionScope = rule.lookup(MojoExecutionScope.class);

			try {
				executionScope.enter();
				executionScope.seed(MavenProject.class, project);
				executionScope.seed(MojoExecution.class, execution);
				mojo.execute();
				MojoExecutionEvent event = new MojoExecutionEvent(session, project, execution, mojo);
				rule.getContainer().lookupList(MojoExecutionListener.class).forEach(listener -> {
					try {
						listener.afterMojoExecutionSuccess(event);
					}
					catch (MojoExecutionException e) {
						throw new RuntimeException(e);
					}
				});
			}
			finally {
				executionScope.exit();
			}
		}
		finally {
			sessionScope.exit();
		}

	}

	/**
	 * merge the plugin configurations (in pom.xml) to configurations (in test/resource/plugin.xml)
	 * @param session maven session
	 * @param execution maven execution
	 */
	protected void appendExecutionConfigurations(MavenSession session, MojoExecution execution) {
		MavenProject project = session.getCurrentProject();
		MojoDescriptor mojoDescriptor = execution.getMojoDescriptor();
		Plugin plugin = project.getPlugin(mojoDescriptor.getPluginDescriptor().getPluginLookupKey());
		if (Objects.isNull(plugin)) {
			return;
		}
		List<PluginExecution> executions = plugin.getExecutions();
		for (PluginExecution pluginExecution : executions) {
			List<String> goals = pluginExecution.getGoals();
			if (CollectionUtil.isEmpty(goals)) {
				return;
			}
			if (goals.contains(execution.getGoal())) {
				Xpp3Dom executionConfiguration = (Xpp3Dom) pluginExecution.getConfiguration();
				// merge the plugin configurations (in pom.xml) to configurations (in test/resource/plugin.xml)
				execution.setConfiguration(Xpp3Dom.mergeXpp3Dom(executionConfiguration, execution.getConfiguration()));
			}
		}
	}

	protected void resolveDependencies(MojoExecution mojoExecution, MavenSession mavenSession) throws ComponentLookupException, LifecycleExecutionException {
		MojoExecutor mojoExecutor = rule.lookup(MojoExecutor.class);
		List<MojoExecution> mojoExecutions = Collections.singletonList(mojoExecution);
		DependencyContext dependencyContext = mojoExecutor.newDependencyContext(mavenSession, mojoExecutions);
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		mojoExecutor.ensureDependenciesAreResolved(mojoDescriptor, mavenSession, dependencyContext);
	}

	private ProjectBuildingRequest createProjectBuildingRequest(File basedir) {
		MavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setBaseDirectory(basedir);

		ProjectBuildingRequest buildingRequest = request.getProjectBuildingRequest();
		buildingRequest.setSystemProperties(createSystemProperties());
		buildingRequest.setRepositorySession(defaultRepositorySystemSession);
		return buildingRequest;
	}

	/**
	 * subclass need to provide the maven project's dir
	 * @return maven project's dir
	 */
	protected abstract String mavenProjectDir();

	/**
	 * setting system's java.version == 8 ,otherwise can't resolve the maven project's dependency.
	 * @return properties that will be setting to maven session
	 */
	private Properties createSystemProperties() {
		Properties properties = new Properties();
		properties.setProperty("java.version", "8");
		return properties;
	}

	/**
	 * create local repository manager. need LOCAL_REPOSITORY to run.
	 * @return LocalRepositoryManager need this to solve the dependencies.
	 * @throws NoLocalRepositoryManagerException if maven can't find local repository
	 */
	private LocalRepositoryManager createLocalRepositoryManager() throws NoLocalRepositoryManagerException {
		DefaultLocalRepositoryProvider provider = new DefaultLocalRepositoryProvider();
		provider.initService(new DefaultServiceLocator());
		return provider.newLocalRepositoryManager(defaultRepositorySystemSession, LOCAL_REPOSITORY);
	}

}
