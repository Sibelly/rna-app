package org.cytoscape.ufms.facom.rna.internal;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.ufms.facom.rna.internal.view.RNAResultsPanel;

public class RNAResultsPanelAction extends AbstractCyAction {
	
	private static final long serialVersionUID = -2559380867039610979L;

	private CySwingApplication desktopApp;
	private final CytoPanel cytoPanel;
	private RNAResultsPanel myControlPanel;
	
	public RNAResultsPanelAction(String nomeArquivo, CySwingApplication desktopApp,CyServiceRegistrar cyServiceRegistrar, RNAResultsPanel myCytoPanel){
		// Add a menu item
		super("Results - " + nomeArquivo);
		setPreferredMenu("Apps.RNA");

		this.desktopApp = desktopApp;
		
		//Note: myControlPanel is bean we defined and registered as a service
		this.cytoPanel = this.desktopApp.getCytoPanel(CytoPanelName.EAST);
		this.myControlPanel = myCytoPanel;
		
		cyServiceRegistrar.registerService(myControlPanel, CytoPanelComponent.class, new Properties());
		cyServiceRegistrar.registerService(this, CyAction.class, new Properties());
		
	}

	public void actionPerformed(ActionEvent e) {
		// If the state of the cytoPanelWest is HIDE, show it
		if (cytoPanel.getState() == CytoPanelState.HIDE) {
			cytoPanel.setState(CytoPanelState.DOCK);
		}	

		// Select my panel
		int index = cytoPanel.indexOfComponent(myControlPanel);
		if (index == -1) {
			return;
		}
		cytoPanel.setSelectedIndex(index);
	}
}
