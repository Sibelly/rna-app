package ufms.facom.rna.internal.task;

import java.util.Collection;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import ufms.facom.rna.internal.util.RNAUtil;
import ufms.facom.rna.internal.view.RNAResultsPanel;

public class RNACloseAllResultsTask implements Task{
	
	@Tunable(description = "<html>You are about to close the RNA app.<br />Do you want to continue?</html>", params="ForceSetDirectly=true")
	public boolean close = true;
	
	private final CySwingApplication swingApplication;
	private final RNAUtil rnaUtil;
	
	public RNACloseAllResultsTask(final CySwingApplication swingApplication, final RNAUtil rnaUtil) {
		this.swingApplication = swingApplication;
		this.rnaUtil = rnaUtil;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Close RNA";
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (close) {
			final Collection<RNAResultsPanel> resultPanels = rnaUtil.getResultPanels();
			
			for (RNAResultsPanel panel : resultPanels) {
				panel.discard(false);
			}

			CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.WEST);

			if (cytoPanel.getCytoPanelComponentCount() == 0){
				cytoPanel.setState(CytoPanelState.HIDE);
			}
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}	
	
}
