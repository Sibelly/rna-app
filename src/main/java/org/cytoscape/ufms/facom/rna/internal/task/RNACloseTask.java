package org.cytoscape.ufms.facom.rna.internal.task;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.ufms.facom.rna.internal.util.RNAUtil;
import org.cytoscape.ufms.facom.rna.internal.view.RNAMainPanel;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * Closes the main RNA panel.
 */
public class RNACloseTask implements Task {
	
	private final RNACloseAllResultsTask closeAllResultsTask;
	private final CyServiceRegistrar registrar;
	private final RNAUtil rnaUtil;
	
	public RNACloseTask(final RNACloseAllResultsTask closeAllResultsTask, final CyServiceRegistrar registrar, final RNAUtil rnaUtil) {
		this.closeAllResultsTask = closeAllResultsTask;
		this.registrar = registrar;
		this.rnaUtil = rnaUtil;
	}	

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (closeAllResultsTask == null || closeAllResultsTask.close) {
			RNAMainPanel mainPanel = rnaUtil.getMainPanel();

			if (mainPanel != null) {
				registrar.unregisterService(mainPanel, CytoPanelComponent.class);
			}

			rnaUtil.reset();
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}	
}
