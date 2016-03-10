package org.cytoscape.ufms.facom.rna_app.internal;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.ufms.facom.rna_app.internal.util.RNAUtil;
import org.cytoscape.ufms.facom.rna_app.internal.view.RNAResultsPanel;
import org.cytoscape.view.model.CyNetworkViewManager;

public class RNADiscardResultAction extends AbstractRNAAction{

	private static final long serialVersionUID = 8869971374752471328L;

	public static final String REQUEST_USER_CONFIRMATION_COMMAND = "requestUserConfirmation";

	private final int resultId;
	private final CyServiceRegistrar registrar;
	private final RNAUtil rnaUtil;

	public RNADiscardResultAction(	final String name,
									final int resultId,
									final CyApplicationManager applicationManager,
									final CySwingApplication swingApplication,
									final CyNetworkViewManager netViewManager,
									final CyServiceRegistrar registrar,
									final RNAUtil rnaUtil) {
		super(name, applicationManager, swingApplication, netViewManager, ActionEnableSupport.ENABLE_FOR_ALWAYS);
		this.resultId = resultId;
		this.registrar = registrar;
		this.rnaUtil = rnaUtil;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		RNAResultsPanel panel = getResultPanel(resultId);

		if (panel != null) {
			int resultId = panel.getResultId();
			Integer confirmed = JOptionPane.YES_OPTION;
			boolean requestUserConfirmation = Boolean.valueOf(getValue(REQUEST_USER_CONFIRMATION_COMMAND).toString());

			if (requestUserConfirmation) {
				// Must make sure the user wants to close this results panel
				String message = "You are about to dispose of Result " + resultId + ".\nDo you wish to continue?";
				confirmed = JOptionPane.showOptionDialog(swingApplication.getJFrame(),
														 new Object[] { message },
														 "Confirm",
														 JOptionPane.YES_NO_OPTION,
														 JOptionPane.QUESTION_MESSAGE,
														 null,
														 null,
														 null);
			}

			if (confirmed == JOptionPane.YES_OPTION) {
				registrar.unregisterService(panel, CytoPanelComponent.class);
				rnaUtil.removeResult(resultId);
			}
		}

		final CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);

		// If there are no more tabs in the cytopanel then we hide it
		if (cytoPanel.getCytoPanelComponentCount() == 0) {
			cytoPanel.setState(CytoPanelState.HIDE);
		}

		if (getResultPanels().size() == 0) {
			// Reset the results cache
			rnaUtil.reset();
		}
	}	
	
}
