# rna-app
Regulatory Network Analyzer

RNA has as entry an interaction network represented by a file of **.txt** format. That file has to be a matrix of size **n** x **n**, where **n** is the number of genes/nodes, and has the notation as follows:

- The first row is the names of the genes separated by comma (,);
     
         1: when the gene j is activated by the gene i;
         0: when there is no interaction between gene i and gene j;
        -1: when the gene j is inhibited by gene i;
- The numbers are also separated by comma (,).

Here goes an example of matrix of the **yeast cell-cycle network**:

    Cln3,MBF,SBF,Cln12,Cdh1,Swi5,Cdc20,Clb56,Sic1,Clb12,Mcm1
    -1,0,0,0,0,0,0,0,0,0,0
    1,0,0,0,0,0,0,0,0,-1,0
    1,0,0,0,0,0,0,0,0,-1,0
    0,0,1,-1,0,0,0,0,0,0,0
    0,0,0,-1,0,0,1,-1,0,-1,0
    0,0,0,0,0,-1,1,0,0,-1,1
    0,0,0,0,0,0,-1,0,0,1,1
    0,1,0,0,0,0,-1,0,-1,0,0
    0,0,0,-1,0,1,1,-1,0,-1,0
    0,0,0,0,-1,0,-1,1,-1,0,1
    0,0,0,0,0,0,0,1,0,1,-1

Then the visualization of that matrix is generated as a network in cytoscape.

Besides that visualization we also generate the states transition network, that has the 2^n nodes, *so is possible that cytoscape will crash for regulatory networks with more than 20 nodes* (2^20), the layout of the network also affects the performance, by default the layout selected is *Grid Layout*. Each basin of attraction of the network has a different color.

And the app computes some informations from the states transition network like the **entropy** and the **derrida coefficient**. These informations is shown in the results panel.
