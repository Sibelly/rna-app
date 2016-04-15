package ufms.facom.rna.internal.task;

import java.awt.Color;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

import ufms.facom.rna.internal.RNADiscardResultAction;
import ufms.facom.rna.internal.RNAResultsPanelAction;
import ufms.facom.rna.internal.model.RNAParameterSet;
import ufms.facom.rna.internal.util.RNAUtil;
import ufms.facom.rna.internal.view.RNAResultsPanel;

public class RNACreateNetworkTask extends AbstractTask {
	
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
	
	/*Parameters*/
	public File arquivo;
   	public boolean optInt;
   	public boolean optStates;
   	public boolean optMai;	
	private static int numIteracoes;	
	
    private final String ENRICH_NEG = "enrichments.negative"; 
    private final String ENRICH_POS =  "enrichments.positive"; 
    
    private boolean interrupted = false;
    private int resultId;
    
    private final RNAUtil rnaUtil;
    private final RNAParameterSet currentParams;
	
	/*Abandon all hope, ye who enter here*/
	public RNACreateNetworkTask(CyNetworkManager netMgr, CyNetworkFactory cnf, CyNetworkNaming namingUtil, CyNetworkViewFactory netView, CyNetworkViewManager networkViewManager, DialogTaskManager dialogTaskManager, VisualMappingManager visualMappingManager, CyLayoutAlgorithmManager cyLayoutManager, VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete, CyServiceRegistrar cyServiceRegistrar, CySwingApplication cyDesktopService, CyApplicationManager applicationManager, RNAUtil rnaUtil, int resultId, RNAParameterSet currentParams){
		this.netMgr = netMgr;
		this.cnf = cnf;
		this.namingUtil = namingUtil;
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
	
   	public static int getNumIteracoes() {
		return numIteracoes;
	}

	public static void setNumIteracoes(int numIteracoes) {
		RNACreateNetworkTask.numIteracoes = numIteracoes;
	}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	public void run(TaskMonitor taskMonitor) {
		
		if (taskMonitor == null){
			throw new IllegalStateException("Task Monitor is not set.");
		}
		
		if(currentParams != null){
			this.arquivo = currentParams.getFile();
			this.optInt = currentParams.isOptGeneInt();
			this.optStates = currentParams.isOptStatesTrans();
		}
		
        //Título da tarefa principal
		taskMonitor.setTitle("Generating/Analysing Network");
		taskMonitor.setProgress(0.001);
		taskMonitor.setStatusMessage("Generating Network");

		//Matriz ini contém as interações entre os genes, nodes é um array que representa um
		//estado qualquer durante a execução (ex nodes = 0, 0, ..., 0 é o estado em que todos
		//os genes estão desativados)
		ArrayList<Integer> matrizIni = new ArrayList<Integer>();
		ArrayList<Integer> nodes 	 = new ArrayList<Integer>();
		
		//Criando redes vazias
		//Contem os nós e suas relacoes
		CyNetwork redeNodes = cnf.createNetwork();
		//Contem a transicao de estados
		CyNetwork redeEstados = cnf.createNetwork();
		//Setando o nome da rede
		redeNodes.getRow(redeNodes).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(arquivo.getName()));
		redeEstados.getRow(redeEstados).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("States Transition"));
		//Outra forma de setar o nome da rede
		//redeNodes.getRow(redeNodes).set(CyNetwork.NAME, "NetworkName");
		
		//indices gerais 
		int x = 0;
		int i = 0;
		int j = 0;
		int q = 0;
		
		//suidsNodes contém os ids (usados pelo cytoscape) dos nós.
		//Como inserimos os nós em ordem, podemos mapear os ids locais
		//(ordem de inserção) com os ids internos (criados pelo cytoscape)
		//assim é possível mudar configurações (ou recuperar nós) de forma
		//simples, usando apenas um índice
		ArrayList<Long> suidsNodes = new ArrayList<Long>();
		BufferedReader reader = null;
		String strRead = "";
		String[] nomes = null;

