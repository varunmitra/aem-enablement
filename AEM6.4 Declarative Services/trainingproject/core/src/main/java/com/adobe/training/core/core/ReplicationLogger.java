package com.adobe.training.core.core;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;

import org.apache.sling.event.jobs.consumer.JobConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import java.util.HashMap;
import java.util.Map;

@Component(immediate = true,
service = JobConsumer.class,
property= {
		JobConsumer.PROPERTY_TOPICS + "=com/adobe/training/core/replicationjob"
}
)

public class ReplicationLogger implements JobConsumer {
	private final Logger logger = LoggerFactory.getLogger(getClass());
    
	@Reference
	private ResourceResolverFactory resourceResolverFactory;
    
	@Override
	public JobResult process(final Job job) {
		
		final String pagePath = job.getProperty("PAGE_PATH").toString();
		
		ResourceResolver resourceResolver = null;
		try { 
	      Map<String, Object> serviceParams = new HashMap<String, Object>();
		  serviceParams.put(ResourceResolverFactory.SUBSERVICE, null);
		  resourceResolver = resourceResolverFactory.getServiceResourceResolver(serviceParams);
		} catch (LoginException e) {
			e.printStackTrace();
		}

		// Create a Page object to log its title
		final PageManager pm = resourceResolver.adaptTo(PageManager.class);
		final Page page = pm.getContainingPage(pagePath);
		if(page != null) {
			logger.info("+++++++++++++ ACTIVATION OF PAGE : {}", page.getTitle());
		}	
		return JobConsumer.JobResult.OK;
	}
}