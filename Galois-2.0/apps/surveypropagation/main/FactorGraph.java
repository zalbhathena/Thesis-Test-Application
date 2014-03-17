/*
Galois, a framework to exploit amorphous data-parallelism in irregular
programs.

Copyright (C) 2010, The University of Texas at Austin. All rights reserved.
UNIVERSITY EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES CONCERNING THIS SOFTWARE
AND DOCUMENTATION, INCLUDING ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR ANY
PARTICULAR PURPOSE, NON-INFRINGEMENT AND WARRANTIES OF PERFORMANCE, AND ANY
WARRANTY THAT MIGHT OTHERWISE ARISE FROM COURSE OF DEALING OR USAGE OF TRADE.
NO WARRANTY IS EITHER EXPRESS OR IMPLIED WITH RESPECT TO THE USE OF THE
SOFTWARE OR DOCUMENTATION. Under no circumstances shall University be liable
for incidental, special, indirect, direct or consequential damages or loss of
profits, interruption of business, or related expenses which may arise from use
of Software or Documentation, including but not limited to those resulting from
defects in Software and/or Documentation, or loss or inaccuracy of data of any
kind.

File: FactorGraph.java 

 */

package surveypropagation.main;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.MorphGraph;
import galois.objects.graph.ObjectGraph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import surveypropagation.main.SurveyPropagationClosures.GetResultClosure;

/**
 * The Class FactorGraph.
 */
public class FactorGraph {

  /** The graph. */
  public ObjectGraph<NodeData, EdgeData> graph;

  /** The nodes. */
  public List<GNode<NodeData>> nodes;

  /** The num clauses. */
  private int numClauses;

  /** The nodes to remove. */
  private List<GNode<NodeData>> nodesToRemove;

  /**
   * Instantiates a new factor graph.
   */
  public FactorGraph() {
    nodes = new ArrayList<GNode<NodeData>>();
    nodesToRemove = new ArrayList<GNode<NodeData>>();
  }

  /**
   * Gets the number of clauses.
   * 
   * @return the num clauses
   */
  protected int getNumClauses() {
    int numClauses = 0;
    NodeData nodeData;
    Iterator<GNode<NodeData>> it = getNodeIterator();
    while (it.hasNext()) {
      GNode<NodeData> node = (GNode<NodeData>) it.next();
      // Read only getNodeData, no changes made, so no need to write back.
      nodeData = node.getData();
      if (nodeData instanceof ClauseNodeData) {
        numClauses++;
      }
    }
    return numClauses;
  }

  /**
   * Gets the number of variables.
   * 
   * @return the num vars
   */
  protected int getNumVars() {
    return graph.size() - getNumClauses();
  }

  /**
   * Gets the node iterator.
   * 
   * @return the node iterator
   */
  protected Iterator<GNode<NodeData>> getNodeIterator() {
    return nodes.iterator();
  }

  /**
   * Reads in the SAT from a CNF file Each line in the input file is
   * formulated as one ClauseNode and a list of Literals is associated with
   * it. Each variable in the input file is a member of another list as a
   * VarNode and with each such VarNode is associated a list of Edges. Each
   * Edge and its corresponding Literal share one EdgeData.
   * 
   * @param cnfFileName
   *            the cnf file name
   */
  public void readIn(final String cnfFileName) {
    Scanner scanner = null;
    int maxnumLits = -1;
    int numVars = -1;
    numClauses = -1;
    ArrayList<GNode<NodeData>> newNodeList = null;

    try {
      scanner = new Scanner(new BufferedReader(new FileReader(cnfFileName)));
      String line = scanner.nextLine();
      numVars = getVarValue(line, "numvar");
      line = scanner.nextLine();
      numClauses = getVarValue(line, "numclause");
      line = scanner.nextLine();
      maxnumLits = getVarValue(line, "maxnumlits");
      graph = new MorphGraph.ObjectGraphBuilder().directed(false).create();
      newNodeList = new ArrayList<GNode<NodeData>>(numVars + numClauses);
      ClauseNodeData.setMaxNumLits(maxnumLits);
      VarNodeData varNode;
      for (int varNum = 1; varNum <= numVars; varNum++) {
        varNode = new VarNodeData(graph);
        varNode.id = varNum;
        varNode.belongsToGraph = true;
        GNode<NodeData> node = graph.createNode(varNode);
        graph.add(node);
        nodes.add(node);
        newNodeList.add(node);
      }
      ClauseNodeData clauseNode;
      Integer varNodeNum;
      scanner.useDelimiter(",");
      for (int clauseNum = 1; clauseNum <= numClauses; clauseNum++) {
        clauseNode = new ClauseNodeData(graph);
        clauseNode.id = clauseNum;
        clauseNode.belongsToGraph = true;
        GNode<NodeData> node = graph.createNode(clauseNode);
        graph.add(node);
        nodes.add(node);
        newNodeList.add(node);

        while (scanner.hasNextInt() && ((varNodeNum = scanner.nextInt()) != null)) {
          int litType = 1;
          if (varNodeNum < 0) {
            litType = -1;
            varNodeNum = -varNodeNum;
          }
          EdgeData edgeData = new EdgeData();
          edgeData.litType = litType;
          GNode<NodeData> dst = newNodeList.get(varNodeNum - 1);
          graph.addEdge(node, dst, edgeData);
          // Back edge
        }
        if (scanner.hasNextLine()) {
          scanner.nextLine();
        }
      }
      scanner.close();
    } catch (final FileNotFoundException excep) {
      System.err.println(excep);
    }

    System.out.println("Finished Reading" + nodes.size());
    System.out.println("Graph :: " + graph.size());
  }