		//Array para mapear as cores em indices
		Color[] cores = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};
		
		//Extraindo os nomes dos nós do arquivo
		try {
			reader = new BufferedReader(new FileReader(arquivo));
			strRead = reader.readLine();
			//remove espacos e whitespaces
			strRead = strRead.replaceAll("\\s","");
			strRead = strRead.replaceAll(" ", ""); 
			nomes = strRead.split(",");
			//Setando os vertices na redeNodes
			while ((strRead = reader.readLine()) != null){
				nodes.add(0);			 
				CyNode node = redeNodes.addNode();
				redeNodes.getDefaultNodeTable().getRow(node.getSUID()).set("name", nomes[i]);
				//insere os ids dos nos em ordem 
				suidsNodes.add(node.getSUID()); 
				i++;				
			}

			i = 0;
			//Abre o arquivo novamente (pra voltar pro início)
			reader = new BufferedReader(new FileReader(arquivo));
			//e pula a linha dos nomes
			strRead = reader.readLine();
			//Setando as arestas na redeNodes
			while ((strRead = reader.readLine()) != null){
				j = 0;
				CyNode a = redeNodes.getNode(suidsNodes.get(i));
				i++;
					//Tira a virgula e separa os valores da linha
					for(String part: strRead.split(",")){				
						//Tira espacos
						part = part.replaceAll("\\s", "");		
						//e insere no array de valores (matriz)
						matrizIni.add(Integer.valueOf(part));
						CyNode b = redeNodes.getNode(suidsNodes.get(j));
						j++;
						
						//Esses ifs adicionam uma aresta entre os nos que tem relacao, a partir da matriz informada
						if(Integer.valueOf(part) == 1){						
							CyEdge newEdge = redeNodes.addEdge(a, b, true);
							redeNodes.getRow(newEdge).set(CyEdge.INTERACTION, ENRICH_POS);
						}else if(Integer.valueOf(part) == -1){
							CyEdge newEdge = redeNodes.addEdge(a, b, false);
							redeNodes.getRow(newEdge).set(CyEdge.INTERACTION, ENRICH_NEG);
						}										
				}	
			}
		}catch (FileNotFoundException e1) {
			System.out.println("Arquivo nao encontrado!");
		}catch (IOException e2){
			System.out.println("Erro I/O");
		}catch (NumberFormatException exc){
			System.out.println("Erro ao converter uma das entradas");
		}	
		
		//Número de genes da rede de interação gênica
		int numGenes = nodes.size();
		//Numero de estados possiveis		
		int qntEstados = (int) Math.pow(2, nodes.size());
		ArrayList <Integer> resAtual = new ArrayList<Integer>(); 
		
		/*Caso precise guardar os resultados, essa variavel foi feita pra isso*/
		//ArrayList <ArrayList> resFinal = new ArrayList<ArrayList>();
		/*derp*/
		//int numGenes = (int) (Math.log(qntEstados)/Math.log(2));
		
		//Cria uma array list que contem os suids dos nos
		ArrayList<Long> suidsEstados = new ArrayList<Long>(); 
		
		//Setando os vertices na redeEstados
		for (i = 0; i < qntEstados; i++){
			CyNode node = redeEstados.addNode();
				//nome binario (caso algum dia precise usar)
				//redeEstados.getDefaultNodeTable().getRow(node.getSUID()).set("name", String.format("%"+numGenes+"s", Integer.toBinaryString(i)).replace(' ', '0')); 
			//nome decimal
			redeEstados.getDefaultNodeTable().getRow(node.getSUID()).set("name", "Node " + i);	
			//Adiciona os nos em ordem (mesma coisa do suidsNodes)
			suidsEstados.add(node.getSUID()); 
		}
		
		int intBin = 0;
		
		//Setando as arestas na redeEstados
		for (i = 0; i < qntEstados && nodes != null; i++){
			resAtual = null; //because reasons
			resAtual = calculaProximoEstado(matrizIni, nodes);
			
			/*Caso precise guardar os estados, aquela variavel ali de cima é usada aqui*/
			//resFinal.add(resAtual);
			//System.out.println(resAtual);
			//add aresta estado atual -> proximo estado
			intBin = converte(resAtual);
			CyNode a = redeEstados.getNode(suidsEstados.get(i));
			CyNode b = redeEstados.getNode(suidsEstados.get(intBin));
		
			//Como os nós foram inseridos nessa array list em ordem >E< a sequência    
			//que representa o estado atual pode ser lido como um binário
			//da pra recuperar o nó pelo valor decimal do estado atual 
			//(estado 0, 0, ..., 0 é o nó da posição 0),
			//(estado 0, 0, ..., 1 é o nó da posição 1) e assim sucessivamente								     
			CyEdge e = redeEstados.addEdge(a, b, false);
			nodes = incrementa(nodes);	//passa pro próximo estado
		}
		
		netMgr.addNetwork(redeNodes);	
		netMgr.addNetwork(redeEstados);	
		
		if (interrupted) return;
		
		taskMonitor.setProgress(0.01);
		taskMonitor.setStatusMessage("Analysing Network");
		
		//Inicializando o array nodes
		nodes = new ArrayList<Integer>();	
		for(int l = 0; l < numGenes; l++){
			nodes.add(0);
		}
		
		ArrayList<Integer> nodesSegundo	 = new ArrayList<Integer>();

		int tamAtual, baciaCerta, count, baciaAtual=1;
		int[] bacia   = new int[qntEstados];												//marca o número da bacia do estado correspondente, caso ele ainda nao tenha bacia bacia[estado] == -1
		Map<Integer, Integer> tamanhosBacias 	= new HashMap<Integer, Integer>(); 			//guarda os tamanhos das bacias
		Map<Integer, Integer> tamanhosAtratores = new HashMap<Integer, Integer>(); 			//guarda os tamanhos dos atratores

		Arrays.fill(bacia, -1);																//Define todas as bacias como -1
		ArrayList<Integer> visitados;														//marca os nós que foram visitados a cada iteração
		String supp;
		String[] arr = new String[numGenes];
		int aux, indiceTransf;
		tamanhosBacias.put(1, 0);															//inicia a primeira bacia com tamanho zero		
		
		for(i = 0; i < qntEstados; i++){													//executa 1 vez para cada estado
			supp = String.format("%"+numGenes+"s", Integer.toBinaryString(i)).replace(' ', '0');	//passa o i para binario, mantendo o formato certo (0 nao sao substituidos por espaços)
			arr = supp.split(""); 															//split na string supp, separa todos os digitos 
			indiceTransf = 0;
			for (String string : arr) { 													//Copia o "i" binario para o array nodes
				aux = Integer.parseInt(string);
				nodes.set(indiceTransf, aux);
				indiceTransf++;
			}

			tamAtual = 0;
			visitados = new ArrayList<>();
			while(bacia[converte(nodes)] == -1){ 
				tamAtual++;
				visitados.add(converte(nodes));
				bacia[converte(nodes)] = baciaAtual;
				nodes = calculaProximoEstado(matrizIni, nodes);
			}
			
			if(tamAtual > 0){
				if(bacia[converte(nodes)] == baciaAtual){
					tamanhosBacias.put(baciaAtual, tamanhosBacias.get(baciaAtual) + tamAtual); 	//Soma o tamanho da bacia que esta armazenado com o tamanho atual
					bacia[converte(nodes)] = -1;												//seta a bacia do node atual para -1, para descobrir o tamanho da bacia de atração
					count =1;
					nodesSegundo = calculaProximoEstado(matrizIni, nodes);

					while(bacia[converte(nodesSegundo)] != -1){									//Enquanto nao encontrar o node que foi marcado como -1
						count++;																//incrementa o contador e pula pro proximo nó
						nodes = calculaProximoEstado(matrizIni, nodesSegundo);								//Como nós temos certeza  que estamos em um atrator, ao fim do while teremos o tamanho do atrator(1 loop completo nele)
						nodesSegundo = nodes;
					
					}
					bacia[converte(nodes)] = baciaAtual;										//Volta a marcação da bacia do nó original
					tamanhosAtratores.put(baciaAtual, count);									//Marca o tamanho do atrator
					baciaAtual++;																//incrementa o identificador da bacia atual
					tamanhosBacias.put(baciaAtual, 0);											//inicializa o tamanho da proxima bacia
				}
				else{																			//caso o caminho tenha caído em um nó com um atrator marcado (nó atual faz parte de uma bacia já identificada)
					baciaCerta = bacia[converte(nodes)];										//Guarda o valor da bacia certa
					tamanhosBacias.put(baciaCerta, (tamanhosBacias.get(baciaCerta) + tamAtual));//incrementa o valor da bacia certa
					for(int ind=0; ind<visitados.size(); ind++){								//for para marcar os nós visitados com a bacia certa
						bacia[visitados.get(ind)] = baciaCerta;
					}
				}
			}
		}

		double entropia = calculaEntropia(tamanhosBacias, qntEstados);
		double maxEntropia = calculaEntropiaMaxima(qntEstados, (tamanhosBacias.size() - 1));
		/*Fiz isso para calcular o coeficiente de derrida por padrão com a metade do numero de estados da rede de transição de estados*/
		setNumIteracoes(qntEstados/2);
		double derrida = derridaCoefficient(numGenes, qntEstados, matrizIni);
		currentParams.setSamplingRate((qntEstados/2));
		currentParams.setDerridaCoefficient(derrida);
		
		rnaUtil.addResult(redeNodes.getSUID());
		RNADiscardResultAction discardResultAction = new RNADiscardResultAction("Discard Result", resultId, applicationManager, cyDesktopService, networkViewManager, cyServiceRegistrar, rnaUtil);
		
		RNAResultsPanel resultsPnl = new RNAResultsPanel(arquivo.getName(), resultId, entropia, maxEntropia, tamanhosAtratores, tamanhosBacias, derrida, matrizIni, numGenes, qntEstados, rnaUtil, cyDesktopService, discardResultAction);
		RNAResultsPanelAction controlPanelAction = new RNAResultsPanelAction(arquivo.getName(), cyDesktopService, cyServiceRegistrar, resultsPnl);

		if (interrupted) return;
		
		/*Generating View*/
		ContinuousMapping nodeColorContinuous = (ContinuousMapping) vmfFactoryContinuous.createVisualMappingFunction("Fold Change", Double.class, BasicVisualLexicon.NODE_FILL_COLOR);
        DiscreteMapping<String, Paint> edgeColorDiscrete = (DiscreteMapping<String, Paint>) vmfFactoryDiscrete.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		CyLayoutAlgorithm layout;
		
		//Caso optInt seja verdadeiro o usuário quer exibir o grafo de interação (o menor)
		if(optInt){ 
			taskMonitor.setProgress(0.1);
			taskMonitor.setStatusMessage("Generating Gene Interaction View");
			CyNetworkView viewRedeNodes = netView.createNetworkView(redeNodes);
			networkViewManager.addNetworkView(viewRedeNodes);
			
			//Percorro os nos/vertices da redeNodes mudando a visualizacao para cada um
			for (CyNode cyNode : redeNodes.getNodeList()){
				View<CyNode> nodeView = viewRedeNodes.getNodeView(cyNode);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.GRAY);
			}

			//Percorro as arestas da redeNodes mudando a visualizacao para cada uma
			for (CyEdge cyEdge : redeNodes.getEdgeList()){
				View<CyEdge> edgeView = viewRedeNodes.getEdgeView(cyEdge);
				if(cyEdge.isDirected()){
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW); 
				}else{
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.T);
				}
			}
		
			//Aplicando o layout da redeNodes
			layout = cyLayoutManager.getLayout("hierarchical");
			dialogTaskManager.execute(layout.createTaskIterator(viewRedeNodes, layout.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
			VisualStyle styleRedeNodes = visualMappingManager.getVisualStyle(viewRedeNodes);
		
			//Adicionando a cor das arestas na redeNodes
			edgeColorDiscrete.putMapValue(ENRICH_NEG, Color.GREEN);
			edgeColorDiscrete.putMapValue(ENRICH_POS, Color.BLUE);
			styleRedeNodes.addVisualMappingFunction(edgeColorDiscrete);
			styleRedeNodes.addVisualMappingFunction(nodeColorContinuous); 
			visualMappingManager.addVisualStyle(styleRedeNodes);
			visualMappingManager.setVisualStyle(styleRedeNodes, viewRedeNodes);
		}

		//Caso optStates seja verdadeiro o usuário quer exibir o grafo de estados (o maior)
		if(optStates){ 
			taskMonitor.setProgress(0.1);
			taskMonitor.setStatusMessage("Generating States Trasition View");
			CyNetworkView viewRedeEstados = netView.createNetworkView(redeEstados);
			networkViewManager.addNetworkView(viewRedeEstados);	
			
			//Percorro os nos/vertices da redeEstados mudando a visualizacao para cada um
			for(i = 0; i < qntEstados; i++){
				View<CyNode> nodeView = viewRedeEstados.getNodeView(redeEstados.getNode(suidsEstados.get(i)));
				nodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
				q = bacia[i];
				//se tiver mais de 12 bacias as cores vão repetir ;-;
				nodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, cores[ q % 13]);		
				/* Aqui dá pra fazer de algumas formas diferentes
					* Usar o resto da divisao da marcação de bacia por 13 (o resultado entao ia ser uma sequencia 0, 1, 2, ..., 12, 0, 1,2 ...)
					* Definir uma cor para a bacia maior/bacias maiores e repetir cores somente para as menores
					* Criar mais cores usando new Color(rand, rand, rand)/ou criar mais cores pré determinadas

				Por enquanto eu vou deixar usando o resto pq é o jeito mais fácil
				*/
			}			
			
			/*
			for (CyNode cyNode : redeEstados.getNodeList()){
				View<CyNode> nodeView = viewRedeEstados.getNodeView(cyNode);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.GRAY);
			}
			*/	
			
			//Percorro as arestas da redeEstados mudando a visualizacao para cada uma
			for (CyEdge cyEdge : redeEstados.getEdgeList()){
				View<CyEdge> edgeView = viewRedeEstados.getEdgeView(cyEdge);
				edgeView.setLockedValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW); 
			}
			
			//Aplicando o layout da redeEstados
			layout = cyLayoutManager.getLayout("grid");
			dialogTaskManager.execute(layout.createTaskIterator(viewRedeEstados, layout.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
			VisualStyle styleRedeEstados = visualMappingManager.getVisualStyle(viewRedeEstados);
			styleRedeEstados.addVisualMappingFunction(nodeColorContinuous);
			visualMappingManager.addVisualStyle(styleRedeEstados);
			visualMappingManager.setVisualStyle(styleRedeEstados, viewRedeEstados);
		}//fim if (optStates)	

		if(optMai){
			CyNetwork redeMaiorBacia = cnf.createNetwork();
			redeMaiorBacia.getRow(redeMaiorBacia).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("Rede da Maior Bacia"));
		
			netMgr.addNetwork(redeMaiorBacia);	
			CyNetworkView viewRedeMaiorBacia = netView.createNetworkView(redeMaiorBacia);
			networkViewManager.addNetworkView(viewRedeMaiorBacia);
			
			ArrayList<Integer> baciasRend = retornaEstadosMaiorBacia(tamanhosBacias);
			Map<Integer, Long> idsRend 	= new HashMap<Integer, Long>(); 

			//Extraindo o nós da maior bacia da redeEstados
			//percorre todos os estados
			for(j = 0; j < baciasRend.size(); j++){
				CyNode node = redeMaiorBacia.addNode();
				redeMaiorBacia.getDefaultNodeTable().getRow(node.getSUID()).set("name", "Node " + baciasRend.get(j));
				idsRend.put(baciasRend.get(j), node.getSUID());
			}
			
			//Extraindo as arestas da maior bacia da redeEstados
			//percorre todos os nos da maior bacia
			int valorEstado;
			String valorEstString;
			for(valorEstado = 0, j = 0; j < baciasRend.size(); j++) {
				valorEstado = baciasRend.get(j);
				valorEstString = Integer.toBinaryString(valorEstado);
				
				while(valorEstString.length() < numGenes){
					valorEstString = 0 + valorEstString;
				}
				
				int valInd;
				for(int ind = 0; ind < numGenes; ind++){
					valInd = (int) valorEstString.charAt(ind);
					valInd = valInd%48;
					nodes.set(ind, valInd);
				}
				
				resAtual = null; 
				resAtual = calculaProximoEstado(matrizIni, nodes);
				intBin = converte(resAtual);
				
				Long c = idsRend.get(j); 
				CyNode a = redeMaiorBacia.getNode(c);
				System.out.print("idsRend.get(j): " + idsRend.get(j) + " ");
				CyNode b = redeMaiorBacia.getNode(idsRend.get(intBin));
				System.out.print("idsRend.get(intBin): " + idsRend.get(intBin) + " ");
				CyEdge e = redeMaiorBacia.addEdge(a, b, false);
			}
			
			//Percorro os nos da redeMaiorBacia mudando a visualizacao para cada uma
			for (CyNode cyNode : redeMaiorBacia.getNodeList()){
				View<CyNode> nodeView = viewRedeMaiorBacia.getNodeView(cyNode);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.GRAY);
			}
			
			//Percorro as arestas da redeMaiorBacia mudando a visualizacao para cada uma
			for (CyEdge cyEdge : redeMaiorBacia.getEdgeList()){
				View<CyEdge> edgeView = viewRedeMaiorBacia.getEdgeView(cyEdge);
				edgeView.setLockedValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW); 
			}
			
			//Aplicando o layout da redeMaiorBacia
			layout = cyLayoutManager.getLayout("hierarchical");
			dialogTaskManager.execute(layout.createTaskIterator(viewRedeMaiorBacia, layout.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
			VisualStyle styleRedeMaiorBacia = visualMappingManager.getVisualStyle(viewRedeMaiorBacia);
			styleRedeMaiorBacia.addVisualMappingFunction(nodeColorContinuous);
			visualMappingManager.addVisualStyle(styleRedeMaiorBacia);
			visualMappingManager.setVisualStyle(styleRedeMaiorBacia, viewRedeMaiorBacia);
			
		}//fim if(optMai)
	}
	
	public static ArrayList<Integer> incrementa(ArrayList<Integer> lista){		
		boolean inc = false;
		for(int i = lista.size() - 1; i >= 0 && !inc; i--){
			if(lista.get(i) == 0){
				lista.set(i, 1);
				inc = true;
			}
			else{
				lista.set(i, 0);
			}
		}
		if(!inc)
			return null;
		return lista;
	}
	
	public static int expon(int a, int b){
		int res = 1;
		while(b > 0){
			res = res * a;
			b--;
		}
		return res;
	}
	
	public static ArrayList<Integer> calculaProximoEstado(ArrayList<Integer> matriz, ArrayList<Integer> estados){
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
	
	/* Entropia ==
	 *		 		Σ pi * log(1/pi)
	 *		 		pi = tam(bacia)/qntEstados
	 *		 
	 */	
	public static double calculaEntropia(Map<Integer, Integer> tamanhosBacias, int qntEstados){
		
		double entropia = 0, pi = 0, entrInd = 0, menorEntr = -1, maiorEntr = 0; 
		
		/* Esse for itera sobre todas as bacias, imprimindo seus tamanhos
		 * Logo, a partir do value teremos acesso aos tamanhos para o somatorio
		 * pi é a probabilidade de i, ou seja sorteando um numero aleatório é a chance 
		 * desse numero estar dentro da bacia i. (Tamanho da bacia/total de estados)
		 * entrInd é a entropia "individual", ou seja, um dos termos do somatório (pi * log(1/pi))
		 * Por fim, entropia é o valor final  (Σpi * log(1/pi))
		 */
		for(Integer key : tamanhosBacias.keySet()){
			Integer value = tamanhosBacias.get(key);
			if(value != 0){
				pi = (double) value/qntEstados;
				entrInd = pi * (Math.log(pi)/Math.log(2));
				entrInd = entrInd * -1;

				if(entrInd < menorEntr || menorEntr == -1){
					menorEntr = entrInd;
				}

				if(entrInd > maiorEntr){
					maiorEntr = entrInd;
				}
				entropia = (double) entropia + entrInd;
			}
		}

		return entropia;
	}
	
	/*
	 * Recebe um numero de estados e um numero de bacias e devolve a entropia maxima considerando esses dois valores.
	 * 
	 * (A entropia é maxima onde a distribuicao de estados é igual (ou o mais proximo de igual que se pode chegar) entre todas as bacias)
	 * */
	public static double calculaEntropiaMaxima(int poss, int numBacias){
		double[] bacias = new double[numBacias];						/*Guarda primeiro o numero de estados dentro de cada bacia, depois guarda a probabilidade de cada bacia*/
		double entropia=0, termoEntropia, off = 0;						/*entropia é o resultado final, termoEntropia é usada pra calcular cada termo individualmente, off é usado pra distribuir os estados entre as bacias*/
		int i;
		
		off = (double)poss/numBacias - (int) poss/numBacias;			
		i = 0;
		if(off > 0){													/*Caso o numero de bacias não seja multiplo do numero de estados*/
			off = poss;											
			while(off > numBacias){										/*conta quantos estados cabem em cada bacia, até que o numero de estados seja menor que o numero de bacias*/
				off = off-numBacias;
				i++;
			}
		}
		
		if(i == 0)														/*Caso o numero de bacias seja multiplo do numero de estados nao entra no if de cima e i continua com 0*/
			i = poss/numBacias;											/*nesse caso o numero  de estados em cada bacia deve ser poss/numBacias (já que é um multiplo e tal)*/
		for(int j =0; j<numBacias; j++, off--){
			bacias[j] = i;								
			if(off > 0)													/*coloca o contador de estados nas bacias e adiciona +1 até que o numero de estados adicionais seja ==0*/
				bacias[j]++;											
		}
		
		for(int j =0; j<numBacias; j++){								/*Calcula a possibilidade individual de cada bacia*/
			bacias[j] = bacias[j]/poss;
		}		
		
		for(i =0; i< numBacias; i++){									/*calcula a entropia usando a mesma fórmula usada na main*/
			
			termoEntropia  = bacias[i] * (Math.log(bacias[i])/Math.log(2));
			termoEntropia = termoEntropia * -1;
			
			entropia= entropia + termoEntropia;
		}
		return entropia;
	}

	
	public static int converte(ArrayList<Integer> resAtual){
		int resInt = 0;
		int j = 0;
		for(int i = resAtual.size()-1; i >= 0; i--){
			resInt = resInt + resAtual.get(i) * expon(2, j);
			j++;
		}
		return resInt;
	}
	
	/*
	 * Calcula a distancia (diferencas) entre dois arrays
	 * */
	public static int calculaDistanciaHamming(ArrayList<Integer> a, ArrayList<Integer> b){
		int ret = 0;	
		for(int i = 0; i < a.size(); i++){
			if(a.get(i) != b.get(i))
				ret++;
		}
		return ret;
	}
	
	/*
	 * Pega dois binarios randomicos, calcula a distancia entre eles (x).
	 * Calcula o proximo estado a partir do que foi gerado aleatoriamente.
	 * calcula a distancia entre eles (y).
	 * 
	 * usando x e y, calcula o coeficiente de Derrida
	 * */
	public static double derridaCoefficient(int numGenes, int quantidadeEstados, ArrayList<Integer> matriz){
		ArrayList<Integer> estadoA = new ArrayList<Integer>();
		ArrayList<Integer> estadoB = new ArrayList<Integer>();
		double resX = 0, resY = 0, x, y;
		
		for(int i = 0; i < getNumIteracoes(); i++){
			estadoA = devolveBinariosDerrida(numGenes, quantidadeEstados);
			//System.out.println("estadoA: " + estadoA);
			estadoB = devolveBinariosDerrida(numGenes, quantidadeEstados);
			//System.out.println("estadoB: " + estadoB);
			x = calculaDistanciaHamming(estadoA, estadoB);
			
			/*Calculando o proximo estado*/
			estadoA = calculaProximoEstado(matriz, estadoA);
			//System.out.println("próximo estadoA: " + estadoA);
			estadoB = calculaProximoEstado(matriz, estadoB);
			//System.out.println("próximo estadoB: " + estadoB);
			y = calculaDistanciaHamming(estadoA, estadoB);
			
			//System.out.println("x: " + x + " y: " + y);
			
			y=y/numGenes;
			x=x/numGenes;
			
			//System.out.println("xn: " + x + " yn: " + y);
			
			resX = resX + (y*x);
			resY = resY + (x*x);
		}
		return resX/resY;
	}
	
	/*
	 * Sorteia dois numeros (limitados ao numero max de estados), passa esse numero pra "binario"
	 * e devolve como uma arraylist
	 * */
	public static ArrayList<Integer> devolveBinariosDerrida(int numGenes, int quantidadeEstados){
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
	
	public static ArrayList<Integer> retornaEstadosMaiorBacia(Map<Integer, Integer> tamanhosBacias){
		ArrayList<Integer> res = new ArrayList<Integer>();

		int valorMaiorBacia = Collections.max(tamanhosBacias.values());
		int chaveMaiorBacia = -1;
		
		for(Map.Entry<Integer, Integer> entry : tamanhosBacias.entrySet()){
			if(entry.getValue() == valorMaiorBacia)
				chaveMaiorBacia = entry.getKey();
		}

		for(Map.Entry<Integer, Integer> entry : tamanhosBacias.entrySet()){
			if(entry.getKey() == chaveMaiorBacia){
				res.add(entry.getValue());
			}
		}
		return res;
	}	
	
	@Override
	public void cancel() {
		this.interrupted = true;
		rnaUtil.removeResult(resultId);
	}
	
	/**
	 * Gets the Task Title.
	 *
	 * @return human readable task title.
	 */
	public String getTitle() {
		return "Generating/Analysing Network";
	}
}