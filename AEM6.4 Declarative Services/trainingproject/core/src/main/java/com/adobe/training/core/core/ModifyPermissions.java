package com.adobe.training.core.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate=true)
public class ModifyPermissions {
	private static final String CONTENT_WE_RETAIL = "/content/we-retail/us/en/experience";
	private static final Logger LOGGER=  LoggerFactory.getLogger(ModifyPermissions.class);
	
	@Reference
	private SlingRepository repository;
	@Activate
	protected void activate(){
		
		LOGGER.info("ModifyPermissions activated");
		modifyPermissions();
	}
	
	private void modifyPermissions() {
		Session adminSession = null;
		try{
			adminSession = repository.loginService(null, repository.getDefaultWorkspace());
			
			UserManager userMgr= ((org.apache.jackrabbit.api.JackrabbitSession)adminSession).getUserManager();
			AccessControlManager accessControlManager = adminSession.getAccessControlManager();
			
			Authorizable denyAccess = userMgr.getAuthorizable("denyaccess");
			
			AccessControlList acl;
		try{
				acl= 	AccessControlUtils.getAccessControlList(adminSession, CONTENT_WE_RETAIL );
			}catch(NoSuchElementException nse){
				acl=(JackrabbitAccessControlList)  accessControlManager.getPolicies(CONTENT_WE_RETAIL)[0];
				
			}
			
			Privilege[] privileges = {accessControlManager.privilegeFromName(Privilege.JCR_READ)};
			acl.addAccessControlEntry(denyAccess.getPrincipal(), privileges);
			accessControlManager.setPolicy(CONTENT_WE_RETAIL, acl);
			adminSession.save();
		}catch (RepositoryException e){
			LOGGER.error("**************************Repo Exception", e);
		}finally{
			if (adminSession != null)
				adminSession.logout();
		}
	}
	
}
