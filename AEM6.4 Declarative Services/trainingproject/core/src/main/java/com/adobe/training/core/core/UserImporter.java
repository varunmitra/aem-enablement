package com.adobe.training.core.core;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jcr.Node; 
import javax.jcr.LoginException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.jackrabbit.api.security.user.*;

import java.security.Principal;

@Component(immediate = true)
public class UserImporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserImporter.class);
	 @Reference
     private SlingRepository repository;
	 
	 public String[] arrayOfURL; 
	
	 
	 protected void activate(ComponentContext componentContext)throws IOException{
         try {
			String[] userArray = this.returnUsers();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
	 
	public String[] returnUsers()throws IOException, InterruptedException{
		
		 String key = new String();
		Properties prop = new Properties();
		InputStream inputFile = this.getClass().getResourceAsStream(
				"/inputUsers.properties");
		prop.load(inputFile);
		LOGGER.info(prop.size()+"");
		arrayOfURL = new String[prop.size()];
		
		for(int i =0;i<prop.size();i++){
			LOGGER.info(""+i);
			key = "user"+(i+1);
			arrayOfURL[i]=prop.getProperty(key);
			
			LOGGER.info(arrayOfURL[i]);
		}
		this.createUsers();
		Thread.sleep(3000);
		this.createProfiles();
		return arrayOfURL;
}
	public void createUsers() throws IOException{
		 	LOGGER.info("inside create users");
		  	try{
	        int userCount = 0;
	        Session session =repository.loginAdministrative(repository.getDefaultWorkspace());
	        UserManager uMgr = ((org.apache.jackrabbit.api.JackrabbitSession)session).getUserManager();
	        Group trainingGroup = uMgr.createGroup("TrainingGroup", new SimplePrincipal("TrainingGroup"),"/home/groups/training" );
	        Group administrator = (Group)uMgr.getAuthorizable("administrators");
	       
	        
	        while (userCount < arrayOfURL.length) {
	            String userId;
	            userId = arrayOfURL[userCount];
	            User user;
	            
	            try{
	             user =  uMgr.createUser(userId, "crx", new SimplePrincipal(userId), "/home/users/training");
	            }catch(AuthorizableExistsException e){
	            	LOGGER.error("user exists", e);
	            	   userCount++;
	            	continue;
	            }
	            
	            administrator.addMember(uMgr.getAuthorizable(userId));
	            trainingGroup.addMember(uMgr.getAuthorizable(userId));
	            userCount++;
	            LOGGER.info("created "+userId);
	          session.save();
	        }
	        session.save();
	        
		  	}catch(RepositoryException e){
		  		LOGGER.error("exception during cleanup", e);
		  	}
		  	catch(Exception e){
		  		LOGGER.error("exception during cleanup", e);
		  	}
	}
	private static class SimplePrincipal implements Principal {
        protected final String name;
 
        public SimplePrincipal(String name) {
            
            this.name = name;
        }
 
        public String getName() {
            return name;
        }
 
        @Override
        public int hashCode() {
            return name.hashCode();
        }
 
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Principal) {
                return name.equals(((Principal) obj).getName());
            }
            return false;
        }
    }
	public void createProfiles(){
		 try {
			 LOGGER.info("inside create Profile");
			Session session =repository.loginAdministrative(repository.getDefaultWorkspace());
			Node node = session.getNode("/home/users/training");
			NodeIterator nodeItr = node.getNodes();
			Node userNode;
			int i=0;
			while(nodeItr.hasNext()){
				
				userNode = nodeItr.nextNode();
				LOGGER.info("node name "+userNode.getName());
				Node profile = userNode.addNode("profile", "nt:unstructured");
				session.save();
				profile.addMixin("rep:AccessControllable");
				profile.setProperty("sling:resourceType", "cq/security/components/profile");
				profile.setProperty("familyName", "");
				profile.setProperty("givenName", userNode.getName());
				session.save();
				
			}
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			LOGGER.error("exception during cleanup", e);
		}
	}
}