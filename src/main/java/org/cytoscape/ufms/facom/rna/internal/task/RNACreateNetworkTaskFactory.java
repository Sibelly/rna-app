package org.cytoscape.ufms.facom.rna.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.ufms.facom.rna.internal.model.RNAParameterSet;
import org.cytoscape.ufms.facom.rna.internal.util.RNAUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class RNACreateNetworkTaskFactory extends AbstractTaskFactory {
		
	private final CyNetworkManager netMgr;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil; 
	private final CyNetworkViewFactory netView;
	private final CyNetworkViewManager networkViewManager;
	private final DialogTaskManager dialogTaskManager;
	private final VisualMappingManager visualMappingManager;
	private final CyLayoutAlgorithmManager cyLayoutManager;
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
	private final VisualMappingFunctionFactory vmfFactoryDiscrete;
	private final CyServiceRegistrar cyServiceRegistrar;
	private final CySwingApplication cyDesktopService;
	private final CyApplicationManager applicationManager;
	
	private final RNAUtil rnaUtil;
	private final int resultId;
	private final RNAParameterSet currentParams;
	
	/*Construtor*/
	public RNACreateNetworkTaskFactory(CyNetworkManager netMgr, CyNetworkFactory cnf, CyNetworkNaming namingUtil, CyNetworkViewFactory netView, CyNetworkViewManager networkViewManager, DialogTaskManager dialogTaskManager, VisualMappingManager visualMappingManager, CyLayoutAlgorithmManager cyLayoutManager, VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete, CyServiceRegistrar cyServiceRegistrar, CySwingApplication cyDesktopService, CyApplicationManager applicationManager, RNAUtil rnaUtil, int resultId, RNAParameterSet currentParams){
		super();
		this.netMgr = netMgr;
		this.namingUtil = namingUtil;
		this.cnf = cnf;
		this.netView = netView;
		this.networkViewManager = networkViewManager;
		this.dialogTaskManager = dialogTaskManager;
		this.visualMappingManager = visualMappingManager;
		this.cyLayoutManager = cyLayoutManager;
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.vmfFactoryDiscrete = vmfFactoryDiscrete;
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.cyDesktopService = cyDesktopService;
		this.applicationManager = applicationManager;
		
		this.rnaUtil = rnaUtil;
		this.resultId = resultId;
		this.currentParams = currentParams;
	}
	
	public TaskIterator createTaskIterator () {
		return new TaskIterator(
			new RNACreateNetworkTask(netMgr, cnf, namingUtil, netView,  networkViewManager, dialogTaskManager, visualMappingManager, cyLayoutManager, vmfFactoryContinuous, vmfFactoryDiscrete, cyServiceRegistrar, cyDesktopService, applicationManager, rnaUtil, resultId, currentParams));
	}
	
	public boolean isReady() { 
		return true; 
	}
}
