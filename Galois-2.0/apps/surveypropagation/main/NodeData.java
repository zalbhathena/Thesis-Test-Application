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

File: NodeData.java 

 */
package surveypropagation.main;

import galois.objects.AbstractNoConflictBaseObject;
import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.Iteration;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Class NodeData.
 */
public abstract class NodeData extends AbstractNoConflictBaseObject {

  /** The Constant nEps. */
  protected final static double nEps = 1.0E-7 * 1.0E-9;

  /** The owner ref. */
  AtomicReference<Iteration> ownerRef = new AtomicReference<Iteration>();

  /** The time. The current iteration of the node.*/
  public static AtomicLong time = new AtomicLong(0);

  /** Does the node belong to the graph or has it been frozen. */
  public boolean belongsToGraph;

  /** The id of the node. */
  public int id;

  /** The current tau of the node. */
  public AtomicLong tau;

  /** The highest ploarization field*/
  public double hmax;

  /** The Replay Id. */
  public long rid;

  /** The graph. */
  public Graph<NodeData> graph = null;

  /**
   * Instantiates a new node data.
   *
   * @param g the graph. 
   */
  public NodeData(Graph<NodeData> g) {
    graph = g;
    belongsToGraph = false;
    id = -1;
    tau = new AtomicLong(0);
    hmax = 0;
    rid = 0;
  }

  /**
   * Update.
   * Update the current node.
   * @param graph the graph
   * @param node the node
   * @return true, if successful
   */
  public abstract boolean update(final ObjectGraph<NodeData, EdgeData> graph, final GNode<NodeData> node);

  /**
   * Gets the Iteration object referring to the owner of current node.
   *
   * @return the owner
   */
  public AtomicReference<Iteration> getOwner() {
    return ownerRef;
  }
 
}
