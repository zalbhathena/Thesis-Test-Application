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

File: ClauseNodeData.java 

 */
package surveypropagation.main;

import galois.objects.MethodFlag;
import galois.objects.graph.GNode;
import galois.objects.graph.Graph;
import galois.objects.graph.ObjectGraph;
import galois.runtime.Callback;
import galois.runtime.GaloisRuntime;
import galois.runtime.Iteration;
import surveypropagation.main.SurveyPropagationClosures.ComputeEetaClauseFirstClosure;
import surveypropagation.main.SurveyPropagationClosures.ComputeEetaClauseSecondClosure;
import surveypropagation.main.SurveyPropagationClosures.PurgeClauseClosure;

/**
 * The Class ClauseNodeData.
 */
public class ClauseNodeData extends NodeData {

  /** The max number of literals. */
  private static int maxNumLits;

  /** Is the node marked for removal. */
  private boolean markedForRemoval = false;

  /**
   * The instance of {@link #SurveyPropagationClosures
   * ComputeEetaClauseFirstClosure}.
   */
  protected ComputeEetaClauseFirstClosure cFirst;
  /**
   * The instance of {@link #SurveyPropagationClosures
   * ComputeEetaClauseSecondClosure}.
   */
  protected ComputeEetaClauseSecondClosure cSecond;
  /** The instance of {@link #SurveyPropagationClosures PurgeClauseClosure}. */
  protected PurgeClauseClosure pcClosure;

  /**
   * Instantiates a new clause node data.
   * 
   * @param g the {@link #galois Graph} object associated with the nodes.
   */
  public ClauseNodeData(Graph<NodeData> g) {
    super(g);
    pcClosure = new PurgeClauseClosure(g);
    cFirst = new ComputeEetaClauseFirstClosure(g);
    cSecond = new ComputeEetaClauseSecondClosure(cFirst);
  }

  /**
   * Sets the maximum number of literals.
   * 
   * @param maxNumLits
   *            the new maximum number of literals.
   */
  public static void setMaxNumLits(final int maxNumLits) {
    ClauseNodeData.maxNumLits = maxNumLits;
  }

  /**
   * Gets the maximum number of literals.
   * 
   * @return the maximum number literals
   */
  public static int getMaxNumLits() {
    return maxNumLits;
  }

  /**
   * Mark the node for removal.
   */
  public void markForRemoval() {
    markedForRemoval = true;
  }

  /**
   * Checks if the node is marked for removal.
   * 
   * @return true, if node has been marked for removal
   */
  public boolean isMarkedForRemoval() {
    return markedForRemoval;
  }

  /*
   * 
   * @see
   * surveypropagation.main.NodeData#update(galois.objects.graph.ObjectGraph,
   * galois.objects.graph.GNode)
   */
  @Override
  public boolean update(final ObjectGraph<NodeData, EdgeData> graph, final GNode<NodeData> node) {
    // No reference to this here! 
    double hmaxAllI = computeEetaAndHMaxAllI(graph, node);
    final ClauseNodeData d = (ClauseNodeData) node.getData(MethodFlag.NONE);
    if (d.hmax > hmaxAllI) {
      hmaxAllI = d.hmax;
    }
    d.hmax = hmaxAllI * (1 - InputParameters.delta);

    d.tau.getAndIncrement();
    if (d.tau.get() == InputParameters.tau) {
      d.tau.set(0);
      time.getAndIncrement();
      GaloisRuntime.getRuntime().onUndo(Iteration.getCurrentIteration(), new Callback() {
        public void call() {
          d.tau.set(InputParameters.tau - 1);
          time.getAndDecrement();

        }
      });
    }
    node.setData(d, MethodFlag.NONE);
    return false;
  }

  /**
   * Compute Eeta and HMax.
   * 
   * @param graph
   *            The {@link #galois Graph} object associated with the nodes.
   * @param node
   *            The node for which Eeta and HMax are to be computed.
   * @return The HMax value computed by applying the first and second
   *         closures.
   */
  private double computeEetaAndHMaxAllI(final ObjectGraph<NodeData, EdgeData> graph, final GNode<NodeData> node) {
    cFirst.reset();
    node.map(cFirst, node, MethodFlag.NONE);
    cSecond.reset();
    node.map(cSecond, node, MethodFlag.NONE);
    return cSecond.hmaxAllI;
  }

@Override
public Object gclone()
{
	ClauseNodeData copy  = new ClauseNodeData(graph);
	copy.belongsToGraph = belongsToGraph;
	copy.cFirst = cFirst;
	copy.cSecond = cSecond;
	copy.hmax = hmax;
	copy.id = id;
	copy.rid = rid;
	copy.tau = tau;
	copy.ownerRef = ownerRef;
	return copy;
}

@Override
public void restoreFrom(Object c)
{	ClauseNodeData copy  = (ClauseNodeData)c;
	belongsToGraph = copy.belongsToGraph;
	cFirst = copy.cFirst;
	cSecond = copy.cSecond;
	hmax = copy.hmax;
	id = copy.id;
	rid = copy.rid;
	tau = copy.tau;
	ownerRef = copy.ownerRef;
}
}
