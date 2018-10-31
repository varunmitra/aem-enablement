package com.adobe.training.core.core.servlets;

import java.io.IOException;

import javax.jcr.Repository;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sling.api.SlingHttpServletRequest;

import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@Component(immediate=true,service = Servlet.class, property = { Constants.SERVICE_DESCRIPTION + "=Simple Sling Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.resourceTypes=" + "weretail/components/structure/page",
		"sling.servlet.selectors=" + "abc"
		 })
public class MySafeMethodServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = -3960692666512058118L;

	@Reference

	private Repository repository;

	Logger logger = LoggerFactory.getLogger(MySafeMethodServlet.class);

	@Override

	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		logger.info("inside servlet");

		response.setHeader("Content-Type", "application/json");

		// response.getWriter().print("{\"coming\" : \"soon\"}");

		String[] keys = repository.getDescriptorKeys();

		JsonObject jsonObject = new JsonObject();
		for (int i = 0; i < keys.length; i++) {

			try {

				jsonObject.addProperty(keys[i], repository.getDescriptor(keys[i]));

			}

			catch (JsonIOException e) {

				logger.error("JsonIOException " + e);
			} catch (JsonSyntaxException e) {

				logger.error("JsonSyntaxException " + e);
			} catch (JsonParseException e) {

				logger.error("JsonParseException " + e);
			}

		}

		response.getWriter().print(jsonObject.toString());

	}

}
