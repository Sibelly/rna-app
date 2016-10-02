package ufms.facom.rna.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import ufms.facom.rna.internal.RNACreateNetworkAction;
import ufms.facom.rna.internal.model.RNAParameterSet;
import ufms.facom.rna.internal.util.RNAResources;
import ufms.facom.rna.internal.util.RNAUtil;
import ufms.facom.rna.internal.util.UIUtil;
import ufms.facom.rna.internal.util.RNAResources.ImageName;

public class RNAMainPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = 6600012510141101516L;

	private final RNAUtil rnaUtil;
	private final RNAParameterSet params;

	private JPanel paramsPnl;
	private JPanel fileParamPnl;
	private JLabel fileNameLbl;
	
    //Parameters
    public File file;
   	public boolean optGeneInt = false;
   	public boolean optStatesTrans = false;
   	public boolean optMai = false;	

	/**
	 * The actual parameter change panel that builds the UI
	 * @param createNetworkAction 
	 */
	public RNAMainPanel(final CySwingApplication swingApplication, final RNACreateNetworkAction createNetworkAction, final RNAUtil rnaUtil) {
		this.rnaUtil = rnaUtil;
		this.params = this.rnaUtil.getCurrentParameters();

		setMinimumSize(new Dimension(340, 400));
		setPreferredSize(new Dimension(380, 400));
		
		final JButton createNetworkBtn = new JButton(createNetworkAction);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGap(1, 1, 1)
				.addComponent(getParamsPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getFileParamPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(createNetworkBtn)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getParamsPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getFileParamPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(0, 1, Short.MAX_VALUE)
				.addComponent(createNetworkBtn)
		);
	}
	
	
	private JPanel getParamsPnl() {
		if (paramsPnl == null) {
			paramsPnl = new JPanel();
			paramsPnl.setBorder(UIUtil.createTitledBorder("Create Network View"));
			
			final JCheckBox geneInteractionCkb = new JCheckBox("Gene regulatory network");
			geneInteractionCkb.addItemListener(new RNAMainPanel.OptGeneIntCheckBoxAction());
			final JCheckBox statesTransitionCkb = new JCheckBox("State transition diagram");
			statesTransitionCkb.addItemListener(new RNAMainPanel.OptStatesTransCheckBoxAction());
			
			final JLabel showLabel = new JLabel("Show view:");
			showLabel.setHorizontalAlignment(JLabel.RIGHT);
			showLabel.setMinimumSize(new Dimension(showLabel.getMinimumSize().width, geneInteractionCkb.getMinimumSize().height));
	        
			final GroupLayout layout = new GroupLayout(paramsPnl);
			paramsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(showLabel)
					).addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(geneInteractionCkb)
							.addComponent(statesTransitionCkb)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addGroup(layout.createSequentialGroup()
							.addComponent(showLabel)
					).addGroup(layout.createSequentialGroup()
							.addComponent(geneInteractionCkb)
							.addComponent(statesTransitionCkb)
					)
			);
			
		}
		return paramsPnl;
	}	
	
	private JPanel getFileParamPnl() {
		if (fileParamPnl == null) {
			fileParamPnl = new JPanel();
			fileParamPnl.setBorder(UIUtil.createTitledBorder("Select File"));
			
			JButton selectFileBtn = new JButton("Open File");
			selectFileBtn.setActionCommand("change");
			selectFileBtn.addActionListener(new ChooseFileAction());
			
			this.fileNameLbl = new JLabel("No file selected");
	        
			final GroupLayout layout = new GroupLayout(fileParamPnl);
			fileParamPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(selectFileBtn)
					).addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(fileNameLbl)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addGroup(layout.createSequentialGroup()
							.addComponent(selectFileBtn)
					).addGroup(layout.createSequentialGroup()
							.addComponent(fileNameLbl)
					)
			);
			
		}
		return fileParamPnl;
	}
	
	/**
	 * Handles setting of the gene interaction view parameter
	 */
	private class OptGeneIntCheckBoxAction implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			params.setOptGeneInt(e.getStateChange() != ItemEvent.DESELECTED);
		}
	}
	
	/**
	 * Handles setting of the states transition view parameter
	 */
	private class OptStatesTransCheckBoxAction implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			params.setOptStatesTrans(e.getStateChange() != ItemEvent.DESELECTED);
		}
	}
	
	/**
	 * Handles setting of the file parameter
	 */
	private class ChooseFileAction implements ActionListener{
		public void actionPerformed(ActionEvent ae) {
			JFileChooser fileChooser = new JFileChooser();
			int returnValue = fileChooser.showOpenDialog(null);
			if(returnValue == JFileChooser.APPROVE_OPTION) {
				params.setFile(fileChooser.getSelectedFile());
				/*Change the label to the name of the file*/
				if("change".equals(ae.getActionCommand())){
					fileNameLbl.setText(params.getFile().getName());
				}
			}
		}
	}
	
	public RNAParameterSet getCurrentParamsCopy() {
		return params;
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		URL iconURL = RNAResources.getUrl(ImageName.LOGO_SMALL);
		return new ImageIcon(iconURL);
	}

	@Override
	public String getTitle() {
		return "";
	}

}
