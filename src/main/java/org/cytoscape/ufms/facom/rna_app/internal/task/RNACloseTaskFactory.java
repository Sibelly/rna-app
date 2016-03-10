package org.cytoscape.ufms.facom.rna_app.internal.task;

import java.util.Collection;
import java.util.Set;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.ufms.facom.rna_app.internal.util.RNAUtil;
import org.cytoscape.ufms.facom.rna_app.internal.view.RNAResultsPanel;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class RNACloseTaskFactory implements TaskFactory, NetworkAboutToBeDestroyedListener {

	private final CySwingApplication swingApplication;
	private final CyServiceRegistrar registrar;
	private final RNAUtil rnaUtil;
	
	public RNACloseTaskFactory(final CySwingApplication swingApplication, final CyServiceRegistrar registrar, final RNAUtil rnaUtil) {
		this.swingApplication = swingApplication;
		this.registrar = registrar;
		this.rnaUtil = rnaUtil;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		final TaskIterator taskIterator = new TaskIterator();
		final Collection<RNAResultsPanel> resultPanels = rnaUtil.getResultPanels();
		final RNACloseAllResultsTask closeResultsTask = new RNACloseAllResultsTask(swingApplication, rnaUtil);

		if (resultPanels.size() > 0)
			taskIterator.append(closeResultsTask);
		
		taskIterator.append(new RNACloseTask(closeResultsTask, registrar, rnaUtil));
		
		return taskIterator;
	}

	@Override
	public boolean isReady() {
		return rnaUtil.isOpened();
	}
	
	@Override
	public void handleEvent(final NetworkAboutToBeDestroyedEvent e) {
		if (rnaUtil.isOpened()) {
			CyNetwork network = e.getNetwork();
			Set<Integer> resultIds = rnaUtil.getNetworkResults(network.getSUID());

			for (int id : resultIds) {
				RNAResultsPanel panel = rnaUtil.getResultPanel(id);
				if (panel != null){
					panel.discard(false);
				}
			}
		}
	}
}
