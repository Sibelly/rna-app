package ufms.facom.rna.internal;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.awt.Component;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import ufms.facom.rna.internal.task.RNACloseTaskFactory;
import ufms.facom.rna.internal.task.RNAOpenTaskFactory;
import ufms.facom.rna.internal.util.RNAUtil;
import ufms.facom.rna.internal.view.RNAMainPanel;
import ufms.facom.rna.internal.view.RNAResultsPanel;

public class CyActivator extends AbstractCyActivator {

	private CyServiceRegistrar serviceRegistrar;
	private RNAUtil rnaUtil;
	
	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		OpenBrowser openBrowser = getService(bundleContext, OpenBrowser.class);
		CySwingApplication swingApplication = getService(bundleContext, CySwingApplication.class);
		CyApplicationManager applicationManager = getService(bundleContext, CyApplicationManager.class);
		TaskManager<?,?> taskManager = getService(bundleContext, TaskManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bundleContext, CyNetworkManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bundleContext, CyNetworkFactory.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bundleContext, CyNetworkNaming.class);
		CyNetworkViewFactory cyNetworkViewFactoryRef = getService(bundleContext, CyNetworkViewFactory.class);	
		CyNetworkViewManager cyNetworkViewManagerRef = getService(bundleContext, CyNetworkViewManager.class);
		DialogTaskManager dialogTaskManager = getService(bundleContext, DialogTaskManager.class);
		VisualMappingManager vmmServiceRef = getService(bundleContext, VisualMappingManager.class);
		CyLayoutAlgorithmManager cyLayoutManager = getService(bundleContext, CyLayoutAlgorithmManager.class);
		VisualMappingFunctionFactory vmfFactoryContinuous = getService(bundleContext, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		VisualMappingFunctionFactory vmfFactoryDiscrete = getService(bundleContext, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		
		serviceRegistrar = getService(bundleContext, CyServiceRegistrar.class);
		rnaUtil = new RNAUtil(swingApplication);
		
		closeRNAPanels();

		RNACreateNetworkAction createNetworkAction = new RNACreateNetworkAction("Generate/Analyze Network", applicationManager, swingApplication, cyNetworkViewManagerRef, cyNetworkManagerServiceRef, cyNetworkFactoryServiceRef, cyNetworkNamingServiceRef, cyNetworkViewFactoryRef, dialogTaskManager, vmmServiceRef, cyLayoutManager, vmfFactoryContinuous, vmfFactoryDiscrete, serviceRegistrar, swingApplication, taskManager, rnaUtil);
		RNAAboutAction aboutAction = new RNAAboutAction("About", applicationManager, swingApplication, cyNetworkViewManagerRef, openBrowser, rnaUtil);
		
		registerService(bundleContext, aboutAction, CyAction.class, new Properties());
		registerAllServices(bundleContext, createNetworkAction, new Properties());
		
		RNAOpenTaskFactory openTaskFactory = new RNAOpenTaskFactory(swingApplication, serviceRegistrar, rnaUtil, createNetworkAction);
		Properties openTaskFactoryProps = new Properties();
		openTaskFactoryProps.setProperty(PREFERRED_MENU, "Apps.RNA");
		openTaskFactoryProps.setProperty(TITLE, "Open RNA");
		openTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		
		registerService(bundleContext, openTaskFactory, TaskFactory.class, openTaskFactoryProps);
		
		RNACloseTaskFactory closeTaskFactory = new RNACloseTaskFactory(swingApplication, serviceRegistrar, rnaUtil);
		Properties closeTaskFactoryProps = new Properties();
		closeTaskFactoryProps.setProperty(PREFERRED_MENU, "Apps.RNA");
		closeTaskFactoryProps.setProperty(TITLE, "Close RNA");
		closeTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		
		registerService(bundleContext, closeTaskFactory, TaskFactory.class, closeTaskFactoryProps);
		registerService(bundleContext, closeTaskFactory, NetworkAboutToBeDestroyedListener.class, new Properties());
	}
	
	@Override
	public void shutDown() {
		closeRNAPanels();
		super.shutDown();
	}
	
	private void closeRNAPanels() {
		// First, unregister result panels...
		final CytoPanel resPanel = rnaUtil.getResultsCytoPanel();
		
		if (resPanel != null) {
			int count = resPanel.getCytoPanelComponentCount();
			
			try {
				for (int i = 0; i < count; i++) {
					final Component comp = resPanel.getComponentAt(i);
					
					// Compare the class names to also get panels that may have been left by old versions of MCODE
					if (comp.getClass().getName().equals(RNAResultsPanel.class.getName()))
						serviceRegistrar.unregisterAllServices(comp);
				}
			} catch (Exception e) {
			}
		}
		
		// Now, unregister main panels...
		final CytoPanel ctrlPanel = rnaUtil.getControlCytoPanel();
		
		if (ctrlPanel != null) {
			int count = ctrlPanel.getCytoPanelComponentCount();
	
			for (int i = 0; i < count; i++) {
				try {
					final Component comp = ctrlPanel.getComponentAt(i);
					
					// Compare the class names to also get panels that may have been left by old versions of MCODE
					if (comp.getClass().getName().equals(RNAMainPanel.class.getName()))
						serviceRegistrar.unregisterAllServices(comp);
				} catch (Exception e) {
				}
			}
		}
	}
}