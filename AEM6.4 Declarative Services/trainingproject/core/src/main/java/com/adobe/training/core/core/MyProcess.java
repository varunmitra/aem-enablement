package com.adobe.training.core.core;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.framework.Constants;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

@Component(	
		property= {
				Constants.SERVICE_DESCRIPTION + "=A sample workflow process implementation.",
				Constants.SERVICE_VENDOR+ "=Adobe",
				"process.label=My Sample Workflow"
				}
)



public class MyProcess implements WorkflowProcess {

	private static final String TYPE_JCR_PATH = "JCR_PATH";

	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
		WorkflowData workflowData = item.getWorkflowData();
		if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
			String path = workflowData.getPayload().toString() + "/jcr:content";
			try {
				Node node = (Node) session.getSession().getItem(path);
				if (node != null) {
					node.setProperty("approved", readArgument(args));
					session.getSession().save();
				}
			} catch (RepositoryException e) {
				throw new WorkflowException(e.getMessage(), e);
			}
		}
	}

	private boolean readArgument(MetaDataMap args) {
		String argument = args.get("PROCESS_ARGS", "false");
		return argument.equalsIgnoreCase("true");
	}
}
