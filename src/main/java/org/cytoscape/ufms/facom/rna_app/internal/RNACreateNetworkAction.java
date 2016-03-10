package org.cytoscape.ufms.facom.rna_app.internal;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.ufms.facom.rna_app.internal.model.RNAParameterSet;
import org.cytoscape.ufms.facom.rna_app.internal.task.RNACreateNetworkTaskFactory;
import org.cytoscape.ufms.facom.rna_app.internal.util.RNAUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;


public class RNACreateNetworkAction extends AbstractRNAAction{

	private static final long serialVersionUID = 7648073661365555566L;
	
	private final TaskManager<?, ?> taskManager;
	private final RNAUtil rnaUtil;
	
	private final CyNetworkManager cyNetworkManagerServiceRef;
	private final CyNetworkFactory cyNetworkFactoryServiceRef;
	private final CyNetworkNaming cyNetworkNamingServiceRef;
	private final CyNetworkViewFactory cyNetworkViewFactoryRef;	
	private final CyNetworkViewManager cyNetworkViewManagerRef;
	private final DialogTaskManager dialogTaskManager;
	private final VisualMappingManager vmmServiceRef;
	private final CyLayoutAlgorithmManager cyLayoutManager;
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
	private final VisualMappingFunctionFactory vmfFactoryDiscrete;
	private final CyServiceRegistrar cyServiceRegistrar;
	private final CySwingApplication cyDesktopService;
	private final CyApplicationManager applicationManager;
	
	public RNACreateNetworkAction(	String title,
									CyApplicationManager applicationManager,
									CySwingApplication swingApplication,
									CyNetworkViewManager networkViewManager, 
									CyNetworkManager netMgr, 
									CyNetworkFactory cnf, 
									CyNetworkNaming namingUtil, 
									CyNetworkViewFactory netView, 
									DialogTaskManager dialogTaskManager, 
									VisualMappingManager visualMappingManager, 
									CyLayoutAlgorithmManager cyLayoutManager, 
									VisualMappingFunctionFactory vmfFactoryContinuous, 
									VisualMappingFunctionFactory vmfFactoryDiscrete, 
									CyServiceRegistrar cyServiceRegistrar, 
									CySwingApplication cyDesktopService,
									TaskManager<?, ?> taskManager,
									RNAUtil rnaUtil) {
		
		super(title, applicationManager, swingApplication, networkViewManager, ActionEnableSupport.ENABLE_FOR_ALWAYS);

		this.cyNetworkManagerServiceRef = netMgr;
		this.cyNetworkNamingServiceRef = namingUtil;
		this.cyNetworkFactoryServiceRef = cnf;
		this.cyNetworkViewFactoryRef = netView;
		this.cyNetworkViewManagerRef = networkViewManager;
		this.dialogTaskManager = dialogTaskManager;
		this.vmmServiceRef = visualMappingManager;
		this.cyLayoutManager = cyLayoutManager;
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.vmfFactoryDiscrete = vmfFactoryDiscrete;
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.cyDesktopService = cyDesktopService;
		this.applicationManager = applicationManager;
		
		this.taskManager = taskManager;
		this.rnaUtil = rnaUtil;
	}
	
	/**
	 * This method is called when the user clicks Create Network.
	 *
	 * @param event Click of the createNetworkButton on the RNAMainPanel.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
	
		RNAParameterSet currentParams = getMainPanel().getCurrentParamsCopy();
		
		if(currentParams.getFile() == null){
			JOptionPane.showMessageDialog(cyDesktopService.getJFrame(),
					  "No file were selected, please choose one to continue.",
					  "Generating Network Interrupted",
					  JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		final int resultId = rnaUtil.getNextResultId();
		
		// Run RNA
		RNACreateNetworkTaskFactory createNetworkTaskFactory = new RNACreateNetworkTaskFactory(	cyNetworkManagerServiceRef, cyNetworkFactoryServiceRef, 
				 																				cyNetworkNamingServiceRef, cyNetworkViewFactoryRef, 
				 																				cyNetworkViewManagerRef, dialogTaskManager, 
				 																				vmmServiceRef, cyLayoutManager, vmfFactoryContinuous,
				 																				vmfFactoryDiscrete, cyServiceRegistrar, cyDesktopService, applicationManager, 
				 																				rnaUtil, resultId, currentParams);
		taskManager.execute(createNetworkTaskFactory.createTaskIterator());
		
	}
}


