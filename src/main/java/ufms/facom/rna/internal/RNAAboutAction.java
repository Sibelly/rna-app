package ufms.facom.rna.internal;

import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewManager;

import ufms.facom.rna.internal.util.RNAUtil;
import ufms.facom.rna.internal.view.RNAAboutDialog;


/**
 * The action to show the About dialog box
 */
public class RNAAboutAction extends AbstractRNAAction {

	private static final long serialVersionUID = -8445425993916988045L;

	private final OpenBrowser openBrowser;
	private final RNAUtil rnaUtil;
	private RNAAboutDialog aboutDialog;

	public RNAAboutAction(	final String name,
							final CyApplicationManager applicationManager,
							final CySwingApplication swingApplication,
							final CyNetworkViewManager netViewManager,
							final OpenBrowser openBrowser,
							final RNAUtil rnaUtil) {
		super(name, applicationManager, swingApplication, netViewManager, ActionEnableSupport.ENABLE_FOR_ALWAYS);
		this.openBrowser = openBrowser;
		this.rnaUtil = rnaUtil;
		setPreferredMenu("Apps.RNA");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//display about box
		synchronized (this) {
			if (aboutDialog == null) {
				aboutDialog = new RNAAboutDialog(swingApplication, openBrowser, rnaUtil);
			}
			
			if (!aboutDialog.isVisible()) {
				aboutDialog.setLocationRelativeTo(null);
				aboutDialog.setVisible(true);
			}
		}
		
		aboutDialog.toFront();
	}
}