  /**
   * Gets the var value.
   * 
   * @param str
   *            the str
   * @param numStr
   *            the num str
   * @return the var value
   */
  private Integer getVarValue(String str, final String numStr) {
    boolean okay = false;
    int value = -1;
    final Scanner scanner = new Scanner(str);
    scanner.useDelimiter("=");
    if (scanner.hasNext()) {
      str = scanner.next();
      if (str.equals(numStr)) {
        if (scanner.hasNextInt()) {
          value = scanner.nextInt();
          okay = true;
        }
      }
    }

    if (!okay) {
      System.err.println("Error in reading the input CNF file");
      System.exit(-1);
    }

    return value;
  }

  /**
   * Update all variable nodes in the graph.
   */
  public void updateAllVarNodes() {
    Iterator<GNode<NodeData>> it = nodes.iterator();
    while (it.hasNext()) {
      GNode<NodeData> n = it.next();
      NodeData node = n.getData();
      if (node instanceof VarNodeData) {
        if (node.update(graph, n)) {
          purgeFrozenVar(n);
        }
      }
    }
  }

  /**
   * Touch neighborhood, to make the implementation one-shot.
   * 
   * @param node
   *            the node to be locked.
   */
  public void touchNeighborhood(final GNode<NodeData> node) {
  }

  /**
   * Update node. This method would first purge any frozen clause nodes,
   * and if the current node is not a clause node or if it is not frozen
   * , it would update the node according to the Survey Propagation algorithm.
   * The update method would return true if the variable node needs to be purged, 
   * otherwise it would return fale in which case nothing needs to be done.  
   * 
   * @param node
   *            the node which is to be updated.
   * @return true, if the node data is not to be purged after this method call.
   */
  public boolean updateNode(final GNode<NodeData> node) {
    final NodeData nodeData = node.getData(MethodFlag.NONE);
    if ((nodeData instanceof ClauseNodeData) && (((ClauseNodeData) nodeData).isMarkedForRemoval())) {
      purgeClause(node);
    } else {
      if (nodeData.update(graph, node)) {
        purgeFrozenVar(node);
      }
    }
    return nodeData.belongsToGraph;
  }

  /**
   * Purge frozen var.
   * 
   * @param varNode
   *            the var node
   */
  private void purgeFrozenVar(final GNode<NodeData> varNode) {
    final VarNodeData v = (VarNodeData) varNode.getData();
    varNode.map(v.pfvClosure, varNode, MethodFlag.NONE);
    ((VarNodeData) (v)).pfvClosure.doRemove(graph);
    v.belongsToGraph = false;
    varNode.setData(v);
    graph.remove(varNode);
  }

  /**
   * Purge clause.
   * 
   * @param clauseNode
   *            the clause node
   */
  private void purgeClause(final GNode<NodeData> clauseNode) {
    final ClauseNodeData v = (ClauseNodeData) clauseNode.getData(MethodFlag.NONE);
    clauseNode.map(v.pcClosure, clauseNode, MethodFlag.NONE);
    v.belongsToGraph = false;
    v.markForRemoval();
    graph.remove(clauseNode, MethodFlag.NONE);
  }

  /**
   * Purge marked clauses.
   */
  private void purgeMarkedClauses() {
    nodesToRemove.clear();
    for (GNode<NodeData> t : nodes) {
      if (t.getData() instanceof NodeData) {
        NodeData nodeData = (NodeData) t.getData();
        if ((nodeData instanceof ClauseNodeData) && (((ClauseNodeData) nodeData).isMarkedForRemoval())) {
          nodesToRemove.add(t);
        }
      }
    }
    for (GNode<NodeData> node : nodesToRemove) {
      purgeClause(node);
    }
  }

  /**
   * Gets the result 
   * 
   * @return the result
   */
  public String getResult() {

    purgeMarkedClauses();
    String res = "\n";
    int currWalksatId = 0;
    Iterator<GNode<NodeData>> it = nodes.iterator();
    while (it.hasNext()) {
      GNode<NodeData> node = it.next();
      // Read only getNodeData so no need to write back.
      NodeData nodeData = (NodeData) node.getData();
      if (nodeData instanceof ClauseNodeData) {

        GetResultClosure r = new SurveyPropagationClosures.GetResultClosure(graph, currWalksatId);
        node.map(r, node, MethodFlag.NONE);
        res += r.getResult() + "\n";
        currWalksatId = r.getWalksatId();
      }
    }
    res += "numvar=" + getNumVars() + "\n";
    res += "numclause=" + getNumClauses() + "\n";
    res += "maxnumlits=" + ClauseNodeData.getMaxNumLits() + "\n";

    return res;
  }

  /**
   * Gets the nodes.
   * 
   * @return the nodes
   */
  public List<GNode<NodeData>> getNodes() {
    return nodes;
  }

  /**
   * Prints the solution.
   * 
   * @return the int
   */
  public int printSolution() {
    int numClauseSatisfied = 0;
    int varFrozen = 0;
    NodeData nData;
    for (GNode<NodeData> n : nodes) {
      nData = n.getData();
      if (nData instanceof ClauseNodeData) {
        if (nData.belongsToGraph == false) {
          numClauseSatisfied++;
        }
      } else if (nData instanceof VarNodeData) {
        if (nData.belongsToGraph == false)
          varFrozen++;
      }
    }
    System.out.println("Finished :: Vars (" + varFrozen + ")  and Clause (" + numClauseSatisfied + ") TMAX ("
        + NodeData.time.get() + "/" + InputParameters.tmax + ")");
    return numClauseSatisfied;
  }
}
