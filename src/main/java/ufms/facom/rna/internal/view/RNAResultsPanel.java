package ufms.facom.rna.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import ufms.facom.rna.internal.RNADiscardResultAction;
import ufms.facom.rna.internal.util.RNAUtil;
import ufms.facom.rna.internal.util.UIUtil;

public class RNAResultsPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = 8652084905407230308L;
	
	private String title;
	private final int resultId;
	private final RNAUtil rnaUtil;
	private final RNADiscardResultAction discardResultAction;
	private final CySwingApplication swingApplication;
	
	//Data
	private Map<Integer, Integer> tamanhosBacias;
	private Map<Integer, Integer> tamanhosAtratores;
	private ArrayList<Integer> matrizIni;
	private Double entropy;
	private Double maxEntropy;
	private Double derridaCoefficient;
	private int numGenes;
	private int qntEstados;
	private int interactionNumber;
	private NumberFormat formatter;  
	

	// Graphical classes
	private ResultsPanel resultsPnl;
	private JPanel bottomPnl;
	private JLabel maxEntropyLbl;
	private JLabel entropyLbl;
	private JLabel derridaLbl;
	//private JFormattedTextField derridaTxt;
	private JLabel interacionNumberLbl;
	private JFormattedTextField interacionNumberTxt;
	
	public RNAResultsPanel(String nomeArquivo, int resultId, double entropy, double maxEntropy, Map<Integer, Integer> tamanhosAtratores, Map<Integer, Integer> tamanhosBacias, double derridaCoefficient, ArrayList<Integer> matrizIni, int numGenes, int qntEstados, RNAUtil rnaUtil, CySwingApplication swingApplication, RNADiscardResultAction discardResultAction) {
		this.title = nomeArquivo;
		this.resultId = resultId;
		this.entropy = entropy;
		this.maxEntropy = maxEntropy;
		this.tamanhosAtratores = tamanhosAtratores;
		this.tamanhosBacias = tamanhosBacias;
		this.derridaCoefficient = derridaCoefficient;
		this.matrizIni = matrizIni;
		this.numGenes = numGenes;
		this.qntEstados = qntEstados;
		this.swingApplication = swingApplication;
		this.rnaUtil = rnaUtil;
		this.discardResultAction = discardResultAction;
		this.resultsPnl = new ResultsPanel();
		this.interactionNumber = (qntEstados/2);
		rnaUtil.getCurrentParameters().setInteractionNumber((qntEstados/2));
		
		formatter = new DecimalFormat("#0.00");
		formatter.setParseIntegerOnly(true);
		
		setLayout(new BorderLayout());
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(resultsPnl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBottomPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(resultsPnl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBottomPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}

	public Component getComponent() {
		return this;
	}


	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}
	
	public String getTitle() {
		return title;
	}

	public int getResultId() {
		return this.resultId;
	}

	public Icon getIcon() {
		return null;
	}
	
	/**
	 * Panel that contains the results table with a scroll bar.
	 */
	@SuppressWarnings("serial")
	private class ResultsPanel extends JPanel {
		
		private final ResultsTableModel resultsModel;
		private final JTable table;
		
		public ResultsPanel() {
			setLayout(new BorderLayout());
			
			// main data table
			resultsModel = new ResultsTableModel(); 
			
			table = new JTable(resultsModel);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setRowHeight(20);
			table.setIntercellSpacing(new Dimension(0, 4));
			table.setShowGrid(true);
			table.setShowHorizontalLines(true);
			table.setGridColor(new JSeparator().getForeground());
			table.getTableHeader().setReorderingAllowed(false);
			
			//Column "Basins of Attraction"
			table.getColumnModel().getColumn(0).setMinWidth(20);
			table.getColumnModel().getColumn(0).setPreferredWidth(20);
			//Column "Sizes of the Basins"
			table.getColumnModel().getColumn(1).setMinWidth(20);
			table.getColumnModel().getColumn(1).setPreferredWidth(20);
			//Column "Attractors"
			table.getColumnModel().getColumn(2).setMinWidth(20);
			table.getColumnModel().getColumn(2).setPreferredWidth(20);
			
			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
			
			// Centralizando as colunas
			for (int i = 0; i < table.getColumnCount(); i++){
				table.setDefaultRenderer(table.getColumnClass(i), centerRenderer);
			}
			
			final JScrollPane tableScrollPane = new JScrollPane(table);
			tableScrollPane.getViewport().setBackground(Color.WHITE);

			add(tableScrollPane, BorderLayout.CENTER);
			add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
		}
	}
	
	/********************************************************************************************************************/	
	/**
	 * Handles the data to be displayed in the results panel table
	 */
	@SuppressWarnings("serial")
	private class ResultsTableModel extends AbstractTableModel {

		private final String[] columnNames = { "Basins of Attraction", "Sizes of the Basins", "Attractors" };
		private final Object[][] data; // the actual table data

		public ResultsTableModel() {
			data = new Object[tamanhosBacias.size()-1][columnNames.length];
			
			//Ordenando o Map da maior para a menor bacia
			Map<Integer, Integer> sortedMap = sortMap(tamanhosBacias);
			
			int i = 0;
			for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
				//System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
				if(entry.getValue() != 0){
					data[i][0] = entry.getKey();
					data[i][1] = entry.getValue();
					data[i][2] = tamanhosAtratores.get(entry.getKey());
					i++;
				}
			}
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		@Override
		public void setValueAt(Object object, int row, int col) {
			data[row][col] = object;
			fireTableCellUpdated(row, col);
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	}	
	/********************************************************************************************************************/
	
	private Map<Integer, Integer> sortMap(Map<Integer, Integer> unsortMap) {

		// Convert Map to List
		List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
			public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
		for (Iterator<Map.Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Integer, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	/**
	 * Creates a panel containing the results calculate to the network of States Transition
	 * 
	 * @return Panel containing the labels and results
	 */
	private JPanel getBottomPnl() {
		if (bottomPnl == null) {
			bottomPnl = new JPanel();
			bottomPnl.setBorder(UIUtil.createTitledBorder("States Transition Network Informations"));
			
			maxEntropyLbl = new JLabel("Maximum Entropy: " + formatter.format(maxEntropy));
			maxEntropyLbl.setToolTipText("<html>The maximum entropy possible of the States Transition Network.</html>");
			
			entropyLbl = new JLabel("Entropy: " + formatter.format(entropy));
			entropyLbl.setToolTipText("<html>The entropy calculated to the States Transition Network.</html>");
			
			derridaLbl = new JLabel("Derrida Coefficient: " + formatter.format(derridaCoefficient));
			derridaLbl.setToolTipText("<html>Identify turbulent behavior of the nework<br>" +
										"greater than 0: chaotic behavior<br>" +
										"equals 0: critical behavior<br>" + 
										"less than 0: frozen behavior</html>");
			//derridaLbl.setToolTipText("<html>The derrida coefficient calculates to the States Transition Network.</html>");
			
			/*Number of Interactions Label*/
			interacionNumberLbl = new JLabel("Number of Interactions: ");
			interacionNumberLbl.setMinimumSize(getInteractionNumberTxt().getMinimumSize());
			interacionNumberLbl.setLabelFor(getInteractionNumberTxt());
			interacionNumberLbl.setToolTipText(getInteractionNumberTxt().getToolTipText());
			
			// The Derrida button
			final JButton derridaButton = new JButton("Calculate Derrida Coefficient");
			derridaButton.setActionCommand("change");
			derridaButton.addActionListener(new DerridaAction());
			derridaButton.setToolTipText("Calculates the derrida coefficient");
	
			final GroupLayout layout = new GroupLayout(bottomPnl);
			bottomPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup((layout.createParallelGroup()
							.addComponent(maxEntropyLbl)
							.addComponent(entropyLbl)
							.addComponent(derridaLbl)
							.addComponent(interacionNumberLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getInteractionNumberTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(derridaButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			layout.setVerticalGroup((layout.createSequentialGroup()
							.addComponent(maxEntropyLbl)
							.addComponent(entropyLbl)
							.addComponent(derridaLbl)
							.addComponent(interacionNumberLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getInteractionNumberTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(derridaButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			

		}

		return bottomPnl;
	}
	
/*	private JFormattedTextField getDerridaCoefficientTxt() {
		if (derridaTxt == null) {
			derridaTxt = new JFormattedTextField(formatter);
			derridaTxt.setColumns(3);
			derridaTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			//derridaTxt.addPropertyChangeListener("value", new RNAMainPanel.FormattedTextFieldAction());
			derridaTxt.setToolTipText("<html>Identify turbulent behavior of the nework<br>" +
										"greater than 0: chaotic behavior<br>" +
										"equals 0: critical behavior<br>" + 
										"less than 0: frozen behavior</html>");
			derridaTxt.setText(String.valueOf(this.rnaUtil.getCurrentParameters().getDerridaCoefficient()));
		}
		
		return derridaTxt;
	}*/
	
	private JFormattedTextField getInteractionNumberTxt() {
		if (interacionNumberTxt == null) {
			interacionNumberTxt = new JFormattedTextField(new DecimalFormat("#0"));
			interacionNumberTxt.setColumns(10);
			interacionNumberTxt.setValue(interactionNumber);
			interacionNumberTxt.setHorizontalAlignment(SwingConstants.LEADING);
			interacionNumberTxt.addPropertyChangeListener("value", new FormattedTextFieldAction());
			interacionNumberTxt.setToolTipText("<html>The number of times that two states<br>" + 
												"of the States Transition network<br>" + 
												"are sorted out</html>");
		}
		
		return interacionNumberTxt;
	}
	
	/********************************************************************************************************************/	
	/**
	 * Handles the computing of derrida coefficient in the result panel
	 */
	@SuppressWarnings("serial")
	private class DerridaAction extends AbstractAction {

		@Override
		public void actionPerformed(final ActionEvent ae) {

			ArrayList<Integer> estadoA = new ArrayList<Integer>();
			ArrayList<Integer> estadoB = new ArrayList<Integer>();
			double resX = 0, resY = 0, x, y;
			
			for(int i = 0; i < interactionNumber; i++){
				estadoA = devolveBinariosDerrida(numGenes, qntEstados);
				//System.out.println("estadoA: " + estadoA);
				estadoB = devolveBinariosDerrida(numGenes, qntEstados);
				//System.out.println("estadoB: " + estadoB);
				x = calculaDistanciaHamming(estadoA, estadoB);
				
				/*Calculando o proximo estado*/
				estadoA = calculaProximoEstado(matrizIni, estadoA);
				//System.out.println("próximo estadoA: " + estadoA);
				estadoB = calculaProximoEstado(matrizIni, estadoB);
				//System.out.println("próximo estadoB: " + estadoB);
				y = calculaDistanciaHamming(estadoA, estadoB);
				
				//System.out.println("x: " + x + " y: " + y);
				
				y=y/numGenes;
				x=x/numGenes;
				
				//System.out.println("xn: " + x + " yn: " + y);
				
				resX = resX + (y*x);
				resY = resY + (x*x);
			}
			/*Setando o novo valor calculado de Derrida*/
			rnaUtil.getCurrentParameters().setDerridaCoefficient(resX/resY);
			/*Change the label to the name of the file*/
			if("change".equals(ae.getActionCommand())){
				derridaLbl.setText("<html>Derrida Coefficient: <b>" + String.valueOf(formatter.format(resX/resY)) + "</b></html>");
			}
		}
		
		/*
		 * Sorteia dois numeros (limitados ao numero max de estados), passa esse numero pra "binario"
		 * e devolve como uma arraylist
		 * */
		private ArrayList<Integer> devolveBinariosDerrida(int numGenes, int quantidadeEstados){
			String binA;
			int j;
			Random rand = new Random();
			ArrayList<Integer> estado = new ArrayList<Integer>();
			
			binA = Integer.toBinaryString(rand.nextInt(quantidadeEstados));
			
			while(binA.length() < numGenes){
				binA = 0 + binA;
			}
			
			for(int i = 0; i < numGenes; i++){
				j = (int) binA.charAt(i);
				j = j%48;
				estado.add(j);
			}
			
			return estado;
		}	
		
		/*
		 * Calcula a distancia (diferencas) entre dois arrays
		 * */
		private int calculaDistanciaHamming(ArrayList<Integer> a, ArrayList<Integer> b){
			int ret = 0;	
			for(int i = 0; i < a.size(); i++){
				if(a.get(i) != b.get(i))
					ret++;
			}
			return ret;
		}
		
		private ArrayList<Integer> calculaProximoEstado(ArrayList<Integer> matriz, ArrayList<Integer> estados){
			int descEstado;
			ArrayList<Integer> estadosNov = new ArrayList<Integer>(); 
			for(int i = 0; i < estados.size(); i++){
				descEstado = 0;
				for(int j = 0; j < estados.size(); j++){
					descEstado = descEstado + (matriz.get((i* estados.size())+ j) * estados.get(j));
				}
				if(descEstado > 0){
					estadosNov.add(1);
				}
				else if(descEstado < 0){
					estadosNov.add(0);
				}
				else{
					estadosNov.add(estados.get(i));
				}
			}
			return estadosNov;
		}
	}
	/********************************************************************************************************************/	
	
	/**
	 * Handles setting for the text field parameters that are numbers.
	 * Makes sure that the numbers make sense.
	 */
	private class FormattedTextFieldAction implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			final JFormattedTextField source = (JFormattedTextField) e.getSource();

			String message = "The value you have entered is invalid.\n";
			boolean invalid = false;

			if (source == interacionNumberTxt) {
				Number value = (Number) interacionNumberTxt.getValue();
				
				if ((value != null) && (value.intValue() > 1)) {
					rnaUtil.getCurrentParameters().setInteractionNumber(value.intValue());
					interactionNumber = rnaUtil.getCurrentParameters().getInteractionNumber();
				} else {
					source.setValue((qntEstados/2));
					interactionNumber = qntEstados/2;
					message += "The number of interactions must be greater than 1.";
					invalid = true;
				}
			} 
			if (invalid) {
				JOptionPane.showMessageDialog(swingApplication.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	
	public void discard(final boolean requestUserConfirmation) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				discardResultAction.putValue(RNADiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND, true);
			}
		});
	}
}
