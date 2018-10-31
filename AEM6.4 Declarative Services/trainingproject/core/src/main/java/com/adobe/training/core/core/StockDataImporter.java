package com.adobe.training.core.core;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.polling.importer.ImportException;
import com.day.cq.polling.importer.Importer;


@Component(immediate=true,
enabled=true,
service = Importer.class,
property = {
		Importer.SCHEME_PROPERTY +"=stock"
}
)
public class StockDataImporter implements Importer {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String SOURCE_URL = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=";
	
	
	private static final String LASTTRADE = "lastTrade";
	
	@Reference
	private SlingRepository repo;
	
	@Override
	public void importData(final String scheme, final String dataSource, final Resource resource)
		throws ImportException {
		try {
			ParseJson parseJson = new ParseJson();
			String lastTrade = parseJson.loadJson(SOURCE_URL+dataSource);
			writeToRepository(dataSource, lastTrade, resource);
		}
		
	catch(Exception e) {
			logger.error("Exception", e);
		}
		
	}
	
	/**
	 * Creates the Yahoo stock data structure
	 * 
	 *  + <STOCK_SYMBOL> [cq:Page]
	 *     + lastTrade [nt:unstructured]
	 *       - lastTrade = <value>
	 *       - requestedDate = <value>
	 *       - requestTime = <value>
	 *       - upDown = <value>
	 *       - openPrice = <value>
	 *       - rangeHigh = <value>
	 *       - rangeLow = <value>
	 *       - volume = <value>
	 */
	private void writeToRepository(final String stockSymbol, final String lastTrade, final Resource resource) throws RepositoryException {
		Session session= repo.loginService(null, repo.getDefaultWorkspace());
		Node parent = resource.adaptTo(Node.class);
		Node stockPageNode = JcrUtil.createPath(parent.getPath() + "/" + stockSymbol, "cq:Page", 
				session);
		Node lastTradeNode = JcrUtil.createPath(stockPageNode.getPath() + "/lastTrade", "nt:unstructured", 
				session);
		
			lastTradeNode.setProperty(LASTTRADE, lastTrade);
		session.save();
		session.logout();
	}

	@Override
	public void importData(String scheme, String dataSource, Resource target,
			String login, String password) throws ImportException {
		importData(scheme, dataSource, target);
		
	}
}

