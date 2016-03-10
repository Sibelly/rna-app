package ufms.facom.rna.internal;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.view.model.CyNetworkViewManager;

import ufms.facom.rna.internal.view.RNAMainPanel;
import ufms.facom.rna.internal.view.RNAResultsPanel;

public class AbstractRNAAction  extends AbstractCyAction {

	private static final long serialVersionUID = -2366095173398648014L;
	
	protected final CySwingApplication swingApplication;
	protected final CyApplicationManager applicationManager;
	protected final CyNetworkViewManager netViewManager;

	public AbstractRNAAction(	final String name,
			   					final CyApplicationManager applicationManager,
			   					final CySwingApplication swingApplication,
			   					final CyNetworkViewManager netViewManager,
			   					final String enableFor) {
		super(name, applicationManager, enableFor, netViewManager);
		this.applicationManager = applicationManager;
		this.swingApplication = swingApplication;
		this.netViewManager = netViewManager;
	}
	
	/**
	 * @return Cytoscape's control panel
	 */
	protected CytoPanel getControlCytoPanel() {
		return swingApplication.getCytoPanel(CytoPanelName.WEST);
	}

	/**
	 * @return Cytoscape's results panel
	 */
	protected CytoPanel getResultsCytoPanel() {
		return swingApplication.getCytoPanel(CytoPanelName.EAST);
	}

	/**
	 * @return The main panel of the app if it is opened, and null otherwise
	 */
	protected RNAMainPanel getMainPanel() {
		CytoPanel cytoPanel = getControlCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			if (cytoPanel.getComponentAt(i) instanceof RNAMainPanel)
				return (RNAMainPanel) cytoPanel.getComponentAt(i);
		}

		return null;
	}

	/**
	 * @return The result panels of the app if it is opened, or an empty collection otherwise
	 */
	protected Collection<RNAResultsPanel> getResultPanels() {
		Collection<RNAResultsPanel> panels = new ArrayList<RNAResultsPanel>();
		CytoPanel cytoPanel = getResultsCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			if (cytoPanel.getComponentAt(i) instanceof RNAResultsPanel)
				panels.add((RNAResultsPanel) cytoPanel.getComponentAt(i));
		}

		return panels;
	}

	
	protected RNAResultsPanel getResultPanel(final int resultId) {
		for (RNAResultsPanel panel : getResultPanels()) {
			if (panel.getResultId() == resultId) return panel;
		}

		return null;
	}

	/**
	 * @return true if the app is opened and false otherwise
	 */
	protected boolean isOpened() {
		return getMainPanel() != null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
