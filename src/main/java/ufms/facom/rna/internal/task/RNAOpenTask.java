package ufms.facom.rna.internal.task;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import ufms.facom.rna.internal.RNACreateNetworkAction;
import ufms.facom.rna.internal.util.RNAUtil;
import ufms.facom.rna.internal.view.RNAMainPanel;

public class RNAOpenTask implements Task {
	
	private final CySwingApplication swingApplication;
	private final CyServiceRegistrar registrar;
	private final RNAUtil rnaUtil;
	private final RNACreateNetworkAction createNetworkAction;
	
	public RNAOpenTask(	final CySwingApplication swingApplication, final CyServiceRegistrar registrar, final RNAUtil rnaUtil, final RNACreateNetworkAction createNetworkAction) {
		this.swingApplication = swingApplication;
		this.registrar = registrar;
		this.rnaUtil = rnaUtil;
		this.createNetworkAction = createNetworkAction;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Display RNAMainPanel in left cytopanel
		synchronized (this) {
			RNAMainPanel mainPanel = null;
			
			// First we must make sure that the app is not already open
			if (!rnaUtil.isOpened()) {
				mainPanel = new RNAMainPanel(swingApplication, createNetworkAction, rnaUtil);
				registrar.registerService(mainPanel, CytoPanelComponent.class, new Properties());
				createNetworkAction.updateEnableState();
			} else {
				mainPanel = rnaUtil.getMainPanel();
			}

			if (mainPanel != null) {
				CytoPanel cytoPanel = rnaUtil.getControlCytoPanel();
				int index = cytoPanel.indexOfComponent(mainPanel);
				cytoPanel.setSelectedIndex(index);
			}
		}
	}

	@Override
	public void cancel() {
		//Do nothing
	}
	
}
