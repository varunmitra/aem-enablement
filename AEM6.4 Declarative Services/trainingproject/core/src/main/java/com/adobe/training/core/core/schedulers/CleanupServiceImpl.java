package com.adobe.training.core.core.schedulers;


import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sling.jcr.api.SlingRepository;

@Component(service = Runnable.class)
@Designate(ocd = CleanupServiceImpl.Config.class)

public class CleanupServiceImpl implements Runnable {

	@ObjectClassDefinition(name = "A simple cleanup task", description = "Simple demo for cron-job like task with properties")
	public static @interface Config {

		@AttributeDefinition(name = "Cron-job expression")
		String scheduler_expression() default "*/30 * * * * ?";

		@AttributeDefinition(name = "Concurrent task", description = "Whether or not to schedule this task concurrently")
		boolean scheduler_concurrent() default false;

		@AttributeDefinition(name = "Cleanup Path", description = "Can be configured in /system/console/configMgr")
		String cleanup_path() default "/var/myPath";

		@AttributeDefinition(name = "A parameter", description = "Can be configured in /system/console/configMgr")
		String myParameter() default "";
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceImpl.class);
	@Reference
	private SlingRepository repository;

	private String cleanupPath;

	@Activate
	protected void activate(final Config config) {
		this.cleanupPath = (String.valueOf(config.cleanup_path()) != null) ? String.valueOf(config.cleanup_path())
				: null;
		LOGGER.info("configure: cleanupPath='{}''", this.cleanupPath);
	}

	@Override
	public void run() {

		Session session = null;
		try {

			session = repository.loginService(null, repository.getDefaultWorkspace());
			//LOGGER.info("Obtained session");
			if (session.itemExists(cleanupPath) == true) {
				session.removeItem(cleanupPath);

				session.save();
			}
		} catch (RepositoryException e) {
			LOGGER.info("RepositoryException", e);

		} finally {
			if (session != null) {
				session.logout();
			}
		}
	}
}