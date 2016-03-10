package org.cytoscape.ufms.facom.rna.internal.util;

import java.awt.Component;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.ufms.facom.rna.internal.model.RNAParameterSet;
import org.cytoscape.ufms.facom.rna.internal.view.RNAMainPanel;
import org.cytoscape.ufms.facom.rna.internal.view.RNAResultsPanel;

public class RNAUtil {
	
	private final CySwingApplication swingApplication;
	private final Properties props;
	
	private int nextResultId;
	private RNAParameterSet currentParameters;
	// Keeps track of analyzed networks (network SUID is key) and their respective results (list of result ids)
	private Map<Long, Set<Integer>> networkResults;
	
	public RNAUtil(CySwingApplication swingApplication){
		this.swingApplication = swingApplication;
		this.props = loadProperties("/rna.properties");
		this.currentParameters = new RNAParameterSet();
		
		this.reset();
	}

	/**
	 * @return Cytoscape's control panel
	 */
	public CytoPanel getControlCytoPanel() {
		return swingApplication.getCytoPanel(CytoPanelName.WEST);
	}
	
	/**
	 * @return Cytoscape's results panel
	 */
	public CytoPanel getResultsCytoPanel() {
		return swingApplication.getCytoPanel(CytoPanelName.EAST);
	}
	
	/**
	 * @return The main panel of the app if it is opened, and null otherwise
	 */
	public RNAMainPanel getMainPanel() {
		CytoPanel cytoPanel = getControlCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			final Component comp = cytoPanel.getComponentAt(i);
			
			if (comp instanceof RNAMainPanel)
				return (RNAMainPanel) comp;
		}

		return null;
	}	
	
	public Set<Integer> getNetworkResults(final long suid) {
		Set<Integer> ids = networkResults.get(suid);

		return ids != null ? ids : new HashSet<Integer>();
	}
	
	/**
	 * @return true if the app is opened and false otherwise
	 */
	public boolean isOpened() {
		return getMainPanel() != null;
	}

	public int getNextResultId() {
		return nextResultId;
	}	
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	private static Properties loadProperties(String name) {
		Properties props = new Properties();

		try {
			InputStream in = org.cytoscape.ufms.facom.rna.internal.CyActivator.class.getResourceAsStream(name);

			if (in != null) {
				props.load(in);
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return props;
	}	
	
	public void reset() {
		nextResultId = 1;
		currentParameters = new RNAParameterSet();
		networkResults = new HashMap<>();
	}		
	
	/**
	 * @return The result panels of the app if it is opened, or an empty collection otherwise
	 */
	public Collection<RNAResultsPanel> getResultPanels() {
		Collection<RNAResultsPanel> panels = new ArrayList<RNAResultsPanel>();
		CytoPanel cytoPanel = getResultsCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			if (cytoPanel.getComponentAt(i) instanceof RNAResultsPanel){
				panels.add((RNAResultsPanel) cytoPanel.getComponentAt(i));
			}
		}

		return panels;
	}	
	
	public RNAResultsPanel getResultPanel(final int resultId) {
		for (RNAResultsPanel panel : getResultPanels()) {
			if (panel.getResultId() == resultId){
				return panel;
			}
		}

		return null;
	}	
	
	public void addResult(final long suid) {
		Set<Integer> ids = networkResults.get(suid);

		if (ids == null) {
			ids = new HashSet<Integer>();
			networkResults.put(suid, ids);
		}

		ids.add(nextResultId);
		
		nextResultId++; // Increment next available ID
	}
	
	public boolean removeResult(final int resultId) {
		boolean removed = false;
		Long networkId = null;

		for (Entry<Long, Set<Integer>> entries : networkResults.entrySet()) {
			Set<Integer> ids = entries.getValue();

			if (ids.remove(resultId)) {
				if (ids.isEmpty())
					networkId = entries.getKey();

				removed = true;
				break;
			}
		}

		if (networkId != null)
			networkResults.remove(networkId);
		
		return removed;
	}	
	
	public RNAParameterSet getCurrentParameters() {
		return currentParameters;
	}
	
}
