package org.cytoscape.ufms.facom.rna.internal.model;

import java.io.File;

public class RNAParameterSet {

    //Parameters
    public File file;
	public boolean optGeneInt;
   	public boolean optStatesTrans;
   	public int interactionNumber; /*The number of times that two states are sorted out*/
	public Double derridaCoefficient /*Identify turbulent behavior (greater than 0: chaotic behavior | equals 0: critical behavior | less than 0: frozen behavior)*/;
   	
	public RNAParameterSet() {
		//default parameters
		setDefaultParams();
	}
   	
   	/*getter & setter parameters*/
   	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public boolean isOptGeneInt() {
		return optGeneInt;
	}

	public void setOptGeneInt(boolean optGeneInt) {
		this.optGeneInt = optGeneInt;
	}

	public boolean isOptStatesTrans() {
		return optStatesTrans;
	}

	public void setOptStatesTrans(boolean optStatesTrans) {
		this.optStatesTrans = optStatesTrans;
	}
	
   	public int getInteractionNumber() {
		return interactionNumber;
	}

	public void setInteractionNumber(int interactionNumber) {
		this.interactionNumber = interactionNumber;
	}
	
	public Double getDerridaCoefficient() {
		return derridaCoefficient;
	}

	public void setDerridaCoefficient(Double derridaCoefficient) {
		this.derridaCoefficient = derridaCoefficient;
	}
	
	/**
	 * Method for setting all parameters to their default values
	 */
	public void setDefaultParams() {
		setAllAlgorithmParams(false, false, null, 0, 0.0);
	}
	
	/**
	 * Convenience method to set all the main algorithm parameters
	 *
	 * @param optGeneInt show the network of gene interaction
	 * @param optStatesTrans show the network of states transition
	 * @param file file that creates the network
	 * @param derridaCoefficient number of interactions to calculate network behavior (used only in the results panel)
	 */
	public void setAllAlgorithmParams(boolean optGeneInt, boolean optStatesTrans, File file, int interactionNumber, Double derridaCoefficient) {
   		this.optGeneInt = optGeneInt;
   		this.optStatesTrans = optStatesTrans;
   		this.file = file;
   		this.interactionNumber = interactionNumber;
   		this.derridaCoefficient = derridaCoefficient;
	}
	
}
