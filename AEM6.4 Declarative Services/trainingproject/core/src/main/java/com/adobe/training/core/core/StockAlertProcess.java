package com.adobe.training.core.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;


@Component(
		property= {
				"process.label=Stock Alert Process"
				}
		)
public class StockAlertProcess implements WorkflowProcess {
	
	private static final String PROPERTY_LAST_TRADE = "lastTrade";
	private static final String TYPE_JCR_PATH = "JCR_PATH";
	private static final String TYPE_JCR_UUID = "JCR_UUID";
	private static final Logger LOGGER = LoggerFactory.getLogger(StockAlertProcess.class);
	
	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
		throws WorkflowException {
		try {
			// get the node the workflow is acting on
			Session session = workflowSession.getSession();
			WorkflowData data = workItem.getWorkflowData();
			Node node = null;
			String type = data.getPayloadType();
			if(type.equals(TYPE_JCR_PATH) && data.getPayload() != null) {
				String payloadData = (String) data.getPayload();
				if(session.itemExists(payloadData)) {
					node = session.getNode(payloadData);
				}
			}
			else if (data.getPayload() != null && type.equals(TYPE_JCR_UUID)) {
				node = session.getNodeByIdentifier((String) data.getPayload());
			}
			LOGGER.info("********running with node {}", node.getPath());
			// parent's is expected to be stock symbol
			String symbol = node.getParent().getName();
			LOGGER.info("********found symbol {}", symbol);
			if (node.hasProperty(PROPERTY_LAST_TRADE)) {
				Double lastTrade = node.getProperty(PROPERTY_LAST_TRADE).getDouble();
				LOGGER.info("********last trade was {}", lastTrade);
				// reading the passed arguments
				Iterator<String> argumentsIterator = Arrays.asList(
						Pattern.compile("\n").split(args.get("PROCESS_ARGS", ""))).iterator();
				while (argumentsIterator.hasNext()) {
					List<String> currentArgumentLine = Arrays.asList(
							Pattern.compile("=").split(argumentsIterator.next()));
					String currentSymbol = currentArgumentLine.get(0);
					Double currentLimit = new Double(currentArgumentLine.get(1));
					if (currentSymbol.equalsIgnoreCase(symbol) && currentLimit < lastTrade) {
						LOGGER.warn("Stock Alert! {} is over {}", symbol, currentLimit);
					}
				}
			}
		}
		catch (RepositoryException e) {
			LOGGER.error("RepositoryException", e);
		}
	}
}
